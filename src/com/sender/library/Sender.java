package com.sender.library;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 18.02.15
 * Time: 08:40
 */
public class Sender implements Runnable {

    private BlockingQueue<SenderRequest> queue = new ArrayBlockingQueue<SenderRequest>(10);
    private ChatDispatcher disp;
    private String url;
    private static boolean sending;
    private KeyStore keyStore;
    private static final Object lock = new Object();

    public Sender(ChatDispatcher disp, String url, KeyStore keyStore) {
        this.disp = disp;
        this.keyStore = keyStore;
        this.url = url;
        synchronized (lock) {
            sending = false;
        }
    }
    
    public void send(SenderRequest sr) {
        tryStart();
        try {
            queue.put(sr);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void tryStart() {
        synchronized (lock) {
            if (!sending) {
                new Thread(this).start();
            } else {
                Log.v(ChatDispatcher.TAG, "already sending");
            }
        }
    }

    @Override
    public void run() {
        synchronized (lock) {
            sending = true;
        }
        try {
            Log.v(ChatDispatcher.TAG, "step to send...");
            while (ChatFacade.SID_UNDEF.equalsIgnoreCase(disp.getMasterKey())) {
                Log.v(ChatDispatcher.TAG, "need reg... wait send");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            List<SenderRequest> toSend = new ArrayList<SenderRequest>();
            JSONArray arr = new JSONArray();
            try {
                while (queue.size() > 0) {
                    SenderRequest sr = queue.take();
                    Log.v(ChatDispatcher.TAG, "request " + sr.getRequestURL() + " id=" + sr.getId() + " resuming from queue");
                    if (ChatFacade.URL_FORM.equalsIgnoreCase(sr.getRequestURL()) && sr.getPostData() != null) {
                        JSONObject data = sr.getPostData();
                        // ------------- TODO: костыль
                        if (data.has("chatId") && "sender".equalsIgnoreCase(data.optString("chatId"))) {
                            data.put("chatId", "user+sender");
                        }
                        // --------------
                        data.put("cid", sr.getId());
                        arr.put(data);
                        toSend.add(sr);
                    } else {
                        doSend(sr);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (toSend.size() > 0) {
                JSONObject rjo = null;
                String id = UUID.randomUUID().toString().replace("-", "");
                try {
                    JSONObject jo = new JSONObject();
                    jo.put("fs", arr);
                    String fullUrl = url + "send?udid=" + disp.getUDID() + "&token=" + disp.getToken();
                    HttpPost post = new HttpPost(fullUrl);
                    post.setEntity(new ByteArrayEntity(jo.toString().getBytes()));
                    Log.v(ChatDispatcher.TAG, "========> " + fullUrl + " " + jo.toString() + " (" + id + ")");
                    String response = Tool.getData(HttpSigleton.getSenderInstance(keyStore).execute(post));
                    Log.v(ChatDispatcher.TAG, "<======== " + response + " (" + id + ")");
                    rjo = new JSONObject(response);
                } catch (Exception e) {
                    Log.v(ChatDispatcher.TAG, "<======== " + e.getMessage() + " (" + id + ")");
                    e.printStackTrace();
                    for (SenderRequest sr : toSend) send(sr);
                }
                if (rjo != null && disp.checkResp(rjo, toSend, null)) {
                    if (rjo.has("cr")) {
                        JSONArray cr = rjo.optJSONArray("cr");
                        for (int i = 0; i < cr.length(); i++) {
                            JSONObject crj = cr.optJSONObject(i);
                            for (SenderRequest sr : toSend) {
                                if (sr.getId().equalsIgnoreCase(crj.optString("cid"))) {
                                    sr.response(crj);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            synchronized (lock) {
                sending = false;
            }
            if (queue.size() > 0) {
                tryStart();
            } else {
                Log.v(ChatDispatcher.TAG, "sender stopped!");
            }
        }
    }
    
    private void doSend(final SenderRequest request) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String resp;
                    String rurl = url + request.getRequestURL() + (request.getRequestURL().contains("?") ? "&" : "?") + "udid=" + disp.getUDID() + "&token=" + disp.getToken();
                    if (request.getData() != null && request.getData().available() > 0) {                        // -------------------- binary post
                        Log.v(this.getClass().getSimpleName(), "========> " + rurl + " with binary data " + " (" + request.getId() + ")");
                        URL purl = new URL(rurl);
                        HttpURLConnection con;
                        if (purl.getProtocol().toLowerCase().equals("https")) {
                            con = (HttpsURLConnection) purl.openConnection();
                        } else {
                            con = (HttpURLConnection) purl.openConnection();
                        }
                        con.setUseCaches(false);
                        con.setDoOutput(true);
                        con.setDoInput(true);
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-type", "image/png");
                        con.connect();
                        int bufferSize = 1024;
                        byte[] buffer = new byte[bufferSize];
                        int len;
                        OutputStream out = con.getOutputStream();
                        while ((len = request.getData().read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                        out.flush();
                        out.close();
                        request.getData().close();
                        StringBuilder sb = new StringBuilder();
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                        String nextLine;
                        while ((nextLine = in.readLine()) != null) {
                            sb.append(nextLine);
                        }
                        in.close();
                        con.disconnect();
                        resp = sb.toString();
                    } else if (request.getPostData() != null) {             // -------------------- post
                        Log.v(ChatDispatcher.TAG, "========> " + rurl + " " + request.getPostData() + " (" + request.getId() + ")");
                        HttpPost post = new HttpPost(rurl);
                        post.addHeader("Accept-Encoding", "gzip");
                        post.setEntity(new ByteArrayEntity(request.getPostData().toString().getBytes("UTF-8")));
                        resp = Tool.getData(HttpSigleton.getSyncClient(keyStore).execute(post));
                    } else {                                                // -------------------- get
                        Log.v(this.getClass().getSimpleName(), "========> " + rurl + " (" + request.getId() + ")");
                        HttpGet get = new HttpGet(rurl);
                        get.addHeader("Accept-Encoding", "gzip");
                        resp = Tool.getData(HttpSigleton.getSyncClient(keyStore).execute(get));
                    }
                    Log.v(this.getClass().getSimpleName(), "<======= " + resp + " (" + request.getId() + ")");
                    JSONObject jo = new JSONObject(resp);
                    if (disp.checkResp(jo, null, request)) {
                        request.response(jo);
                    }
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    request.error(new Exception("file is too large"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.v(this.getClass().getSimpleName(), "<======== " + e.getMessage() + " error req " + " (" + request.getId() + ")");
                    if (e instanceof java.io.IOException || e instanceof java.lang.IllegalStateException) {
                        send(request);
                    }
                    else {
                        request.error(new Exception(e.getMessage()));
                    }
                }
            }
        }).start();
    }


}