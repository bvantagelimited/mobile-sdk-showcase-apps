package com.ipification.demoapp.manager;


import android.util.Log;

import com.ipification.demoapp.BuildConfig;
import com.ipification.demoapp.Urls;
import com.ipification.demoapp.callback.TokenCallback;
import com.ipification.demoapp.util.Util;
import com.ipification.mobile.sdk.android.IPConfiguration;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IPHelper {

    private static final String TAG = "IPHelper";


    public static void doPostToken(String code, final TokenCallback callback) {
        try {
            String url = Urls.getExchangeTokenUrl();

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
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String accessToken = Util.parseAccessTokenFromJSON(response.body().string());
                        if (!accessToken.equals("")) {
                            // call userInfo
                            doPostUserInfo(accessToken, callback);
                        } else {
//                            callback.result("", response.body().string());
                        }
                    } else {
                        try {
                            if (response.body() != null) {
//                                callback.result("", response.body().string());
                            } else {
//                                callback.result("", "error body is null " + response.code());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                    callback.result("", e.getMessage());
                    Log.e(TAG, "token exchange error: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "token exchange error: " + e.getMessage());
            e.printStackTrace();
//            callback.result("", e.getMessage());
        }
    }
    public static void doPostUserInfo(String accessToken, final TokenCallback callback) {
        try {
            String url = Urls.getUserInfoUrl();

            RequestBody body = new FormBody.Builder()
                    .add("access_token", accessToken)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).post(body)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
//                        callback.result(response.body().string(), "");
                    } else {
                        try {
                            if (response.body() != null) {
//                                callback.result("", response.body().string());
                            } else {
//                                callback.result("", "error body is null " + response.code());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                    callback.result("", e.getMessage());
                    Log.e(TAG, "token exchange error: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "token exchange error: " + e.getMessage());
            e.printStackTrace();
//            callback.result("", e.getMessage());
        }
    }
}


