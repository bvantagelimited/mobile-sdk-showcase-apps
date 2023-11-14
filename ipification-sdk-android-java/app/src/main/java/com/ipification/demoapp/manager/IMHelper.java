package com.ipification.demoapp.manager;

import android.util.Log;

import com.ipification.demoapp.Urls;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class IMHelper {

    private static final String TAG = "IMHelper";
    public static String currentState = null;
    public static String currentToken = null;

    public static void registerDevice(String deviceToken, String state) {
        if (deviceToken.isEmpty() || state == null || state.equals("")) {
            Log.d(TAG, "deviceToken or state null. failed");
            return;
        }
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        String url = Urls.DEVICE_TOKEN_REGISTRATION_URL;
        String json = "{\"device_id\": \"" + state + "\",\"device_token\": \"" + deviceToken + "\", \"device_type\":\"android\"}";
        Log.d(TAG, "registerDevice " + json);

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).post(RequestBody.create(JSON, json))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d(TAG, "registered successfully - " + response.body().string());
                } else {
                    Log.e(TAG, "registered failed " + response.body().string());
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "registered failed" + e.getMessage());
            }
        });
    }
}