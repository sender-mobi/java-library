package com.sender.library;

import java.io.*;
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
    public static boolean toFile = false;

    public static void v(String tag, String msg) {
        if (toFile) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("/storage/sdcard0/sender.log", true));
                writer.write(new SimpleDateFormat("dd.MM.yy HH.mm.ss.SSS").format(new Date())+" ["+tag+"] "+msg+ "\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (verbose) {
            if (Tool.isAndroid()) {
                System.out.println("["+tag+"] " + msg);
            } else {
                System.out.println(new SimpleDateFormat("dd.MM.yy HH.mm.ss.SSS").format(new Date())+" ["+tag+"] "+msg);
            }
        }
    }
}
