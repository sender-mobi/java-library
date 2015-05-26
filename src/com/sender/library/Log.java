package com.sender.library;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 03.11.14
 * Time: 11:26
 */
public class Log {

    public static boolean verbose = false;

    public static void v(String tag, String msg) {
        if (verbose) {
            if (Tool.isAndroid()) {
                System.out.println("["+tag+"] " + msg);
            } else {
                System.out.println(new SimpleDateFormat("dd.MM.yy HH.mm.ss.SSS").format(new Date())+" ["+tag+"] "+msg);
            }
        }
    }
}
