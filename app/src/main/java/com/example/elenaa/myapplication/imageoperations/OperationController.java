package com.example.elenaa.myapplication.imageoperations;

import com.abbyy.mobile.imaging.FineOperation;
import com.example.elenaa.myapplication.imageoperations.enums.OperationTypeEnum;

public abstract class  OperationController {

    protected boolean isGrayscaleValue;
    protected boolean isFastValue;
    protected OperationTypeEnum operationType;

    public abstract FineOperation createOperation();

    public OperationTypeEnum getOperationType() {
        return operationType;
    }

}
