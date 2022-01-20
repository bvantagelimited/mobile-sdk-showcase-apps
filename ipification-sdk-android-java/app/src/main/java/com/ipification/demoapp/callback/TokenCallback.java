package com.ipification.demoapp.callback;

public interface TokenCallback {
    void onError(String error);

    void onSuccess(String response);
}
