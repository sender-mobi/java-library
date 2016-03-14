package com.sender.library;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 30.07.14
 * Time: 19:11
 */
public class SenderRequest {
    private String requestURL;
    private JSONObject postData;
    private HttpDataListener hdl;
    private InputStream data;
    private String id = UUID.randomUUID().toString().replace("-", "");

    public SenderRequest(String requestURL, JSONObject postData, HttpDataListener hdl) {
        this.requestURL = requestURL;
        this.postData = postData;
        this.hdl = hdl;
    }

    public SenderRequest(String requestURL, JSONObject postData, String id, HttpDataListener hdl) {
        this.requestURL = requestURL;
        this.postData = postData;
        this.id = id;
        this.hdl = hdl;
    }

    public SenderRequest(String requestURL, InputStream postData, HttpDataListener hdl) {
        this.requestURL = requestURL;
        this.data = postData;
        this.hdl = hdl;
    }

    public String getRequestURL() {
        return requestURL;
    }

    public JSONObject getPostData() {
        return postData;
    }

    public HttpDataListener getHdl() {
        return hdl;
    }

    public String getId() {
        return String.valueOf(id);
    }

    public SenderRequest(String requestURL, HttpDataListener hdl) {
        this.requestURL = requestURL;
        this.hdl = hdl;
    }

    public SenderRequest(String requestURL) {
        this.requestURL = requestURL;
    }

    public SenderRequest(String requestURL, JSONObject postData) {
        this.requestURL = requestURL;
        this.postData = postData;
    }

    public void response(JSONObject jo) {
        if (hdl!=null) hdl.onResponse(jo);
    }

    public void error(Exception e) {
        if (hdl!=null) hdl.onError(e);
        else e.printStackTrace();
    }

    public InputStream getData() {
        return data;
    }

    public interface HttpDataListener {
        public void onResponse(JSONObject data);
        public void onError(Exception e);
    }

    @Override
    public String toString() {
        return "SenderRequest{" +
                "requestURL='" + requestURL + '\'' +
                ", postData=" + postData +
                ", data=" + data +
                ", id='" + id + '\'' +
                '}';
    }
}
