package com.ipification.demoapp.manager;

import android.app.Activity
import android.content.Context
import android.util.Log
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.Constant
import com.ipification.demoapp.callback.IPAuthorizationCallback
import com.ipification.demoapp.callback.IPCheckCoverageCallback
import com.ipification.demoapp.callback.TokenCallback
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.callback.CellularCallback
import com.ipification.mobile.sdk.android.callback.IPificationCallback
import com.ipification.mobile.sdk.android.exception.CellularException
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.android.response.CoverageResponse
import okhttp3.*

import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class APIManager {

    companion object Factory {
        private const val TAG = "ApiManager"
        var currentState : String? = null
        var deviceToken : String? = null
        fun doPostToken(code: String, callback: TokenCallback) {
            val url = Constant.EXCHANGE_TOKEN_URL
            val body: RequestBody = FormBody.Builder()
                .add("client_id", IPConfiguration.getInstance().CLIENT_ID)
                .add("grant_type", "authorization_code")
                .add("client_secret", BuildConfig.CLIENT_SECRET)
                .add("redirect_uri", IPConfiguration.getInstance().REDIRECT_URI.toString())
                .add("code", code)
                .build();

            val client = OkHttpClient.Builder().build()
            val request: Request = Request.Builder().url(url).post(body)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val error = e.localizedMessage
                    Log.e(TAG, "doPostToken error : $error")
                    callback.onError(e.localizedMessage ?: "doPostToken - onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body().string()
                        Log.d(TAG, "doPostToken response : $responseBody")
                        if (response.isSuccessful) {
                            val accessToken = Util.parseAccessTokenFromJSON(responseBody)
                            if(accessToken != null){
                                //get user info via API
                                postUserInfo(accessToken, callback)
                            }else{
                                // error
                                callback.onError(responseBody)
                            }
                        } else {
                            callback.onError("doPostToken - error $responseBody")
                        }

                    } catch(e: Exception){
                        e.printStackTrace()
                        callback.onError("doPostToken - error ${e.message}")
                    }
                }
            })

        }

        fun postUserInfo(accessToken: String, callback: TokenCallback) {
            val url = Constant.USER_INFO_URL
            val body: RequestBody = FormBody.Builder()
                .add("access_token", accessToken)

                .build();

            val client = OkHttpClient.Builder().build()
            val request: Request = Request.Builder().url(url).header("Content-Type","application/x-www-form-urlencoded").post(body)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    val error = e.localizedMessage
                    Log.e(TAG, "postUserInfo onFailure : $error")
                    callback.onError(e.localizedMessage ?: "postUserInfo - onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body().string()
                        Log.d(TAG, "postUserInfo response : $responseBody")
                        if (response.isSuccessful) {
                            callback.onSuccess(responseBody)
                        } else {
                            callback.onError(responseBody)
                        }

                    }catch(e: Exception){
                        e.printStackTrace()
                        callback.onError(e.localizedMessage)
                    }
                }
            })
        }


        fun signIn(state: String, callback: TokenCallback) {
            val url = Constant.AUTOMODE_SIGN_IN_URL
//            val mediaType = "application/json; charset=utf-8".toMediaType()
            val JSON = MediaType.parse("application/json; charset=utf-8");

            val jsonObject = JSONObject()
            try {
                jsonObject.put("state", state)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

//            val body = jsonObject.toString().toRequestBody(mediaType)
            val body = RequestBody.create(JSON, jsonObject.toString())



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
                        val responseBody = response.body().string()
                        Log.d(TAG, "signIn response : $responseBody")
                        if (response.isSuccessful) {
                            callback.onSuccess(responseBody)
                        } else {
                            callback.onError(responseBody)
                        }

                    }catch(e: Exception){
                        e.printStackTrace()
                        callback.onError(e.localizedMessage)
                    }
                }
            })

        }
        fun registerDevice(dToken: String? = deviceToken, state: String? = currentState) {
            if(dToken?.isEmpty() != false || state.isNullOrEmpty()){
                Log.d(TAG, "deviceToken or state null. failed")
                return
            }
            val JSON = MediaType.parse("application/json; charset=utf-8");
            val url = Constant.DEVICE_TOKEN_REGISTRATION_URL
            val json =
                "{\"device_id\":\"$currentState\",\"device_token\":\"${deviceToken}\", \"device_type\":\"android\"}";
            Log.d(TAG, "registerDevice $json")
//            val requestBody = json.toRequestBody(JSON)
            val requestBody = RequestBody.create(JSON, json)

            val client = OkHttpClient.Builder().build()
            val request: Request = Request.Builder().url(url).post(requestBody)
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d(TAG, "registered failed")
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.d(TAG, "registered successfully - " + response.body()?.string())
                }

            })
        }

        fun checkCoverage(phoneNumber: String, context: Context, callback: IPCheckCoverageCallback){
            val coverageCallback = object : CellularCallback<CoverageResponse>
            {
                override fun onSuccess(response: CoverageResponse) {
                    if(response.isAvailable()) {
                        // supported Telco. Collect User Phone Number then Call Authorization API
                        callback.result(true, response.getOperatorCode(), null)
                    } else {
                        // unsupported Telco. Fallback to another authentication service flow
                        callback.result(false, null, "not supported")
                    }
                }
                override fun onError(error: CellularException) {
                    Log.d(TAG, "checkCoverage - error : " + error.responseCode + " - " + error.getErrorMessage())
                    // error, handle it with another authentication service flow
                    callback.result(false, null, error.getErrorMessage())
                }
            }
            IPificationServices.Factory.startCheckCoverage(phoneNumber, context = context, callback = coverageCallback)
        }


        fun callAuthorization(phoneNumber: String, activity: Activity, callback: IPAuthorizationCallback){
            val authRequestBuilder = AuthRequest.Builder()
            authRequestBuilder.setScope("openid ip:phone_verify")
            authRequestBuilder.addQueryParam("login_hint", phoneNumber)

            val authRequest = authRequestBuilder.build()
            IPificationServices.startAuthentication(activity, authRequest, object:
                IPificationCallback {
                override fun onSuccess(response: AuthResponse) {
                    //check auth_code
                    val code = response.getCode()
                    if(code != null){
                        callback.result(code, null)

                    }else{
                        callback.result(null, response.responseData)

                    }

                }
                override fun onError(error: IPificationError) {
                    callback.result(null, error.getErrorMessage())
                }
            })
        }


    }

}

