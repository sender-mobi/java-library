package mobi.sender.library;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
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
    private String url, UDID, devModel, devType, clientVersion, authToken, companyId, developerId;


    public Register(ChatDispatcher disp, String url, String developerId, String UDID, String devModel, String devType, String clientVersion, String authToken, String companyId) {
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
            jo.put("devOS", Log.isAndroid() ? "android" : System.getProperty("os.name"));
            jo.put("clientType", Log.isAndroid() ? "android" : System.getProperty("os.name"));
            jo.put("versionOS", System.getProperty("os.version"));
            jo.put("authToken", authToken);
            jo.put("companyId", companyId);
            Log.v(this.getClass().getSimpleName(), "======> " + reqUrl + " data: " + jo.toString() + "(" + key + ")");
            HttpPost post = new HttpPost(reqUrl);
            post.setEntity(new ByteArrayEntity(jo.toString().getBytes()));
            DefaultHttpClient client = new DefaultHttpClient();
            client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
            client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, 10000);
            String rResp = EntityUtils.toString(client.execute(post).getEntity());
            Log.v(this.getClass().getSimpleName(), "<======= " + rResp + "(" + key + ")");
            JSONObject rjo = new JSONObject(rResp);
            if (!rjo.has("deviceKey")) {
                throw new Exception("invalid response: " + rResp);
            }
            String deviceKey = rjo.optString("deviceKey");
            disp.onRegOk(deviceKey);
        } catch (Exception e) {
            e.printStackTrace();
            disp.onRegError(e);
        }
    }

}
