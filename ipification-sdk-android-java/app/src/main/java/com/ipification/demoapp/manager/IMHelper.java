package com.ipification.demoapp.manager;

import android.util.Log;

import com.ipification.demoapp.Urls;
import com.ipification.demoapp.callback.TokenCallback;
import com.ipification.mobile.sdk.android.model.IMSession;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");
    public static IMSession sessionInfo;

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

    public static void signIn(String state, TokenCallback callback) {
        String url = Urls.AUTOMODE_SIGN_IN_URL;
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("state", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonObject.toString(), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String error = e.getLocalizedMessage();
                Log.e(TAG, "SignIn onFailure: " + error, e);
                callback.onError(error != null ? error : "postUserInfo - onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseBody = response.body().string();

                    if (response.isSuccessful()) {
                        callback.onSuccess(responseBody != null ? responseBody : "");
                    } else {
                        callback.onError(responseBody != null ? responseBody : "");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response", e);
                    callback.onError(e.getLocalizedMessage() != null ? e.getLocalizedMessage() : "Error processing response");
                }
            }
        });
    }

}