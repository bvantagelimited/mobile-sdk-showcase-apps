package com.ipification.demoapp.manager;

import android.util.Log
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.ipification.demoapp.Constant
import com.ipification.mobile.sdk.android.IPConfiguration
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class APIManager {

    companion object Factory {
        private const val TAG = "ApiManager"
        var currentState : String? = null
        fun doPostToken(code: String, callback: TokenCallback) {
            val url = Constant.TOKEN_URL
            val body: RequestBody = FormBody.Builder()
                .add("client_id", IPConfiguration.getInstance().CLIENT_ID)
                .add("grant_type", "authorization_code")
                .add("client_secret", Constant.CLIENT_SECRET)
                .add("redirect_uri", IPConfiguration.getInstance().REDIRECT_URI.toString())
                .add("code", code)
                .build();

            val client = OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor()).build()
            val request: Request = Request.Builder().url(url).post(body)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val error = e.localizedMessage
                    Log.e(TAG, "doPostToken error : $error")
                    callback.onError(e.localizedMessage ?: "error")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body!!.string()
                        if (response.isSuccessful) {
                            callback.onSuccess(responseBody)
                        } else {
                            callback.onError(responseBody)
                        }
                        Log.d(TAG, "doPostToken response : $responseBody")
                    }catch(e: Exception){
                        e.printStackTrace()
                    }
                }
            })

        }

        fun registerDevice(deviceToken: String, state: String? = currentState) {
            if(deviceToken.isEmpty() || state.isNullOrEmpty()){
                Log.d(TAG, "deviceToken or state null. failed")
                return
            }
            val JSON = ("application/json; charset=utf-8").toMediaType();
            val url = Constant.DEVICE_TOKEN_REGISTRATION_URL
            val json =
                "{\"device_id\":\"$currentState\",\"device_token\":\"${deviceToken}\", \"device_type\":\"android\"}";
            Log.d(TAG, "registerDevice $json")
            val requestBody = json.toRequestBody(JSON)

            val client = OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor()).build()
            val request: Request = Request.Builder().url(url).post(requestBody)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d(TAG, "registered failed")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG, "registered successfully - " + response.body?.string())
                }

            })
        }
    }
}

interface TokenCallback {
    fun onError(error: String)
    fun onSuccess(response: String)
}