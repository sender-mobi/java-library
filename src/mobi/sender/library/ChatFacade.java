package mobi.sender.library;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public static final String CLASS_VIDEO_ROUTE = ".videoMsg.sender";
    public static final String CLASS_INFO_USER = ".getUserInfo.sender";
    @Deprecated
    public static final String CLASS_INFO_CHAT = "info.chatrobot.sender";
    public static final String CLASS_UPDATE_CONTACT = "update.contactrobot.sender";
    public static final String CLASS_TYPING_ROUTE = "typing.routerobot.sender";
    public static final String CLASS_ADDUSER_NOTIFY = "adduser_notify.chatrobot.sender";
    public static final String CLASS_READ = "read.statusrobot.sender";
    public static final String CLASS_DELIV = "deliv.statusrobot.sender";
    public static final String CLASS_IS_AUTH = "isauth.authrobot.sender";
    public static final String CLASS_SYNC_CONTACT = ".contactSync.sender";
    public static final String CLASS_SET_CONTACT = ".contactSet.sender";
    public static final String CLASS_DEL_CONTACT = ".contactDelete.sender";
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
    public static final String CLASS_AUTH_IVR = "ivr.auth.sender";
    public static final String CLASS_AUTH_CONFIRM_OTHER = "confirm.auth.sender";
    public static final String CLASS_RECHARGE_PHONE = ".payMobile.sender";
    public static final String CLASS_FINISH_AUTH = "finish.auth.sender";
    public static final String CLASS_ALERT = ".alert.sender";
    public static final String CLASS_ALERT_KICK = "kickass.alert.sender";
    public static final String CLASS_SET_CHAT_INFO = ".chatSetInfoForm.sender";
    public static final String CLASS_CHAT_INFO_NOTIFICATION = ".chatSetNotification.sender";
    public static final String CLASS_SET_CHAT_PROFILE = ".chatSetInfo.sender";
    public static final String CLASS_SENDMONEY = ".sendMoney.sender";
    public static final String CLASS_P2P_TRANSFER = "transfer.p2p.sender";
    public static final String CLASS_SEND_MONITORING = ".monitoringData.sender";
    public static final String CLASS_SEND_LOCALE = ".setDeviceLocale.sender";
    public static final String CLASS_SHOP = ".worldOfTanks.sender";
    public static final String CLASS_QRCODE = ".qr.sender";
    public static final String CLASS_NOTIFICATION_ADD = ".addChatNotification.sender";
    public static final String CLASS_NOTIFICATION_DEL = ".delChatNotification.sender";
    public static final String CLASS_NOTIFICATION_LEAVE = ".leaveChatNotification.sender";
    public static final String CLASS_LEAVE_CHAT = ".leaveChat.sender";
    public static final String CLASS_STICKER = ".sticker.sender";
    public static final String CLASS_DEVICES = ".devices.sender";
    public static final String CLASS_SEND_ESCALATION = ".reqEscalation.sender";
    public static final String CLASS_IS_ONLINE = ".areYouOnline.sender";
    
    public static final int NEXT_STEP_AUTH_PHONE = 0;
    public static final int NEXT_STEP_AUTH_OTP = 1;
    public static final int NEXT_STEP_AUTH_CONFIRM = 2;
    public static final int NEXT_STEP_AUTH_IVR = 3;
    public static final int NEXT_STEP_AUTH_END = 4;
    
    private CopyOnWriteArrayList<RespWatcher> queue = new CopyOnWriteArrayList<RespWatcher>();
    
    private ChatConnector cc;
    private Monitor monitor;

    @SuppressWarnings("unused")
    public ChatFacade(String sid, String imei, String devModel, String devType, String clientVersion, int protocolVersoin, int number, ChatConnector.SenderListener listener) throws Exception {
        this(ChatConnector.URL_PROD, sid, imei, devModel, devType, clientVersion, protocolVersoin, number, listener);
    }
    
    @SuppressWarnings("unused")
    public ChatFacade(String url, String sid, String imei, String devModel, String devType, String clientVersion, int protocolVersoin, int number, ChatConnector.SenderListener listener) throws Exception {
        this(url, sid, imei, devModel, devType, clientVersion, protocolVersoin, number, null, null, listener);
    }
    
    @SuppressWarnings("unused")
    public ChatFacade(String url, String sid, String imei, String devModel, String devType, String clientVersion, int protocolVersoin, int number, String authToken, String companyId, final ChatConnector.SenderListener listener) throws Exception {
        if (monitor != null) {
            monitor.interrupt();
        }
        monitor = new Monitor();
        monitor.start();
        this.cc = new ChatConnector(url, sid, imei, devModel, devType, clientVersion, protocolVersoin, number, authToken, companyId, new ChatConnector.SenderListener() {
            @Override
            public void onData(JSONObject jo) {
                String formClass = jo.optString("class");
                if (formClass.trim().length() == 0) {
                    listener.onData(jo);
                    return;
                }
                RespWatcher watcher = null;
                try {
//                    if (CLASS_IS_AUTH.equals(formClass)) {
//                        watcher = getWatcher(formClass);
//                        if (watcher == null) {
//                            listener.onData(jo);
//                            return;
//                        }
//                        JSONObject model = jo.optJSONObject("model");
//                        boolean isAuth = model.optBoolean("auth");
//                        ((CheckAuthListener) watcher.getListener()).onSuccess(isAuth);
//                    } else
                    if (CLASS_INFO_USER.equalsIgnoreCase(formClass)) {
                        JSONObject model = jo.optJSONObject("model");
                        if (model == null) {
                            listener.onData(jo);
                            return;
                        }
                        watcher = getWatcher(formClass + "_" + model.optString("userId"));
                        if (watcher == null) {
                            listener.onData(jo);
                            return;
                        }
                        String userId = model.optString("userId");
                        String urlPhoto = model.optString("photo");
                        String name = model.optString("name");
                        ((UserDataListener)watcher.getListener()).onSuccess(userId, name, urlPhoto);
                    } else if (CLASS_INFO_CHAT.equalsIgnoreCase(formClass)) {
                        JSONObject model = jo.optJSONObject("model");
                        if (model == null) {
                            listener.onData(jo);
                            return;
                        }
                        watcher = getWatcher(formClass + "_" + model.optString("chatId"));
                        if (watcher == null) {
                            listener.onData(jo);
                            return;
                        }
                        ((ChatInfoListener)watcher.getListener()).onSuccess(model);
                    } else if (CLASS_AUTH_PHONE.equalsIgnoreCase(formClass)) {
                        if (jo.has("view") && "disable".equalsIgnoreCase(jo.optJSONObject("view").optString("state"))) return;
                        if (!jo.has("model")) {
                            listener.onData(jo);
                            return;
                        }
                        watcher = getWatcher(formClass);
                        if (watcher == null) {
                            listener.onData(jo);
                            return;
                        }
                        if (jo.optJSONObject("model").has("phone")) {
                            ((AuthNativeListener)watcher.getListener()).onSuccess(
                                    NEXT_STEP_AUTH_PHONE
                                    , null
                                    , null);
                        } else if (!"ok".equalsIgnoreCase(jo.optJSONObject("model").optString("resp"))) {
                            ((AuthNativeListener)watcher.getListener()).onSuccess(
                                    NEXT_STEP_AUTH_PHONE
                                    , jo.optString("procId")
                                    , jo.optJSONObject("model").optString("code"));   
                        } else {
                            Log.v("ChatFacade", "auth_phone success");
                        }
                    } else if (CLASS_AUTH_OTP.equalsIgnoreCase(formClass)) {
                        if (jo.has("view") && "disable".equalsIgnoreCase(jo.optJSONObject("view").optString("state"))) return;
                        //  ---------------------- try close phone request
                        if (!jo.has("model")) {
                            listener.onData(jo);
                            return;
                        }
                        watcher = getWatcher(CLASS_AUTH_PHONE);
                        if (watcher == null) {
                            listener.onData(jo);
                            return;
                        }
                        ((AuthNativeListener)watcher.getListener()).onSuccess(
                                NEXT_STEP_AUTH_OTP
                                , jo.optString("procId")
                                , jo.optJSONObject("model").optString("code"));
                        //  ---------------------- try close otp request
                        watcher = getWatcher(CLASS_AUTH_OTP);
                        if (watcher == null) {
                            listener.onData(jo);
                            return;
                        }
                        ((AuthNativeListener)watcher.getListener()).onSuccess(
                                NEXT_STEP_AUTH_OTP
                                , jo.optString("procId")
                                , jo.optJSONObject("model").optString("code"));
                    } else if (CLASS_AUTH_CONFIRM_OTHER.equalsIgnoreCase(formClass)) {
                        if (!jo.has("model")) {
                            listener.onData(jo);
                            return;
                        }
                        //  ---------------------- try close phone request
                        watcher = getWatcher(CLASS_AUTH_PHONE);
                        if (watcher == null) {
                            listener.onData(jo);
                            return;
                        }
                        ((AuthNativeListener)watcher.getListener()).onSuccess(
                                NEXT_STEP_AUTH_CONFIRM
                                , null
                                , null);
                    } else if (CLASS_FINISH_AUTH.equalsIgnoreCase(formClass)) {
                        if (!jo.has("model")) {
                            listener.onData(jo);
                            return;
                        }
                        //  ---------------------- try close otp request
                        watcher = getWatcher(CLASS_AUTH_OTP);
                        if (watcher == null) {
                            listener.onData(jo);
                            return;
                        }
                        ((AuthNativeListener)watcher.getListener()).onSuccess(
                                NEXT_STEP_AUTH_END
                                , null
                                , null);
                    } else if (CLASS_AUTH_IVR.equalsIgnoreCase(formClass)) {
                        if (!jo.has("model")) {
                            listener.onData(jo);
                            return;
                        }
                        //  ---------------------- try close otp request
                        watcher = getWatcher(CLASS_AUTH_OTP);
                        if (watcher == null) {
                            listener.onData(jo);
                            return;
                        }
                        ((AuthNativeListener)watcher.getListener()).onSuccess(
                                NEXT_STEP_AUTH_IVR
                                , null
                                , null);
                    }
                    // TODO: other resp    
                    else listener.onData(jo);
                } catch (Exception e) {
                    if (watcher != null) watcher.getListener().onError(e);
                    else e.printStackTrace();
                }
            }

            @Override
            public void onReg(String sid) {
                listener.onReg(sid);
            }

            @Override
            public void onNeedUpdate() {
                listener.onNeedUpdate();
            }

            @Override
            public void onRegError(Exception e) {
                listener.onRegError(e);
            }
        });
    }
    
    private RespWatcher getWatcher(String className) {
        for (RespWatcher rw : queue) {
            if (className.equalsIgnoreCase(rw.getClassName())) {
                queue.remove(rw);
                return rw;
            }
        }
        return null;
    }
    
