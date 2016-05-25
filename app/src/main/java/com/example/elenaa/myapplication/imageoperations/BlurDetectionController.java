package com.example.elenaa.myapplication.imageoperations;

import com.abbyy.mobile.imaging.FineOperation;
import com.abbyy.mobile.imaging.MIListener;
import com.abbyy.mobile.imaging.defects.MIBlurInfo;
import com.abbyy.mobile.imaging.operations.FineDetectBlur;
import com.example.elenaa.myapplication.imageoperations.enums.OperationTypeEnum;

public class BlurDetectionController extends OperationController  {

    private MIBlurInfo blurInfo;

    public BlurDetectionController(boolean isGrayscale, boolean isFast) {
        operationType = OperationTypeEnum.FINE_DETECT_BLUR;

        isGrayscaleValue = isGrayscale;
        isFastValue = isFast;
    }

    @Override
    public FineOperation createOperation() {
        return new FineDetectBlur(new MIListener<MIBlurInfo>() {
            @Override
            public void onProgressFinished(MIBlurInfo miBlurInfo) {
                blurInfo = miBlurInfo;
            }
        }, isGrayscaleValue, isFastValue);
    }

    public MIBlurInfo getBlurInfo() {
        return blurInfo;
    }
}
