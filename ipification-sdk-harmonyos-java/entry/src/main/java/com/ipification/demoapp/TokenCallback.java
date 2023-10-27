package com.ipification.demoapp;

public interface TokenCallback {
    void onError(String error);
    void onSuccess(String response);
}
