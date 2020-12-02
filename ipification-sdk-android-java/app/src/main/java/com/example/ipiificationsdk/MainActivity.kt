package com.example.ipiificationsdk

import android.content.Context
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ipification.mobile.sdk.android.*
import com.ipification.mobile.sdk.android.callback.CellularCallback
import com.ipification.mobile.sdk.android.exception.CellularException
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.android.response.CoverageResponse

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
//    private val URI_IFCONFIG = "https://ifconfig.co/json"
//    private val URI_IPIFY = "https://api6.ipify.org?format=json"
//
//    private val URI_IPIFY_PLAIN_HTTP = "http://api6.ipify.org?format=json"
//    private val URI_HTTPSTAT = "https://httpstat.us/200"
//    private val URI_MOCKY = "https://www.mocky.io/v2/5e85d5203000003f1397b4ed"
//    private val URI_BEECEPTOR = "https://iptest.free.beeceptor.com"

    private lateinit var buttonCellular: Button
    private lateinit var textCellular: TextView
    private lateinit var buttonWiFi: Button
    private lateinit var textWiFi: TextView
    private lateinit var buttonSDK: Button
    private lateinit var textSDK: TextView
//    private lateinit var testURI: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    fun init() {
        buttonSDK = findViewById<View>(R.id.buttonSDK) as Button
        textSDK = findViewById<View>(R.id.textViewSDK) as TextView
        textSDK.movementMethod = ScrollingMovementMethod()

        buttonSDK.setOnClickListener {

//            val testURI = testURI.text
//            if (testURI?.toString() == null || testURI.toString().isEmpty()) {
//                textSDK.text = "URI is not configured!"
//            } else if (!testURI.toString().startsWith("http://") && !testURI.toString().startsWith("https://")) {
//                textSDK.text = "URI is invalid - must starts with prefix http(s)://"
//            } else {
                textSDK.text = "... trying to make request via IPification SDK  ... connecting"


                checkCoverage(object : CellularCallback<CoverageResponse> {
                    override fun onSuccess(response: CoverageResponse) {
                        Log.i("MainActivity", "Response: " + response.responseData)
                        textSDK.post {
                            textSDK.text = response.responseData
                        }
                        val isAvailable = response.parseResponse()
                        if (isAvailable) {
                            callAuth(object : CellularCallback<AuthResponse> {
                                override fun onError(error: CellularException) {
                                    Log.d("MainActivity", "error ${error.exception?.message}")
                                }

                                override fun onSuccess(response: AuthResponse) {
                                    Log.d("MainActivity", "code ${response.parseResponse()}")
                                    textSDK.post {
                                        textSDK.text ="auth code: ${ response.parseResponse()}"
                                    }
                                }

                            })
                        }
                    }

                    override fun onError(error: CellularException) {
                        Log.i("MainActivity", error.responseCode.toString())
                        textSDK.post {
                            if (error.exception != null) {
                                textSDK.text = error.responseCode.toString() + " - " + error.exception?.message
                            } else {
                                textSDK.text = "something went wrong"
                            }
                        }
                    }
                })

        }
    }

    private fun callAuth(callback: CellularCallback<AuthResponse>) {
        val checkCoverageService = CellularService<AuthResponse>(this)
        checkCoverageService.registerCallback(callback)
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.addQueryParam("login_hint", "381692023534")
        val authRequest = authRequestBuilder.build()
        checkCoverageService.performAuth(authRequest)
    }

    private fun checkCoverage(callback: CellularCallback<CoverageResponse>) {
        val checkCoverageService = CellularService<CoverageResponse>(this)
        checkCoverageService.registerCallback(callback)
        checkCoverageService.checkCoverage()
    }

}