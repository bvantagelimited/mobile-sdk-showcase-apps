package com.ipification.demoapp;

import com.ipification.mobile.sdk.android.IPConfiguration;
import com.ipification.mobile.sdk.android.IPEnvironment;

public  class Urls {



    // warning: for IM only
    public static String DEVICE_TOKEN_REGISTRATION_URL = "https://demo-api.ipification.com/register-device";

    // warning: only call these apis on DEMO
    public static String getExchangeTokenUrl(){
        return IPConfiguration.getInstance().getENV() == IPEnvironment.PRODUCTION ? "https://api.ipification.com/auth/realms/ipification/protocol/openid-connect/token" : "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token";
    }
    // warning: only call these apis on DEMO
    public static String getUserInfoUrl(){
        return IPConfiguration.getInstance().getENV() == IPEnvironment.PRODUCTION ? "https://api.ipification.com/auth/realms/ipification/protocol/openid-connect/userinfo" : "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/userinfo";
    }
}
