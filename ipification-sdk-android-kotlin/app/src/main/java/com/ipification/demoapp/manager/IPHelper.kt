package com.ipification.demoapp.manager;

import android.app.Activity
import android.util.Log
import com.ipification.demoapp.Urls
import com.ipification.demoapp.callback.TokenCallback
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.IPConfiguration
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException

class IPHelper {
    companion object Factory {
        private const val TAG = "IPHelper"

        // Just for testing. The client app needs to call their backend service.
        fun callTokenExchangeAPI(activity: Activity, code: String) {
            doPostToken(code, object : TokenCallback {
                override fun onSuccess(response: String) {
                    handleTokenExchangeSuccess(activity, response)
                }

                override fun onError(errorMessage: String) {
                    Util.openErrorActivity(activity, errorMessage ?: "unknown error")
                }
            })
        }

        private fun handleTokenExchangeSuccess(activity: Activity, response: String) {
            val phoneNumberVerified = Util.parseTokenJSON(response, "phone_number_verified")
            val phoneNumber = Util.parseTokenJSON(response, "phone_number")

            if (phoneNumberVerified == "true" || phoneNumber != null) {
                Util.openSuccessActivity(activity, response)
            } else {
                Util.openErrorActivity(activity, response)
            }
        }

        private fun doPostToken(code: String, callback: TokenCallback) {
            val url = Urls.getTokenExchangeUrl()
            val body: RequestBody = FormBody.Builder()
                .add("client_id", IPConfiguration.getInstance().CLIENT_ID)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", IPConfiguration.getInstance().REDIRECT_URI.toString())
                .add("code", code)
                .build()

            val client = OkHttpClient.Builder().build()
            val request: Request = Request.Builder().url(url).post(body).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val error = e.localizedMessage
                    Log.e(TAG, "doPostToken error: $error")
                    callback.onError(e.localizedMessage ?: "doPostToken - onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()
                        Log.d(TAG, "doPostToken response: $responseBody")

                        if (responseBody != null && response.isSuccessful) {

                            callback.onSuccess(responseBody)
                        } else {
                            callback.onError("doPostToken - error $responseBody")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onError("doPostToken - error ${e.message}")
                    }
                }
            })
        }

    }
}

