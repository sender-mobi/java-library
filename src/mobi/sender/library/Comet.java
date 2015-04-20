package mobi.sender.library;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
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
    private String url;
    private String lastSrvBatchId;
    private String id = UUID.randomUUID().toString().replace("-", "");

    public Comet(ChatDispatcher disp, String url) {
        this.disp = disp;
        this.url = url;
    }

    public String getCometId() {
        return id;
    }

    @Override
    public void run() {
        Log.v(ChatDispatcher.TAG, "comet started id = " + id);
        try {
            while (disp.getCometId() != null) {
                if (!id.equalsIgnoreCase(disp.getCometId())) {
                    Log.v(ChatDispatcher.TAG, "duplicate comet: my id = " + id + " active = " + disp.getCometId());
                    return;
                }
                while (ChatFacade.SID_UNDEF.equalsIgnoreCase(disp.getMasterKey())) {
                    Log.v(ChatDispatcher.TAG, "need reg... wait comet");
                    sleep(1000);
                }
                Log.v(ChatDispatcher.TAG, "step comet... id: " + id);
                JSONObject jo = new JSONObject();
                jo.put("lbi", lastSrvBatchId);
                jo.put("meta", new JSONObject());
                String fullUrl = url + "comet?udid=" + disp.getUDID() + "&token=" + disp.getToken();
                HttpPost post = new HttpPost(fullUrl);
                post.setEntity(new ByteArrayEntity(jo.toString().getBytes()));
                Log.v(ChatDispatcher.TAG, "========> " + fullUrl + " " + jo.toString() + " (" + id + ")");
                String response = EntityUtils.toString(new DefaultHttpClient().execute(post).getEntity());
                Log.v(ChatDispatcher.TAG, "<======== " + response + " (" + id + ")");
                JSONObject rjo = new JSONObject(response);
                if (disp.checkResp(rjo, null, null)) {
                    if (rjo.has("bi")) {
                        lastSrvBatchId = rjo.optString("bi");
                    }
                    if (rjo.has("fs")) {
                        JSONArray fs = rjo.optJSONArray("fs");
                        for (int i = 0; i < fs.length(); i++) {
                            JSONObject fsj = fs.optJSONObject(i);
                            disp.onMessage(fsj);
                        }
                    }
                }
                sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        disp.setCometId(null);
        Log.v(ChatDispatcher.TAG, "comet ending id = " + id);
    }
}
