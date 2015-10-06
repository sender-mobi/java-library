package com.sender.library;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.KeyStore;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 15.04.15
 * Time: 14:36
 */
public class Comet extends Thread {

    private ChatDispatcher disp;
    private String lastSrvBatchId;
    private KeyStore keyStore;
    public static int MAX_EMPTY_RESP = 2;
    public static int MAX_RETRY = 3;
    private boolean isShort = false;
    private String id = UUID.randomUUID().toString().replace("-", "");

    public Comet(ChatDispatcher disp, KeyStore keyStore, boolean isShort) {
        this.keyStore = keyStore;
        this.disp = disp;
        this.isShort = isShort;
    }

    public String getCometId() {
        return id;
    }

    @Override
    public void run() {
        Log.v(ChatDispatcher.TAG, "comet started id = " + id);
        try {
            int k = 0, r = 0;
            while (disp.getCometId() != null) {
                if (!id.equalsIgnoreCase(disp.getCometId())) {
                    Log.v(ChatDispatcher.TAG, "duplicate comet: my id = " + id + " active = " + disp.getCometId());
                    return;
                }
                while (ChatFacade.SID_UNDEF.equalsIgnoreCase(disp.getMasterKey())) {
                    disp.onNeedReg();
                    Log.v(ChatDispatcher.TAG, "need reg... wait comet");
                    sleep(1000);
                }
                if (k > MAX_EMPTY_RESP) {
                    k = 0;
                    Log.v(ChatDispatcher.TAG, "disconnected after server inactive. Id: " + id);
                    disp.setCometId(null);
                    continue;
                }
                if (r > MAX_RETRY) {
                    r = 0;
                    Log.v(ChatDispatcher.TAG, "disconnected after server not responded. Id: " + id);
                    disp.setCometId(null);
                    continue;
                }
                Log.v(ChatDispatcher.TAG, "step comet... id: " + id);
                JSONObject jo = new JSONObject();
                jo.put("lbi", lastSrvBatchId);
                if (isShort) jo.put("connection", "close");
                jo.put("meta", new JSONObject());
                String fullUrl = disp.getUrl() + "comet?udid=" + disp.getUDID() + "&token=" + disp.getToken();
                HttpPost post = new HttpPost(fullUrl);
                post.setEntity(new ByteArrayEntity(jo.toString().getBytes()));
                Log.v(ChatDispatcher.TAG, "========> " + fullUrl + " " + jo.toString() + " (" + id + ")");
                try {
                    HttpResponse rr = HttpSigleton.getCometInstance(fullUrl, keyStore).execute(post);
                    if (rr.getStatusLine().getStatusCode() != 200) {
                        throw new Exception("HTTP CODE " + rr.getStatusLine().getStatusCode());
                    }
                    disp.onConnected();
                    r = 0;
                    String response = Tool.getData(rr);
                    Log.v(ChatDispatcher.TAG, "<======== " + response + " (" + id + ")");
                    JSONObject rjo = new JSONObject(response);
                    if (ChatDispatcher.CODE_DUPLICATE_COMET.equalsIgnoreCase(rjo.optString("code"))) {
                        Log.v(ChatDispatcher.TAG, "duplicate comet: my id = " + id + " (code 5)");
                        return;
                    }
                    if (disp.checkResp(rjo, null, null)) {
                        if (rjo.has("bi")) {
                            lastSrvBatchId = rjo.optString("bi");
                        }
                        if (rjo.has("fs")) {
                            k = 0;
                            JSONArray fs = rjo.optJSONArray("fs");
                            for (int i = 0; i < fs.length(); i++) {
                                JSONObject fsj = fs.optJSONObject(i);
                                disp.onMessage(fsj);
                            }
                            sleep(500);
                        } else {
                            sleep(3000);
                            k++;
                        }
                    } else {
                        sleep(1000);
                    }
                } catch (Exception e) {
                    Log.v(ChatDispatcher.TAG, "Comet error: " + e.getMessage() + " id = " + id);
                    r++;
                    e.printStackTrace();
                    sleep(3000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        disp.setCometId(null);
        disp.onDisconnected();
        Log.v(ChatDispatcher.TAG, "comet ending id = " + id);
    }

    public void disconnect() {
        HttpSigleton.invalidateCometInstance();
        interrupt();
    }
}
