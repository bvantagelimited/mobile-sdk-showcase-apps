package com.ipification.demoapp

import com.ipification.mobile.sdk.android.IPConfiguration

class Constant {

    companion object Factory {

        var HOST = IPConfiguration.getInstance().STAGE_HOST

        var EXCHANGE_TOKEN_URL = "$HOST/auth/realms/ipification/protocol/openid-connect/token"
        var USER_INFO_URL = "$HOST/auth/realms/ipification/protocol/openid-connect/userinfo"

        const val DEVICE_TOKEN_REGISTRATION_URL = "https://cases.ipification.com/merchant-service/register-device"
        const val AUTOMODE_SIGN_IN_URL = ""


    }
}
