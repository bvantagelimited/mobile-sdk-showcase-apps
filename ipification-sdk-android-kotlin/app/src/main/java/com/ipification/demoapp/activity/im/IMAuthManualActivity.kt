package com.ipification.demoapp.activity.im

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.databinding.ActivityImAuthenticationBinding
import com.ipification.demoapp.manager.APIManager
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.IMPublicAPIServices
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.android.response.IMResponse
import com.ipification.mobile.sdk.android.utils.IPConstant
import com.ipification.mobile.sdk.im.IMService
import com.ipification.mobile.sdk.im.listener.IMPublicAPICallback
import com.ipification.mobile.sdk.im.util.isPackageInstalled


// IM Authentication (Manually Implementation)
// See :  https://developer.ipification.com/#/android/latest/?id=_3-instant-message-im-authentication-flow-manual-implementation
class IMAuthManualActivity : AppCompatActivity() {
    private val TAG: String = "IMAuthActivity"

    lateinit var binding: ActivityImAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImAuthenticationBinding.inflate(layoutInflater)

        setContentView(binding.root)
        initIPification()
        initView()
    }

    private fun initIPification(){
        IPConfiguration.getInstance().ENV = if(BuildConfig.ENVIRONMENT == "sandbox" ) IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
        IPConfiguration.getInstance().CLIENT_ID = BuildConfig.CLIENT_ID
        IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI)
    }

    private fun initView() {
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "IM - Manual Implementation - ${BuildConfig.VERSION_NAME}"
        // init FCM
        initFirebase()
        // check and show IM
        checkAndShowIMButtons()

        binding.whatsappBtn.setOnClickListener {
            doIMFlow("wa")
        }
        binding.viberBtn.setOnClickListener {
            doIMFlow("viber")
        }
        binding.telegramBtn.setOnClickListener {
            doIMFlow("telegram")
        }
    }

    private fun checkAndShowIMButtons() {
        if(!packageManager.isPackageInstalled(IPConfiguration.getInstance().whatsappPackageName))
        {
            binding.whatsappBtn.isEnabled = false
            binding.whatsappBtn.alpha =  0.3f

        }
        if(!(packageManager.isPackageInstalled(IPConfiguration.getInstance().telegramPackageName) || packageManager.isPackageInstalled(IPConfiguration.getInstance().telegramWebPackageName)))
        {
            binding.telegramBtn.isEnabled = false
            binding.telegramBtn.alpha =  0.3f

        }
        if(!packageManager.isPackageInstalled(IPConfiguration.getInstance().viberPackageName))
        {
            binding.viberBtn.isEnabled = false
            binding.viberBtn.alpha =  0.3f

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

    private fun doIMFlow(channel: String) {
        updateStateAndDeviceToken()

        val callback = object : IMPublicAPICallback {
            override fun onSuccess(imResponse: IMResponse?, ipResponse: AuthResponse?) {
                Log.d("IMResponse", "sessionId:"+ imResponse?.sessionInfo?.sessionId)
                Log.d("IMResponse", "whatsappLink: "+ imResponse?.sessionInfo?.waLink)
                Log.d("IMResponse", "telegramLink: "+ imResponse?.sessionInfo?.telegramLink)
                Log.d("IMResponse", "viberLink: "+ imResponse?.sessionInfo?.viberLink)
                Log.d("IMResponse", "completeSessionUrl: "+ imResponse?.sessionInfo?.completeSessionUrl)
            }
            override fun onError(error: IPificationError) {
                Log.d(TAG,"doIMAuth - error "+ error.error_description)
                Util.openErrorActivity(this@IMAuthManualActivity, error.getErrorMessage())
            }
        }
        // do IM Auth
        doIMAuth(channel, callback)
    }

    private fun doIMAuth(channel: String, callback: IMPublicAPICallback) {

        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.setState(APIManager.currentState)
        authRequestBuilder.setScope("openid ip:phone")
        authRequestBuilder.addQueryParam("channel", channel)
        IMPublicAPIServices.startAuthentication(this, authRequestBuilder.build(), callback)

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

