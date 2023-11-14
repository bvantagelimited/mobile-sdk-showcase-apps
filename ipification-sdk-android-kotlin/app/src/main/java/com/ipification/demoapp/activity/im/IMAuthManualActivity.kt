package com.ipification.demoapp.activity.im

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.databinding.ActivityImAuthenticationBinding
import com.ipification.demoapp.manager.IMHelper
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.IMPublicAPIServices
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.android.response.IMResponse
import com.ipification.mobile.sdk.android.utils.IPLogs
import com.ipification.mobile.sdk.im.IMService
import com.ipification.mobile.sdk.im.listener.IMPublicAPICallback
import com.ipification.mobile.sdk.im.util.isPackageInstalled
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

    private fun initIPification() {
        IPConfiguration.getInstance().ENV =
            if (BuildConfig.ENVIRONMENT == "sandbox") IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
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
        disableButtonIfNotInstalled(binding.whatsappBtn, IPConfiguration.getInstance().whatsappPackageName)
        disableButtonIfNotInstalled(binding.telegramBtn, IPConfiguration.getInstance().telegramPackageName)
        disableButtonIfNotInstalled(binding.viberBtn, IPConfiguration.getInstance().viberPackageName)
    }

    private fun disableButtonIfNotInstalled(button: View, packageName: String) {
        if (!packageManager.isPackageInstalled(packageName)) {
            button.isEnabled = false
            button.alpha = 0.3f
        }
    }

    // Update onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IMService.onActivityResult(requestCode, resultCode, data)
    }

    private fun initFirebase() {
        IPLogs.getInstance().LOG += "init FCM \n"
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                handleFirebaseTokenError(task)
                return@OnCompleteListener
            }
            handleFirebaseTokenSuccess(task)
        })
    }

    private fun handleFirebaseTokenError(task: Task<String>) {
        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
        IPLogs.getInstance().LOG += "Fetching FCM registration token failed \n"
        try {
            IPLogs.getInstance().LOG += task.exception?.message?.substring(0, 100)
        } catch (e: Exception) {
            print(e.message)
        }
    }

    private fun handleFirebaseTokenSuccess(task: Task<String>) {
        val token = task.result.toString()
        IMHelper.deviceToken = token
        IPLogs.getInstance().LOG += "[initFirebas] get device Token ${token} \n"
    }

    private fun doIMFlow(channel: String) {
        updateStateAndDeviceToken()

        val callback = object : IMPublicAPICallback {
            override fun onSuccess(imResponse: IMResponse?, ipResponse: AuthResponse?) {
                handleIMFlowSuccess(imResponse)
            }

            override fun onError(error: IPificationError) {
                handleIMFlowError(error)
            }
        }
        // do IM Auth
        doIMAuth(channel, callback)
    }

    private fun handleIMFlowSuccess(imResponse: IMResponse?) {
        Log.d("IMResponse", "sessionId:" + imResponse?.sessionInfo?.sessionId)
        Log.d("IMResponse", "whatsappLink: " + imResponse?.sessionInfo?.waLink)
        Log.d("IMResponse", "telegramLink: " + imResponse?.sessionInfo?.telegramLink)
        Log.d("IMResponse", "viberLink: " + imResponse?.sessionInfo?.viberLink)
        Log.d("IMResponse", "completeSessionUrl: " + imResponse?.sessionInfo?.completeSessionUrl)
    }

    private fun handleIMFlowError(error: IPificationError) {
        Log.d(TAG, "doIMAuth - error " + error.error_description)
        Util.openErrorActivity(this@IMAuthManualActivity, error.getErrorMessage())
    }

    private fun doIMAuth(channel: String, callback: IMPublicAPICallback) {
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.setState(IMHelper.currentState)
        authRequestBuilder.setScope("openid ip:phone")
        authRequestBuilder.addQueryParam("channel", channel)
        IMPublicAPIServices.startAuthentication(this, authRequestBuilder.build(), callback)
    }

    // update state and device token to client server
    private fun updateStateAndDeviceToken() {
        IMHelper.currentState = IPificationServices.generateState()
        IMHelper.registerDevice(IMHelper.deviceToken, IMHelper.currentState)
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


