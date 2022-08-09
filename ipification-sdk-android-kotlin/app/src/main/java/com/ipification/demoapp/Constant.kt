package com.ipification.demoapp

import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment

class Constant {

    companion object Factory {

        private val HOST = if(IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX){"https://stage.ipification.com"}else{"https://api.ipification.com"}

        var EXCHANGE_TOKEN_URL = "$HOST/auth/realms/ipification/protocol/openid-connect/token"
        var USER_INFO_URL = "$HOST/auth/realms/ipification/protocol/openid-connect/userinfo"

        const val DEVICE_TOKEN_REGISTRATION_URL = "https://cases.ipification.com/merchant-service/register-device"
        const val AUTOMODE_SIGN_IN_URL = ""


    }
}
