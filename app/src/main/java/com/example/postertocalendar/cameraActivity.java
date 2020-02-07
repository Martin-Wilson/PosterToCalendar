//Martin Wilson yeet


package com.example.postertocalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;

public class cameraActivity extends AppCompatActivity {

    private static final String TAG = "cameraActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 101;

    private Button takeAPictureButton;

    private Handler backgroundHandler;
    private HandlerThread handlerThread = null;

    protected CameraDevice ourCamera;
    private String cameraID;

    private TextureView cameraPreview;
    private Surface imagePreview;
    private Size imageDimension;

    protected CaptureRequest.Builder takePictureBuilder;
    protected CaptureRequest.Builder captureRequestBuilder;
    protected CaptureRequest captureRequest;
    protected CameraCaptureSession cameraSession;


    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreview = findViewById(R.id.texture);
        cameraPreview.setSurfaceTextureListener(surfaceTextureListener);


        takeAPictureButton = findViewById(R.id.btn_take_picture);
        takeAPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private void takePicture() {
        try {
            takePictureBuilder = ourCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            takePictureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));


            int id = cameraSession.capture(captureRequest, captureCallback, null);


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    private void openCamera() throws CameraAccessException {
        CameraManager cameraMan = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraID = cameraMan.getCameraIdList()[0];

        CameraCharacteristics characteristics = cameraMan.getCameraCharacteristics(cameraID);
        StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        imageDimension = configMap.getOutputSizes(SurfaceTexture.class)[0];

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
        PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(cameraActivity.this,new String[] {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
            return;
        }

         cameraMan.openCamera(cameraID, stateCallback, null);

    }


    private void updatePreview() throws CameraAccessException {
        if (ourCamera == null)
            return;

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }
    private void cameraPreviewCreation() throws CameraAccessException {
        SurfaceTexture texture = cameraPreview.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimension.getWidth(),imageDimension.getHeight());
        
        Surface surface = new Surface(texture);

        captureRequestBuilder = ourCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

        captureRequestBuilder.addTarget(surface);

        ourCamera.createCaptureSession(Arrays.asList(surface), captureSessionCallBack,null);
    }


    private void stopBackgroundThread() throws InterruptedException {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    private void startBackgroundThread() {
        handlerThread = new HandlerThread("Camera Background");
        handlerThread.start();

        backgroundHandler = new Handler(handlerThread.getLooper());
    }


    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            try {
                openCamera();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };



    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            ourCamera = camera;
            try {
                cameraPreviewCreation();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
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

    final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);


            takePictureBuilder.addTarget(new Surface(cameraPreview.getSurfaceTexture()));
            takePictureBuilder.set(CaptureRequest.CONTROL_MODE,CameraMetadata.CONTROL_MODE_AUTO);

        }
    };


    CameraCaptureSession.StateCallback captureSessionCallBack  = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if(ourCamera == null){
                return;
            }

            cameraSession = session;

            try {
                updatePreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

        }

    };



    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if(cameraPreview.isAvailable()){

            try{
                openCamera();
            } catch (CameraAccessException e){
                e.printStackTrace();
            }
        } else {

            cameraPreview.setSurfaceTextureListener(surfaceTextureListener);

        }
    }


    @Override
    protected void onPause() {
        try {
            stopBackgroundThread();
        } catch(InterruptedException e){
            e.printStackTrace();
        }

        super.onPause();
    }

}
