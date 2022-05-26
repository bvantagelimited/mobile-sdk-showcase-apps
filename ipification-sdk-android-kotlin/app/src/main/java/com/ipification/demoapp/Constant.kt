package com.ipification.demoapp

class Constant {

    companion object Factory {

        private const val HOST = "https://stage.ipification.com"

        const val CHECK_COVERAGE_URL = "$HOST/auth/realms/ipification/coverage/202.175.50.128"
        const val AUTH_URL = "$HOST/auth/realms/ipification/protocol/openid-connect/auth"
        const val EXCHANGE_TOKEN_URL = "$HOST/auth/realms/ipification/protocol/openid-connect/token"
        const val USER_INFO_URL = "$HOST/auth/realms/ipification/protocol/openid-connect/userinfo"

        const val DEVICE_TOKEN_REGISTRATION_URL = ""


        const val CLIENT_ID = ""
        const val REDIRECT_URI = ""
        const val CLIENT_SECRET = ""
    }
}
