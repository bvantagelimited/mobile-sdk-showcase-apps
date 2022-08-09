package com.ipification.demoapp;

public  class  Constant {
    private static final String HOST = "https://stage.ipification.com"; // stage or live

    public static String CHECK_COVERAGE_URL = HOST + "/auth/realms/ipification/coverage/202.175.50.128";
    public static String AUTH_URL = HOST + "/auth/realms/ipification/protocol/openid-connect/auth";
    public static String EXCHANGE_TOKEN_URL = HOST + "/auth/realms/ipification/protocol/openid-connect/token";
    public static String USER_INFO_URL = HOST + "/auth/realms/ipification/protocol/openid-connect/userinfo";

    public static String DEVICE_TOKEN_REGISTRATION_URL = "https://demo-api.ipification.com/register-device";


    public static String CLIENT_ID = "";
    public static String REDIRECT_URI = "";
    public static String CLIENT_SECRET = "";
}
