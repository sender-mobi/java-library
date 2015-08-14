package com.sender.library;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 07.08.15
 * Time: 15:13
 */
public class IP {

    private String ip;
    private long lastErr = -1;

    public IP(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public boolean isAlive() {
        return lastErr < 0 || lastErr + (20 * 1000) < System.currentTimeMillis();
    }

    public void setErr() {
        lastErr = System.currentTimeMillis();
    }
}
