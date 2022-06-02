package com.ipification.demoapp.util;

import com.auth0.android.jwt.JWT;
import com.ipification.demoapp.data.TokenInfo;

import org.json.JSONObject;

public class Util {
    public static TokenInfo parseAccessToken(String accessToken) {
        if (accessToken == null || accessToken.equals("")) {
            return null;
        }

        JWT jwt = null;
        String error = null;
        try {
            jwt = new JWT(accessToken);
        } catch (Exception e) {
            error = e.getLocalizedMessage();
        }
        if (error != null && !error.equals("") || jwt == null) {
            return null;
        } else {
            String phoneVerify = jwt.getClaim("phone_number_verified").asString();
            String phoneNumber = jwt.getClaim("phone_number").asString();
            String loginHint = jwt.getClaim("login_hint").asString();
            String sub = jwt.getClaim("sub").asString();
            String mobileID = jwt.getClaim("mobile_id").asString();
            return new TokenInfo(
                    phoneVerify == null || phoneVerify.equals("true"),
                    phoneNumber,
                    loginHint,
                    sub,
                    mobileID
            );
        }
    }

    public static String parseAccessTokenFromJSON(String jsonStr) {
        try {
            JSONObject jObject = new JSONObject(jsonStr);
            return jObject.getString("access_token");
        } catch (Exception e) {
            return "";
        }
    }

    public static String parseUserInfoJSON(String jsonStr, String pattern) {
        try {
            JSONObject jObject = new JSONObject(jsonStr);
            return jObject.getString(pattern);
        } catch (Exception e) {
            return "";
        }
    }
}
