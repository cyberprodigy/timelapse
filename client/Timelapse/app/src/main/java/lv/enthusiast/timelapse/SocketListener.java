package lv.enthusiast.timelapse;

/**
 * Created by mitnick on 4/19/14.
 */
public interface SocketListener {
    void onMessageReceived(String msg);
    void onSocketListenerError(String error);
    void onSocketListenerInfo(String message);
}
