package com.ipification.demoapp.util

import com.auth0.android.jwt.JWT
import com.ipification.demoapp.data.TokenInfo
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
    }
}