//    @SuppressWarnings("unused")
//    public void checkAuth(final CheckAuthListener checkAuthListener) {
//        try {
//            final JSONObject form2Send = getForm2Send(null, CLASS_IS_AUTH, ChatConnector.senderChatId);
//            cc.send(new SenderRequest("fsubmit", form2Send, new SenderRequest.HttpDataListener() {
//                @Override
//                public void onResponse(String data) {
//                    try {
//                        JSONObject jo = new JSONObject(data);
//                        String serverId = jo.optString("packetId");
//                        long time = jo.optLong("time");
//                        queue.add(new RespWatcher(CLASS_IS_AUTH, form2Send, time, serverId, checkAuthListener));
//                    } catch (Exception e) {
//                        checkAuthListener.onError(e);
//                    }
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    checkAuthListener.onError(e);
//                }
//            }));
//        } catch (Exception e) {
//            checkAuthListener.onError(e);
//        }
//    }

//    @SuppressWarnings("unused")
//    public void checkVersion(final CheckVersionListener cvl) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    int ver = -1;
//                    String url = null;
//                    Log.v(cc.getTAG(), "====> " + ChatConnector.URL_PROD + "get_version");
//                    String s = EntityUtils.toString(new DefaultHttpClient().execute(new HttpGet(ChatConnector.URL_PROD + "get_version")).getEntity());
//                    Log.v(cc.getTAG(), "<---- " + s);
//                    JSONObject jo = new JSONObject(s);
//                    if (jo.has("android_version")) {
//                        ver = jo.optInt("android_version");
//                    }
//                    if (jo.has("android")) {
//                        url = jo.optString("android");
//                    }
//                    cvl.onSuccess(ver, url);
//                } catch (Exception e) {
//                    cvl.onError(e);
//                }
//            }
//        }).start();
//    }

    @SuppressWarnings("unused")
    public void callDevices() {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_DEVICES, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unused")
    public void callWallet() {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_WALLET, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callSetChatInfo(String chatId) {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_SET_CHAT_INFO, chatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callShop(String chatId) {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_SHOP, chatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendSticker(String id, String chatId, final SendMsgListener sml) {
        try {
            JSONObject model = new JSONObject();
            model.put("id", id);
            JSONObject form2Send = getForm2Send(model, CLASS_STICKER, chatId);
            cc.send(new SenderRequest("fsubmit", form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(String data) {
                    JSONObject jo = null;
                    try {
                        jo = new JSONObject(data);
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        sml.onSuccess(serverId, time);
                    } catch (JSONException e) {
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

    @SuppressWarnings("unused")
    public void send(JSONObject model, String className) {
        send(model, className, null, null);
    }

    @SuppressWarnings("unused")
    public void sendForm(JSONObject model, String className, String chatId, String procId, final SendMsgListener sml) {
        JSONObject form2Send = getForm2Send(model, className, chatId == null ? ChatConnector.senderChatId : chatId, procId);
        cc.send(new SenderRequest("fsubmit", form2Send, new SenderRequest.HttpDataListener() {
            @Override
            public void onResponse(String resp) {
                try {
                    JSONObject jo = new JSONObject(resp);
                    String serverId = jo.optString("packetId");
                    long time = jo.optLong("time");
                    sml.onSuccess(serverId, time);
                } catch (JSONException e) {
                    sml.onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                sml.onError(e);
            }
        }));
    }

    private void send(JSONObject model, String className, String chatId, String procId) {
        try {
            JSONObject form2Send = getForm2Send(model, className, chatId == null ? ChatConnector.senderChatId : chatId, procId);
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

    @SuppressWarnings("unused")
    public void syncContacts(JSONArray users) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("contactRecordList", users);
            JSONObject form2Send = getForm2Send(jo, CLASS_SYNC_CONTACT, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void updateContact(JSONObject contact) {
        try {
            JSONObject model = new JSONObject();
            model.put("contactRecord", contact);
            JSONObject form2Send = getForm2Send(model, CLASS_SET_CONTACT, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void deleteContact(String contactId) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("contactId", contactId);
            JSONObject form2Send = getForm2Send(jo, CLASS_DEL_CONTACT, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendAlert(final String chatId) {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_ALERT, chatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendQR(final String qr) {
        try {
            JSONObject model = new JSONObject();
            model.put("qrCode", qr);
            JSONObject form2Send = getForm2Send(model, CLASS_QRCODE, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void leaveChat(final String chatId) {
        JSONObject jo = new JSONObject();
        try {
            JSONObject form2Send = getForm2Send(jo, CLASS_LEAVE_CHAT, chatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @SuppressWarnings("unused")
    public void sendMonitoringData(final float deltaPower, long period, int req_out, int req_in, List<String> apps, final SendListener sl) {
        try {
            JSONObject model = new JSONObject();
            model.put("delta_power", deltaPower);
            model.put("period", period);
            model.put("requests_out", req_out);
            model.put("messages_in", req_in);
            JSONArray arr = new JSONArray();
            for (String s : apps) {
                arr.put(s);
            }
            model.put("apps", arr);
            JSONObject form2Send = getForm2Send(model, CLASS_SEND_MONITORING, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(String data) {
                    sl.onSuccess();
                }

                @Override
                public void onError(Exception e) {
                    sl.onError(e);
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendEscalation(RespWatcher rw) {
        try {
            JSONObject model = new JSONObject();
            model.put("class", rw.getClassName());
            model.put("packetId", rw.getPacketId());
            model.put("timeReq", rw.getSended());
            model.put("model", rw.getModel());
            JSONObject form2Send = getForm2Send(model, CLASS_SEND_ESCALATION, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unused")
    public void sendLocale() {
        try {
            JSONObject model = new JSONObject();
            model.put("locale", Locale.getDefault().getLanguage());
            JSONArray arr = new JSONArray();
            JSONObject form2Send = getForm2Send(model, CLASS_SEND_LOCALE, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit",
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callCompany(String chatId, String compUserId) {
        try {
            JSONObject jo = getForm2Send(new JSONObject(), ".contact."+compUserId, chatId);

            cc.send(new SenderRequest("fsubmit", jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
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
                        String serverId = jo.optString("packetId");
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
    
    @SuppressWarnings("unused")
    public void getUserData(final String userId, final UserDataListener udl) {
        try {
            JSONObject model = new JSONObject();
            model.put("userId", userId);
            JSONObject jo = getForm2Send(model, CLASS_INFO_USER, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", jo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(String data) {
                    try {
                        JSONObject jo = new JSONObject(data);
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        queue.add(new RespWatcher(CLASS_INFO_USER + "_" + userId, jo, time, serverId, udl));
                    } catch (Exception e) {
                        udl.onError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    udl.onError(e);
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void nativeAuthStepPhone(final String phone, String procId, final AuthNativeListener aSpl) {
        try {
            final JSONObject model = new JSONObject();
            model.put("phone", phone);
            JSONObject form2Send = getForm2Send(model, CLASS_AUTH_PHONE, ChatConnector.senderChatId, procId);
            cc.send(new SenderRequest("fsubmit", form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(String data) {
                    try {
                        JSONObject jo = new JSONObject(data);
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        queue.add(new RespWatcher(CLASS_AUTH_PHONE, model, time, serverId, aSpl));
                    } catch (Exception e) {
                        aSpl.onError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    aSpl.onError(e);
                }
            }));
        } catch (Exception e) {
            aSpl.onError(e);
        }
    }

    @SuppressWarnings("unused")
    public void nativeAuthStepOtpIvr(String otp, String procId, boolean isIvr, final AuthNativeListener aSpl) {
        try {
            final JSONObject model = new JSONObject();
            model.put("password", otp);
            model.put("ivr", isIvr);
            JSONObject form2Send = getForm2Send(model, CLASS_AUTH_OTP, ChatConnector.senderChatId, procId);
            cc.send(new SenderRequest("fsubmit", form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(String data) {
                    try {
                        JSONObject jo = new JSONObject(data);
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        queue.add(new RespWatcher(CLASS_AUTH_OTP, model, time, serverId, aSpl));
                    } catch (Exception e) {
                        aSpl.onError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    aSpl.onError(e);
                }
            }));
        } catch (Exception e) {
            aSpl.onError(e);
        }
    }

    @SuppressWarnings("unused")
    public void nativeAuthStepName(String userName, String procId) {
        try {
            JSONObject model = new JSONObject();
            model.put("userName", userName);
            JSONObject form2Send = getForm2Send(model, CLASS_AUTH_SUCCESS, ChatConnector.senderChatId, procId);
            cc.send(new SenderRequest("fsubmit", form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unused")
    public void setChatProfile(InputStream icon, final String type, final String name, final String desc, final String chatId) {
        if (icon != null) {
            uploadFile(icon, type, new UploadFileListener() {

                @Override
                public void onSuccess(String url) {
                    try {
                        JSONObject model = new JSONObject();
                        model.put("chatName", name);
                        model.put("chatDesc", desc);
                        model.put("chatPhoto", url);
                        model.put("chatId", chatId);
                        cc.send(new SenderRequest("fsubmit", getForm2Send(model, CLASS_SET_CHAT_PROFILE, ChatConnector.senderChatId)));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });    
        } else {
            try {
                JSONObject model = new JSONObject();
                model.put("chatName", name);
                model.put("chatDesc", desc);
                model.put("chatId", chatId);
                cc.send(new SenderRequest("fsubmit", getForm2Send(model, CLASS_SET_CHAT_PROFILE, ChatConnector.senderChatId)));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void getChatInfo(final String chatId, final ChatInfoListener cil) {
        try {
            JSONObject jo = getForm2Send(null, CLASS_INFO_CHAT, chatId);
            cc.send(new SenderRequest("fsubmit", jo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(String data) {
                    try {
                        JSONObject jo = new JSONObject(data);
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        queue.add(new RespWatcher(CLASS_INFO_CHAT + "_" + chatId, jo, time, serverId, cil));
                    } catch (Exception e) {
                        cil.onError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    cil.onError(e);
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
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

//    @SuppressWarnings("unused")
//    public void sendIAmOnline() {
//        try {
//            JSONObject rjo = new JSONObject();
//            rjo.put("sid", cc.getSid());
//            send("i_am_online", rjo);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    private void sendIAmHere(String lat, String lon, String message, String mapImgUrl, String chatId, final SendMsgListener sml) {
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
                        String serverId = jo.optString("packetId");
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
        return getForm2Send(model, formClass, chatId, null);
    }

    private JSONObject getForm2Send(JSONObject model, String formClass, String chatId, String procId) {
        JSONObject rez = new JSONObject();
        try {
            if (model != null) rez.put("model", model);
            if (chatId != null) rez.put("chatId", chatId);
            if (procId != null) rez.put("procId", procId);
            if (formClass != null) {
                rez.put("class", formClass);
                String[] ss = formClass.split("\\.");
                if (ss.length == 3) {
                    rez.put("formId", ss[0]);
                    rez.put("robotId", ss[1]);
                    rez.put("companyId", ss[2]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rez;
    }

    @SuppressWarnings("unused")
    public void sendFile2Chat(final InputStream file, final byte[] preview, String fname, final String desc, final String length, final String chatId, final String lat, final String lon, final SendFileListener sfl) {
        try {
            fname = fname.replace("\\", "/");
            if (fname.contains("/")) fname = fname.substring(fname.lastIndexOf("/")+1);
            if (!fname.contains(".")) throw new Exception("invalid file name");
            final String ffname = fname;
            final String[] parts = fname.split("\\.");
            if (preview != null) {
                
                uploadFile(new ByteArrayInputStream(preview), "png", new UploadFileListener() {
                    @Override
                    public void onSuccess(final String previewUrl) {
                        if (lat != null && lon !=null) {
                            sendIAmHere(lat, lon, desc, previewUrl, chatId, new SendMsgListener() {
                                @Override
                                public void onSuccess(String serverId, long time) {
                                    sfl.onSuccess(serverId, time, CLASS_SHARE_LOCATION, parts[1], previewUrl);
                                }

                                @Override
                                public void onError(Exception e) {
                                    sfl.onError(e);
                                }
                            });
                        }
                        else {
                            uploadFile(file, parts[1], new UploadFileListener() {
                                @Override
                                public void onSuccess(final String url) {
                                    try {
                                        doSendFileInfo(ffname, parts[1], desc == null ? "" : desc, length == null ? String.valueOf(file.available() / 1024) : length, url, previewUrl, getMediaClass(parts[1]), chatId, sfl);   
                                    } catch (Exception e) {
                                        sfl.onError(e);
                                    }
                                }

                                @Override
                                public void onError(Exception e) {
                                    sfl.onError(e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                uploadFile(file, parts[1], new UploadFileListener() {
                    @Override
                    public void onSuccess(final String url) {
                        if (lat != null && lon !=null) {
                            sendIAmHere(lat, lon, desc, url, chatId, new SendMsgListener() {
                                @Override
                                public void onSuccess(String serverId, long time) {
                                    sfl.onSuccess(serverId, time, CLASS_SHARE_LOCATION, parts[1], url);
                                }

                                @Override
                                public void onError(Exception e) {
                                    sfl.onError(e);
                                }
                            });
                        } else {
                            try {
                                doSendFileInfo(ffname, parts[1], desc == null ? "" : desc, length == null ? String.valueOf(file.available() / 1024) : length, url, null, getMediaClass(parts[1]), chatId, sfl);
                            } catch (Exception e) {
                                sfl.onError(e);
                            }
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        sfl.onError(e);
                    }
                });
            }
        } catch (Exception e) {
            sfl.onError(e);
        }
    }

    @SuppressWarnings("unused")
    public void cancelSend() {
        cc.cancelSend();
    }

    public void uploadFile(final InputStream file, final String type, final UploadFileListener ufl) {
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
            e.printStackTrace();
        }
    }

    private void doSendFileInfo(String name, final String type, String desc, String length, final String url, final String previewUrl, final String className, String chatId, final SendFileListener sfl) {
        try {
            JSONObject model = new JSONObject();
            model.put("name", name);
            model.put("type", type);
            model.put("desc", desc);
            if (previewUrl != null) model.put("preview", previewUrl);
            if (length != null) model.put("length", length);
            model.put("url", url);
            final JSONObject jo = getForm2Send(model, className, chatId);
            cc.send(new SenderRequest("fsubmit", jo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(String data) {
                    try {
                        JSONObject jo = new JSONObject(data);
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        sfl.onSuccess(serverId, time, className, type, url);
                    } catch (Exception e) {
                        sfl.onError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    sfl.onError(e);
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void sendDeliv(String url, String packetId, String sid) {
        ChatConnector.sendDeliv(url, packetId, sid);
    }

    private String getMediaClass(String ext) {
        if ("png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext) || "gif".equals(ext)) {
            return CLASS_IMAGE_ROUTE;
        } else if ("mp3".equals(ext)) {
            return CLASS_AUDIO_ROUTE;
        } else if ("mp4".equals(ext)) {
            return CLASS_VIDEO_ROUTE;
        } else {
            return CLASS_FILE_ROUTE;
        }
    }

    @SuppressWarnings("unused")
    public void callRechargePhone() {
        try {
            JSONObject jo = getForm2Send(new JSONObject(), CLASS_RECHARGE_PHONE, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void setMySelfData(final String name, final InputStream icon, final String type, final String description) {
        try {
            if (icon != null && icon.available() > 0) {
                uploadFile(icon, type, new UploadFileListener() {

                    @Override
                    public void onSuccess(String url) {
                        sendMySelfData(url, name, description);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                sendMySelfData(null, name, description);
            }   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMySelfData(String url, String name, String description) {
        try {
            JSONObject model = new JSONObject();
            model.put("name", name);
            model.put("photo", url);
            model.put("description", description);
            JSONObject jo = getForm2Send(model,CLASS_SET_SELF_INFO, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void getMySelfData() {
        try {
            JSONObject jo = getForm2Send(new JSONObject(), CLASS_GET_SELF_INFO, ChatConnector.senderChatId);
            cc.send(new SenderRequest("fsubmit", jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    @SuppressWarnings("unused")
    public boolean isConnected() {
        return cc.isAlive();
    }

    @SuppressWarnings("unused")
    public void stop() {
        
        cc.doDisconnect();
    }

    // ----------------------------------------------------------------------------------------------------------------

    public interface AuthNativeListener extends RespWatcher.RespListener {
        public void onSuccess(int next, String procId, String errMsg);
    }
    
    public interface UserDataListener extends RespWatcher.RespListener {
        public void onSuccess(String userId, String name, String photoUrl);
    }
    
    public interface ChatInfoListener extends RespWatcher.RespListener {
        public void onSuccess(JSONObject model);
    }
    
    public interface CheckAuthListener extends RespWatcher.RespListener {
        public void onSuccess(boolean auth);
    }
    
    public interface UploadFileListener {
        public void onSuccess(String url);

        public void onError(Exception e);
    }

    public interface SendMsgListener {
        public void onSuccess(String serverId, long time);

        public void onError(Exception e);
    }
    
    public interface SendListener {
        public void onSuccess();

        public void onError(Exception e);
    }

    public interface SendFileListener {
        public void onSuccess(String serverId, long time, String className, String type, String url);

        public void onError(Exception e);
    }
    
    public interface CheckVersionListener {
        public void onSuccess(int ver, String url);

        public void onError(Exception e);
    }

    private class Monitor extends Thread {

        private static final long MAX_WAIT_TIME = 20 * 1000;

        public Monitor() {
            super("respMonitor");
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    for (RespWatcher rw : queue) {
                        if (System.currentTimeMillis() - rw.getSended() > MAX_WAIT_TIME) {
                            queue.remove(rw);
                            sendEscalation(rw);
                            rw.getListener().onTimeout();
                        }
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
