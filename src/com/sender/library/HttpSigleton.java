package com.sender.library;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 25.05.15
 * Time: 11:32
 */
public class HttpSigleton {

    private static DefaultHttpClient senderClient = null;
    private static DefaultHttpClient cometClient = null;

    protected HttpSigleton() {
    }

    public static HttpClient getSenderInstance(String addr, KeyStore keyStore) throws UnknownHostException {
        if (senderClient == null) {
            senderClient = getClient(addr, keyStore, true, 10000, 10000);
        }
        return senderClient;
    }

    public static HttpClient getCometInstance(String addr, KeyStore keyStore) throws UnknownHostException {
        if (cometClient == null) {
            cometClient = getClient(addr, keyStore, true, 10000, 60000);
        }

        return cometClient;
    }

    public static HttpClient getSyncClient(String addr, KeyStore keyStore, int readTimeout) throws UnknownHostException {
        return getClient(addr, keyStore, false, 10000, readTimeout);
    }

    public static void invalidateCometInstance() {
        if (cometClient != null) {
            cometClient.getConnectionManager().shutdown();
            cometClient = null;
        }
    }

    private static DefaultHttpClient getClient(String addr, KeyStore keyStore, boolean isKeepAlive, int connectTimeouat, int readTimeout) throws UnknownHostException {
        DefaultHttpClient client = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, null, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, connectTimeouat);
        HttpConnectionParams.setSoTimeout(httpParameters, readTimeout);
        client = (Tool.isAddrIsIP(addr)) ? new SHttpClient(httpParameters, keyStore) : new DefaultHttpClient(httpParameters);
        if (isKeepAlive) {
            client.setKeepAliveStrategy(
                    new ConnectionKeepAliveStrategy() {
                        @Override
                        public long getKeepAliveDuration(
                                HttpResponse response, HttpContext context) {
                            return 60000;
                        }
                    });

        }
        client.addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(final HttpResponse response, final HttpContext context) throws HttpException, IOException {
                HttpEntity entity = response.getEntity();
                Header encheader = entity.getContentEncoding();
                if (encheader != null) {
                    HeaderElement[] codecs = encheader.getElements();
                    for (int i = 0; i < codecs.length; i++) {
                        if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                            Log.v(ChatDispatcher.TAG, " === gzip detected!");
                            response.setEntity(new GzipDecompressingEntity(entity));
                            return;
                        }
                    }
                }
            }
        });
        return client;
    }
}