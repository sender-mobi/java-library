package com.sender.library;

import android.os.Build;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.*;
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
    public static final String CLASS_INFO_CHAT = "info.chatrobot.sender";
    public static final String CLASS_UPDATE_CONTACT = "update.contactrobot.sender";
    public static final String CLASS_TYPING_ROUTE = "typing.routerobot.sender";
    public static final String CLASS_ADDUSER_NOTIFY = "adduser_notify.chatrobot.sender";
    public static final String CLASS_READ = ".read.sender";
    public static final String CLASS_DELIV = ".deliv.sender";
    public static final String CLASS_IS_AUTH = "isauth.authrobot.sender";
    public static final String CLASS_SYNC_CONTACT = ".contactSync.sender";
    public static final String CLASS_SET_CONTACT = ".contactSet.sender";
    public static final String CLASS_DEL_CONTACT = ".contactDelete.sender";
    public static final String CLASS_GET_SELF_INFO = ".getSelfInfo.sender";
    public static final String CLASS_SET_SELF_INFO = ".setSelfInfo.sender";
    public static final String CLASS_SET_CHAT = "set.chatrobot.sender";
    public static final String CLASS_PUSH = "push.pushrobot.sender";
    public static final String CLASS_CHECK_ONLINE = ".checkUserStatus.sender";
    public static final String CLASS_SET_LOCATION = ".setDeviceLocation.sender";
    public static final String CLASS_SHARE_LOCATION = ".shareMyLocation.sender";
    public static final String CLASS_WALLET = ".wallet.sender";
    public static final String CLASS_P2P = ".p2p.sender";
    public static final String CLASS_NOTIFY_ADD_YOU = "notify_add_you.chatrobot.sender";
    public static final String CLASS_NOTIFY_DEL_YOU = "notify_del_you.chatrobot.sender";
    public static final String CLASS_NOTIFY_SET = "notify_set.chatrobot.sender";
    public static final String CLASS_AUTH_SUCCESS = "success.auth.sender";
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
    public static final String CLASS_CHESS = ".chess.sender";
    public static final String CLASS_QRCODE = ".qr.sender";
    public static final String CLASS_NOTIFICATION_ADD = ".addChatNotification.sender";
    public static final String CLASS_NOTIFICATION_DEL = ".delChatNotification.sender";
    public static final String CLASS_NOTIFICATION_LEAVE = ".leaveChatNotification.sender";
    public static final String CLASS_LEAVE_CHAT = ".leaveChat.sender";
    public static final String CLASS_STICKER = ".sticker.sender";
    public static final String CLASS_DEVICES = ".devices.sender";
    public static final String CLASS_SEND_ESCALATION = ".reqEscalation.sender";
    public static final String CLASS_IS_ONLINE = ".areYouOnline.sender";
    public static final String CLASS_GAME_TTT = ".ticTacToe.sender";
    public static final String CLASS_ADDUSER_OFFER = "offer.addCtByUserId.sender";
    public static final String CLASS_USER_STATE = ".userState.sender";
    public static final String CLASS_AUTH = ".authNative.sender";
    public static final String CLASS_GAME_WINNIE = ".winnieThePoohHoney.sender";
    public static final String CLASS_VIBRO = ".vibro.sender";
    public static final String CLASS_IP = ".ip.sender";
    public static final String CLASS_START_SYNC = ".startSyncCt.sender";
    public static final String CLASS_FULL_VERSION = ".fullVersion.sender";

    public static final String AUTH_ACTION_PHONE = "phone";
    public static final String AUTH_ACTION_OTP = "otp";
    public static final String AUTH_ACTION_BREAK = "break";
    public static final String AUTH_ACTION_IVR = "ivr";

    public static final String AUTH_STEP_PHONE = "phone";
    public static final String AUTH_STEP_OTP = "otp";
    public static final String AUTH_STEP_CONFIRM = "confirm";
    public static final String AUTH_STEP_IVR = "ivr";
    public static final String AUTH_STEP_FINISH = "success";

    public static final String URL_DEV = "api-dev.sender.mobi";
    public static final String URL_RC = "api-pre.sender.mobi";
    public static final String URL_PROD = "api.sender.mobi";
    public static final String URL_PROD_COM = "senderapi.com";
    public static final String URL_PROD_WWW = "www.senderapi.com";

    public static final CopyOnWriteArrayList<IP> IP_POOL = new CopyOnWriteArrayList<IP>();

    public static final String URL_FORM = "fsubmit";
    public static final String SID_UNDEF = "undef";
    public static final String senderChatId = "sender";

    private static String currUrl;

    private ChatDispatcher cc;

    @SuppressWarnings("unused")
    public ChatFacade(String developerId, String developerKey, String sid, String imei, String devModel, String devType, String clientVersion, int protocolVersoin, SenderListener listener) throws Exception {
        this(URL_PROD_WWW, developerId, developerKey, sid, imei, devModel, devType, clientVersion, protocolVersoin, null, false, listener);
    }
    
    @SuppressWarnings("unused")
    public ChatFacade(String url, String developerId, String developerKey, String sid, String imei, String devModel, String devType, String clientVersion, int protocolVersoin, KeyStore keystore, boolean isShort, SenderListener listener) throws Exception {
        this(url, developerId, developerKey, sid, imei, devModel, devType, clientVersion, protocolVersoin, null, null, keystore, isShort, listener);
    }
    
    @SuppressWarnings("unused")
    public ChatFacade(String url, String developerId, String developerKey,String sid, String imei, String devModel, String devType, String clientVersion, int protocolVersoin, String authToken, String companyId, KeyStore keystore, boolean isShort, final SenderListener listener) throws Exception {
        currUrl = url;
        this.cc = ChatDispatcher.getInstanse(developerId, developerKey, sid, imei, devModel, devType, clientVersion, protocolVersoin, authToken, companyId, keystore, new SenderListener() {
            @Override
            public void onData(JSONObject jo) {
                try {
                    String formClass = (jo.has("formId") ? jo.optString("formId") : "") + "." + jo.optString("robotId") + "." + jo.optString("companyId");
                    if (jo.has("robotId") && jo.has("companyId")) {
                        jo.put("class", formClass);
                    }
                    if (formClass.equalsIgnoreCase(ChatFacade.CLASS_IP)) {
//                        IP_POOL.clear();
//                        JSONObject model = jo.optJSONObject("model");
//                        Iterator keys = model.keys();
//                        while (keys.hasNext()) {
//                            String ip = model.optString(keys.next().toString());
//                            if (ip != null && ip.length() > 6) {
//                                IP_POOL.add(new IP(ip));
//                            }
//                        }
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                listener.onData(jo);
            }

            @Override
            public void onReg(String masterKey, String UDID, boolean fullVer) {
                listener.onReg(masterKey, UDID, fullVer);
            }

            @Override
            public void onNeedUpdate() {
                listener.onNeedUpdate();
            }

            @Override
            public void onToken(String token) {
                listener.onToken(token);
            }

            @Override
            public void onRegError(Exception e) {
                listener.onRegError(e);
            }

            @Override
            public void onConnected() {
                listener.onConnected();
            }

            @Override
            public void onDisconnected() {
                listener.onDisconnected();
            }
        });
        this.cc.startComet(isShort);
    }

    public void startComet(boolean isShort) {
        cc.startComet(isShort);
    }

    public static String getUrl() {
        Collections.shuffle(IP_POOL);
        for (IP ip: IP_POOL) {
            if (ip.isAlive()) {
                try {
                    InetAddress address = InetAddress.getByName(ip.getIp());
                    if (address.isReachable(500)) {
                        return ip.getIp();
                    }
                    Log.v(ChatDispatcher.TAG, "addr " + ip.getIp() + " is dead :(");
                    ip.setErr();
                } catch (Exception e) {
                    e.printStackTrace();
                    ip.setErr();
                }
            }
        }
        return currUrl;
    }

    @SuppressWarnings("unused")
    public void callDevices() {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_DEVICES, senderChatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callTicTacToe(String chatId) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("cmd", "newGame");
            JSONObject form2Send = getForm2Send(jo, CLASS_GAME_TTT, chatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callWinnie(String chatId) {
        try {
            JSONObject jo = new JSONObject();
            JSONObject form2Send = getForm2Send(jo, CLASS_GAME_WINNIE, chatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unused")
    public void callWallet() {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_WALLET, senderChatId);
            cc.send(new SenderRequest(URL_FORM,
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callFullVersion(boolean enable) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("activate", enable);
            JSONObject form2Send = getForm2Send(jo, CLASS_FULL_VERSION, senderChatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callSetChatInfo(String chatId) {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_SET_CHAT_INFO, chatId);
            cc.send(new SenderRequest(URL_FORM,
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void getSipLogin(final String userId, final SipLoginListener sll) {
        final JSONObject jo = new JSONObject();
        try {
            jo.put("userId", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        cc.sendSync("get_user_dev_info", jo, new SenderRequest.HttpDataListener() {
            @Override
            public void onResponse(JSONObject jo) {
                if (jo.has("devs")) {
                    JSONArray arr = jo.optJSONArray("devs");
                    if (arr.length() > 0) {
                        String[] login = new String[arr.length()];
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject joi = arr.optJSONObject(i);
                            login[i] = joi.optString("sipLogin");
                        }
                        sll.onSuccess(login);
                    } else {
                        sll.onSuccess(null);
                    }
                } else {
                    sll.onSuccess(null);
                }
            }

            @Override
            public void onError(Exception e) {
                sll.onError(e, "get_user_dev_info : " + jo.toString());
            }
        });
    }

    @SuppressWarnings("unused")
    public void callShop(String chatId) {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_SHOP, chatId);
            cc.send(new SenderRequest(URL_FORM,
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callChess(String chatId) {
        try {
            JSONObject jo = new JSONObject();
            jo.put("cmd", "create");
            JSONObject form2Send = getForm2Send(jo, CLASS_CHESS, chatId);
            cc.send(new SenderRequest(URL_FORM,
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
            final JSONObject form2Send = getForm2Send(model, CLASS_STICKER, chatId);
            cc.send(new SenderRequest(URL_FORM, form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    try {
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        sml.onSuccess(serverId, time);
                    } catch (Exception e) {
                        sml.onError(e, form2Send.toString());
                    }
                }

                @Override
                public void onError(Exception e) {
                    sml.onError(e, form2Send.toString());
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
        final JSONObject form2Send = getForm2Send(model, className, chatId == null ? senderChatId : chatId, procId);
        cc.send(new SenderRequest(URL_FORM, form2Send, new SenderRequest.HttpDataListener() {
            @Override
            public void onResponse(JSONObject jo) {
                try {
                    String serverId = jo.optString("packetId");
                    long time = jo.optLong("time");
                    sml.onSuccess(serverId, time);
                } catch (Exception e) {
                    sml.onError(e, form2Send.toString());
                }
            }

            @Override
            public void onError(Exception e) {
                sml.onError(e, form2Send.toString());
            }
        }));
    }

    private void send(JSONObject model, String className, String chatId, String procId) {
        try {
            JSONObject form2Send = getForm2Send(model, className, chatId == null ? senderChatId : chatId, procId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
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
    public void sendShareLocation(String lat, String lon, String text, String chatId, final SendMsgListener sml) {
        sendIAmHere(lat, lon, text, "", chatId, sml);
    }

    @SuppressWarnings("unused")
    public void sendVibro(String chatId, boolean isBegin, final SendMsgListener sml) {
        try {
            final JSONObject jo = new JSONObject();
            jo.put("oper", isBegin ? "begin" : "end");
            JSONObject form2Send = getForm2Send(jo, CLASS_VIBRO, chatId);
            cc.send(new SenderRequest(URL_FORM, form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    try {
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        sml.onSuccess(serverId, time);
                    } catch (Exception e) {
                        sml.onError(e, jo.toString());
                    }
                }

                @Override
                public void onError(Exception e) {
                    sml.onError(e, jo.toString());
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void syncContacts(JSONArray users, final JsonRespListener scl) {
        try {
            final JSONObject jo = new JSONObject();
            jo.put("contactRecordList", users);
            cc.sendSync("sync_ct", jo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    scl.onSuccess(jo);
                }

                @Override
                public void onError(Exception e) {
                    scl.onError(e, "sync_ct : " + jo.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void syncDialogs(final JsonRespListener scl) {
        try {
            cc.sendSync("sync_dlg", new JSONObject(), new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    scl.onSuccess(jo);
                }

                @Override
                public void onError(Exception e) {
                    scl.onError(e, "sync_dlg");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void updateContact(JSONObject contact) {
        try {
            JSONObject model = new JSONObject();
            model.put("contactRecord", contact);
            JSONObject form2Send = getForm2Send(model, CLASS_SET_CONTACT, senderChatId);
            cc.send(new SenderRequest(URL_FORM,
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
            JSONObject form2Send = getForm2Send(jo, CLASS_DEL_CONTACT, senderChatId);
            cc.send(new SenderRequest(URL_FORM,
                    form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendAlert(final String chatId) {
        try {
            JSONObject form2Send = getForm2Send(null, CLASS_ALERT, chatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendQR(final String qr) {
        try {
            JSONObject model = new JSONObject();
            model.put("qrCode", qr);
            JSONObject form2Send = getForm2Send(model, CLASS_QRCODE, senderChatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void addToChat(final String chatId, String userId, JsonRespListener listener) {
        addToChat(chatId, new String[]{userId}, listener);
    }

    @SuppressWarnings("unused")
    public void addToChat(final String chatId, String[] userIds, JsonRespListener listener) {
        JSONArray arr = new JSONArray();
        try {
            for (String s : userIds) {
                JSONObject jo = new JSONObject();
                jo.put("userId", s);
                jo.put("cmd", "add");
                arr.put(jo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setChat(chatId, arr, listener);
    }

    @SuppressWarnings("unused")
    public void delFromChat(final String chatId, String userId, JsonRespListener listener) {
        JSONArray arr = new JSONArray();
        try {
            JSONObject jo = new JSONObject();
            jo.put("userId", userId);
            jo.put("cmd", "del");
            arr.put(jo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setChat(chatId, arr, listener);
    }

    @SuppressWarnings("unused")
    public void leaveChat(final String chatId) {
        JSONObject jo = new JSONObject();
        try {
            JSONObject form2Send = getForm2Send(jo, CLASS_LEAVE_CHAT, chatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setChat(final String chatId, JSONArray users, final JsonRespListener listener) {
        try {
            final JSONObject model = new JSONObject();
            model.put("members", users);
            model.put("chatId", chatId);
            cc.sendSync("chat_set", model, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    if (listener != null) listener.onSuccess(jo.optJSONObject("chatInfo"));
                }

                @Override
                public void onError(Exception e) {
                    if (listener != null) listener.onError(e, "chat_set : " + model);
                    else e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendMonitoringData(final float deltaPower, long period, int req_out, int req_in, List<String> apps, final SendListener sl) {
        try {
            final JSONObject model = new JSONObject();
            model.put("delta_power", deltaPower);
            model.put("period", period);
            model.put("requests_out", req_out);
            model.put("messages_in", req_in);
            JSONArray arr = new JSONArray();
            for (String s : apps) {
                arr.put(s);
            }
            model.put("apps", arr);
            JSONObject form2Send = getForm2Send(model, CLASS_SEND_MONITORING, senderChatId);
            cc.send(new SenderRequest(URL_FORM, form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject data) {
                    sl.onSuccess();
                }

                @Override
                public void onError(Exception e) {
                    sl.onError(e, model.toString());
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendEscalation(String className, String packedId, String timeReq, JSONObject reqModel) {
        try {
            JSONObject model = new JSONObject();
            model.put("class", className);
            model.put("packetId", packedId);
            model.put("timeReq", timeReq);
            model.put("model", reqModel);
            JSONObject form2Send = getForm2Send(model, CLASS_SEND_ESCALATION, senderChatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unused")
    public void sendLocale(String locale) {
        try {
            if (locale == null || locale.length() == 0) locale = Locale.getDefault().getLanguage();
            JSONObject model = new JSONObject();
            model.put("locale", locale);
            JSONArray arr = new JSONArray();
            JSONObject form2Send = getForm2Send(model, CLASS_SEND_LOCALE, senderChatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendState(String cat1, String cat2, String cat3, String userId, String chatId) {
        try {
            JSONObject model = new JSONObject();
            model.put("cat1", cat1);
            model.put("cat2", cat2);
            model.put("cat3", cat3);
            model.put("userId", userId);
            model.put("chatId", chatId);
            JSONArray arr = new JSONArray();
            JSONObject form2Send = getForm2Send(model, CLASS_USER_STATE, senderChatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callCompany(String chatId, String compUserId) {
        try {
            JSONObject jo = getForm2Send(new JSONObject(), ".contact."+compUserId, chatId);
            cc.send(new SenderRequest(URL_FORM, jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void callCompanies(final JsonRespListener jrl) {
        try {
            final JSONObject model = new JSONObject();
            cc.sendSync("get_companies_cf", model, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    jrl.onSuccess(jo);
                }

                @Override
                public void onError(Exception e) {
                    jrl.onError(e, "get_companies_cf " + model);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void getUserInfo(String userId, final JsonRespListener jrl) {
        try {
            final JSONObject model = new JSONObject();
            model.put("userId", userId);
            cc.sendSync("get_user_info", model, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    jrl.onSuccess(jo);
                }

                @Override
                public void onError(Exception e) {
                    jrl.onError(e, "get_user_info " + model);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendMessage(final String text, final String chatId, final SendMsgListener sml) {
        try {
            final JSONObject model = new JSONObject();
            model.put("text", text);
            JSONObject form2Send = getForm2Send(model, CLASS_TEXT_ROUTE, chatId);
            cc.send(new SenderRequest(URL_FORM, form2Send, new SenderRequest.HttpDataListener() {

                @Override
                public void onResponse(JSONObject jo) {
                    try {
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        sml.onSuccess(serverId, time);
                    } catch (Exception e) {
                        sml.onError(e, model.toString());
                    }
                }

                @Override
                public void onError(Exception e) {
                    sml.onError(e, model.toString());
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
//    @SuppressWarnings("unused")
//    public void getUserData(final String userId, final UserDataListener udl) {
//        try {
//            JSONObject model = new JSONObject();
//            model.put("userId", userId);
//            JSONObject jo = getForm2Send(model, CLASS_INFO_USER, senderChatId);
//            cc.send(new SenderRequest(URL_FORM, jo, new SenderRequest.HttpDataListener() {
//                @Override
//                public void onResponse(String data) {
//                    try {
//                        JSONObject jo = new JSONObject(data);
//                        String serverId = jo.optString("packetId");
//                        long time = jo.optLong("time");
//                        queue.add(new RespWatcher(CLASS_INFO_USER + "_" + userId, jo, time, serverId, udl));
//                    } catch (Exception e) {
//                        udl.onError(e);
//                    }
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    udl.onError(e);
//                }
//            }));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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
    public void getHistory(final String chatId, final JsonRespListener jrl) {
        try {
            final JSONObject rjo = new JSONObject();
            rjo.put("pos", 0);
            rjo.put("size", 50);
            rjo.put("chatId", chatId);
            cc.sendSync("history", rjo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject data) {
                    jrl.onSuccess(data);
                }

                @Override
                public void onError(Exception e) {
                    jrl.onError(e, rjo.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void nativeAuth(final String action, final String value, final String procId, final AuthListener al) {
        try {
            final JSONObject model = new JSONObject();
            final String url;
            if (AUTH_ACTION_PHONE.equalsIgnoreCase(action)) {
                model.put("phone", value);
            } else if (AUTH_ACTION_OTP.equalsIgnoreCase(action)) {
                model.put("otp", value);
            }
            cc.sendSync("auth_" + action, model, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    al.onSuccess(
                        jo.optString("step")
                        , jo.optString("procId")
                        , jo.optString("devName")
                        , jo.optString("error"));
                }

                @Override
                public void onError(Exception e) {
                    al.onError(e, "auth_" + action + " " + model);
                }
            });
        } catch (Exception e) {
            al.onError(e, "auth_" + action);
        }
    }
    
    @SuppressWarnings("unused")
    public void setChatProfile(InputStream icon, final String type, final String name, final String desc, final String chatId, final UploadFileListener ufl) {
        if (icon != null) {
            uploadFile(icon, type, new UploadFileListener() {

                @Override
                public void onSuccess(final String url) {
                    try {
                        final JSONObject model = new JSONObject();
                        model.put("chatName", name);
                        model.put("chatDesc", desc);
                        model.put("chatPhoto", url);
                        model.put("chatId", chatId);
                        cc.send(new SenderRequest(URL_FORM, getForm2Send(model, CLASS_SET_CHAT_PROFILE, senderChatId), new SenderRequest.HttpDataListener() {
                            @Override
                            public void onResponse(JSONObject data) {
                                ufl.onSuccess(url);
                            }

                            @Override
                            public void onError(Exception e) {
                                ufl.onError(e, model.toString());
                            }
                        }));
                    } catch (Exception e) {
                        ufl.onError(e, null);
                    }
                }

                @Override
                public void onError(Exception e, String req) {
                    ufl.onError(e, req);
                }
            });    
        } else {
            try {
                JSONObject model = new JSONObject();
                model.put("chatName", name);
                model.put("chatDesc", desc);
                model.put("chatId", chatId);
                cc.send(new SenderRequest(URL_FORM, getForm2Send(model, CLASS_SET_CHAT_PROFILE, senderChatId)));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        
    }

    @SuppressWarnings("unused")
    public void sendRead(final String packetId, String from, final String chatId, final JsonRespListener rrl) {
        try {
            JSONObject model = new JSONObject();
            model.put("packetId", packetId);
            model.put("from", from);
            final JSONObject form2Send = getForm2Send(model, CLASS_READ, chatId);
            cc.send(new SenderRequest(URL_FORM, form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject data) {
                    rrl.onSuccess(data);
                }

                @Override
                public void onError(Exception e) {
                    rrl.onError(e, form2Send.toString());
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void getChatInfo(final String chatId, final JsonRespListener cil) {
        try {
            final JSONObject jo = new JSONObject();
            jo.put("chatId", chatId);
            cc.sendSync("chat_info", jo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    cil.onSuccess(jo);
                }

                @Override
                public void onError(Exception e) {
                    cil.onError(e, "chat_info : " + jo.toString());
                }
            });
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

    @SuppressWarnings("unused")
    public void checkOnline(String[] userIdS) {
        try {
            JSONObject model = new JSONObject();
            JSONArray arr = new JSONArray();
            for (String s : userIdS) arr.put(s);
            model.put("userId", arr);
            JSONObject form2Send = getForm2Send(model, CLASS_CHECK_ONLINE, senderChatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void sendOnline(String onlineKey) {
        try {
            cc.send(new SenderRequest("online?online_key=" + onlineKey));
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
            JSONObject form2Send = getForm2Send(model, CLASS_SET_LOCATION, senderChatId);
            cc.send(new SenderRequest(URL_FORM, form2Send));
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
            final JSONObject form2Send = getForm2Send(model, CLASS_SHARE_LOCATION, chatId);
            cc.send(new SenderRequest(URL_FORM, form2Send, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    try {
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        sml.onSuccess(serverId, time);
                    } catch (Exception e) {
                        sml.onError(e, form2Send.toString());
                    }
                }

                @Override
                public void onError(Exception e) {
                    sml.onError(e, form2Send.toString());
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void searchCompanies(final String t, final JsonRespListener cil) {
        try {
            final JSONObject jo = new JSONObject();
            jo.put("t", t);
            cc.sendSync("global_search", jo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    cil.onSuccess(jo);
                }

                @Override
                public void onError(Exception e) {
                    cil.onError(e, "global_search : " + jo.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void versionUpdateSet(final JsonRespListener cil) {
        try {
            final JSONObject jo = new JSONObject();
            jo.put("devType", cc.getDevType());
            jo.put("devModel", cc.getDevModel());
            jo.put("devManufact", "");
            jo.put("devOS", Tool.isAndroid() ? "android" : System.getProperty("os.name"));
            jo.put("versionOS", Tool.isAndroid() ? Build.VERSION.RELEASE : System.getProperty("os.version"));
            jo.put("clientType", Tool.isAndroid() ? "android" : System.getProperty("os.name"));
            jo.put("clientVersion", cc.getClientVersion());
            cc.sendSync("version_set", jo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    cil.onSuccess(jo);
                }

                @Override
                public void onError(Exception e) {
                    cil.onError(e, "version_set : " + jo.toString());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void checkNewMessages(String UDID, CheckListener cl) {
        checkNewMessages(UDID, 0, cl);
    }

    private static void checkNewMessages(final String UDID, final int counter, final CheckListener cl) {
        UDP.send(UDID, 10 * 1000, new UDP.SendListener() {
            @Override
            public void onTimeout() {
                if (counter < 10) {
                    try {
                        Thread.sleep(1000);
                        checkNewMessages(UDID, counter + 1, cl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    cl.onCheck(false);
                }
            }

            @Override
            public void onError(Exception e) {
                cl.onCheck(false);
            }

            @Override
            public void onSuccess(String resp) {
                cl.onCheck(resp.startsWith("1"));
            }
        });
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
    public void sendFile2Chat(final InputStream file, final byte[] preview, String fname, final boolean forceFile, final String desc, final String length, final String chatId, final String lat, final String lon, final SendFileListener sfl) {
        try {
            fname = fname.replace("\\", "/");
            if (fname.contains("/")) fname = fname.substring(fname.lastIndexOf("/")+1);
            final String ffname = fname;
            final String extPart = fname.contains(".") ? fname.substring(fname.lastIndexOf(".") + 1) : "zip";
            if (preview != null) {
                uploadFile(new ByteArrayInputStream(preview), "png", new UploadFileListener() {
                    @Override
                    public void onSuccess(final String previewUrl) {
                        if (lat != null && lon !=null) {
                            sendIAmHere(lat, lon, desc, previewUrl, chatId, new SendMsgListener() {
                                @Override
                                public void onSuccess(String serverId, long time) {
                                    sfl.onSuccess(serverId, time, CLASS_SHARE_LOCATION, extPart, previewUrl);
                                }

                                @Override
                                public void onError(Exception e, String req) {
                                    sfl.onError(e, req);
                                }
                            });
                        }
                        else {
                            uploadFile(file, extPart, new UploadFileListener() {
                                @Override
                                public void onSuccess(final String url) {
                                    try {
                                        doSendFileInfo(ffname, extPart, desc == null ? "" : desc, length == null ? String.valueOf(file.available() / 1024) : length, url, previewUrl, getMediaClass(extPart, forceFile), chatId, sfl);
                                    } catch (Exception e) {
                                        sfl.onError(e, null);
                                    }
                                }

                                @Override
                                public void onError(Exception e, String req) {
                                    sfl.onError(e, req);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e, String req) {
                        sfl.onError(e, req);
                    }
                });
            } else {
                uploadFile(file, extPart, new UploadFileListener() {
                    @Override
                    public void onSuccess(final String url) {
                        if (lat != null && lon !=null) {
                            sendIAmHere(lat, lon, desc, url, chatId, new SendMsgListener() {
                                @Override
                                public void onSuccess(String serverId, long time) {
                                    sfl.onSuccess(serverId, time, CLASS_SHARE_LOCATION, extPart, url);
                                }

                                @Override
                                public void onError(Exception e, String req) {
                                    sfl.onError(e, req);
                                }
                            });
                        } else {
                            try {
                                doSendFileInfo(ffname, extPart, desc == null ? "" : desc, length == null ? String.valueOf(file.available() / 1024) : length, url, null, getMediaClass(extPart, forceFile), chatId, sfl);
                            } catch (Exception e) {
                                sfl.onError(e, null);
                            }
                        }
                    }

                    @Override
                    public void onError(Exception e, String req) {
                        sfl.onError(e, req);
                    }
                });
            }
        } catch (Exception e) {
            sfl.onError(e, "send file " + fname + " to chat " + chatId);
        }
    }

    @SuppressWarnings("unused")
    public void cancelSend() {
//        cc.cancelSend();
    }

    public void uploadFile(final InputStream file, final String type, final UploadFileListener ufl) {
        try {
            final String req = "upload?filetype=" + type + "&sid=" + cc.getSid();
            cc.send(new SenderRequest(req,
                file,
                new SenderRequest.HttpDataListener() {

                    @Override
                    public void onResponse(JSONObject jo) {
                        try {
                            ufl.onSuccess(jo.optString("url"));
                        } catch (Exception e) {
                            ufl.onError(e, req);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        ufl.onError(e, req);
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
            cc.send(new SenderRequest(URL_FORM, jo, new SenderRequest.HttpDataListener() {
                @Override
                public void onResponse(JSONObject jo) {
                    try {
                        String serverId = jo.optString("packetId");
                        long time = jo.optLong("time");
                        sfl.onSuccess(serverId, time, className, type, url);
                    } catch (Exception e) {
                        sfl.onError(e, jo.toString());
                    }
                }

                @Override
                public void onError(Exception e) {
                    sfl.onError(e, jo.toString());
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMediaClass(String ext, boolean forceFile) {
        if (forceFile) return CLASS_FILE_ROUTE;
        if ("png".equalsIgnoreCase(ext) || "jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext) || "gif".equalsIgnoreCase(ext)) {
            return CLASS_IMAGE_ROUTE;
        } else if ("mp3".equalsIgnoreCase(ext)) {
            return CLASS_AUDIO_ROUTE;
        } else if ("mp4".equalsIgnoreCase(ext)) {
            return CLASS_VIDEO_ROUTE;
        } else {
            return CLASS_FILE_ROUTE;
        }
    }

    @SuppressWarnings("unused")
    public void callRechargePhone() {
        try {
            JSONObject jo = getForm2Send(new JSONObject(), CLASS_RECHARGE_PHONE, senderChatId);
            cc.send(new SenderRequest(URL_FORM, jo));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void setMySelfData(
            final String name,
            final String iconUrl,
            final InputStream icon,
            final String type,
            final String description,
            final String msgKey,
            final String payKey,
            final SetSelfListener ufl) {
        try {
            if (icon != null && icon.available() > 0) {
                uploadFile(icon, type, new UploadFileListener() {

                    @Override
                    public void onSuccess(String url) {
                        if (ufl != null) ufl.onSuccess(url);
                        setMySelfData(null, url, null, null, null, null, null, null);
                    }

                    @Override
                    public void onError(Exception e, String request) {
                        if (ufl != null) {
                            ufl.onError(e, request);
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                final JSONObject model = new JSONObject();
                model.put("name", name);
                model.put("photo", iconUrl);
                model.put("description", description);
                model.put("btcAddr", payKey);
                model.put("msgKey", msgKey);
                cc.sendSync("selfinfo_set", model, new SenderRequest.HttpDataListener() {
                    @Override
                    public void onResponse(JSONObject data) {
                        if (ufl != null) ufl.onSetSuccess();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (ufl != null) ufl.onError(e, model.toString());
                        else e.printStackTrace();
                    }
                });
            }   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void getMySelfData(final JsonRespListener jrl) {
        try {
            cc.sendSync("selfinfo_get", new JSONObject(), new SenderRequest.HttpDataListener() {
                        @Override
                        public void onResponse(JSONObject data) {
                            JSONObject jo = data.optJSONObject("selfInfo");
                            jrl.onSuccess(jo);
                        }

                        @Override
                        public void onError(Exception e) {
                            jrl.onError(e, "selfinfo_get");
                        }
                    });
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
        cc.end();
    }

    // ----------------------------------------------------------------------------------------------------------------


    public interface JsonRespListener extends RespListener {
        void onSuccess(JSONObject model);
    }

    public interface SipLoginListener extends RespListener {
        void onSuccess(String[] login);
    }

    public interface AuthListener extends RespListener {
        void onSuccess(String step, String procId, String desc, String errMsg);
    }

    public interface SetSelfListener extends UploadFileListener {
        void onSetSuccess();
    }

    public interface UploadFileListener extends RespListener {
        void onSuccess(String url);
    }

    public interface SendMsgListener extends RespListener {
        void onSuccess(String serverId, long time);
    }
    
    public interface SendListener extends RespListener {
        void onSuccess();
    }

    public interface SendFileListener extends RespListener {
        void onSuccess(String serverId, long time, String className, String type, String url);
    }

    public interface RespListener {
        void onError(Exception e, String req);
    }

    public interface CheckListener extends RespListener{
        void onCheck(boolean rez);
    }

    public interface SenderListener {
        void onData(JSONObject jo);
        void onReg(String masterKey, String UDID, boolean fullVer);
        void onNeedUpdate();
        void onToken(String token);
        void onRegError(Exception e);
        void onConnected();
        void onDisconnected();
    }

}
