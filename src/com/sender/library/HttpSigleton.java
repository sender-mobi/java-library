package com.sender.library;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;

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

    public static HttpClient getSenderInstance() {
        if(senderClient == null) {
            senderClient = new DefaultHttpClient();
            senderClient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
            senderClient.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, 5000);

            senderClient.setKeepAliveStrategy(
                    new ConnectionKeepAliveStrategy() {
                        @Override
                        public long getKeepAliveDuration(
                                HttpResponse response, HttpContext context) {
                            return 180000;
                        }
                    });
        }
        return senderClient;
    }

    public static HttpClient getCometInstance() {
        if(cometClient == null) {
            cometClient = new DefaultHttpClient();
            cometClient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
            cometClient.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, 60000);
            cometClient.setKeepAliveStrategy(
                    new ConnectionKeepAliveStrategy() {
                        @Override
                        public long getKeepAliveDuration(
                                HttpResponse response, HttpContext context) {
                            return 180000;
                        }
                    });
        }
        return cometClient;
    }

    public static HttpClient getSyncClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, 5000);
        client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, 60000);
        client.setKeepAliveStrategy(
                new ConnectionKeepAliveStrategy() {
                    @Override
                    public long getKeepAliveDuration(
                            HttpResponse response, HttpContext context) {
                        return 180000;
                    }
                });
        return client;
    }

    public static void invalidateCometInstance() {
        if (cometClient != null) {
            cometClient.getConnectionManager().shutdown();
            cometClient = null;
        }
    }
}