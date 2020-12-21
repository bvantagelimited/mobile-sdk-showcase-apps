package com.ipification.demoapp


import android.app.AlertDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import com.facebook.stetho.Stetho
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ipification.mobile.sdk.android.AuthorizationServiceConfiguration
import com.ipification.mobile.sdk.android.CellularService
import com.ipification.mobile.sdk.android.callback.CellularCallback
import com.ipification.mobile.sdk.android.exception.CellularException
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
import com.ipification.mobile.sdk.android.response.CellularResponse
import com.ipification.mobile.sdk.android.response.CoverageResponse
import com.mukesh.countrypicker.CountryPicker
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Method


class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    lateinit var textResult: TextView
    private lateinit var countryCodeTxt: EditText
    private lateinit var phoneCodeTxt: EditText


    private lateinit var phone: String
    private lateinit var button: Button

    private val EXCHANGE_TOKEN_ENDPOINT = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Stetho.initializeWithDefaults(this)
        setContentView(R.layout.activity_main)

        countryCodeTxt = findViewById(R.id.countryCode)

        phoneCodeTxt = findViewById(R.id.phoneCode)
        phoneCodeTxt.requestFocus()
        textResult = findViewById(R.id.result)

        button = findViewById(R.id.button)
        button.setOnClickListener {
            requestIPification()
        }

        val builder = CountryPicker.Builder().with(this)
            .listener { country ->
                Log.d(TAG, "countrycode = " + country?.dialCode)
                countryCodeTxt.setText(country?.dialCode)
                phoneCodeTxt.requestFocus()
            }

        val picker = builder.build()
        val self = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            countryCodeTxt.showSoftInputOnFocus = false
        } else {
            try {
                val method: Method = EditText::class.java.getMethod(
                    "setShowSoftInputOnFocus", *arrayOf<Class<*>?>(Boolean::class.javaPrimitiveType)
                )
                method.isAccessible = true
                method.invoke(countryCodeTxt, false)
            } catch (e: Exception) {
                // ignore
            }
        }

        countryCodeTxt.setOnFocusChangeListener { view: View, b: Boolean ->
            if (b) {
                picker.showBottomSheet(self)
                countryCodeTxt.hideKeyboard()
            }
        }

    }
    private fun handleError() {
        updateButton(false)
    }
    private fun showMessage(message: String){
        textResult.post {
            textResult.text = message
        }
    }
    private fun updateButton(enabled : Boolean = true){
        button.post{
            button.isEnabled = enabled
            button.alpha = if (enabled) 1f else 0.5f
        }
    }
    private fun requestIPification() {
        showMessage("connecting...")
        val isValidPhoneNumber = validatePhoneNumber()
        if (!isValidPhoneNumber){
            return
        }
        updateButton(false)

        checkCoverage(callback = object: CellularCallback<CoverageResponse>{
            override fun onSuccess(response: CoverageResponse) {
                val isAvailable = response.parseResponse()
                Log.d(TAG, "isAvailable $isAvailable")
                if(isAvailable){
                    showMessage("checkCoverage - supported Telco ...")
                    doAuthorization()
                }else{
                    showMessage("checkCoverage - not supported Telco ...")
                    handleError()
                }
            }

            override fun onError(error: CellularException) {
                Log.d(TAG, "error" + error.exception!!.message)
                showMessage("checkCoverage - error: ${ error.exception?.message ?: "" }")
                handleError()
            }
        })

    }




    private fun checkCoverage(callback : CellularCallback<CoverageResponse>) {
        val cellularService = CellularService<CoverageResponse>(this)
        cellularService.registerCallback(callback)
        cellularService.checkCoverage()
    }
    private fun doAuthorization(){

        val authCallback = object :
            CellularCallback<AuthResponse> {
            override fun onSuccess(response: AuthResponse) {
                Log.d(TAG, "response" + response.responseData)
                val code = response.parseResponse()
                Log.d(TAG, "code $code")
                if(code != null){
                    // exchange Token
                    doExchangeToken(code)
                }else{
                    handleError()
                }

            }

            override fun onError(error: CellularException) {
                Log.d(TAG, "onFailed" + error.error_code)
                handleError()
                showMessage(error.exception?.message ?: "doAuthorization - unknown error")
            }
        }

        authorize(authCallback)

    }

    private fun authorize(callback : CellularCallback<AuthResponse>){
        var cellularService = CellularService<AuthResponse>(this)

        cellularService.registerCallback(callback)
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.addQueryParam("login_hint", phone)
        authRequestBuilder.addQueryParam("state", "213e23423423423423423423")
//            authRequestBuilder.addQueryParam("request",request)
        val authRequest = authRequestBuilder.build()

        cellularService.performAuth(authRequest)
    }

    private fun validatePhoneNumber(): Boolean {
        if (countryCodeTxt.text.toString().isEmpty()) {
            showAlert(message = "Country code is empty")
            return false
        }
        if (phoneCodeTxt.text.toString().isEmpty()) {
            showAlert(message = "Phone number is empty")
            return false
        }
        val countryCode = countryCodeTxt.text.toString().replace("+", "")
        val phoneNumberUtil = PhoneNumberUtil.getInstance()

        val isoCode =
            phoneNumberUtil.getRegionCodeForCountryCode(countryCode.toInt())
        return try {
            val phoneNumber = phoneNumberUtil.parse(phoneCodeTxt.text.toString(), isoCode)
            phone = "${phoneNumber.countryCode}${phoneNumber.nationalNumber}"
            val isValid = phoneNumberUtil.isValidNumber(phoneNumber)
            if (!isValid) {
                showAlert(message = "Your Phone number is not correct")
            }

            isValid
        } catch (e: Exception) {
            showAlert(message = "Your Phone number is not correct")
            false
        }
    }


    private fun doExchangeToken(code : String){
        val callback = object : CellularCallback<CellularResponse> {
            override fun onSuccess(response: CellularResponse) {
                try {
                    val jObject = JSONObject(response.responseData)
                    val jtw = JWT(jObject.getString("access_token"))
                    showMessage("phone_number_verified: ${jtw.getClaim("phone_number_verified").asBoolean()}\n\nsub: ${jtw.getClaim("sub").asString()} ")
                    updateButton()
                }catch (e: Exception){
                    showMessage("error: ${e.message}")
                    handleError()
                }
            }

            override fun onError(error: CellularException) {
                showMessage("error: ${error.error_description ?: error.exception?.localizedMessage ?: "error"}")
                handleError()
            }

        }
        doExchangeToken(code, callback)
    }
    @Throws(IOException::class)
    private fun doExchangeToken(code: String, callback: CellularCallback<CellularResponse>) {

        try{

            val url = EXCHANGE_TOKEN_ENDPOINT
            val cellularService = CellularService<CoverageResponse>(this)
            val body: RequestBody = FormBody.Builder()
                .add("client_id", cellularService.getConfiguration("client_id") ?: "")
                .add("redirect_uri", cellularService.getConfiguration("redirect_uri") ?: "")
                .add("grant_type", "authorization_code")
                .add("client_secret", "")
                .add("code", code)
                .build()

            val client = OkHttpClient.Builder().addNetworkInterceptor(StethoInterceptor()).build()
            val request: Request = Request.Builder().url(url).post(body)
                .build()
            client.newCall(request).execute()
                .use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body

                        callback.onSuccess(CellularResponse(response.code, responseBody!!.string()))
                    } else {
                        val responseBody = response.body!!.string()
                        callback.onError(CellularException(Exception(responseBody)))
                    }
                }
        }catch (e: Exception){
            callback.onError(CellularException(e))
        }

    }



    private fun showAlert(title: String? = "", message: String?): AlertDialog {
        return AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message) // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { dialog, which ->
                    // Continue with delete operation
                }) // A null listener allows the button to dismiss the dialog and take no further action.
//            .setNegativeButton(android.R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}

fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}
