//Martin Wilson was here


package com.example.postertocalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;

public class cameraActivity extends AppCompatActivity {

    CameraDevice ourCamera;
    String cameraID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }

    private void openCamera() throws CameraAccessException {
        CameraManager cameraMan = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        cameraID = cameraMan.getCameraIdList()[0];

        CameraCharacteristics characteristics = cameraMan.getCameraCharacteristics(cameraID);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            return;

        cameraMan.openCamera(cameraID,stateCallback,null);

    }


    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            ourCamera = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            ourCamera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            ourCamera.close();
            ourCamera = null;
        }
    };

}
