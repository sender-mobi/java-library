package mobi.sender.library;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sender API controller: requests, responses long pooling connect, ping monitoring, etc..  
 */
public class ChatConnector {

    private static enum State {registering, connecting, connected, disconnecting, disconnected}
    private State state;
    private String sid = ChatFacade.SID_UNDEF;
    public static final String CODE_NOT_REGISTERED = "4";
    public static final String CODE_NEED_UPDATE = "8";
    public static final String CODE_CONCURRENT = "5";
    private String url, devId, devModel, devType, clientVersion, authToken, companyId, TAG;
    private ChatFacade.SenderListener sml;
    private HttpURLConnection conn;
    private long lastPingTime;
    private String jsonPart = "";
    private static final CopyOnWriteArrayList<String> currDids = new CopyOnWriteArrayList<String>();
    public static final String senderChatId = "sender";
    private CopyOnWriteArrayList<SenderRequest> queue = new CopyOnWriteArrayList<SenderRequest>();
    private SenderRequest currReq;
    private ExecutorService e = Executors.newCachedThreadPool();
    public ChatConnector(String url,
                         String sid,
                         String devId,
                         String devModel,
                         String devType,
                         String clientVersion,
                         int protocolVersion,
                         int number,
                         String authToken,
                         String companyId,
                         ChatFacade.SenderListener sml) throws Exception {
        this.sid = sid;
        this.url = url + protocolVersion + "/";
        this.devId = devId;
        this.devModel = devModel;
        this.devType = devType;
        this.clientVersion = clientVersion;
        this.authToken = authToken;
        this.companyId = companyId;
        this.sml = sml;
        this.TAG = String.valueOf(number);
        if (ChatFacade.SID_UNDEF.equalsIgnoreCase(sid)) {
            doReg();
        } else {
            if (!currDids.contains(devId)) {
                doConnect();
            } else {
                if (isAlive()) state = State.connected;
                else cutConnection();
            }
        }
    }

