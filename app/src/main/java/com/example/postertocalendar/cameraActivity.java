//Martin Wilson yeet


package com.example.postertocalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class cameraActivity extends AppCompatActivity {


    private static final int REQUEST_CAMERA_PERMISSION = 101;

    protected Handler backgroundHandler;
    protected HandlerThread handlerThread = null;

    protected CameraDevice ourCamera;
    protected CameraCharacteristics characteristics;
    private String cameraID;


    private ImageReader imageReader;
    private TextureView cameraPreview;
    private Size imageDimension;

    protected CaptureRequest.Builder takePictureBuilder;
    protected CaptureRequest.Builder captureRequestBuilder;
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


        Button takeAPictureButton = findViewById(R.id.btn_take_picture);
        takeAPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private void takePicture() {
        try {
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int width = 640;
            int height = 480;

            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            characteristics = manager.getCameraCharacteristics(cameraID);
            Size[] jpegSizes;
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(imageReader.getSurface());
            outputSurfaces.add(new Surface(cameraPreview.getSurfaceTexture()));
            takePictureBuilder = ourCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            takePictureBuilder.addTarget(imageReader.getSurface());
            takePictureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            takePictureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            final File imageFile = new File(Environment.getExternalStorageDirectory() + "preAnalysisImage.jpg");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(imageFile);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };

            imageReader.setOnImageAvailableListener(readerListener, backgroundHandler);

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                        Log.w("File",imageFile.getPath());
                }
            };
            ourCamera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(takePictureBuilder.build(), captureListener, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, null);

        } catch (CameraAccessException e){
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }


    private void openCamera() {
        CameraManager cameraMan = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraID = cameraMan.getCameraIdList()[0];

            characteristics = cameraMan.getCameraCharacteristics(cameraID);
            StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert configMap != null;
            imageDimension = configMap.getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(cameraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }

            cameraMan.openCamera(cameraID, stateCallback, null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }


    private void updatePreview() {
        if (null == ourCamera)
            Log.w("UHHH","OHHHHH");

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void closeCamera(){
        if(ourCamera != null){
            ourCamera.close();
            ourCamera = null;
        }
        if(null != imageReader){
            imageReader.close();
            imageReader = null;
        }
    }

    private void cameraPreviewCreation(){
        try {
            SurfaceTexture texture = cameraPreview.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());

            Surface surface = new Surface(texture);

            captureRequestBuilder = ourCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            captureRequestBuilder.addTarget(surface);

            ourCamera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (null == ourCamera) {
                        return;
                    }

                    cameraSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.w("Camera", "Camera Preview Failed");
                }

            },null);

        } catch(CameraAccessException e){
            e.printStackTrace();
        }
    }


    private void stopBackgroundThread()  {
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

                openCamera();

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
                cameraPreviewCreation();
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



    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();

        if(cameraPreview.isAvailable()){
                openCamera();
        } else {

            cameraPreview.setSurfaceTextureListener(surfaceTextureListener);

        }
    }


    @Override
    protected void onPause() {
            stopBackgroundThread();
            closeCamera();


        super.onPause();
    }

}

