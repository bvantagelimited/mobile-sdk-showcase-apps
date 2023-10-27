package com.ipification.demoapp;

import com.ipification.demoapp.slice.ResultAbilitySlice;
import com.ipification.sdk.IPConfiguration;
import com.ipification.sdk.IPEnvironment;
import com.ipification.sdk.util.LogUtil;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.app.Context;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class IPUtil {


    private static final String TAG = "Util";
    private static String LOG = "";


    // TODO : this API is S2S. Donot call this api from the client side. client must call api to their backend service
    public static void doPostToken(Context context, String code, final TokenCallback callback) {
        IPUtil.LOG += "[APIManager] " + getCurrentDate() + " - TOKEN_EXCHANGE - START - code: " + code + ".\n";

        String TOKEN_URL = (IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX)
                ? "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token"
                : "https://api.ipification.com/auth/realms/ipification/protocol/openid-connect/token";

        RequestBody body = new FormBody.Builder()
                .add("client_id", IPConfiguration.getInstance().CLIENT_ID)
                .add("grant_type", "authorization_code")
                .add("client_secret", IPConfiguration.getInstance().ENV == IPEnvironment.PRODUCTION ? Constant.LIVE_CLIENT_SECRET : Constant.STAGE_CLIENT_SECRET)
                .add("redirect_uri", IPConfiguration.getInstance().REDIRECT_URI.toString())
                .add("code", code)
                .build();

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String error = e.getLocalizedMessage();
                LogUtil.error(TAG, "doPostToken error : " + error);
                LOG += "[APIManager] " + getCurrentDate() + " - TOKEN_EXCHANGE - FAILED - error : " + error + ".\n";
                callback.onError(error != null ? error : "error");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        String accessToken = parseAccessTokenFromJSON(responseBody);
                        LOG += "[APIManager] " + getCurrentDate() + " - TOKEN_EXCHANGE - SUCCESS - accessToken: " + accessToken + ".\n";
                        if (accessToken != null) {
                            postUserInfo(context, accessToken, callback);
                        }else{
                            callback.onError(responseBody);
                        }
                    } else {
                        callback.onError(responseBody);
                    }
                    LogUtil.debug(TAG, "doPostToken response : " + responseBody);
                } catch (Exception e) {
                    LOG += "[APIManager] " + getCurrentDate() + " - TOKEN_EXCHANGE - FAILED " + e.getMessage() + ".\n";
                    e.printStackTrace();
                }
            }
        });
    }

    private static void postUserInfo(Context context, String accessToken, TokenCallback callback) {
        LOG += "[APIManager] " + getCurrentDate() + " - GET_USER_INFO - START - accessToken: " + accessToken + ".\n";

        String url = IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX
                ? "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/userinfo"
                : "https://api.ipification.com/auth/realms/ipification/protocol/openid-connect/userinfo";

        RequestBody body = new FormBody.Builder()
                .add("access_token", accessToken)
                .build();

        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(url)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String error = e.getMessage();
                LogUtil.error(TAG, "postUserInfo onFailure : " + error);
                LOG += "[APIManager] " + getCurrentDate() + " - GET_USER_INFO - FAILED - error: " + error + ".\n";

                callback.onError(error != null ? error : "postUserInfo - onFailure");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String responseBody = response.body().string();
                    LogUtil.debug(TAG, "postUserInfo response : " + responseBody);
                    LOG += "[APIManager] " + getCurrentDate() + " - GET_USER_INFO - SUCCESS - response: " + responseBody + ".\n";

                    if (response.isSuccessful()) {
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onError(responseBody);
                    }
                } catch (Exception e) {
                    LOG += "[APIManager] " + getCurrentDate() + " - GET_USER_INFO - FAILED " + e.getMessage() + ".\n";
                    e.printStackTrace();
                    callback.onError(e.getMessage() != null ? e.getMessage() : "");
                }
            }
        });
    }


    public static String parseAccessTokenFromJSON(String jsonStr) {
        try {
            JSONObject jObject = new JSONObject(jsonStr);
            return jObject.getString("access_token");
        } catch (Exception error) {
            return null;
        }
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        return sdf.format(new Date());
    }


    public static void handleTokenExchangeSuccess(AbilitySlice activity, String response) {
        try {
            if (!response.isEmpty()) {
                LOG += getCurrentDate() + " - Result: AUTH SUCCESS : " + response + " . end";
                openSuccessActivity(activity, response);
            } else {
                LOG += getCurrentDate() + " - Result: TOKEN_EXCHANGE - ERROR : response: " + response + ". end";
                openErrorActivity(activity, response);
            }
        } catch (Exception error) {
            openErrorActivity(activity, error.getMessage() != null ? error.getMessage() : "unknown error");
        }
    }


    public static void handleTokenExchangeError(AbilitySlice activity, String error) {
        LOG += getCurrentDate() + " - Result: ERROR : " + error + ". end";
        if (!error.equals("USER_CANCELED")) {
            openErrorActivity(activity, error);
        }
    }

    public static void handleAuthError(AbilitySlice activity, String error) {
        if (!error.equals("USER_CANCELED")) {
            openErrorActivity(activity, error);
        }
    }

    private static void openSuccessActivity(AbilitySlice activity, String response) {

        Intent intent = new Intent();
        intent.setParam("response", response);
//
        // Use the OperationBuilder class of Intent to construct an Operation object and set the deviceId (left empty if a local ability is required), bundleName, and abilityName attributes for the object.
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName("com.ipification.demoapp")
                .withAbilityName("com.ipification.demoapp.ResultAbility")
                .build();

        // Set the created Operation object to the Intent as its operation attribute.
        intent.setOperation(operation);
        activity.startAbility(intent);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                activity.terminate();
            }
        }, 1000);

    }

    private static void openErrorActivity(AbilitySlice activity, String error) {

        Intent intent = new Intent();
        intent.setParam("error", error);
//
        // Use the OperationBuilder class of Intent to construct an Operation object and set the deviceId (left empty if a local ability is required), bundleName, and abilityName attributes for the object.
        Operation operation = new Intent.OperationBuilder()
                .withDeviceId("")
                .withBundleName("com.ipification.demoapp")
                .withAbilityName("com.ipification.demoapp.ResultAbility")
                .build();

        // Set the created Operation object to the Intent as its operation attribute.
        intent.setOperation(operation);
        activity.startAbility(intent);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                activity.terminate();
            }
        }, 1000);

    }

}