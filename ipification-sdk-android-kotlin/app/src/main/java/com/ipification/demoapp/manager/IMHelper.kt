package com.ipification.demoapp.manager

import android.util.Log
import com.ipification.demoapp.Constant
import com.ipification.demoapp.callback.TokenCallback
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class IMHelper {

    companion object Factory {
        private const val TAG = "IMHelper"
        var currentState : String? = null
        internal var deviceToken : String? = null

        fun registerDevice(dToken: String? = deviceToken, state: String? = currentState) {
            if(dToken?.isEmpty() != false || state.isNullOrEmpty()){
                Log.d(TAG, "deviceToken or state null. failed")
                return
            }
            val JSON = "application/json; charset=utf-8".toMediaTypeOrNull();
//            val JSON = MediaType.parse("application/json; charset=utf-8")
            val url = Constant.DEVICE_TOKEN_REGISTRATION_URL
            val json =
                "{\"device_id\":\"$currentState\",\"device_token\":\"${deviceToken}\", \"device_type\":\"android\"}";
            Log.d(TAG, "registerDevice $json")
            val requestBody = json.toRequestBody(JSON)
//            val requestBody = RequestBody.create(JSON, json)

            val client = OkHttpClient.Builder().build()
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

        fun signIn(state: String, callback: TokenCallback) {

            val url = Constant.AUTOMODE_SIGN_IN_URL
            val mediaType = "application/json; charset=utf-8".toMediaType()
//            val JSON = MediaType.parse("application/json; charset=utf-8")

            val jsonObject = JSONObject()
            try {
                jsonObject.put("state", state)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val body = jsonObject.toString().toRequestBody(mediaType)
//            val body = RequestBody.create(JSON, jsonObject.toString())



            val client = OkHttpClient.Builder().build()
            val request: Request = Request.Builder().url(url).post(body)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val error = e.localizedMessage
                    Log.e(TAG, "signIn onFailure : $error")
                    callback.onError(e.localizedMessage ?: "postUserInfo - onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body?.string()

                        if(responseBody != null){
                            if (response.isSuccessful) {
                                callback.onSuccess(responseBody)
                            } else {
                                callback.onError(responseBody)
                            }
                        }
                    }catch(e: Exception){
                        e.printStackTrace()
                        callback.onError(e.localizedMessage)
                    }
                }
            })

        }
    }
}