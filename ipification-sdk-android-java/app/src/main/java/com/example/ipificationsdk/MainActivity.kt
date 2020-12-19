package com.example.ipificationsdk

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ipification.mobile.sdk.android.CellularService
import com.ipification.mobile.sdk.android.callback.CellularCallback
import com.ipification.mobile.sdk.android.exception.CellularException
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.android.response.CoverageResponse

class MainActivity : AppCompatActivity() {

    private lateinit var buttonCellular: Button
    private lateinit var textCellular: TextView
    private lateinit var buttonWiFi: Button
    private lateinit var textWiFi: TextView
    private lateinit var buttonSDK: Button
    private lateinit var textSDK: TextView

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
                                    textSDK.text = "auth code: ${response.parseResponse()}"
                                }
                            }
                        })
                    }
                }

                override fun onError(error: CellularException) {
                    Log.i("MainActivity", error.responseCode.toString())
                    textSDK.post {
                        if (error.exception != null) {
                            textSDK.text = "onError is fired! Response error code: " + error.responseCode.toString() + " - " + error.exception?.message
                        } else {
                            textSDK.text = "something went wrong"
                        }
                    }
                }
            })
        }
    }

    private fun callAuth(callback: CellularCallback<AuthResponse>) {
        val cellularService = CellularService<AuthResponse>(this)
        cellularService.registerCallback(callback)
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.addQueryParam("login_hint", "381692023534")
        val authRequest = authRequestBuilder.build()
        cellularService.performAuth(authRequest)
    }

    private fun checkCoverage(callback: CellularCallback<CoverageResponse>) {
        val checkCoverageService = CellularService<CoverageResponse>(this)
        checkCoverageService.registerCallback(callback)
        checkCoverageService.checkCoverage()
    }

}