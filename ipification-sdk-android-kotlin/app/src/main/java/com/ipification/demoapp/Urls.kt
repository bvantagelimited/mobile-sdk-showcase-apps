package com.ipification.demoapp

import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
class Urls {
    companion object Factory {
        // Base URLs
        private const val STAGE_EXCHANGE_TOKEN_URL = "https://your-stage-backend-api.com/"
        private const val LIVE_EXCHANGE_TOKEN_URL = "https://your-live-backend-api.com/"

        // Specific endpoints
//        private const val EXCHANGE_TOKEN_ENDPOINT = "token"
//        private const val USER_INFO_ENDPOINT = "userinfo"

          // IM endpoints
        const val DEVICE_TOKEN_REGISTRATION_URL = "https://client-backend-api/register-device"
        const val AUTOMODE_SIGN_IN_URL = "https://client-backend-api/s2s/signin"



        // Get Token Exchange URL based on the environment
        fun getTokenExchangeUrl(): String {
            return if (IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX) {
                STAGE_EXCHANGE_TOKEN_URL
            } else {
                LIVE_EXCHANGE_TOKEN_URL
            }
        }

        // Get User Info URL based on the environment
//        fun getUserInfoUrl(): String {
//            return if (IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX) {
//                STAGE_USER_INFO_URL
//            } else {
//                LIVE_USER_INFO_URL
//            }
//        }
    }
}
