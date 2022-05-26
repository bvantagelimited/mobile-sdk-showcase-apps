package com.ipification.demoapp.activity

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.ipification.demoapp.Constant
import com.ipification.demoapp.callback.TokenCallback
import com.ipification.demoapp.data.TokenInfo
import com.ipification.demoapp.databinding.ActivityHomeBinding
import com.ipification.demoapp.manager.APIManager
import com.ipification.demoapp.util.*
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.callback.IPificationCallback
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.im.IMLocale
import com.ipification.mobile.sdk.im.IMService
import com.ipification.mobile.sdk.im.IMTheme
import org.json.JSONObject
import java.util.*

class HomeActivity : AppCompatActivity() {
    private val TAG: String = "HomeActivity"

    lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ipButton.setOnClickListener {
            doIPFlow()
        }
        binding.imButton.setOnClickListener {
            doIMFlow()
        }
        initIPification()
        initFirebase()
    }

    private fun initIPification() {
        IPConfiguration.getInstance().COVERAGE_URL = Uri.parse(Constant.CHECK_COVERAGE_URL)
        IPConfiguration.getInstance().AUTHORIZATION_URL = Uri.parse(Constant.AUTH_URL)
        IPConfiguration.getInstance().CLIENT_ID = Constant.CLIENT_ID
        IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(Constant.REDIRECT_URI)
    }


    //TODO
    private fun registerDevice() {
        APIManager.currentState = IPificationServices.generateState()
        APIManager.registerDevice(APIManager.deviceToken, APIManager.currentState)
    }

    // 4. Update onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IMService.onActivityResult(requestCode, resultCode, data)
    }



    private fun doIPFlow() {
        val intent = Intent(applicationContext, PhoneVerifyActivity::class.java)
        startActivity(intent)
    }

    private fun doIMFlow() {
        // disable button
        disableButton(binding.imButton)
        // register state and device token
        registerDevice()
        // do IM Auth
        doIMAuth(object : IPificationCallback{

            override fun onSuccess(response: AuthResponse) {
                Log.d(TAG, "doIMAuth - code " + response.getCode())

                if(response.getCode() != null){
                    callTokenExchange(response.getCode()!!)
                }else{
                    openErrorActivity("code is empty ${response.getErrorMessage()}")
                }
                enableButton(binding.imButton)
            }
            override fun onError(error: IPificationError) {
                Log.d(TAG,"doIMAuth - error "+ error.error_description)
                openErrorActivity(error.getErrorMessage())
                enableButton(binding.imButton)
            }

            override fun onIMCancel() {
                enableButton(binding.imButton)
            }
        })
    }

    private fun callTokenExchange(code: String) {
        APIManager.doPostToken(code, callback = object : TokenCallback{
            override fun result(response: String?, errorMessage: String?) {
                if(response != null){
                    val phoneNumberVerified = Util.parseUserInfoJSON(response, "phone_number_verified")
                    val phoneNumber = Util.parseUserInfoJSON(response, "phone_number")
                    if(phoneNumberVerified == "true" || phoneNumber != null){
                        openSuccessActivity(response)
                    }else{
                        openErrorActivity(response)
                    }
                }else{
                    openErrorActivity(errorMessage ?: "error")
                }
            }

        })
    }


    private fun initFirebase() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result.toString()
            APIManager.deviceToken = token
            APIManager.registerDevice()
        })
    }


    private fun doIMAuth(callback: IPificationCallback) {
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.setState(APIManager.currentState)
        authRequestBuilder.setScope("openid ip:phone")
        authRequestBuilder.addQueryParam("channel", "wa viber telegram")

//        ## 4. Edit IM Verification Theme
//        IPificationServices.theme = IMTheme(backgroundColor = Color.parseColor("#FFFFFF"), toolbarTextColor = Color.parseColor("#FFFFFF"), toolbarColor = Color.parseColor("#E35259"))


//        ## 5. Edit IM Verification Locale
//        IPificationServices.locale = IMLocale(mainTitle= "your_title", description = "your_desc", whatsappText = "Quick Login via Whatsapp", telegramText = "Quick Login via Telegram", viberText = "Quick Login via Viber")

        IPificationServices.startIMAuthentication(this, authRequestBuilder.build(), callback)
    }





    private fun openSuccessActivity(responseStr: String) {
        val intent = Intent(this, SuccessResultActivity::class.java)
        intent.putExtra("responseStr", responseStr)
        startActivity(intent)

    }


    private fun openErrorActivity(error: String){
        val intent = Intent(this, FailResultActivity::class.java)
        intent.putExtra(
            "error",
            error
        )

        startActivity(intent)
    }


    private fun enableButton(view: View) {
        view.post {
            view.alpha = 1f
            view.isEnabled = true
        }
    }

    private fun disableButton(view: View) {
        view.post {
            view.alpha = 0.2f
            view.isEnabled = false
        }
    }

}

