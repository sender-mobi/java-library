package com.sender.library;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import java.security.KeyStore;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 26.05.15
 * Time: 13:29
 */
public class SHttpClient extends DefaultHttpClient {

    private KeyStore keyStore;

    public SHttpClient(HttpParams params, KeyStore keyStore) {
        super(params);
        Log.v(ChatDispatcher.TAG, "make SHttpClient with keystore " + keyStore);
        this.keyStore = keyStore;
    }

    @Override
    protected ClientConnectionManager createClientConnectionManager() {
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", getSslSocketFactory(), 443));
        return new ThreadSafeClientConnManager(getParams(), registry);
    }

    private SSLSocketFactory getSslSocketFactory() {
        try {
            SSLSocketFactory sf = new SSLSocketFactory(keyStore);
            sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
            return sf;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
