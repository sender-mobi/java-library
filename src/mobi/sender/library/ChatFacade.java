package mobi.sender.library;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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


    private ChatConnector cc;
    private String TAG;
    private ChatConnector.SenderListener listener;

    public ChatFacade(String sid, String imei, String devName, String devType, int number, ChatConnector.SenderListener listener) {
        this.cc = new ChatConnector(sid, imei, devName, devType, number, listener);
        this.TAG = "["+number+"]";
    }

    public void checkAuth() {
        try {
            JSONObject form2Send = getForm2Send(null, "isauth.authrobot.sender", ChatConnector.senderChatId);
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

    public void syncContacts(JSONArray contacts) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("contacts", contacts);
            JSONObject form2Send = getForm2Send(jo, "sync.contactrobot.sender", ChatConnector.senderChatId);
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
            JSONObject form2Send = getForm2Send(model, "set.chatrobot.sender", ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(final String text, final String chatId) {
        try {
            JSONObject model = new JSONObject();
            model.put("text", text);
            JSONObject form2Send = getForm2Send(model, "text.routerobot.sender", chatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send
                    , new SenderRequest.HttpDataListener() {

                @Override
                public void onResponse(String resp) {
                    try {
                        JSONObject jo = new JSONObject(resp);
                        String serverId = jo.optString("ref");
                        long time = jo.optLong("time");
                        Log.v(TAG, "Message sended. ServerId=" + serverId + ", time=" + time);
                    } catch (JSONException e) {
                        e.printStackTrace();
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

    public void sendRead(final String packetId, final String chatId) {
        try {
            JSONObject model = new JSONObject();
            model.put("packetId", packetId);
            JSONObject form2Send = getForm2Send(model, "read.statusrobot.sender", chatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getChatInfo(String chatId) {
        try {
            JSONObject jo = getForm2Send(null, "info.chatrobot.sender", chatId);
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
            HttpPost post = new HttpPost(cc.getUrl() + "typing");
            post.setEntity(new ByteArrayEntity(rjo.toString().getBytes()));
            Log.v(TAG, "========> "+rjo.toString());
            HttpResponse response = new DefaultHttpClient().execute(post);
            Log.v(TAG, "<-------- "+response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendIAmOnline() {
        try {
            JSONObject rjo = new JSONObject();
            rjo.put("sid", cc.getSid());
            HttpPost post = new HttpPost(cc.getUrl() + "i_am_online");
            post.setEntity(new ByteArrayEntity(rjo.toString().getBytes()));
            Log.v(TAG, "========> "+rjo.toString());
            HttpResponse response = new DefaultHttpClient().execute(post);
            Log.v(TAG, "<-------- "+response);
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
            JSONObject form2Send = getForm2Send(model, "check.status.sender", ChatConnector.senderChatId);
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
        try {
            cc.send(new SenderRequest("upload?filetype=png" + "&sid=" + cc.getSid(),
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

    public interface UploadFileListener {
        public void onSuccess(String url);

        public void onError(Exception e);
    }


}
