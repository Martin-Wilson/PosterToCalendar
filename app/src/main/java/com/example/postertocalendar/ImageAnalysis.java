package com.example.postertocalendar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;


public class ImageAnalysis {
    Bitmap preanalysisImage;

    private void imageToBitMap(){
        preanalysisImage = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "preAnalysisImage.jpg");

    }

    private void runTextRecognition(){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(preanalysisImage);
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        detector.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseVisionText>(){
                            @Override
                            public void onSuccess(FirebaseVisionText texts){
                                Log.w("Text found", texts.getText());
                                processTextRecognitionResult(texts);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        });

    }

    private void processTextRecognitionResult(FirebaseVisionText text){


    }

}
