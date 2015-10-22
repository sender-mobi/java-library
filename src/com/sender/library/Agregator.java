package com.sender.library;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 18.02.15
 * Time: 08:40
 */
public class Agregator {

    private BlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(20);
    private Timer timer;
    private boolean isRunning = false;
    private static Agregator instance;
    private AgListener al;
    private int timeoutSec;

    private Agregator(AgListener al, int timeoutSec) {
        this.al = al;
        this.timeoutSec = timeoutSec;
    }

    public static Agregator getInstance(AgListener al, int timeoutSec) {
        if (instance == null) instance = new Agregator(al, timeoutSec);
        return instance;
    }

    public void add(JSONObject jo) {
            queue.add(jo);
        if (!isRunning) {
            isRunning = true;
            timer = new Timer();
            timer.schedule(new Packer(), 0, timeoutSec * 1000);
        }
    }

    private class Packer extends TimerTask {
        @Override
        public void run() {
            try {
                JSONArray arr = new JSONArray();
                while (queue.size() > 0) {
                    arr.put(queue.take());
                }
                if (arr.length() > 0) {
                    al.onPack(arr);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface AgListener {
        void onPack(JSONArray arr);
    }

    public void stop() {
        if (isRunning) {
            Log.v(ChatDispatcher.TAG, "stopped!");
            timer.cancel();
            isRunning = false;
        }
    }
}
