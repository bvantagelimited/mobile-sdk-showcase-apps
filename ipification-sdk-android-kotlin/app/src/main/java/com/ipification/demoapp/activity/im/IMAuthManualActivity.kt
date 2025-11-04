package com.ipification.demoapp.activity.im

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.R
import com.ipification.demoapp.manager.IMHelper
import com.ipification.demoapp.ui.components.IPificationTopBar
import com.ipification.demoapp.ui.theme.IPDarkGray
import com.ipification.demoapp.ui.theme.IPificationTheme
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

class IMAuthManualActivity : ComponentActivity() {
    private val TAG: String = "IMAuthActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIPification()
        initFirebase()
        
        setContent {
            IPificationTheme {
                IMManualAuthScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun initIPification() {
        IPConfiguration.getInstance().ENV =
            if (BuildConfig.ENVIRONMENT == "sandbox") IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
        IPConfiguration.getInstance().CLIENT_ID = BuildConfig.CLIENT_ID
        IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI)
    }

    @Composable
    fun IMManualAuthScreen(onBackClick: () -> Unit) {
        val context = LocalContext.current
        val activity = context as? IMAuthManualActivity
        val packageManager = context.packageManager
        
        // Check which IM apps are installed
        val isWhatsAppInstalled = remember { 
            packageManager.isPackageInstalled(IPConfiguration.getInstance().whatsappPackageName) 
        }
        val isTelegramInstalled = remember { 
            packageManager.isPackageInstalled(IPConfiguration.getInstance().telegramPackageName)
        }
        val isViberInstalled = remember { 
            packageManager.isPackageInstalled(IPConfiguration.getInstance().viberPackageName) 
        }
        
        Scaffold(
            topBar = {
                IPificationTopBar(
                    title = "IM - Manual Implementation - ${BuildConfig.VERSION_NAME}",
                    onBackClick = onBackClick
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                
                Text(
                    text = "Please tap on the preferred messaging app then follow instruction on screen",
                    fontSize = 18.sp,
                    color = IPDarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // WhatsApp
                    Image(
                        painter = painterResource(id = R.drawable.whatsapp),
                        contentDescription = "WhatsApp",
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(enabled = isWhatsAppInstalled) {
                                activity?.doIMFlow("wa")
                            }
                            .then(
                                if (!isWhatsAppInstalled) Modifier else Modifier
                            ),
                        alpha = if (isWhatsAppInstalled) 1f else 0.3f
                    )
                    
                    // Telegram
                    Image(
                        painter = painterResource(id = R.drawable.telegram),
                        contentDescription = "Telegram",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(3.dp)
                            .clickable(enabled = isTelegramInstalled) {
                                activity?.doIMFlow("telegram")
                            }
                            .then(
                                if (!isTelegramInstalled) Modifier else Modifier
                            ),
                        alpha = if (isTelegramInstalled) 1f else 0.3f
                    )
                    
                    // Viber
                    Image(
                        painter = painterResource(id = R.drawable.viber),
                        contentDescription = "Viber",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(10.dp)
                            .clickable(enabled = isViberInstalled) {
                                activity?.doIMFlow("viber")
                            }
                            .then(
                                if (!isViberInstalled) Modifier else Modifier
                            ),
                        alpha = if (isViberInstalled) 1f else 0.3f
                    )
                }
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
            print(e.message)
        }
    }

    private fun handleFirebaseTokenSuccess(task: Task<String>) {
        val token = task.result.toString()
        IMHelper.deviceToken = token
        IPLogs.getInstance().LOG += "[initFirebas] get device Token ${token} \n"
    }

    fun doIMFlow(channel: String) {
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
        // Add login_hint to satisfy SDK requirement
//        val loginHint = if (BuildConfig.ENVIRONMENT == "sandbox") "+999123456789" else ""
//        authRequestBuilder.addQueryParam("login_hint", loginHint)
        authRequestBuilder.addQueryParam("channel", channel)
        IMPublicAPIServices.startAuthentication(this, authRequestBuilder.build(), callback)
    }

    // update state and device token to client server
    private fun updateStateAndDeviceToken() {
        IMHelper.currentState = IPificationServices.generateState()
        IMHelper.registerDevice(IMHelper.deviceToken, IMHelper.currentState)
    }
}


