package mobi.sender.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 05.11.14
 * Time: 11:02
 */
public class ChatFacade {

    public static final String CLASS_TEXT_ROUTE = "text.routerobot.sender";
    public static final String CLASS_FILE_ROUTE = "file.routerobot.sender";
    public static final String CLASS_AUDIO_ROUTE = "audio.routerobot.sender";
    public static final String CLASS_IMAGE_ROUTE = "image.routerobot.sender";
    public static final String CLASS_INFO_USER = ".getUserInfo.sender";
    public static final String CLASS_INFO_CHAT = "info.chatrobot.sender";
    public static final String CLASS_TYPING_ROUTE = "typing.routerobot.sender";
    public static final String CLASS_ADDUSER_NOTIFY = "adduser_notify.chatrobot.sender";
    public static final String CLASS_READ = "read.statusrobot.sender";
    public static final String CLASS_DELIV = "deliv.statusrobot.sender";
    public static final String CLASS_IS_AUTH = "isauth.authrobot.sender";
    public static final String CLASS_SYNC_CONTACT = "sync.contactrobot.sender";
    public static final String CLASS_PHONE_AUTH = "phone.auth.sender";
    public static final String CLASS_OTP_AUTH = "otp.auth.sender";
    public static final String CLASS_UPDATE_CONTACT = "update.contactrobot.sender";
    public static final String CLASS_GET_SELF_INFO = ".getSelfInfo.sender";
    public static final String CLASS_SET_SELF_INFO = ".setSelfInfo.sender";
    public static final String CLASS_SET_CHAT = "set.chatrobot.sender";
    public static final String CLASS_PUSH = "push.pushrobot.sender";
    public static final String CLASS_CHECK_ONLINE = "check.status.sender";
    public static final String CLASS_SET_LOCATION = ".setDeviceLocation.sender";
    public static final String CLASS_SHARE_LOCATION = ".shareMyLocation.sender";
    public static final String CLASS_WALLET = ".wallet.sender";
    public static final String CLASS_P2P = ".p2p.sender";
    public static final String CLASS_NOTIFY_ADD_YOU = "notify_add_you.chatrobot.sender";
    public static final String CLASS_NOTIFY_DEL_YOU = "notify_del_you.chatrobot.sender";
    public static final String CLASS_NOTIFY_SET = "notify_set.chatrobot.sender";
    public static final String CLASS_AUTH_SUCCESS = "success.auth.sender";
    public static final String CLASS_AUTH_PHONE = "phone.auth.sender";
    public static final String CLASS_AUTH_OTP = "otp.auth.sender";
    public static final String CLASS_RECHARGE_PHONE = ".payMobile.sender";

    private ChatConnector cc;
    private ChatConnector.SenderListener listener;

    public ChatFacade(String sid, String imei, String devName, String devType, int number, ChatConnector.SenderListener listener) {
        this.cc = new ChatConnector(ChatConnector.URL_PROD, sid, imei, devName, devType, number, listener);
    }

    public ChatFacade(String url, String sid, String imei, String devName, String devType, int number, ChatConnector.SenderListener listener) {
        this.cc = new ChatConnector(url, sid, imei, devName, devType, number, listener);
    }

