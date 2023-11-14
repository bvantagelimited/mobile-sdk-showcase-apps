package com.ipification.demoapp.manager

import android.util.Log
import com.ipification.demoapp.Urls
import com.ipification.demoapp.callback.TokenCallback
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
class IMHelper {
    companion object Factory {
        private const val TAG = "IMHelper"
        var currentState: String? = null
        internal var deviceToken: String? = null
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaTypeOrNull()

        fun registerDevice(dToken: String? = deviceToken, state: String? = currentState) {
            if (dToken.isNullOrEmpty() || state.isNullOrEmpty()) {
                Log.d(TAG, "deviceToken or state null. failed")
                return
            }

            val url = Urls.DEVICE_TOKEN_REGISTRATION_URL
            val json = "{\"device_id\":\"$currentState\",\"device_token\":\"${deviceToken}\", \"device_type\":\"android\"}"
            Log.d(TAG, "registerDevice $json")

            val requestBody = json.toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder().url(url).post(requestBody).build()

            val client = OkHttpClient.Builder().build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, "Registration failed", e)
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG, "Registration successful - ${response.body?.string()}")
                }
            })
        }

        fun signIn(state: String, callback: TokenCallback) {
            val url = Urls.AUTOMODE_SIGN_IN_URL
            val jsonObject = JSONObject().apply {
                put("state", state)
            }

            val body = jsonObject.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder().url(url).post(body).build()

            val client = OkHttpClient.Builder().build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val error = e.localizedMessage
                    Log.e(TAG, "SignIn onFailure: $error", e)
                    callback.onError(error ?: "postUserInfo - onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()

                        if (response.isSuccessful) {
                            callback.onSuccess(responseBody ?: "")
                        } else {
                            callback.onError(responseBody ?: "")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing response", e)
                        callback.onError(e.localizedMessage ?: "Error processing response")
                    }
                }
            })
        }
    }
}