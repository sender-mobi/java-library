package com.sender.library;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 15.02.16
 * Time: 14:01
 */
public class Http {

    public Http() {
        System.setProperty("http.keepAlive", "true");
        System.setProperty("http.maxConnections", "20");
    }

    public String postImg(String url, InputStream is) {
        return request(url, "image/png", null, is, false);
    }

    public String post(String url, String xpHeader, String s) {
        return post(url, xpHeader, s, false);
    }

    public String post(String url, String xpHeader, String s, boolean isComet) {
        ByteArrayInputStream data = new ByteArrayInputStream(s.getBytes());
        return request(url, null, xpHeader, data, isComet);
    }

    public String get(String url, String xpHeader) {
        return request(url, null, xpHeader, null, false);
    }

    public String request(String url, String conntype, String xpHeader, InputStream data, boolean isComet) {
        String rez = null;
        HttpsURLConnection conn = null;

        try {
            URL a = new URL(url);
            conn = (HttpsURLConnection) a.openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(isComet ? 50000 : 10000);
            if (data != null) {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setChunkedStreamingMode(0);
            } else {
                conn.setRequestMethod("GET");
            }
            if (conntype != null) {
                conn.setRequestProperty("Content-type", conntype);
            }
            conn.setRequestProperty("Accept-Encoding", "gzip");
            if (xpHeader != null) {
                conn.setRequestProperty("X-Platform", xpHeader);
            }
//            Log.v(ChatDispatcher.TAG, "~~~ make connection with timeouts: " + conn.getConnectTimeout() + " | " + conn.getReadTimeout());
            if (data != null) {
//                Log.v(ChatDispatcher.TAG, "~~~ try to send post data: " + data.available());
                OutputStream os = conn.getOutputStream();
                byte[] buffer = new byte[512];
                int len;
                while ((len = data.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
                os.close();
//                Log.v(ChatDispatcher.TAG, "~~~ sent!");
            }
//            if (200 != conn.getResponseCode()) throw new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
//            Log.v(ChatDispatcher.TAG, "~~~ resp code: 200");
            InputStream is = conn.getInputStream();
//            Log.v(ChatDispatcher.TAG, "~~~ try to read " + is.available());
            rez = read(is, "gzip".equals(conn.getContentEncoding()));
            is.close();
//            Log.v(ChatDispatcher.TAG, "~~~ received");
        } catch (IOException e) {
//            Log.v(ChatDispatcher.TAG, "~~~ " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    int respCode = conn.getResponseCode();
                    Log.v(ChatDispatcher.TAG, "resp code " + respCode);
                    InputStream es = conn.getErrorStream();
                    rez = read(es, "gzip".equals(conn.getContentEncoding()));
                    if (es != null) es.close();
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            Log.v(ChatDispatcher.TAG, "~~~ end!");
        }
        return rez;
    }

    private String read(InputStream is, boolean isZip) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        BufferedReader reader;
        if (isZip) reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(is)));
        else reader = new BufferedReader(new InputStreamReader(is));
        String s;
        while ((s = reader.readLine()) != null) sb.append(s);
        return sb.toString();
    }
}
