package com.ipification.demoapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.ipification.demoapp.Constant
import com.ipification.demoapp.data.TokenInfo
import com.ipification.demoapp.databinding.ActivityHomeBinding
import com.ipification.demoapp.manager.APIManager
import com.ipification.demoapp.manager.TokenCallback
import com.ipification.demoapp.util.*
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.callback.IPificationCallback
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.im.IMService
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

        initFirebase()
    }




    override fun onResume() {
        super.onResume()
        updateState()
    }

    //TODO
    // make sure that state always be updated to your backend.
    private fun updateState() {
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

        // do IM Auth
        doIMAuth(object : IPificationCallback{

            override fun onSuccess(response: AuthResponse) {
                Log.d(TAG, "doIMAuth - code " + response.getCode())

                if(response.getCode() != null){
                    callTokenExchange(response.getCode()!!)
                }else{
                    openErrorActivity("code null")
                }
                enableButton(binding.imButton)
            }
            override fun onError(error: IPificationError) {
                Log.d(TAG,"doIMAuth - error "+ error.error_description)
                if(error.error_description != "USER_CANCELED"){
                    openErrorActivity(error.getErrorMessage())
                }
                enableButton(binding.imButton)
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
        Log.d(TAG,"state " + APIManager.currentState )
        authRequestBuilder.setState(APIManager.currentState)
        authRequestBuilder.setScope("openid ip:phone")
        authRequestBuilder.addQueryParam("channel", "wa viber telegram")

//        ## 4. Edit IM Verification Theme
//        IPificationServices.theme = IMTheme(backgroundColor = Color.parseColor("#FFFFFF"), toolbarTextColor = Color.parseColor("#FFFFFF"), toolbarColor = Color.parseColor("#E35259"),  toolbarTitle="IPification Verification", toolbarVisibility = View.VISIBLE)


//        ## 5. Edit IM Verification Locale
//        IPificationServices.locale = IMLocale(mainTitle= "your_title", description = "your_desc", whatsappText = "Quick Login via Whatsapp", telegramText = "Quick Login via Telegram", viberText = "Quick Login via Viber")

        IPificationServices.startIMAuthentication(this, authRequestBuilder.build(), callback)
    }

    private fun callTokenExchange(code: String) {
        APIManager.doPostToken(code, callback = object: TokenCallback {
            override fun onSuccess(response: String) {
                handleTokenExchangeSuccess(response)
            }
            override fun onError(error: String) {
                handleTokenExchangeError(error)
            }
        })
    }

    private fun handleTokenExchangeSuccess(response: String) {
        try{
            val jObject = JSONObject(response)
            val accessToken = jObject.getString("access_token")
            val tokenInfo = Util.parseAccessToken(accessToken)
            if(tokenInfo != null && tokenInfo.phoneNumberVerified){
                openSuccessActivity(tokenInfo)
            }else{
                openErrorActivity("token null", tokenInfo)
            }
        }catch (error: Exception){
            openErrorActivity(error.localizedMessage ?: "unknown error")
        }
    }

    private fun handleTokenExchangeError(error: String) {
        if(error != "USER_CANCELED"){
            openErrorActivity(error)
        }
    }


    private fun openSuccessActivity(tokenInfo: TokenInfo) {
        val intent = Intent(this, SuccessResultActivity::class.java)
        intent.putExtra("tokenInfo", tokenInfo)
        startActivity(intent)

    }


    private fun openErrorActivity(error: String, tokenInfo: TokenInfo? = null){
        val intent = Intent(this, FailResultActivity::class.java)
        intent.putExtra(
            "error",
            error
        )
        if(tokenInfo != null){
            intent.putExtra("tokenInfo", tokenInfo)
        }
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

