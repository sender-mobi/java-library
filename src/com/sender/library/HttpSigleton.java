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

import java.io.IOException;
import java.security.KeyStore;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 25.05.15
 * Time: 11:32
 */
public class HttpSigleton {

    private static DefaultHttpClient senderClient = null;
    private static DefaultHttpClient cometClient = null;

    protected HttpSigleton() {}

    public static HttpClient getSenderInstance(String addr, KeyStore keyStore) {
        if(senderClient == null) {
            senderClient = getClient(addr, keyStore, true, 10000, 10000);
        }
        return senderClient;
    }

    public static HttpClient getCometInstance(String addr, KeyStore keyStore) {
        if(cometClient == null) {
            cometClient = getClient(addr, keyStore, true, 20000, 60000);
        }

        return cometClient;
    }

    public static HttpClient getSyncClient(String addr, KeyStore keyStore, int readTimeout) {
        return getClient(addr, keyStore, false, 10000, readTimeout);
    }

    public static void invalidateCometInstance() {
        if (cometClient != null) {
            cometClient.getConnectionManager().shutdown();
            cometClient = null;
        }
    }

    private static DefaultHttpClient getClient(String addr, KeyStore keyStore, boolean isKeepAlive, int connectTimeouat, int readTimeout) {
        DefaultHttpClient client = null;
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