    private void doReg() {
        if (state == State.registering) {
            Log.v(TAG, "reg in process...");
            return;
        }
        state = State.registering;
        e.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("reg");
                try {
                    currDids.remove(devId);
                    String key = UUID.randomUUID().toString().replace("-", "");
                    String reqUrl = url + "reg";
                    JSONObject jo = new JSONObject();
                    jo.put("imei", devId);
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
                    Log.v(TAG, "======> " + reqUrl + " data: " + jo.toString() + "(" + key + ")");
                    HttpPost post = new HttpPost(reqUrl);
                    post.setEntity(new ByteArrayEntity(jo.toString().getBytes()));
                    String rResp = EntityUtils.toString(new DefaultHttpClient().execute(post).getEntity());
                    Log.v(TAG, "<------: " + rResp + "(" + key + ")");
                    JSONObject rjo = new JSONObject(rResp);
                    if (!rjo.has("sid")) {
                        throw new Exception("invalid response: " + rResp);
                    }
                    sid = rjo.optString("sid");
                    sml.onReg(sid);
                    if (!currDids.contains(devId)) {
                        doConnect();
                    }
                } catch (Exception e) {
                    state = State.disconnected;
                    e.printStackTrace();
                    sml.onRegError(e);
                }
            }
        });
    }

    public void doConnect() {
        currDids.add(devId);
        state = State.connecting;
        final String id = UUID.randomUUID().toString().replace("-", "");
        e.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("stream" + id);
                BufferedReader in = null;
                Thread pw = null;
                String requestURL = url + "stream?sid=" + sid;
                Log.v(TAG, "long req: " + requestURL + " (" + id + ")");
                try {
                    conn = (HttpsURLConnection) new URL(requestURL).openConnection();
                    conn.setDoInput(true);
                    conn.setConnectTimeout(3 * 1000);
                    conn.setReadTimeout(30 * 60 * 1000);
                    conn.setRequestMethod("GET");
                    conn.connect();
                    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new IOException("invalid http code: " + conn.getResponseCode());
                    }
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    if (state != State.connecting) throw new Exception("invalid state");
                    state = State.connected;
                    pw = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            lastPingTime = System.currentTimeMillis();
                            try {
                                while (System.currentTimeMillis() - lastPingTime < 30 * 1000) {
                                    Thread.sleep(1000);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.v(TAG, "ping lost! " + id);
                            cutConnection();
                        }
                    }, "PingWatcher");
                    pw.start();
                    for (SenderRequest sr : queue) {
                        queue.remove(sr);
                        Log.v(TAG, "request " + sr.getRequestURL() + " id=" + sr.getId() + " resuming from queue");
                        send(sr);
                    }
                    String nextLine;
                    while ((nextLine = in.readLine()) != null && state == State.connected) {
                        if (nextLine.trim().length() > 0) {
                            Log.v(TAG, "<======== " + nextLine + " from " + id);
                            if (!doMessage(nextLine)) {
                                Log.v(TAG, "breaking long pool connect: " + id);
                                break;
                            }
                        } else {
                            lastPingTime = System.currentTimeMillis();
                            Log.v(TAG, "PING " + id);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (e instanceof UnknownHostException) {
                        state = State.disconnecting;
                    }
                } finally {
                    currDids.remove(devId);
                    try {
                        if (in != null) {
                            in.close();
                        }
                        if (conn != null) {
                            conn.disconnect();
                            conn = null;
                        }
                        if (pw != null) {
                            pw.interrupt();
                            pw.join(1000);
                        }
                        Log.v(TAG, "disconnected " + id);
                        if (state == State.connected) {
                            if (!currDids.contains(devId)) {
                                doConnect();
                            }
                        } else if (state == State.connecting) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (state == State.connecting) {
                                if (!currDids.contains(devId)) {
                                    doConnect();
                                }
                            }
                        } else if (state == State.disconnecting) {
                            state = State.disconnected;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    
    public void doDisconnect() {
        cutConnection();
    }
    
    private boolean doMessage(String data) {
        if (jsonPart.length() > 0) {
            data = jsonPart + data;
            jsonPart = "";
        }
        try {
            JSONObject jo = new JSONObject(data);
            if (jo.has("packetId")) {
                sendDeliv(url, jo.optString("packetId"), sid);
            }
            String code = jo.optString("code");
            if (CODE_NOT_REGISTERED.equals(code)) {
                Log.v(TAG, "invalid sid");
                doReg();
                return false;
            }
            if (CODE_NEED_UPDATE.equals(code)) {
                Log.v(TAG, "need update");
                sml.onNeedUpdate();
                doDisconnect();
                return false;
            }
            if (CODE_CONCURRENT.equals(code)) {
                Log.v(TAG, "concurrent!");
                doDisconnect();
                return false;
            }
            sml.onData(jo);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            jsonPart = data;
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public void send(final SenderRequest request) {
        e.execute(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("Send");
                currReq = request;
                Log.v(TAG, "while sendidng req id " + request.getId() + " state = " + state);
                try {
                    if (state != State.connected) {
                        queue.add(request);
                        Log.v(TAG, "request " + request.getRequestURL() + " id=" + request.getId() + " delayed");
                        if (state == State.disconnected) {
                            if (!currDids.contains(devId)) {
                                doConnect();
                            } else {
                                cutConnection();
                            }
                        }
                    } else {
                        String resp;
                        DefaultHttpClient httpClient = new DefaultHttpClient();
                        if (request.getData() != null && request.getData().available() > 0) {                        // -------------------- binary post
                            Log.v(TAG, "========> " + request.getRequestURL() + " with binary data " + " (" + request.getId() + ")");
                            URL purl = new URL(url + request.getRequestURL());
                            HttpURLConnection con;
                            if (purl.getProtocol().toLowerCase().equals("https")) {
                                con = (HttpsURLConnection) purl.openConnection();
                            } else {
                                con = (HttpURLConnection) purl.openConnection();
                            }
                            con.setUseCaches(false);
                            con.setDoOutput(true);
                            con.setDoInput(true);
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Content-type", "image/png");
                            con.connect();
                            int bufferSize = 1024;
                            byte[] buffer = new byte[bufferSize];
                            int len;
                            OutputStream out = con.getOutputStream();
                            while ((len = request.getData().read(buffer)) != -1) {
                                out.write(buffer, 0, len);
                            }
                            out.flush();
                            out.close();
                            request.getData().close();
                            StringBuilder sb = new StringBuilder();
                            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                            String nextLine;
                            while ((nextLine = in.readLine()) != null) {
                                sb.append(nextLine);
                            }
                            in.close();
                            con.disconnect();
                            resp = sb.toString();
                        } else if (request.getPostData() != null) {             // -------------------- post
                            request.getPostData().put("sid", sid);
                            if ("undef".equals(request.getPostData().optString("chatId"))) {
                                request.getPostData().put("chatId", senderChatId);
                            }
                            Log.v(TAG, "========> " + request.getRequestURL() + " " + request.getPostData() + " (" + request.getId() + ")");
                            HttpPost post = new HttpPost(url + request.getRequestURL());
                            post.setEntity(new ByteArrayEntity(request.getPostData().toString().getBytes("UTF-8")));
                            resp = EntityUtils.toString(httpClient.execute(post).getEntity());
                        } else {                                                // -------------------- get
                            Log.v(TAG, "========> " + request.getRequestURL() + " (" + request.getId() + ")");
                            HttpGet get = new HttpGet(url + request.getRequestURL());
                            resp = EntityUtils.toString(httpClient.execute(get).getEntity());
                        }
                        Log.v(TAG, "<------ " + resp + " (" + request.getId() + ")");
                        try {
                            JSONObject jo = new JSONObject(resp);
                            if (CODE_NOT_REGISTERED.equalsIgnoreCase(jo.optString("code"))) {
                                queue.add(request);
                                doReg();
                                return;
                            }
                            if (CODE_NEED_UPDATE.equalsIgnoreCase(jo.optString("code"))) {
                                sml.onNeedUpdate();
                                doDisconnect();
                                return;
                            }
                            request.response(jo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            request.error(e);
                        }
                    }
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    request.error(new Exception("file is too large"));
                } catch (Exception e) {
                    e.printStackTrace();
                    request.error(new Exception(e.getMessage()));
                }
                currReq = null;
            }
        });
    }

    public void sendSync(final String action, final JSONObject data, final SenderRequest.HttpDataListener srl) {
        try {
            if (ChatFacade.SID_UNDEF.equalsIgnoreCase(sid)) {
                Log.v(TAG, "not have sid: try reg...");
                doReg();
            }
            try {
                int counter = 0;
                while (ChatFacade.SID_UNDEF.equalsIgnoreCase(sid) && counter < 300) {
                    counter++;
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (ChatFacade.SID_UNDEF.equalsIgnoreCase(sid)) {
                Log.v(TAG, "not have sid: request cancelled");
                throw new Exception("Error connection to server");
            }
            data.put("sid", sid);
            e.execute(new Runnable() {
                @Override
                public void run() {
                    Thread.currentThread().setName("sendSync");
                    HttpPost post = new HttpPost(url + action);
                    post.setEntity(new ByteArrayEntity(data.toString().getBytes()));
                    String id = UUID.randomUUID().toString().replace("-", "");
                    try {
                        Log.v(TAG, "~~~~~~> " + url + action + " " + data.toString() + " (" + id + ")");
                        DefaultHttpClient client = new DefaultHttpClient();
                        HttpResponse response = client.execute(post);
                        if (200 != response.getStatusLine().getStatusCode()) {
                            throw new Exception("http code " + response.getStatusLine().getStatusCode());
                        }
                        String s = EntityUtils.toString(response.getEntity());
                        Log.v(TAG, "<~~~~~~" + s + " (" + id + ")");
                        JSONObject jo = new JSONObject(s);
                        if (0 != jo.optInt("code")) {
                            throw new Exception("code " + jo.optInt("code") + " reason:" + jo.optString("reason"));
                        }
                        srl.onResponse(new JSONObject(s));
                    } catch (Exception e) {
                        Log.v(TAG, "!~~~~~~" + e.getMessage() + " (" + id + ")");
                        srl.onError(e);
                    }
                }
            });
        } catch (Exception e) {
            srl.onError(e);
        }
    }

    public static void sendDeliv(final String url, final String packetId, String sid) {
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

    public void cancelSend() {
        if (currReq != null) {
            currReq.error(new Exception("cancelled"));
        }
    }
    
    public String getTAG() {
        return TAG;
    }

    public String getSid() {
        return sid;
    }

    private void cutConnection() {
        state = State.disconnecting;
        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception ignored) {}
            conn = null;
        }
        currDids.remove(devId);
        state = State.disconnected;
    }

    public boolean isAlive() {
        return state == State.connected;
    }

}
