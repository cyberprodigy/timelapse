package lv.enthusiast.timelapse;

import android.os.StrictMode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * Created by mitnick on 4/19/14.
 */
public class ServerConnection {
    private SocketListener _listener;
    private DataInputStream _inputStream;
    private DataOutputStream _outputStream;


    public ServerConnection(SocketListener listener) {
        _listener=listener;
    }
    private Socket _socket;

    public void connect(String ip, int port) {
        if(_socket == null) {
            try {
                _listener.onSocketListenerInfo("Trying to connect to " + ip);
                if (android.os.Build.VERSION.SDK_INT > 9) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                }

                _socket = new Socket(ip, 5000);
                _socket.setTcpNoDelay(true);


                InputStream inFromServer = _socket.getInputStream();
                _inputStream = new DataInputStream(inFromServer);

                OutputStream outToServer = _socket.getOutputStream();
                _outputStream = new DataOutputStream(outToServer);

                _listener.onSocketListenerInfo("Waiting for server to respond");
                while (_inputStream.available()>0) {
                    _listener.onSocketListenerInfo("Server responded");
                    byte[] b = new byte[_inputStream.available()];
                    _inputStream.readFully(b);
                        _listener.onMessageReceived(new String(b, StandardCharsets.US_ASCII));

                }
            }
            catch (UnknownHostException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            _listener.onSocketListenerError("Connect was called before, aborting");
        }

    }

    public void sendMessage(String msg) {
        try {
            _outputStream.writeBytes(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            _socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        _socket = null;
    }
}
