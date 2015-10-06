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
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 18.02.15
 * Time: 08:40
 */
public class Sender {

    private BlockingQueue<SenderRequest> packQueue = new ArrayBlockingQueue<SenderRequest>(20);
    private ChatDispatcher disp;
    private KeyStore keyStore;
    private Timer timer;
    private boolean isRunning = false;
    private static Sender instance;

    private Sender(ChatDispatcher disp, KeyStore keyStore) {
        this.disp = disp;
        this.keyStore = keyStore;
    }

    public static Sender getInstance(ChatDispatcher disp, KeyStore keyStore) {
        if (instance == null) instance = new Sender(disp, keyStore);
        return instance;
    }

    public void send(SenderRequest sr) {
        try {
            if (ChatFacade.URL_FORM.equalsIgnoreCase(sr.getRequestURL()) && sr.getPostData() != null) {
                packQueue.put(sr);
            } else {
                sendSync(sr);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isRunning) {
            isRunning = true;
            timer = new Timer();
            timer.schedule(new SendTask(), 0, 1000);
        }
    }
    
    private void sendSync(final SenderRequest request) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String resp;
                    String rurl = disp.getUrl() + request.getRequestURL() + (request.getRequestURL().contains("?") ? "&" : "?") + "udid=" + disp.getUDID() + "&token=" + disp.getToken();
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
                        Log.v(ChatDispatcher.TAG, "========> (" + request.getId() + ") " + rurl + " " + request.getPostData());
                        HttpPost post = new HttpPost(rurl);
                        post.addHeader("Accept-Encoding", "gzip");
                        post.setEntity(new ByteArrayEntity(request.getPostData().toString().getBytes("UTF-8")));
                        resp = Tool.getData(HttpSigleton.getSyncClient(rurl, keyStore, 60000).execute(post));
                    } else {                                                // -------------------- get
                        Log.v(this.getClass().getSimpleName(), "========> (" + request.getId() + ") " + rurl);
                        HttpGet get = new HttpGet(rurl);
                        get.addHeader("Accept-Encoding", "gzip");
                        resp = Tool.getData(HttpSigleton.getSyncClient(rurl, keyStore, 60000).execute(get));
                    }
                    Log.v(this.getClass().getSimpleName(), "<======= (" + request.getId() + ") " + resp);
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
                    request.error(e);
//                    if (e instanceof java.io.IOException || e instanceof java.lang.IllegalStateException) {
//                        send(request);
//                    }
//                    else {
//                        request.error(new Exception(e.getMessage()));
//                    }
                }
            }
        }).start();
    }

    private class SendTask extends TimerTask {
        @Override
        public void run() {
            try {
                JSONArray arr = new JSONArray();
                List<SenderRequest> toSend = new ArrayList<SenderRequest>();
                while (packQueue.size() > 0) {
                    try {
                        SenderRequest sr = packQueue.take();
                        toSend.add(sr);
                        Log.v(ChatDispatcher.TAG, "request " + sr.getRequestURL() + " id=" + sr.getId() + " resuming from queue");
                        JSONObject data = sr.getPostData();
                        // ------------- TODO: костыль
                        if (data.has("chatId") && "sender".equalsIgnoreCase(data.optString("chatId"))) {
                            data.put("chatId", "user+sender");
                        }
                        // --------------
                        data.put("cid", sr.getId());
                        arr.put(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (arr.length() > 0) {
                    JSONObject rjo = null;
                    String id = UUID.randomUUID().toString().replace("-", "");
                    try {
                        JSONObject jo = new JSONObject();
                        jo.put("fs", arr);
                        String fullUrl = disp.getUrl() + "send?udid=" + disp.getUDID() + "&token=" + disp.getToken();
                        HttpPost post = new HttpPost(fullUrl);
                        post.setEntity(new ByteArrayEntity(jo.toString().getBytes()));
                        Log.v(ChatDispatcher.TAG, "========> (" + id + ") " + fullUrl + " "  + jo.toString());
                        String response = Tool.getData(HttpSigleton.getSenderInstance(fullUrl, keyStore).execute(post));
                        Log.v(ChatDispatcher.TAG, "<======== ("  + id + ") " + response);
                        rjo = new JSONObject(response);
                    } catch (Exception e) {
                        Log.v(ChatDispatcher.TAG, "<======== (" + id + ") " + e.getMessage());
                        e.printStackTrace();
                        for (SenderRequest sr : toSend) {
                            sr.error(e);
                        }
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
                    Log.v(ChatDispatcher.TAG, "empty queue");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (isRunning) {
            Log.v(ChatDispatcher.TAG, "stopped!");
            timer.cancel();
            isRunning = false;
        }
    }
}
