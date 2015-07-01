package com.sender.library;

import android.os.Build;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.json.JSONObject;

import java.security.KeyStore;
import java.util.Locale;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 18.02.15
 * Time: 08:40
 */
public class Register extends Thread{

    private ChatDispatcher disp;
    private KeyStore keyStore;
    private String url, UDID, devModel, devType, clientVersion, authToken, companyId, developerId;
    public static Register instance;

    public static Register getInstance(ChatDispatcher disp, String url, String developerId, String UDID, String devModel, String devType, String clientVersion, String authToken, String companyId, KeyStore keyStore) {
        if (instance == null) {
            instance = new Register(disp, url, developerId, UDID, devModel, devType, clientVersion, authToken, companyId, keyStore);
        }
        return instance;
    }

    private Register(ChatDispatcher disp, String url, String developerId, String UDID, String devModel, String devType, String clientVersion, String authToken, String companyId, KeyStore keyStore) {
        super("Register");
        this.disp = disp;
        this.developerId = developerId;
        this.url = url;
        this.UDID = UDID;
        this.devModel = devModel;
        this.devType = devType;
        this.clientVersion = clientVersion;
        this.authToken = authToken;
        this.companyId = companyId;
        this.keyStore = keyStore;
    }

    @Override
    public void run() {
        try {
            String key = UUID.randomUUID().toString().replace("-", "");
            String reqUrl = url + "reg";
            JSONObject jo = new JSONObject();
            jo.put("developerId", developerId);
            jo.put("udid", UDID);
            jo.put("devType", devType);
            jo.put("language", Locale.getDefault().getLanguage());
            jo.put("devModel", devModel);
            jo.put("devName", devModel);
            jo.put("clientVersion", clientVersion);
            jo.put("devOS", Tool.isAndroid() ? "android" : System.getProperty("os.name"));
            jo.put("clientType", Tool.isAndroid() ? "android" : System.getProperty("os.name"));
            jo.put("versionOS", Tool.isAndroid() ? Build.VERSION.RELEASE : System.getProperty("os.version"));
            jo.put("authToken", authToken);
            jo.put("companyId", companyId);
            Log.v(this.getClass().getSimpleName(), "======> " + reqUrl + " data: " + jo.toString() + "(" + key + ")");
            HttpPost post = new HttpPost(reqUrl);
            post.setEntity(new ByteArrayEntity(jo.toString().getBytes()));
            String rResp = Tool.getData(HttpSigleton.getSenderInstance(keyStore).execute(post));
            Log.v(this.getClass().getSimpleName(), "<======= " + rResp + "(" + key + ")");
            JSONObject rjo = new JSONObject(rResp);
            if (!rjo.has("deviceKey")) {
                throw new Exception("invalid response: " + rResp);
            }
            String deviceKey = rjo.optString("deviceKey");
            boolean fullVer = true;
            if (rjo.has("mode")) {
                if ("restricted".equalsIgnoreCase(rjo.optString("mode"))) fullVer = false;
            }
            disp.onRegOk(deviceKey, fullVer);
        } catch (Exception e) {
            e.printStackTrace();
            disp.onRegError(e);
        }
    }
}
