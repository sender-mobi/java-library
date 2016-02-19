package com.sender.library;

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

    public static boolean checkServer(String url) throws IOException {
        return "ok".equalsIgnoreCase(new Http().get(url + "ping", null));
    }
}
