package lv.enthusiast.timelapse;

/**
 * Created by mitnick on 4/19/14.
 */
public interface CameraControllerListener {
    void onCameraControllerError(String error);
    void onCameraControllerInfo(String message);
}
