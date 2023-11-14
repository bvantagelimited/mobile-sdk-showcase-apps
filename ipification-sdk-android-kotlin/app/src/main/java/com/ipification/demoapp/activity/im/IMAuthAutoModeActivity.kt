package com.ipification.demoapp.activity.im

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.databinding.ActivityImAutomodeAuthenticationBinding
import com.ipification.demoapp.manager.IMHelper
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.callback.IPificationCallback
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.android.utils.IPLogs
import com.ipification.mobile.sdk.im.IMService
import com.ipification.mobile.sdk.im.IMServices

class IMAuthAutoModeActivity : AppCompatActivity() {
    private val TAG: String = "IMAutoAuth"
    lateinit var binding: ActivityImAutomodeAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImAutomodeAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initIPification()
        initView()
    }

    private fun initIPification() {
        IPConfiguration.getInstance().ENV =
            if (BuildConfig.ENVIRONMENT == "sandbox") IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
        IPConfiguration.getInstance().CLIENT_ID = BuildConfig.CLIENT_ID
        IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI)  // this uri should be used for S2S to exchange token

        // Enable Auto Mode
        IPConfiguration.getInstance().IM_AUTO_MODE = true
        IPConfiguration.getInstance().IM_PRIORITY_APP_LIST = arrayOf("wa")
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
        }
    }

    private fun handleFirebaseTokenSuccess(task: Task<String>) {
        val token = task.result.toString()
        IMHelper.deviceToken = token
        IPLogs.getInstance().LOG += "[initFirebas] get device Token ${token} \n"
    }

    private fun doIMFlow() {
        updateStateAndDeviceToken()

        val callback = object : IPificationCallback {
            override fun onSuccess(response: AuthResponse) {
                handleIMFlowSuccess(response)
            }

            override fun onError(error: IPificationError) {
                handleIMFlowError(error)
            }
        }
        // do IM Auth
        doIMAuth(callback)
    }

    private fun handleIMFlowSuccess(response: AuthResponse) {
        Log.d("aaa", "eeee" + response.getState())
        Util.callLoginAPI(this@IMAuthAutoModeActivity, IMHelper.currentState!!)
    }

    private fun handleIMFlowError(error: IPificationError) {
        Log.d(TAG, "doIMAuth - error " + error.error_description)
        Util.openErrorActivity(this@IMAuthAutoModeActivity, error.getErrorMessage())
    }

    private fun doIMAuth(callback: IPificationCallback) {
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.setState(IMHelper.currentState)
        authRequestBuilder.setScope("openid ip:phone")
        IMServices.startAuthentication(this, authRequestBuilder.build(), callback)
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


