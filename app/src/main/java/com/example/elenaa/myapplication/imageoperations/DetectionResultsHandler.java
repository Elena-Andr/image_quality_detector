package com.example.elenaa.myapplication.imageoperations;

import android.content.Context;

import com.abbyy.mobile.imaging.defects.MIBlurInfo;
import com.abbyy.mobile.imaging.defects.MIGlareInfo;
import com.abbyy.mobile.imaging.defects.MINoiseInfo;
import com.example.elenaa.myapplication.MIContext;
import com.example.elenaa.myapplication.R;
import com.example.elenaa.myapplication.imageoperations.enums.OperationTypeEnum;

public class DetectionResultsHandler {

    private String readableResults;
    private Context mainContext;

    // This property indicates the quality of the taken photo
    private boolean isGoodQuality;

    public DetectionResultsHandler(OperationController[] operations) {
        isGoodQuality = true;
        readableResults = "";
        mainContext = MIContext.getInstance().getMainContext();

        generateReadableResults(operations);
    }

    public String getReadableResults() {
        return readableResults;
    }

    private void generateReadableResults(OperationController[] operations) {
        for(OperationController operation : operations) {

            OperationTypeEnum operationType = operation.getOperationType();

            switch (operationType) {
                case FINE_DETECT_BLUR:
                    BlurDetectionController blurDetectionController = (BlurDetectionController)operation;
                    MIBlurInfo blurInfo = blurDetectionController.getBlurInfo();

                    readableResults += handleInfo(mainContext.getString(R.string.results_blur_title), blurInfo.isDetected);
                    break;
                case FINE_DETECT_GLARE:
                    GlareDetectionController glareDetectionController = (GlareDetectionController)operation;
                    MIGlareInfo glareInfo = glareDetectionController.getGlareInfo();

                    readableResults += handleInfo(mainContext.getString(R.string.results_glare_title), glareInfo.isDetected);
                    break;
                case FINE_DETECT_NOISE:
                    NoiseDetectionController noiseDetectionController = (NoiseDetectionController)operation;
                    MINoiseInfo noiseInfo = noiseDetectionController.getNoiseInfo();

                    readableResults += handleInfo(mainContext.getString(R.string.results_noise_title), noiseInfo.isHigh);
                    break;
            }
        }

        if(isGoodQuality == true){
            readableResults += mainContext.getString(R.string.results_save_photo_suggestion);
        }
        else
            readableResults += mainContext.getString(R.string.results_delete_photo_suggestion);
    }

    private String handleInfo(String operationType, boolean isDetected) {
        String readableResult = "";

        readableResult += operationType + ": ";

        if(isDetected == true) {
            isGoodQuality = false;
            readableResult += mainContext.getString(R.string.results_detected_message);
        }
        else {
            readableResult += mainContext.getString(R.string.results_not_detected_message);
        }
        return readableResult;
    }
}