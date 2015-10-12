package com.sender.library;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 18.05.15
 * Time: 12:05
 */
public class Tool {

    public static boolean isAndroid() {
        try {
            Class.forName("android.app.Activity");
            return true;
        } catch(ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isAddrIsIP(String addr) throws UnknownHostException {
        addr = addr.replace("https://", "");
        addr = addr.replace("http://", "");
        addr = addr.substring(0, addr.indexOf("/"));
        InetAddress ia = InetAddress.getByName(addr);
        return addr.equalsIgnoreCase(ia.getHostAddress());
    }

    public static String getData(HttpResponse resp) throws IOException {
        int status = resp.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK == status) {
            return EntityUtils.toString(resp.getEntity());
        } else {
            resp.getEntity().consumeContent();
            throw new IOException("invalid status code " + status);
        }
    }

    public static boolean checkServer(String url) throws IOException {
        return 200 == new DefaultHttpClient().execute(new HttpGet(url + "ping")).getStatusLine().getStatusCode();
    }
}
