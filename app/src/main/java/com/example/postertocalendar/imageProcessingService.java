package com.example.postertocalendar;

import android.app.IntentService;
import android.content.Intent;

public class imageProcessingService extends IntentService {



    public imageProcessingService(){
        super("ImageProcessingService");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        imageProcessing proceser = new imageProcessing();
        proceser.runTextRecognition();


    }

}
