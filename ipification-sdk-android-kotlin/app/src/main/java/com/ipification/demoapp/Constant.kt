package com.ipification.demoapp

class Constant {

    companion object Factory {

        private const val HOST = "https://stage.ipification.com"

        const val EXCHANGE_TOKEN_URL = "$HOST/auth/realms/ipification/protocol/openid-connect/token"
        const val USER_INFO_URL = "$HOST/auth/realms/ipification/protocol/openid-connect/userinfo"

        const val DEVICE_TOKEN_REGISTRATION_URL = "https://demo.ipification.com/merchant-service/register-device"
        const val AUTOMODE_SIGN_IN_URL = ""


        const val CLIENT_SECRET = ""
    }
}
