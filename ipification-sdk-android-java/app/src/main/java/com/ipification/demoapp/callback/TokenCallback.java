package com.ipification.demoapp.callback;

public interface TokenCallback {

//    void result(String response, String errorMessage);
    void onSuccess(String response);
    void onError(String errorMessage);


}
