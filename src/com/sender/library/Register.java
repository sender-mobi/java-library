package com.sender.library;

import android.os.Build;
import org.json.JSONObject;

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
    private String UDID, devModel, devType, clientVersion, authToken, developerId;

    public Register(ChatDispatcher disp, String developerId, String UDID, String devModel, String devType, String clientVersion, String authToken) {
        super("Register");
        this.disp = disp;
        this.developerId = developerId;
        this.UDID = UDID;
        this.devModel = devModel;
        this.devType = devType;
        this.clientVersion = clientVersion;
        this.authToken = authToken;
    }

    @Override
    public void run() {
        try {
            String key = UUID.randomUUID().toString().replace("-", "");
            String reqUrl = disp.getUrl() + "reg";
            JSONObject jo = new JSONObject();
            jo.put("developerId", developerId);
            jo.put("udid", UDID);
            jo.put("devType", devType);
            jo.put("language", Locale.getDefault().getLanguage());
            jo.put("devModel", devModel);
            jo.put("devName", devModel);
            jo.put("clientVersion", clientVersion);
            jo.put("devOS", Tool.isAndroid() ? "android" : System.getProperty("os.name").toUpperCase().contains("MAC") ? "mac" : "linux");
            jo.put("clientType", Tool.isAndroid() ? "android" : "desktop");
            jo.put("versionOS", Tool.isAndroid() ? Build.VERSION.RELEASE : System.getProperty("os.version"));
            jo.put("authToken", authToken);
            jo.put("companyId", disp.getCompanyId());
            Log.v(this.getClass().getSimpleName(), "======> " + reqUrl + " data: " + jo.toString() + "(" + key + ")");
            String rResp = new Http().post(reqUrl, disp.getCompanyId(), jo.toString());// Tool.getData(HttpSigleton.getSyncClient(reqUrl, keyStore, 10000).execute(post));
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
