package mobi.sender.library;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 14.09.14
 * Time: 08:33
 */
public class ChatConnector {

//    public static String url = "http://api-rc.sender.mobi/";
//    public static String url = "http://dev.sender.mobi/";
    public static String url;
    public static final String URL_DEV = "http://dev.sender.mobi/";
    public static final String URL_DEV_LOC = "http://api-dev.sender.loc/";
    public static final String URL_RC = "https://api-rc.sender.mobi/";
    public static final String URL_PROD = "https://api.sender.mobi/";
//    public static final String CODE_OK = "0";
    public static final String CODE_NOT_REGISTERED = "4";
    private CopyOnWriteArrayList<SenderRequest> queue = new CopyOnWriteArrayList<SenderRequest>();
    private boolean alive = false;
    private boolean isReconnectProcess = false;
    private boolean pingMonitoring = false;
    private long lastPingTime;
    private String sid;
    private static final long WAIT_TIMEOUT = 10 * 1000;
    private String TAG;
    private String hash;
    private String devName, devType, imei;
    private HttpURLConnection conn;
    private SenderListener listener;
    public static final String senderChatId = "sender";

    ChatConnector(String url, String sid, String imei, String devName, String devType, int number, SenderListener listener) {
        if (sid == null || sid.trim().length() == 0) sid = "undef";
        this.url = url;
        this.sid = sid;
        this.imei = imei;
        this.devName = devName;
        this.devType = devType;
        this.TAG = "["+number+"]";
        Log.v(TAG, "Start as "+(Log.isAndroid() ? "Android" : "Desktop"));
        this.listener = listener;
        isReconnectProcess = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if ("undef".equals(ChatConnector.this.sid)) {
                        reg();
                    }
                    initStream();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String getUrl() {
        return url;
    }

    public boolean isAlive() {
        return alive;
    }

    public void send(final SenderRequest request) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!alive) {
                        queue.add(request);
                        Log.v(TAG, "request " + request.getRequestURL() + " id=" + request.getId() + " delayed");
                        if (!isReconnectProcess) {
                            Log.v(TAG, "Stream not found, try reconnect...");
                            initStream();
                        }
                        return;
                    }
                    String resp;
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    if (request.getData() != null) {
                        Log.v(TAG, "========> " + request.getRequestURL() + " with binary data " + " (" + request.getId() + ")");
                        HttpPost post = new HttpPost(url + request.getRequestURL());
                        post.setEntity(new ByteArrayEntity(request.getData()));
                        post.addHeader("Content-Type", "image/png");
                        resp = EntityUtils.toString(httpClient.execute(post).getEntity());
                    } else {
                        JSONObject pd = request.getPostData();
                        if (pd == null) {
                            pd = new JSONObject();
                        }
                        pd.put("sid", sid);
                        if ("undef".equals(pd.optString("chatId"))) {
                            pd.put("chatId", senderChatId);
                        }
                        Log.v(TAG, "========> " + request.getRequestURL() + " " + request.getPostData() + " (" + request.getId() + ")");

                        HttpPost post = new HttpPost(url + request.getRequestURL());
                        post.setEntity(new ByteArrayEntity(request.getPostData().toString().getBytes("UTF-8")));
                        resp = EntityUtils.toString(httpClient.execute(post).getEntity());
                    }
                    Log.v(TAG, "<------ " + resp + " (" + request.getId() + ")");
                    request.response(resp);
                } catch (Exception e) {
                    e.printStackTrace();
                    request.error(new Exception(e.getMessage()));
                }
            }
        }).start();
    }

    private void initStream() {
        final String id = UUID.randomUUID().toString().replace("-", "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                stopStream();
                String requestURL = url + "stream?sid=" + sid;
                BufferedReader in = null;
                hash = id;
                Log.v(TAG, "long req: " + requestURL + " (" + hash + ")");
                try {
                    Integer respCode;
                    URL url = new URL(requestURL);
                    if (url.getProtocol().toLowerCase().equals("https")) {
                        conn = (HttpsURLConnection) url.openConnection();
                    } else {
                        conn = (HttpURLConnection) url.openConnection();
                    }
                    conn.setUseCaches(false);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setAllowUserInteraction(false);
                    conn.setConnectTimeout(5 * 1000);
                    conn.setReadTimeout(30 * 60 * 1000);
                    conn.setRequestMethod("GET");
                    conn.connect();
                    new PingWatcher().start();
                    lastPingTime = System.currentTimeMillis();
                    Log.v(TAG, "connected " + hash);
                    respCode = conn.getResponseCode();
                    Log.v(TAG, "HTTP code " + respCode);
                    if (respCode != HttpURLConnection.HTTP_OK) {
                        throw new Exception("http code " + respCode);
                    }
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String nextLine;
                    while ((nextLine = in.readLine()) != null) {
                        alive = true;
                        if (nextLine.trim().length() > 0) {
                            Log.v(TAG, "<======== " + nextLine + " from " + hash);
                        }
                        if (!onData(nextLine, hash)) {
                            Log.v(TAG, "breaking long pool connect: " + hash);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    alive = false;
                    isReconnectProcess = false;
                    if (in != null) try {
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        if (conn != null) conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.v(TAG, "disconnected " + hash);
                }
            }
        }, "stream").start();
    }

    private boolean onData(String data, String hash) {
        try {
            if (data.trim().length() == 0) {
                Log.v(TAG, "[PING] " + hash);
                lastPingTime = System.currentTimeMillis();
                while (queue.size() > 0) {
                    SenderRequest sr = queue.get(0);
                    queue.remove(0);
                    Log.v(TAG, "request " + sr.getRequestURL() + " id=" + sr.getId() + " from queue resuming");
                    send(sr);
                }
                return true;
            }
            try {
                JSONObject jo = new JSONObject(data);
                if (jo.has("packetId")) {
                    sendDeliv(jo.optString("packetId"), sid);
                }
                String code = jo.optString("code");
                if (CODE_NOT_REGISTERED.equals(code)) {
                    reg();
                    return false;
                }
                listener.onData(jo);
            } catch (JSONException e) {
                Log.v(TAG, "data = "+data);
                e.printStackTrace();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getSid() {
        return sid;
    }

    public static void sendDeliv(final String packetId, String sid) {
        try {
            final JSONObject rjo = new JSONObject();
            rjo.put("packetId", packetId);
            rjo.put("sid", sid);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HttpPost post = new HttpPost(url + "deliv");
                    post.setEntity(new ByteArrayEntity(rjo.toString().getBytes()));
                    try {
                        HttpResponse response = new DefaultHttpClient().execute(post);
                        Log.v("[]", "[stream] deliv of msg packetId=" + packetId + " sended" + " response = " + EntityUtils.toString(response.getEntity()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reg() throws Exception {
        String reqUrl = url + "reg";
        JSONObject jo = new JSONObject();
        jo.put("imei", imei);
        jo.put("devType", devType);
        jo.put("devName", devName);
//        jo.put("username", CtConnector.getMyName());
        Log.v(TAG, "======> " + reqUrl + " data: " + jo.toString());
        HttpPost post = new HttpPost(reqUrl);
        post.setEntity(new ByteArrayEntity(jo.toString().getBytes()));
        String rResp = EntityUtils.toString(new DefaultHttpClient().execute(post).getEntity());
        Log.v(TAG, "<------: " + rResp);
        JSONObject rjo = new JSONObject(rResp);
        if (!rjo.has("sid") || !rjo.has("chat_id")) {
            throw new Exception("invalid response: " + rResp);
        }
        sid = rjo.optString("sid");
        listener.onReg(sid);
    }

    private class PingWatcher extends Thread {
        @Override
        public void run() {
            Log.v(TAG, "PingWatcher started");
            try {
                pingMonitoring = true;
                isReconnectProcess = false;
                while (pingMonitoring) {
                    if (System.currentTimeMillis() > WAIT_TIMEOUT + lastPingTime) {
                        Log.v(TAG, "PING LOST! Init reconnect...");
                        alive = false;
                        isReconnectProcess = true;
                        lastPingTime = System.currentTimeMillis();
                        initStream();
                        break;
                    }
                    Thread.sleep(100);
                }
                isReconnectProcess = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.v(TAG, "PingWatcher stopped");
        }
    }

    void stopStream() {
        pingMonitoring = false;
        if (conn != null) {
            Log.v(TAG, "try close old stream " + hash);
            try {
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v(TAG, "stream closed " + hash);
            conn = null;
        }
        alive = false;
    }

    void disconnect() {
        stopStream();
        try {
            while (isAlive()) Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public interface SenderListener {
        public void onReg(String sid);
        public void onData(JSONObject jo);
    }
}
