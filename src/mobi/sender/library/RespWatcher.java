package mobi.sender.library;

import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: vp
 * Date: 09.02.15
 * Time: 14:35
 */
public class RespWatcher {
    
    private long sended;
    private String packetId;
    private RespListener listener;
    private JSONObject model;
    private String className;

    public RespWatcher(String className, JSONObject model, long sended, String packetId, RespListener listener) {
        this.sended = sended;
        this.className = className;
        this.model = model;
        this.packetId = packetId;
        this.listener = listener;
    }

    public interface RespListener {
        public void onTimeout();
        public void onError(Exception e);
    }

    public JSONObject getModel() {
        return model;
    }

    public String getClassName() {
        return className;
    }

    public long getSended() {
        return sended;
    }

    public String getPacketId() {
        return packetId;
    }

    public RespListener getListener() {
        return listener;
    }
}
