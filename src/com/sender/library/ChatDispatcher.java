package com.sender.library;

import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyStore;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 18.02.15
 * Time: 08:38
 */
public class ChatDispatcher {

    private String UDID, masterKey;
    public static final String CODE_NEW_CHALLENGE = "1";
    public static final String CODE_INVALID_UDID = "3";
    public static final String CODE_OK = "0";
    public static final String CODE_NEED_UPDATE = "8";
    private String url;
    private ChatFacade.SenderListener sml;
    public static final String senderChatId = "sender";
    public static ChatDispatcher instanse;
    private KeyStore keyStore;
    public static final String TAG = "ChatDispatcher";
    private String devModel, devType, clientVersion, authToken, companyId;
    private String developerKey;
    private String developerId;

    private Sender sender;
    private Comet comet;
    private String token;
    private String cometId;

    private ChatDispatcher(String url,
                         String developerId,
                         String developerKey,
                         String masterKey,
                         String devId,
                         String devModel,
                         String devType,
                         String clientVersion,
                         int protocolVersion,
                         String authToken,
                         String companyId,
                         KeyStore keyStore,
                         ChatFacade.SenderListener sml) {
        this.developerId = developerId;
        this.developerKey = developerKey;
        this.devModel = devModel;
        this.devType = devType;
        this.keyStore = keyStore;
        this.clientVersion = clientVersion;
        this.authToken = authToken;
        this.companyId = companyId;
        this.masterKey = masterKey;
        this.UDID = hmacDigest(devId, developerKey, "HmacSHA1");
        this.url = url + protocolVersion + "/";
        this.sml = sml;
        sender = new Sender(this, this.url, keyStore);
    }

    public static ChatDispatcher getInstanse(String url,
                              String developerId,
                              String developerKey,
                              String masterKey,
                              String devId,
                              String devModel,
                              String devType,
                              String clientVersion,
                              int protocolVersion,
                              String authToken,
                              String companyId,
                              KeyStore keyStore,
                              ChatFacade.SenderListener sml) {
        if (instanse == null) instanse = new ChatDispatcher(url, developerId, developerKey, masterKey, devId, devModel, devType, clientVersion, protocolVersion, authToken, companyId, keyStore, sml);
        instanse.masterKey = masterKey;
        instanse.startComet();
        if (ChatFacade.SID_UNDEF.equalsIgnoreCase(masterKey)) {
            instanse.onNeedReg();
        }
        return instanse;
    }

    public void end() {
        setCometId(null);
        comet.disconnect();
    }
    
    public void send(SenderRequest request) {
        sender.send(request);
        startComet();
    }

    public void sendSync(String url, JSONObject data, SenderRequest.HttpDataListener listener) {
        SenderRequest sr = new SenderRequest(url, data, listener);
        send(sr);
    }
    
    public void onRegOk(String deviceKey) {
        String masterKey = hmacDigest(UDID + deviceKey, developerKey, "HmacSHA256");
        this.masterKey = masterKey;
        sml.onReg(masterKey, UDID);
    }

    public void onRegError(Exception e) {
        sml.onRegError(e);
    }

    public void onNeedReg() {
        masterKey = ChatFacade.SID_UNDEF;
        new Register(this, url, developerId, UDID, devModel, devType, clientVersion, authToken, companyId, keyStore).start();
    }

    public void onNeedUpdate() {
        sml.onNeedUpdate();
    }


    public void onMessage(JSONObject jo) {
        sml.onData(jo);
    }

    public synchronized String getCometId() {
        Log.v(TAG, "getCometId: "+ cometId);
        return cometId;
    }

    public synchronized void setCometId(String cometId) {
        Log.v(TAG, "setCometId: "+ cometId);
        this.cometId = cometId;
    }

    public static String hmacDigest(String msg, String keyString, String algo) {
        String digest = null;
        try {
            SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), algo);
            Mac mac = Mac.getInstance(algo);
            mac.init(key);

            byte[] bytes = mac.doFinal(msg.getBytes("ASCII"));

            StringBuilder hash = new StringBuilder();
            for (byte aByte : bytes) {
                String hex = Integer.toHexString(0xFF & aByte);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            digest = hash.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return digest;
    }

    public String getSid() {
        return null;
    }

    public String getUDID() {
        return UDID;
    }

    public synchronized String getToken() {
        return token;
    }

    public String getMasterKey() {
        return masterKey;
    }

    public synchronized void setToken(String token) {
        this.token = token;
    }

    public void startComet() {
        if (getCometId() == null) {
            Log.v(TAG, "comet not running! Started...");
            comet = new Comet(this, url, keyStore);
            setCometId(comet.getCometId());
            comet.start();
        } else {
            Log.v(TAG, "comet already running");
        }
    }

    public boolean checkResp(JSONObject rjo, List<SenderRequest> toSend, SenderRequest request) {
        if (ChatDispatcher.CODE_OK.equalsIgnoreCase(rjo.optString("code"))) return true;
        if (ChatDispatcher.CODE_NEW_CHALLENGE.equalsIgnoreCase(rjo.optString("code"))) {
            if (toSend != null) for (SenderRequest sr : toSend) sender.send(sr);
            if (request != null) sender.send(request);
            String challenge = rjo.optString("challenge");
            String t = ChatDispatcher.hmacDigest(challenge, getMasterKey(), "HmacSHA256");
            Log.v(ChatDispatcher.TAG, "new token: " + t);
            setToken(t);
        } else if (ChatDispatcher.CODE_INVALID_UDID.equalsIgnoreCase(rjo.optString("code"))) {
            if (toSend != null) for (SenderRequest sr : toSend) sender.send(sr);
            if (request != null) sender.send(request);
            onNeedReg();
        } else if (ChatDispatcher.CODE_NEED_UPDATE.equalsIgnoreCase(rjo.optString("code"))) {
            onNeedUpdate();
            end();
        }
        return false;
    }

    public boolean isAlive() {
        return getCometId() != null;
    }
}
