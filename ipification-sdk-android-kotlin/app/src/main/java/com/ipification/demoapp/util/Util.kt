package com.ipification.demoapp.util

import android.app.Activity
import android.content.Intent
import com.auth0.android.jwt.JWT
import com.ipification.demoapp.activity.ResultFailActivity
import com.ipification.demoapp.activity.ResultSuccessActivity
import com.ipification.demoapp.callback.TokenCallback
import com.ipification.demoapp.data.TokenInfo
import com.ipification.demoapp.manager.IMHelper
import org.json.JSONObject

class Util {

    companion object Factory {
        fun parseAccessToken(accessToken: String?): TokenInfo? {
            if (accessToken.isNullOrEmpty()) {
                return null
            }

            lateinit var jwt: JWT
            var error: String? = null
            try {
                jwt = JWT(accessToken)
            } catch (e: Exception) {
                error = e.localizedMessage
            }
            if (error.isNullOrEmpty() == false) {
                return null
            } else {
                val phoneVerify = jwt.getClaim("phone_number_verified").asString()
                val phoneNumber = jwt.getClaim("phone_number").asString()
                val loginHint = jwt.getClaim("login_hint").asString()
                val sub = jwt.getClaim("sub").asString()
                val mobileID = jwt.getClaim("mobile_id").asString()
                return TokenInfo(
                    if (phoneVerify == null) true else phoneVerify == "true",
                    phoneNumber,
                    loginHint,
                    sub,
                    mobileID
                )
            }
        }

        fun parseAccessTokenFromJSON(jsonStr: String): String?{
            try {
                val jObject = JSONObject(jsonStr)
                return jObject.getString("access_token")
            } catch (error: Exception) {
                return null
            }
        }
        fun parseUserInfoJSON(jsonStr: String, pattern: String) : String?{
            try {
                val jObject = JSONObject(jsonStr)
                return jObject.getString(pattern)
            } catch (error: Exception) {
                return null
            }
        }

        fun callLoginAPI(activity: Activity, state: String) {
            IMHelper.signIn(state, callback = object : TokenCallback {
                override fun onSuccess(response: String) {
                    openSuccessActivity(activity, responseStr = response)
                }

                override fun onError(errorMessage: String) {
                    openErrorActivity(activity, error = errorMessage)
                }
            })
        }

        fun openSuccessActivity(activity: Activity, responseStr: String) {
            val intent = Intent(activity, ResultSuccessActivity::class.java)
            intent.putExtra("responseStr", responseStr)
            activity.startActivity(intent)

        }
        fun openErrorActivity(activity: Activity, error: String){
            val intent = Intent(activity, ResultFailActivity::class.java)
            intent.putExtra(
                "error",
                error
            )
            activity.startActivity(intent)
        }


    }
}