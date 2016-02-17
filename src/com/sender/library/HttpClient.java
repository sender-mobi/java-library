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
public class HttpClient {

    private static HttpClient instance;
    private InputStream is, es;
    private OutputStream os;

    private HttpClient() {
        System.setProperty("http.keepAlive", "true");
        System.setProperty("http.maxConnections", "10");
    }

    public static HttpClient getInstance() {
        if (instance == null) instance = new HttpClient();
        return instance;
    }

    public String postImg(String url, InputStream is) {
        return request(url, "image/png", null, is);
    }

    public String post(String url, String xpHeader, String s) {
        ByteArrayInputStream data = new ByteArrayInputStream(s.getBytes());
        return request(url, null, xpHeader, data);
    }

    public String get(String url, String xpHeader) {
        return request(url, null, xpHeader, null);
    }

    public String request(String url, String conntype, String xpHeader, InputStream data) {
        String rez = null;
        HttpsURLConnection conn = null;
        try {
            URL a = new URL(url);
            conn = (HttpsURLConnection) a.openConnection();
            if (data != null) {
                conn.setRequestMethod("POST");
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
            conn.connect();
            if (data != null) {
                os = conn.getOutputStream();
                byte[] buffer = new byte[512];
                int len;
                while ((len = data.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
                os.close();
            }
            if (200 != conn.getResponseCode()) throw new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
            is = conn.getInputStream();
            rez = read(is, "gzip".equals(conn.getContentEncoding()));
            is.close();
        } catch (IOException e) {
//            e.printStackTrace();
            try {
                if (conn != null) {
                    int respCode = conn.getResponseCode();
                    System.out.println("resp code " + respCode);
                    es = conn.getErrorStream();
                    rez = read(es, "gzip".equals(conn.getContentEncoding()));
                    if (es != null) es.close();
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
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
