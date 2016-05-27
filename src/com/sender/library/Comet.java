package com.sender.library;

import org.json.JSONArray;
import org.json.JSONObject;

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
    public static int MAX_RETRY = 3;
    private static final String SYS_CHAT = "@sys";
    private boolean isShort = false;
    private String id = UUID.randomUUID().toString().replace("-", "");

    public Comet(ChatDispatcher disp, boolean isShort) {
        this.disp = disp;
        this.isShort = isShort;
    }

    public void setIsShort(boolean isShort) {
        this.isShort = isShort;
    }

    public String getCometId() {
        return id;
    }

    @Override
    public void run() {
        Log.v(ChatDispatcher.TAG, "comet started id = " + id);
        try {
            int r = 0;
            boolean close = false;
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
                if (r > MAX_RETRY) {
                    r = 0;
                    Log.v(ChatDispatcher.TAG, "disconnected after server not responded. Id: " + id);
                    disp.setCometId(null);
                    continue;
                }
                Log.v(ChatDispatcher.TAG, "step comet... id: " + id);
                JSONObject jo = new JSONObject();
                jo.put("lbi", lastSrvBatchId);
                if (isShort && close) jo.put("connection", "close");
                jo.put("meta", new JSONObject());
                String fullUrl = disp.getUrl() + "comet?udid=" + disp.getUDID() + "&token=" + disp.getToken();
                Log.v(ChatDispatcher.TAG, "========> " + fullUrl + " " + jo.toString() + " (" + id + ")");
                boolean checkResp = true;
                try {
                    if (!disp.isConnStateOk() && Tool.checkServer(disp.getUrl(false))) disp.onConnected();
                    String response = new Http().post(fullUrl, disp.getCompanyId(), jo.toString(), true);//Tool.getData(rr);
                    Log.v(ChatDispatcher.TAG, "<======== " + response + " (" + id + ")");
                    JSONObject rjo = new JSONObject(response);
                    if (ChatDispatcher.CODE_DUPLICATE_COMET.equalsIgnoreCase(rjo.optString("code"))) {
                        Log.v(ChatDispatcher.TAG, "duplicate comet: my id = " + id + " (code 5)");
                        disp.setCometId(null);
                        return;
                    }
                    checkResp = disp.checkResp(rjo, null, null);
                    Log.v(ChatDispatcher.TAG, "checkResp: " + checkResp + ", " + id);
                    if (checkResp) {
                        if (rjo.has("bi")) {
                            lastSrvBatchId = rjo.optString("bi");
                        }
                        if (rjo.has("fs")) {
                            JSONArray fs = rjo.getJSONArray("fs");
                            for (int i = 0; i < fs.length(); i++) {
                                JSONObject fsj = fs.optJSONObject(i);
                                String chatId = fsj.optString("chatId");
                                if (SYS_CHAT.equals(chatId)) {
                                    disp.onSysMessages(fsj.optJSONArray("msgs"));
                                } else {
                                    disp.onChatMessages(fsj.optJSONArray("msgs"), chatId, fsj.optInt("unread"), fsj.optBoolean("more"));
                                }
                            }
                            if (!isShort) sleep(500);
                        } else {
                            if (!isShort) sleep(3000);
                        }
                    } else {
                        if (!isShort) sleep(1000);
                    }
                } catch (Exception e) {
                    disp.onDisconnected();
                    Log.v(ChatDispatcher.TAG, "Comet error: " + e.getMessage() + " id = " + id);
                    r++;
                    e.printStackTrace();
                    if (!isShort) sleep(3000);
                }
                if (isShort) {
                    if (checkResp) {
                        if (close) {
                            disp.setCometId(null);
                        } else {
                            close = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        disp.setCometId(null);
        disp.onDisconnected();
        Log.v(ChatDispatcher.TAG, "comet ending id = " + id);
    }

    public boolean isShort() {
        return isShort;
    }

    public void disconnect() {
//        HttpSigleton.invalidateCometInstance();
        interrupt();
    }
}
