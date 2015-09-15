package com.sender.library;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 03.08.15
 * Time: 16:06
 */
public class UDP {

    private static final String ip = "api-pre-udp.sender.mobi";
    private static final int port = 15616;

    public static void send(final String req, final int timeout, final SendListener sl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket clientSocket = null;
                Thread waiter = null;
                try {
                    clientSocket = new DatagramSocket();
                    byte[] sendData = req.getBytes();
                    Log.v(ChatDispatcher.TAG, "----> " + req + " to " + ip + ":" + port);
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ip), port);
                    clientSocket.send(sendPacket);
                    if (timeout > 0 && sl != null) {
                        waiter = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(timeout);
                                    Log.v(ChatDispatcher.TAG, "<---- timeout :(");
                                    sl.onTimeout();
                                } catch (InterruptedException ignored) {}
                            }
                        });
                        waiter.start();
                    }
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    clientSocket.receive(receivePacket);
                    if (waiter != null) waiter.interrupt();
                    String resp = new String(receivePacket.getData());
                    Log.v(ChatDispatcher.TAG, "<---- " + (resp.contains("}") ? resp.substring(0, resp.lastIndexOf("}") + 1) : resp));
                    if (sl != null) sl.onSuccess(resp);
                } catch (Exception e) {
                    Log.v(ChatDispatcher.TAG, "<---- error: " + e.getMessage());
                    if (sl != null) sl.onError(e);
                    else e.printStackTrace();
                } finally {
                    if (clientSocket != null) clientSocket.close();
                }
            }
        }).start();
    }

    public interface SendListener {
        void onTimeout();
        void onError(Exception e);
        void onSuccess(String resp);
    }
}
