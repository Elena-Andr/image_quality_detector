package com.example.elenaa.myapplication.imageoperations;

import com.abbyy.mobile.imaging.FineOperation;
import com.abbyy.mobile.imaging.MIListener;
import com.abbyy.mobile.imaging.defects.MINoiseInfo;
import com.abbyy.mobile.imaging.operations.FineDetectNoise;
import com.example.elenaa.myapplication.imageoperations.enums.OperationTypeEnum;

public class NoiseDetectionController  extends OperationController  {

    private MINoiseInfo noiseInfo;

    public NoiseDetectionController(boolean isGrayscale, boolean isFast) {
        operationType = OperationTypeEnum.FINE_DETECT_NOISE;

        isGrayscaleValue = isGrayscale;
        isFastValue = isFast;
    }

    @Override
    public FineOperation createOperation() {
        return new FineDetectNoise(new MIListener<MINoiseInfo>() {
            @Override
            public void onProgressFinished(MINoiseInfo miNoiseInfo) {
                noiseInfo = miNoiseInfo;
            }
        }, isGrayscaleValue, isFastValue);
    }

    public MINoiseInfo getNoiseInfo() {
        return noiseInfo;
    }

}
