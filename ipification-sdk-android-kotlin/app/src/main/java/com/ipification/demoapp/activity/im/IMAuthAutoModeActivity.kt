package com.ipification.demoapp.activity.im

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.manager.IMHelper
import com.ipification.demoapp.ui.components.IMButton
import com.ipification.demoapp.ui.components.IPificationTopBar
import com.ipification.demoapp.ui.theme.IPificationTheme
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

class IMAuthAutoModeActivity : ComponentActivity() {
    private val TAG: String = "IMAutoAuth"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIPification()
        initFirebase()
        
        setContent {
            IPificationTheme {
                IMAutoModeScreen(
                    onBackClick = { finish() }
                )
            }
        }
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

    @Composable
    fun IMAutoModeScreen(onBackClick: () -> Unit) {
        val context = LocalContext.current
        val activity = context as? IMAuthAutoModeActivity
        var isLoading by remember { mutableStateOf(false) }
        
        Scaffold(
            topBar = {
                IPificationTopBar(
                    title = "IM - AutoMode - ${BuildConfig.VERSION_NAME}",
                    onBackClick = onBackClick
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                IMButton(
                    text = "Login with Instant Message",
                    onClick = {
                        isLoading = true
                        activity?.doIMFlow {
                            isLoading = false
                        }
                    },
                    modifier = Modifier.width(300.dp),
                    enabled = !isLoading
                )
            }
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

    fun doIMFlow(onComplete: () -> Unit = {}) {
        updateStateAndDeviceToken()

        val callback = object : IPificationCallback {
            override fun onSuccess(response: AuthResponse) {
                handleIMFlowSuccess(response)
                onComplete()
            }

            override fun onError(error: IPificationError) {
                handleIMFlowError(error)
                onComplete()
            }
        }
        // do IM Auth
        doIMAuth(callback)
    }

    private fun handleIMFlowSuccess(response: AuthResponse) {
        Util.callLoginAPI(this@IMAuthAutoModeActivity, response.getState())
    }

    private fun handleIMFlowError(error: IPificationError) {
        Log.d(TAG, "doIMAuth - error " + error.error_description)
        Util.openErrorActivity(this@IMAuthAutoModeActivity, error.getErrorMessage())
    }

    private fun doIMAuth(callback: IPificationCallback) {
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.setState(IMHelper.currentState)
        authRequestBuilder.setScope("openid ip:phone")
        // Add login_hint to satisfy SDK requirement
//        val loginHint = if (BuildConfig.ENVIRONMENT == "sandbox") "+999123456789" else ""
//        authRequestBuilder.addQueryParam("login_hint", loginHint)
        IMServices.startAuthentication(this, authRequestBuilder.build(), callback)
    }

    // update state and device token to client server
    private fun updateStateAndDeviceToken() {
        IMHelper.currentState = IPificationServices.generateState()
        IMHelper.registerDevice(IMHelper.deviceToken, IMHelper.currentState)
    }
}


