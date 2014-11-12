package mobi.sender.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public static final String CLASS_INFO_USER = "info.userrobot.sender";
    public static final String CLASS_INFO_CHAT = "info.chatrobot.sender";
    public static final String CLASS_TYPING_ROUTE = "typing.routerobot.sender";
    public static final String CLASS_ADDUSER_NOTIFY = "adduser_notify.chatrobot.sender";
    public static final String CLASS_READ = "read.statusrobot.sender";
    public static final String CLASS_DELIV = "deliv.statusrobot.sender";
    public static final String CLASS_IS_AUTH = "isauth.authrobot.sender";
    public static final String CLASS_SYNC_CONTACT = "sync.contactrobot.sender";
    public static final String CLASS_CHAT_CREATE = "create.chatrobot.sender";
    public static final String CLASS_PHONE_AUTH = "phone.auth.sender";
    public static final String CLASS_OTP_AUTH = "otp.auth.sender";
    public static final String CLASS_UPDATE_CONTACT = "update.contactrobot.sender";
    public static final String CLASS_GET_SELF_INFO = ".getSelfInfo.sender";
    public static final String CLASS_SET_SELF_INFO = ".setSelfInfo.sender";
    public static final String CLASS_SET_CHAT = "set.chatrobot.sender";
    public static final String CLASS_PUSH = "push.pushrobot.sender";
    public static final String CLASS_CHECK_ONLINE = "check.status.sender";

    private ChatConnector cc;
    private ChatConnector.SenderListener listener;

    public ChatFacade(String sid, String imei, String devName, String devType, int number, ChatConnector.SenderListener listener) {
        this.cc = new ChatConnector(sid, imei, devName, devType, number, listener);
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
        JSONArray arr = new JSONArray();
        try {
            JSONObject jo = new JSONObject();
            jo.put("id", userId);
            jo.put("cmd", "add");
            arr.put(jo);
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

    public void uploadFile2Server(final byte[] file, final UploadFileListener ufl) {
        uploadFile2Server(file, "png", ufl);
    }

    public void uploadFile2Server(final byte[] file, String type, final UploadFileListener ufl) {
        try {
            cc.send(new SenderRequest("upload?filetype=" + type + "&sid=" + cc.getSid(),
                    file,
                    new SenderRequest.HttpDataListener() {

                        @Override
                        public void onResponse(String resp) {
                            try {
                                JSONObject jo = new JSONObject(resp);
                                ufl.onSuccess(jo.optString("url"));

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

    public void sendFile(String name, String desc, String type, String url, String chatId, String formClass) {
        try {
            JSONObject model = new JSONObject();
            model.put("name", name);
            model.put("type", type);
            model.put("desc", desc);
            model.put("url", url);
            final JSONObject jo = getForm2Send(model, formClass, chatId);
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
        public void onSuccess(String url);

        public void onError(Exception e);
    }

    public interface SendMsgListener {
        public void onSuccess(String serverId, long time);

        public void onError(Exception e);
    }

}
