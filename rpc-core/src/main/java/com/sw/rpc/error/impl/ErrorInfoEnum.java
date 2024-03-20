package com.sw.rpc.error.impl;

import com.sw.rpc.error.ErrorInfo;

public enum ErrorInfoEnum implements ErrorInfo {
    RpcError(1, "rpc error");

    private int errorCode;
    private String errorMsg;

    ErrorInfoEnum(int errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    @Override
    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        return errorMsg;
    }
}
