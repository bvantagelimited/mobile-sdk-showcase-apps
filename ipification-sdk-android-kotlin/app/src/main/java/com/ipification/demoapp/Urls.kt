package com.ipification.demoapp

import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
class Urls {
    companion object Factory {
        // Base URLs
        private const val STAGE_BASE_URL = "https://api.stage.ipification.com/auth/realms/ipification/protocol/openid-connect/"
        private const val LIVE_BASE_URL = "https://api.ipification.com/auth/realms/ipification/protocol/openid-connect/"

        // Specific endpoints
        private const val EXCHANGE_TOKEN_ENDPOINT = "token"
        private const val USER_INFO_ENDPOINT = "userinfo"

        // IM endpoints
        const val DEVICE_TOKEN_REGISTRATION_URL = "https://cases.ipification.com/merchant-service/register-device"
        const val AUTOMODE_SIGN_IN_URL = "https://cases.ipification.com/merchant-service/s2s/signin"

        // Demo URLs
        var STAGE_EXCHANGE_TOKEN_URL = "$STAGE_BASE_URL$EXCHANGE_TOKEN_ENDPOINT"
        var STAGE_USER_INFO_URL = "$STAGE_BASE_URL$USER_INFO_ENDPOINT"

        var LIVE_EXCHANGE_TOKEN_URL = "$LIVE_BASE_URL$EXCHANGE_TOKEN_ENDPOINT"
        var LIVE_USER_INFO_URL = "$LIVE_BASE_URL$USER_INFO_ENDPOINT"

        // Get Token Exchange URL based on the environment
        fun getTokenExchangeUrl(): String {
            return if (IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX) {
                STAGE_EXCHANGE_TOKEN_URL
            } else {
                LIVE_EXCHANGE_TOKEN_URL
            }
        }

        // Get User Info URL based on the environment
        fun getUserInfoUrl(): String {
            return if (IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX) {
                STAGE_USER_INFO_URL
            } else {
                LIVE_USER_INFO_URL
            }
        }
    }
}
