package lv.enthusiast.timelapse;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by mitnick on 4/19/14.
 */
public class CameraController {
    private CameraControllerListener _listener;
    private CameraPreview mPreview;
    private FrameLayout _surface;
    private Context _context;
    private Camera camera;

    public CameraController(Context context, FrameLayout surface, CameraControllerListener listener) {
        _listener = listener;
        _surface = surface;
        _context = context;
        if(checkCameraHardware(context)) {
            _listener.onCameraControllerInfo("Camera exists");
        }
        else {
            _listener.onCameraControllerInfo("Camera does not exist");
        }
        openCamera();
    }

    public void takePicture() {
        camera.takePicture(null, null, mPicture);
    }

    private void openCamera() {
        if(camera != null) {
            return;
        }
        camera = getCameraInstance();
        if(camera != null) {
            _listener.onCameraControllerInfo("Camera instance retrieved");
        }
        else {
            _listener.onCameraControllerInfo("Could not retrieve camera instance");
        }

        camera.setDisplayOrientation(90);
        List<Camera.Size> supportedPictureSizes = camera.getParameters().getSupportedPictureSizes();
        Camera.Parameters params =  camera.getParameters();
        if (supportedPictureSizes != null) {
            params.setPictureSize(supportedPictureSizes.get(13).width, supportedPictureSizes.get(13).height);
        }
        camera.setParameters(params);
        mPreview = new CameraPreview(_context, camera);
        _surface.addView(mPreview);
    }

    public void closeCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private Camera getCameraInstance(){
        Camera c = null;
        try {
            c = android.hardware.Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            System.out.print("Camera unavailable");
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }



    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            _listener.onCameraControllerInfo("onPictureTaken starting save");
            File pictureFile = getOutputMediaFile(0);
            if (pictureFile == null){
                _listener.onCameraControllerError("Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                _listener.onCameraControllerInfo("File saved: " + pictureFile.getAbsoluteFile().getPath());
            } catch (FileNotFoundException e) {
                _listener.onCameraControllerError("File not found: " + e.getMessage());
            } catch (IOException e) {
                _listener.onCameraControllerError("Error accessing file: " + e.getMessage());
            }
        }
    };

    private File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Timelapse");
        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                _listener.onCameraControllerError("MyCameraApp failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == 0){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
