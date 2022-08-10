package com.ipification.demoapp.manager;


import android.util.Log;

import androidx.annotation.NonNull;

import com.ipification.demoapp.BuildConfig;
import com.ipification.demoapp.Constant;
import com.ipification.demoapp.callback.TokenCallback;
import com.ipification.demoapp.util.Util;
import com.ipification.mobile.sdk.android.IPConfiguration;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiManager {
    private static String TAG = "ApiManager";
    public static String currentState = null;
    public static String currentToken = null;

    public static void doPostToken(String code, final TokenCallback callback) {
        try {
            String url = Constant.EXCHANGE_TOKEN_URL;

            RequestBody body = new FormBody.Builder()
                    .add("client_id", IPConfiguration.getInstance().getCLIENT_ID())
                    .add("redirect_uri", IPConfiguration.getInstance().getREDIRECT_URI().toString())
                    .add("grant_type", "authorization_code")
                    .add("client_secret", BuildConfig.CLIENT_SECRET)
                    .add("code", code)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).post(body)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback(){
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if(response.isSuccessful() && response.body() != null){
                        String accessToken = Util.parseAccessTokenFromJSON(response.body().string());
                        if(!accessToken.equals("")){
                            // call userInfo
                            doPostUserInfo(accessToken, callback);

                        }else{
                            callback.result("", response.body().string());
                        }
                    }else{
                        try{
                            if(response.body() != null){
                                callback.result("", response.body().string());
//                            Log.e(TAG, "token exchange error: " + response.body().string());
                            }else{
                                callback.result("", "error body is null " + response.code());
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.result("", e.getMessage());
                    Log.e(TAG, "token exchange error: " + e.getMessage());
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "token exchange error: " + e.getMessage());
            e.printStackTrace();
            callback.result("", e.getMessage());
        }
    }

    public static void registerDevice(String deviceToken, String state) {
        if(deviceToken.isEmpty() || state == null || state.equals("")){
            Log.d(TAG, "deviceToken or state null. failed");
            return;
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url = Constant.DEVICE_TOKEN_REGISTRATION_URL;
        String json =
                "{\"device_id\": \"" + state + "\",\"device_token\": \"" + deviceToken + "\", \"device_type\":\"android\"}";
        Log.d(TAG, "registerDevice " + json);

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request  = new Request.Builder().url(url).post(RequestBody.create(JSON, json))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    Log.d(TAG, "registered successfully - " + response.body().string());
                }else{
                    Log.e(TAG, "registered failed " + response.body().string());
                }

            }
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "registered failed" + e.getMessage());

            }
        });
    }
    public static void doPostUserInfo(String accessToken, final TokenCallback callback) {
        try {
            String url = Constant.USER_INFO_URL;

            RequestBody body = new FormBody.Builder()
                    .add("access_token", accessToken)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).post(body)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback(){
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if(response.isSuccessful() && response.body() != null){
                        callback.result( response.body().string(), "");
                    }else{
                        try{
                            if(response.body() != null){
                                callback.result("", response.body().string());
//                            Log.e(TAG, "token exchange error: " + response.body().string());
                            }else{
                                callback.result("", "error body is null " + response.code());
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    callback.result("", e.getMessage());
                    Log.e(TAG, "token exchange error: " + e.getMessage());
                }
            });


        } catch (Exception e) {
            Log.e(TAG, "token exchange error: " + e.getMessage());
            e.printStackTrace();
            callback.result("", e.getMessage());

        }
    }

}

