package lv.enthusiast.timelapse;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SocketListener, CameraControllerListener {

    private ServerConnection _connection;
    private CameraController _cameraController;
    private Thread _currentCaptureThread;
    private Timer timer;
    private FunctionThresholdCalc _ftcalc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        _ftcalc = new FunctionThresholdCalc();
        timer = new Timer();

        _cameraController = new CameraController(this, (FrameLayout) findViewById(R.id.preview_frm), this);

        addUIEventHandlers();
    }

    private void addUIEventHandlers() {
        Button connect_btn = (Button) findViewById(R.id.connectServer_btn);
        connect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToServer();
            }
        });

        Button turnRight_btn = (Button) findViewById(R.id.turnRight_btn);
        turnRight_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _connection.sendMessage("TURN:10");
            }
        });

        Button turnLeft_btn = (Button) findViewById(R.id.turnLeft_btn);
        turnLeft_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _connection.sendMessage("TURN:-10");
            }
        });

        Button turnRightMore_btn = (Button) findViewById(R.id.turnRightMore_btn);
        turnRightMore_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _connection.sendMessage("TURN:90");
            }
        });

        Button turnLeftMore_btn = (Button) findViewById(R.id.turnLeftMore_btn);
        turnLeftMore_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _connection.sendMessage("TURN:-90");
            }
        });

        Button disconnectServer_btn = (Button) findViewById(R.id.disconnectServer_btn);
        disconnectServer_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _connection.disconnect();
            }
        });


        final Button testAngle_btn = (Button) findViewById(R.id.testAngle_btn);
        testAngle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText angle_txt = (EditText) findViewById(R.id.angle_txt);
                float deg = Float.parseFloat(angle_txt.getText().toString());
                String degStr = Float.toString(deg);
                _connection.sendMessage("TURN:"+degStr);
            }
        });

        Button returnTestAngle_btn = (Button) findViewById(R.id.returnTestAngle_btn);
        returnTestAngle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText angle_txt = (EditText) findViewById(R.id.angle_txt);
                float deg = Float.parseFloat(angle_txt.getText().toString());
                deg = deg * (-1);
                String degStr = Float.toString(deg);
                _connection.sendMessage("TURN:"+degStr);
            }
        });

        final Switch start_btn = (Switch)findViewById(R.id.startCapture_swc);
        start_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    EditText angle_txt = (EditText) findViewById(R.id.angle_txt);
                    EditText captureTime_txt = (EditText) findViewById(R.id.captureTime_txt);
                    if(angle_txt.getText().toString() == "") {
                        log("Angle must not be empty");
                    }

                    if(captureTime_txt.getText().toString() == "") {
                        log("Time must not be empty");
                    }
                    final float toDegrees = Float.parseFloat(angle_txt.getText().toString());
                    final int minutes = Integer.parseInt(captureTime_txt.getText().toString());
                    startCapturing(toDegrees, minutes);
                }
                else {
                    stopCapturing();
                }
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                _cameraController.closeCamera();
            }
        });

    }

    private void stopCapturing() {
        _cameraController.closeCamera();
        timer.cancel();
    }

    private void startCapturing(float captureDegrees, float hours) {
        int outputFPS = 24;
        int outputVideoLenSec = 30;

        float captureSeconds = hours * 60 * 60;

        int totalOutputPictureNum = outputFPS * outputVideoLenSec;
        int takePictureEveryNumbedOfSeconds = (int)captureSeconds / totalOutputPictureNum;

        _ftcalc.init(captureDegrees, totalOutputPictureNum,0.07f);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                float turnDeg = _ftcalc.getNextElem();
                if(turnDeg == -1) {
                    stopCapturing();
                }
                else {
                    _cameraController.takePicture();
                    if(turnDeg > 0) {
                        String deltaStr = Float.toString(turnDeg);
                        _connection.sendMessage("TURN:" + deltaStr);
                    }
                }
            }
        }, new Date(), takePictureEveryNumbedOfSeconds*1000);

    }



    private void connectToServer() {
        EditText serverIp = (EditText) findViewById(R.id.serverIp_txt);
        if(_connection == null) {
            _connection = new ServerConnection(this);
        }
        _connection.connect(serverIp.getText().toString(), 5000);
    }

    @Override
    public void onMessageReceived(String msg) {
        log(msg);
    }

    @Override
    public void onSocketListenerError(String error) {
        log(error);
    }

    @Override
    public void onSocketListenerInfo(String message) {
        log(message);
    }

    private void log(CharSequence msg) {
        TextView log_txt = (TextView)findViewById(R.id.log_txt);
        log_txt.setText(msg);
    }

    @Override
    public void onCameraControllerError(String error) {
        log(error);
    }

    @Override
    public void onCameraControllerInfo(String message) {
        log(message);
    }
}
