package com.ipification.demoapp

import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment

class Urls {

    companion object Factory {
        // for demo only
        var STAGE_EXCHANGE_TOKEN_URL = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token"
        var STAGE_USER_INFO_URL = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/userinfo"

        var LIVE_EXCHANGE_TOKEN_URL = "https://api.ipification.com/auth/realms/ipification/protocol/openid-connect/token"
        var LIVE_USER_INFO_URL = "https://api.ipification.com/auth/realms/ipification/protocol/openid-connect/userinfo"


        // for IM
        const val DEVICE_TOKEN_REGISTRATION_URL = "https://cases.ipification.com/merchant-service/register-device"
        const val AUTOMODE_SIGN_IN_URL = "https://cases.ipification.com/merchant-service/s2s/signin"

        // for demo only
        fun getTokenExchangeUrl(): String{
            if(IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX){
                return STAGE_EXCHANGE_TOKEN_URL
            } else{
                return LIVE_EXCHANGE_TOKEN_URL
            }
        }

        fun getUserInfoUrl(): String{
            if(IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX){
                return STAGE_USER_INFO_URL
            } else{
                return LIVE_USER_INFO_URL
            }
        }
    }
}
