package com.ipificationsdk_reactnative.ipificationmodule;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.ipification.mobile.sdk.android.CellularService;
import com.ipification.mobile.sdk.android.callback.CellularCallback;
import com.ipification.mobile.sdk.android.exception.CellularException;
import com.ipification.mobile.sdk.android.response.CoverageResponse;

import org.jetbrains.annotations.NotNull;

public class RNCoverageServiceModule extends ReactContextBaseJavaModule {
    private Context context;

    RNCoverageServiceModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "RNCoverageService";
    }

    @ReactMethod
    public void checkCoverage(Callback successCallback, Callback errorCallback) {
        Log.d("DEBUG", "checkCoverage");
        checkCoverage(new CellularCallback<CoverageResponse>() {
            @Override
            public void onSuccess(CoverageResponse coverageResponse) {
                Log.d("DEBUG", "onSuccess " + coverageResponse.responseData);
                successCallback.invoke(null, coverageResponse.parseResponse());
            }

            @Override
            public void onError(@NotNull CellularException e) {
                Log.d("DEBUG", "onError " + e.exception.getMessage());
                errorCallback.invoke(null, e.exception.getMessage());
            }
        });


    }


    private void checkCoverage(CellularCallback<CoverageResponse> callback) {
        CellularService<CoverageResponse> checkCoverageService = new CellularService<>(this.context);
        checkCoverageService.registerCallback(callback);
        checkCoverageService.checkCoverage();
    }
}
