package com.ipification.demoapp

class Constant {

    companion object Factory {
        // for demo only
        var EXCHANGE_TOKEN_URL = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token"
        var USER_INFO_URL = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/userinfo"


        // for IM
        const val DEVICE_TOKEN_REGISTRATION_URL = "https://cases.ipification.com/merchant-service/register-device"
        const val AUTOMODE_SIGN_IN_URL = "https://cases.ipification.com/merchant-service/s2s/signin"


    }
}
