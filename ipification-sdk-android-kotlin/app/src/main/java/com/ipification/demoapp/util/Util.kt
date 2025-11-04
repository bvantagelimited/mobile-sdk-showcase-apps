package com.ipification.demoapp.util

import android.app.Activity
import android.content.Intent
import com.ipification.demoapp.activity.ResultFailActivity
import com.ipification.demoapp.activity.ResultSuccessActivity
import com.ipification.demoapp.callback.TokenCallback
import com.ipification.demoapp.manager.IMHelper
import org.json.JSONObject

class Util {

    companion object Factory {

//        fun parseToken(accessToken: String?): TokenInfo? {
//            if (accessToken.isNullOrEmpty()) {
//                return null
//            }
//
//            try {
//                val jwt = JWT(accessToken)
//                val phoneVerify = jwt.getClaim("phone_number_verified").asString()
//                val phoneNumber = jwt.getClaim("phone_number").asString()
//                val loginHint = jwt.getClaim("login_hint").asString()
//                val sub = jwt.getClaim("sub").asString()
//                val mobileID = jwt.getClaim("mobile_id").asString()
//
//                return TokenInfo(
//                    phoneVerify == "true" || phoneVerify == null,
//                    phoneNumber,
//                    loginHint,
//                    sub,
//                    mobileID
//                )
//            } catch (e: Exception) {
//                return null
//            }
//        }
//
//        fun parseAccessTokenFromJSON(jsonStr: String): String? {
//            return try {
//                JSONObject(jsonStr).getString("access_token")
//            } catch (error: Exception) {
//                null
//            }
//        }

        fun parseTokenJSON(jsonStr: String, pattern: String): String? {
            return try {
                JSONObject(jsonStr).getString(pattern)
            } catch (error: Exception) {
                null
            }
        }

        fun callLoginAPI(activity: Activity, state: String?) {
            IMHelper.signIn(state, object : TokenCallback {
                override fun onSuccess(response: String) {
                    openSuccessActivity(activity, response)
                }

                override fun onError(errorMessage: String) {
                    openErrorActivity(activity, errorMessage)
                }
            })
        }

        fun openSuccessActivity(activity: Activity, responseStr: String) {
            startResultActivity(activity, ResultSuccessActivity::class.java, "responseStr", responseStr)
        }

        fun openErrorActivity(activity: Activity, error: String) {
            startResultActivity(activity, ResultFailActivity::class.java, "error", error)
        }

        private fun startResultActivity(activity: Activity, resultActivity: Class<*>, key: String, value: String) {
            val intent = Intent(activity, resultActivity)
            intent.putExtra(key, value)
            activity.startActivity(intent)
        }
    }
}