    public void checkAuth() {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_IS_AUTH, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callWallet() {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_WALLET, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(JSONObject model, String className) {
        send(model, className, null);
    }

    public void send(JSONObject model, String className, String chatId) {
        try {
            JSONObject form2Send = getForm2Send(model, className, chatId == null ? ChatConnector.senderChatId : chatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String urlPart, JSONObject data) {
        try {
            cc.send(new SenderRequest(urlPart, data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncContacts(JSONArray contacts) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("contacts", contacts);
            JSONObject form2Send = getForm2Send(jo, CLASS_SYNC_CONTACT, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createChat(String userId) {
        addToChat(null, userId);
    }

    public void addToChat(final String chatId, String userId) {
        addToChat(chatId, new String[]{userId});
    }

    public void addToChat(final String chatId, String[] userIds) {
        JSONArray arr = new JSONArray();
        try {
            for (String s : userIds) {
                JSONObject jo = new JSONObject();
                jo.put("id", s);
                jo.put("cmd", "add");
                arr.put(jo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setChat(chatId, arr);
    }

    public void delFromChat(final String chatId, String userId) {
        JSONArray arr = new JSONArray();
        try {
            JSONObject jo = new JSONObject();
            jo.put("id", userId);
            jo.put("cmd", "del");
            arr.put(jo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setChat(chatId, arr);
    }

    private void setChat(final String chatId, JSONArray users) {
        try {
            JSONObject model = new JSONObject();
            model.put("users", users);
            model.put("chatId", chatId);
            JSONObject form2Send = getForm2Send(model, CLASS_SET_CHAT, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(final String text, final String chatId, final SendMsgListener sml) {
        try {
            JSONObject model = new JSONObject();
            model.put("text", text);
            JSONObject form2Send = getForm2Send(model, CLASS_TEXT_ROUTE, chatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send
                    , new SenderRequest.HttpDataListener() {

                @Override
                public void onResponse(String resp) {
                    try {
                        JSONObject jo = new JSONObject(resp);
                        String serverId = jo.optString("ref");
                        long time = jo.optLong("time");
                        sml.onSuccess(serverId, time);
                    } catch (JSONException e) {
                        sml.onError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUserData() {
        getUserData(null);
    }

    public void getUserData(final String userId) {
        try {
            JSONObject jo;
            if (userId == null) {
                jo = getForm2Send(new JSONObject(), CLASS_INFO_USER, ChatConnector.senderChatId);
            } else {
                jo = getForm2Send(new JSONObject("{userId:" + userId + "}"), CLASS_INFO_USER, ChatConnector.senderChatId);
            }

            cc.send(new SenderRequest("fsubmit", jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToken(final String token) {
        try {
            JSONObject rjo = new JSONObject();
            rjo.put("token", token);
            rjo.put("sid", cc.getSid());
            send("token", rjo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRead(final String packetId, final String chatId) {
        try {
            JSONObject model = new JSONObject();
            model.put("packetId", packetId);
            JSONObject form2Send = getForm2Send(model, CLASS_READ, chatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getChatInfo(String chatId) {
        try {
            JSONObject jo = getForm2Send(null, CLASS_INFO_CHAT, chatId);
            cc.send(new SenderRequest("fsubmit", jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendTyping(String chatId) {
        try {
            JSONObject rjo = new JSONObject();
            rjo.put("chatId", chatId);
            rjo.put("sid", cc.getSid());
            send("typing", rjo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendIAmOnline() {
        try {
            JSONObject rjo = new JSONObject();
            rjo.put("sid", cc.getSid());
            send("i_am_online", rjo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkOnline(String[] userIdS) {
        try {
            JSONObject model = new JSONObject();
            model.put("force", false);
            JSONArray arr = new JSONArray();
            for (String s : userIdS) arr.put(s);
            model.put("userIds", arr);
            JSONObject form2Send = getForm2Send(model, CLASS_CHECK_ONLINE, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCoordinates(double lat, double lon, double accuracy, Map<String, String> blePoints) {
        try {
            JSONObject model = new JSONObject();
            model.put("lat", lat);
            model.put("lon", lon);
            model.put("accuracy", accuracy);
            JSONArray arr = new JSONArray();
            for (String k : blePoints.keySet()) {
                JSONObject ble = new JSONObject();
                ble.put("mac", k);
                ble.put("RSSI", blePoints.get(k));
                arr.put(ble);
            }
            model.put("bleList", arr);
            JSONObject form2Send = getForm2Send(model, CLASS_SET_LOCATION, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendIAmHere(double lat, double lon, String message, String mapImgUrl, String chatId, final SendMsgListener sml) {
        try {
            JSONObject model = new JSONObject();
            model.put("lat", lat);
            model.put("lon", lon);
            model.put("textMsg", message);
            model.put("preview", mapImgUrl);
            JSONObject form2Send = getForm2Send(model, CLASS_SHARE_LOCATION, chatId);
            cc.send(new SenderRequest("fsubmit", form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(String data) {
                    try {
                        JSONObject jo = new JSONObject(data);
                        String serverId = jo.optString("ref");
                        long time = jo.optLong("time");
                        sml.onSuccess(serverId, time);
                    } catch (Exception e) {
                        sml.onError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    sml.onError(e);
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private JSONObject getForm2Send(JSONObject model, String formClass, String chatId) {
        JSONObject rez = new JSONObject();
        try {
            if (model != null) rez.put("model", model);
            if (chatId != null) rez.put("chatId", chatId);
            if (formClass != null) rez.put("class", formClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rez;
    }

    public void uploadFile2Server(final byte[] file, String fname, final UploadFileListener ufl) {
        try {
            fname = fname.replace("\\", "/");
            if (fname.contains("/")) fname = fname.substring(fname.lastIndexOf("/")+1);
            if (!fname.contains(".")) throw new Exception("invalid file name");
            final String[] parts = fname.split("\\.");
            cc.send(new SenderRequest("upload?filetype=" + parts[1] + "&sid=" + cc.getSid(),
                    file,
                    new SenderRequest.HttpDataListener() {

                        @Override
                        public void onResponse(String resp) {
                            try {
                                JSONObject jo = new JSONObject(resp);
                                ufl.onSuccess(jo.optString("url"), parts[0], parts[1], getMediaClass(parts[1]));
                            } catch (Exception e) {
                                ufl.onError(e);
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            ufl.onError(e);
                        }
                    }));
        } catch (Exception e) {
            ufl.onError(e);
        }
    }

    public static void sendDeliv(String packetId, String sid) {
        ChatConnector.sendDeliv(packetId, sid);
    }

    private String getMediaClass(String ext) {
        if ("png".equals(ext) || "jpg".equals(ext)) {
            return "image.routerobot.sender";
        } else {
            return "file.routerobot.sender";
        }
    }

    public void sendFile(String name, String desc, String type, String url, String chatId, String formClass, final SendMsgListener sml) {
        sendFile(name, desc, type, null, url, chatId, formClass, sml);
    }

    public void sendFile(String name, String desc, String type, String length, String url, String chatId, String formClass, final SendMsgListener sml) {
        try {
            JSONObject model = new JSONObject();
            model.put("name", name);
            model.put("type", type);
            model.put("desc", desc);
            if (length != null) model.put("length", length);
            model.put("url", url);
            final JSONObject jo = getForm2Send(model, formClass, chatId);
            cc.send(new SenderRequest("fsubmit", jo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(String data) {
                    try {
                        JSONObject jo = new JSONObject(data);
                        String serverId = jo.optString("ref");
                        long time = jo.optLong("time");
                        sml.onSuccess(serverId, time);
                    } catch (Exception e) {
                        sml.onError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    sml.onError(e);
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void callRechargePhone() {
        try {
            JSONObject jo = getForm2Send(new JSONObject(), CLASS_RECHARGE_PHONE, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMySelfData(String name, String iconUrl, String description) {
        try {
            JSONObject model = new JSONObject();
            model.put("name", name);
            model.put("photo", iconUrl);
            model.put("description", description);
            JSONObject jo = getForm2Send(model,CLASS_SET_SELF_INFO, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMySelfData() {
        try {
            JSONObject jo = getForm2Send(new JSONObject(), CLASS_GET_SELF_INFO, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return cc.isAlive();
    }

    public void stop() {
        cc.disconnect();
    }

    public interface UploadFileListener {
        /**
         * File uploaded
         * @param url - url uploaded file on server
         * @param name - name of file
         * @param type - type of file
         * @param className - class of form to upload file
         */
        public void onSuccess(String url, String name, String type, String className);

        public void onError(Exception e);
    }

    public interface SendMsgListener {
        public void onSuccess(String serverId, long time);

        public void onError(Exception e);
    }

}
