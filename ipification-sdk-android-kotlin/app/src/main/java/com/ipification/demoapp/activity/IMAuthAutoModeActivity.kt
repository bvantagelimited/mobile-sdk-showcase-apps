package com.ipification.demoapp.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.databinding.ActivityImAutomodeAuthenticationBinding
import com.ipification.demoapp.manager.APIManager
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.callback.IPificationCallback
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.android.utils.IPConstant
import com.ipification.mobile.sdk.im.IMService


// IM Authentication (Auto Mode)
// See :  https://developer.ipification.com/#/android-automode/latest/
class IMAuthAutoModeActivity : AppCompatActivity() {
    private val TAG: String = "IMAuthActivity"

    lateinit var binding: ActivityImAutomodeAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImAutomodeAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initIPification()
        initView()
    }

    private fun initIPification(){
        IPConfiguration.getInstance().ENV = if(BuildConfig.ENVIRONMENT == "sandbox" ) IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
        IPConfiguration.getInstance().CLIENT_ID = BuildConfig.CLIENT_ID
        IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI) // this uri should be do S2S to exchange token

        //enable Auto Mode
        IPConfiguration.getInstance().IM_AUTO_MODE = true
        IPConfiguration.getInstance().IM_PRIORITY_APP_LIST = arrayOf("telegram","viber","wa")

    }

    private fun initView() {
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        supportActionBar?.title = "IM - AutoMode - ${BuildConfig.VERSION_NAME}"
        // init FCM
        initFirebase()

        binding.imButton.setOnClickListener {
            doIMFlow()
        }
    }



    //Update onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IMService.onActivityResult(requestCode, resultCode, data)
    }


    private fun initFirebase() {
        IPConstant.getInstance().LOG += "init FCM \n"

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                IPConstant.getInstance().LOG += "Fetching FCM registration token failed \n"
                try {
                    IPConstant.getInstance().LOG += task.exception?.message?.substring(0, 100)
                }catch (e: Exception){

                }
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result.toString()
            APIManager.deviceToken = token
            IPConstant.getInstance().LOG += "[initFirebas] get device Token ${token} \n"
        })
    }

    private fun doIMFlow() {
        updateStateAndDeviceToken()

        val callback = object : IPificationCallback {
            override fun onSuccess(response: AuthResponse) {
                Util.callLoginAPI(this@IMAuthAutoModeActivity, APIManager.currentState!!)
            }
            override fun onError(error: IPificationError) {
                Log.d(TAG,"doIMAuth - error "+ error.error_description)
                Util.openErrorActivity(this@IMAuthAutoModeActivity, error.getErrorMessage())
            }
        }
        // do IM Auth
        doIMAuth(callback)
    }

    private fun doIMAuth(callback: IPificationCallback) {
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.setState(APIManager.currentState)
        authRequestBuilder.setScope("openid ip:phone")
        IPificationServices.startAuthentication(this, authRequestBuilder.build(), callback)

    }
    // update state and device token to client server
    private fun updateStateAndDeviceToken() {
        APIManager.currentState = IPificationServices.generateState()
        APIManager.registerDevice(APIManager.deviceToken, APIManager.currentState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // API 5+ solution
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

