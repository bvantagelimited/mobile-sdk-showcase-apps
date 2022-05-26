package com.ipification.demoapp.manager;

import android.app.Activity
import android.content.Context
import android.util.Log
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
                .add("client_secret", Constant.CLIENT_SECRET)
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
                    callback.result(null, e.localizedMessage ?: "doPostToken - onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body!!.string()
                        Log.d(TAG, "doPostToken response : $responseBody")
                        if (response.isSuccessful) {
                            val accessToken = Util.parseAccessTokenFromJSON(responseBody)
                            if(accessToken != null){
                                //get user info via API
                                postUserInfo(accessToken, callback)
                            }else{
                                // error
                                callback.result(null, responseBody)
                            }
                        } else {
                            callback.result(null, "doPostToken - error $responseBody")
                        }

                    } catch(e: Exception){
                        e.printStackTrace()
                        callback.result(null, "doPostToken - error ${e.message}")
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
                    callback.result(null, e.localizedMessage ?: "postUserInfo - onFailure")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val responseBody = response.body!!.string()
                        Log.d(TAG, "postUserInfo response : $responseBody")
                        if (response.isSuccessful) {
                            callback.result(responseBody, null)
                        } else {
                            callback.result(null, responseBody)
                        }

                    }catch(e: Exception){
                        e.printStackTrace()
                        callback.result(null, e.localizedMessage)
                    }
                }
            })

        }

        fun registerDevice(dToken: String? = deviceToken, state: String? = currentState) {
            if(dToken?.isEmpty() != false || state.isNullOrEmpty()){
                Log.d(TAG, "deviceToken or state null. failed")
                return
            }
            val JSON = ("application/json; charset=utf-8").toMediaType();
            val url = Constant.DEVICE_TOKEN_REGISTRATION_URL
            val json =
                "{\"device_id\":\"$currentState\",\"device_token\":\"${deviceToken}\", \"device_type\":\"android\"}";
            Log.d(TAG, "registerDevice $json")
            val requestBody = json.toRequestBody(JSON)

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

        fun checkCoverage(context: Context, callback: IPCheckCoverageCallback){
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
            IPificationServices.startCheckCoverage(context = context, callback = coverageCallback)
        }


        fun callAuthorization(phoneNumber: String, activity: Activity, callback: IPAuthorizationCallback){

            val authRequestBuilder = AuthRequest.Builder()
            authRequestBuilder.setScope("openid ip:phone_verify")
            authRequestBuilder.addQueryParam("login_hint", phoneNumber)
            authRequestBuilder.setState(currentState) // to show notification

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

