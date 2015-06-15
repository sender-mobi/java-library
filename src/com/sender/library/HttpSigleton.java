package com.sender.library;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

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

    public static HttpClient getSenderInstance(KeyStore keyStore) {
        if(senderClient == null) {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 20000);
            senderClient = (keyStore == null) ? new DefaultHttpClient(httpParameters) : new SHttpClient(httpParameters, keyStore);

            senderClient.setKeepAliveStrategy(
                    new ConnectionKeepAliveStrategy() {
                        @Override
                        public long getKeepAliveDuration(
                                HttpResponse response, HttpContext context) {
                            return 60000;
                        }
                    });
        }
        return senderClient;
    }

    public static HttpClient getCometInstance(KeyStore keyStore) {
        if(cometClient == null) {
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 20000);
            cometClient = (keyStore == null) ? new DefaultHttpClient(httpParameters) : new SHttpClient(httpParameters, keyStore);
            cometClient.setKeepAliveStrategy(
                    new ConnectionKeepAliveStrategy() {
                        @Override
                        public long getKeepAliveDuration(
                                HttpResponse response, HttpContext context) {
                            return 60000;
                        }
                    });
        }
        return cometClient;
    }

    public static HttpClient getSyncClient(KeyStore keyStore) {
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
        HttpConnectionParams.setSoTimeout(httpParameters, 60000);
        DefaultHttpClient client = (keyStore == null) ? new DefaultHttpClient(httpParameters) : new SHttpClient(httpParameters, keyStore);
        client.setKeepAliveStrategy(
                new ConnectionKeepAliveStrategy() {
                    @Override
                    public long getKeepAliveDuration(
                            HttpResponse response, HttpContext context) {
                        return 60000;
                    }
                });
        return client;
    }

    public static void invalidateCometInstance() {
        if (cometClient != null) {
//            cometClient.getConnectionManager().shutdown();
            cometClient = null;
        }
    }
}