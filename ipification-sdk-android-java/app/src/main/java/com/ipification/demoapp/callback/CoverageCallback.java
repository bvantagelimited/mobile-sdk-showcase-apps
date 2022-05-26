package com.ipification.demoapp.callback;

public interface CoverageCallback {
    void result(Boolean isAvailable, String operatorCode, String errorMessage);
}
