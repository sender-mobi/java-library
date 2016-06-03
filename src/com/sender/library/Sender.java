package com.sender.library;

import org.json.JSONArray;
import org.json.JSONObject;

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
    private Timer timer;
    private boolean isRunning = false;
    private static Sender instance;

    private Sender(ChatDispatcher disp) {
        this.disp = disp;
    }

    public static Sender getInstance(ChatDispatcher disp) {
        if (instance == null) instance = new Sender(disp);
        return instance;
    }

    public void send(SenderRequest sr) {
        try {
            if (ChatFacade.URL_FORM.equalsIgnoreCase(sr.getRequestURL()) && sr.getPostData() != null) {
                packQueue.put(sr);
                Log.v(ChatDispatcher.TAG, "added to queue: " + sr);
            } else {
                sendSync(sr);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isRunning) {
            Log.v(ChatDispatcher.TAG, "run sender...");
            isRunning = true;
            timer = new Timer();
            timer.schedule(new SendTask(), 0, 1000);
        } else {
            Log.v(ChatDispatcher.TAG, "sender is running...");
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
                        resp = new Http().postImg(rurl, request.getData());
                    } else if (request.getPostData() != null) {             // -------------------- post
                        Log.v(ChatDispatcher.TAG, "========> (" + request.getId() + ") " + rurl + " " + request.getPostData());
                        resp = new Http().post(rurl, disp.getCompanyId(), request.getPostData().toString());
                    } else {                                                // -------------------- get
                        Log.v(this.getClass().getSimpleName(), "========> (" + request.getId() + ") " + rurl);
                        resp = new Http().get(rurl, disp.getCompanyId());
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
                }
            }
        }).start();
    }

    private class SendTask extends TimerTask {
        @Override
        public void run() {
//            Log.v(ChatDispatcher.TAG, "step sender begin... Queue size: " + packQueue.size());
            try {
                JSONArray arr = new JSONArray();
                List<SenderRequest> toSend = new ArrayList<SenderRequest>();
                while (packQueue.size() > 0) {
                    try {
                        SenderRequest sr = packQueue.take();
                        toSend.add(sr);
                        Log.v(ChatDispatcher.TAG, "request " + sr.getRequestURL() + " id=" + sr.getId() + " resuming from queue");
                        JSONObject data = sr.getPostData();
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
                        Log.v(ChatDispatcher.TAG, "========> (" + id + ") " + fullUrl + " "  + jo.toString());
                        String response = new Http().post(fullUrl, disp.getCompanyId(), jo.toString());
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
//                    Log.v(ChatDispatcher.TAG, "empty queue");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            Log.v(ChatDispatcher.TAG, "step sender end");
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
