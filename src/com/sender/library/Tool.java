package com.sender.library;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

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

    public static String getData(HttpResponse resp) throws IOException {
        int status = resp.getStatusLine().getStatusCode();
        if (HttpStatus.SC_OK == status) {
            return EntityUtils.toString(resp.getEntity());
        } else {
            EntityUtils.consume(resp.getEntity());
            throw new IOException("invalid status code " + status);
        }
    }

}
