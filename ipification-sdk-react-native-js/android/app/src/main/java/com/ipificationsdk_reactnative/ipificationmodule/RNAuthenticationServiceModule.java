package com.ipificationsdk_reactnative.ipificationmodule;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.ipification.mobile.sdk.android.CellularService;
import com.ipification.mobile.sdk.android.callback.CellularCallback;
import com.ipification.mobile.sdk.android.exception.CellularException;
import com.ipification.mobile.sdk.android.request.AuthRequest;
import com.ipification.mobile.sdk.android.response.AuthResponse;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class RNAuthenticationServiceModule extends ReactContextBaseJavaModule {
    private Context context;
    RNAuthenticationServiceModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "RNAuthenticationService";
    }

    @ReactMethod
    public void doAuthorization(Callback successCallback, Callback errorCallback) {
        Log.d("DEBUG", "doAuthorization");
        doAuthorization(new CellularCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse authResponse) {
                Log.d("DEBUG", "onSuccess " + authResponse.getCode());
                successCallback.invoke(null, authResponse.getCode());
            }

            @Override
            public void onError(@NotNull CellularException e) {
                Log.d("DEBUG", "onError " + e.getErrorMessage());
                errorCallback.invoke(null, e.getErrorMessage());
            }
        });


    }
    @ReactMethod
    public void doAuthorizationWithParams(ReadableMap params, Callback successCallback, Callback errorCallback) {
        Log.d("DEBUG", "checkCoverage");
        doAuthorizationWithParams(params, new CellularCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse authResponse) {
                Log.d("DEBUG", "onSuccess " + authResponse.getCode());
                successCallback.invoke(null, authResponse.getCode());
            }

            @Override
            public void onError(@NotNull CellularException e) {
                Log.d("DEBUG", "onError " + e.getErrorMessage());
                errorCallback.invoke(null, e.getErrorMessage());
            }
        });


    }


    private void doAuthorization(CellularCallback<AuthResponse> cb) {
        CellularService<AuthResponse> doAuthService = new CellularService<>(context);
        doAuthService.performAuth(cb);
    }
    private void doAuthorizationWithParams(ReadableMap params, CellularCallback<AuthResponse> cb) {
        CellularService<AuthResponse> doAuthService = new CellularService<>(context);
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        try {

            HashMap data = params.toHashMap();
            for(Object key : data.keySet()){
                Log.d("DEBUG","map: " + key + " "+data.get(key));
                authRequestBuilder.addQueryParam(key.toString(), data.get(key).toString());
            }
            AuthRequest authRequest = authRequestBuilder.build();
            doAuthService.performAuth(authRequest, cb);
        }catch(Exception e){
            cb.onError(new CellularException(e));
        }

    }
}
