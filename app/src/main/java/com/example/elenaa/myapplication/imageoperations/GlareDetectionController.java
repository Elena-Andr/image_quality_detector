package com.example.elenaa.myapplication.imageoperations;

import com.abbyy.mobile.imaging.FineOperation;
import com.abbyy.mobile.imaging.MIListener;
import com.abbyy.mobile.imaging.defects.MIGlareInfo;
import com.abbyy.mobile.imaging.operations.FineDetectGlare;
import com.example.elenaa.myapplication.imageoperations.enums.OperationTypeEnum;

public class GlareDetectionController extends OperationController {

    private MIGlareInfo glareInfo;

    public GlareDetectionController(boolean isGrayscale, boolean isFast) {
        operationType = OperationTypeEnum.FINE_DETECT_GLARE;

        isGrayscaleValue = isGrayscale;
        isFastValue = isFast;
    }

    @Override
    public FineOperation createOperation() {
        return new FineDetectGlare(new MIListener<MIGlareInfo>() {
            @Override
            public void onProgressFinished(MIGlareInfo miGlareInfo) {
                glareInfo = miGlareInfo;
            }
        }, isGrayscaleValue, isFastValue );
    }

    public MIGlareInfo getGlareInfo() {
        return glareInfo;
    }
}
