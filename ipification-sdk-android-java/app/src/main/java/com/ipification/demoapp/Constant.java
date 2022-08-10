package com.ipification.demoapp;

import com.ipification.mobile.sdk.android.IPConfiguration;

public  class  Constant {
    private static final String HOST = IPConfiguration.getInstance().getHOST_NAME(); // stage or live


    public static String EXCHANGE_TOKEN_URL = HOST + "/auth/realms/ipification/protocol/openid-connect/token";
    public static String USER_INFO_URL = HOST + "/auth/realms/ipification/protocol/openid-connect/userinfo";

    public static String DEVICE_TOKEN_REGISTRATION_URL = "https://demo-api.ipification.com/register-device";

}
