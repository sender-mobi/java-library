package mobi.sender.library;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 03.11.14
 * Time: 11:26
 */
public class Log {

    public static final boolean isLogVerbose = true;

    public static void v(String tag, String msg) {
        if (isLogVerbose) System.out.println(new SimpleDateFormat("dd.MM.yy HH.mm.ss.SSS").format(new Date())+" ["+tag+"] "+msg);
    }

    public static void n(String tag, String msg) {
        System.out.println(new SimpleDateFormat("dd.MM.yy HH.mm.ss.SSS").format(new Date())+" ["+tag+"] "+msg);
    }
}
