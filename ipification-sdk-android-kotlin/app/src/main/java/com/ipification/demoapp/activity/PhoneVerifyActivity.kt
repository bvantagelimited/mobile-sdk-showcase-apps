package com.ipification.demoapp.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.ipification.demoapp.Constant
import com.ipification.demoapp.callback.IPAuthorizationCallback
import com.ipification.demoapp.callback.IPCheckCoverageCallback
import com.ipification.demoapp.callback.TokenCallback
import com.ipification.demoapp.databinding.ActivityPhoneVerifyBinding
import com.ipification.demoapp.manager.APIManager
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.CellularService
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.im.IMService
import com.mukesh.countrypicker.CountryPicker
import java.lang.reflect.Method


class PhoneVerifyActivity : AppCompatActivity() {
    val TAG = "PhoneVerifyActivity"
    lateinit var binding: ActivityPhoneVerifyBinding
    var log = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initIPification()
    }

    private fun initView() {
        //hide autofocus
        binding.countryCodeEditText.hideKeyboard()
        binding.loginBtn.setOnClickListener {
            startIPAuth()
        }

        val builder = CountryPicker.Builder().with(this)
            .listener { country ->
                Log.d(TAG, "country code = " + country?.dialCode)
                binding.countryCodeEditText.setText(country?.dialCode)
                binding.phoneCodeEditText.requestFocus()
            }

        binding.phoneCodeEditText.requestFocus()

        val picker = builder.build()
        val self = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.countryCodeEditText.showSoftInputOnFocus = false
        } else {
            try {
                val method: Method = EditText::class.java.getMethod(
                    "setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType
                )
                method.isAccessible = true
                method.invoke(binding.countryCodeEditText, false)
            } catch (e: Exception) {
                // ignore
            }
        }

        binding.countryCodeEditText.setOnFocusChangeListener { view: View, b: Boolean ->
            if (b) {
                picker.showBottomSheet(self)
                binding.countryCodeEditText.hideKeyboard()
            }
        }
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
    }


    private fun initIPification() {
        IPConfiguration.getInstance().COVERAGE_URL = Uri.parse(Constant.CHECK_COVERAGE_URL)
        IPConfiguration.getInstance().AUTHORIZATION_URL = Uri.parse(Constant.AUTH_URL)
        IPConfiguration.getInstance().CLIENT_ID = Constant.CLIENT_ID
        IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(Constant.REDIRECT_URI)
    }

    //TODO for IM case
    private fun registerDevice() {
        APIManager.currentState = IPificationServices.generateState()
        APIManager.registerDevice(APIManager.deviceToken, APIManager.currentState)
    }

    private fun startIPAuth() {
        // TODO
        hideKeyboard()
        updateButton(isEnable = false)
        // TODO : IM Only:  register device token to receive FCM notification
//        registerDevice()

        APIManager.checkCoverage(context = this, callback = object: IPCheckCoverageCallback {
            override fun result(isAvailable: Boolean, operatorCode: String?, errorMessage: String?) {
                if(isAvailable){
                    // call Authorization API
                    callAuthorization()
                }else{
                    // TODO fallback to other service
                    Log.e("IP CheckCoverage", "not supported : $errorMessage")
                }
            }
        })
    }




    private fun callAuthorization() {
        log += "\n"
        log += "#####################################\n"
        log += "1. DO AUTHORIZATION - start\n\n"
        showlog()
        val phoneNumber  = "${binding.countryCodeEditText.text}${binding.phoneCodeEditText.text}"

        APIManager.callAuthorization(activity = this, phoneNumber = phoneNumber, callback = object: IPAuthorizationCallback{
            override fun result(code: String?, errorMessage: String?) {
               if(code != null){
                    callTokenExchange(code)

               }else{
                    binding.result1.post {
                        log += "Result: AUTH ERROR : $errorMessage"
                        log += "\n2. DO AUTHORIZATION - end\n"
                        showlog()
                        binding.result1.text = "auth error: $errorMessage"

                    }
               }
                updateButton(isEnable = true)
            }

        })
    }


    // TODO IM : implement onActivityResult() for IM cases
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IMService.onActivityResult(requestCode, resultCode, data)
    }


    // have to call to your backend API to do token exchange
    private fun callTokenExchange(code: String) {
        APIManager.doPostToken(code, callback = object: TokenCallback {
            override fun result(response: String?, errorMessage: String?) {
                if(response != null){
                    val phoneNumberVerified = Util.parseUserInfoJSON(response, "phone_number_verified")
                    val phoneNumber = Util.parseUserInfoJSON(response, "phone_number")
                    if(phoneNumberVerified == "true" || phoneNumber != null){
                        openSuccessActivity(response)
                    }else{
                        openErrorActivity(response)
                    }
                }else{
                    openErrorActivity(errorMessage ?: "error")
                }
            }

        })
    }



    //
//    private fun handleTokenExchangeSuccess(response: String) {
//        try{
//            log += "handleTokenExchangeSuccess\n"
//            val jObject = JSONObject(response)
//            val accessToken = jObject.getString("access_token")
//            log += "accessToken: ${accessToken}\n"
//            showlog()
//            val tokenInfo = Util.parseAccessToken(accessToken)
//            if(tokenInfo != null && tokenInfo.phoneNumberVerified){
//                openSuccessActivity(tokenInfo)
//            }else{
//                openErrorActivity("token null", tokenInfo)
//            }
//        }catch (error: Exception){
//            openErrorActivity(error.localizedMessage ?: "unknown error")
//        }
//    }
//
//    private fun handleTokenExchangeError(error: String) {
//        log += "handleTokenExchangeError: ${error}\n"
//        showlog()
//        if(error != "USER_CANCELED"){
//            openErrorActivity(error)
//        }
//    }
    private fun openSuccessActivity(responseStr: String) {
        val intent = Intent(this, SuccessResultActivity::class.java)
        intent.putExtra("responseStr", responseStr)
        startActivity(intent)

    }
    private fun openErrorActivity(error: String){
        val intent = Intent(this, FailResultActivity::class.java)
        intent.putExtra(
            "error",
            error
        )

        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        // unregister network. See https://developer.ipification.com/#/android/latest/?id=_5-unregister-cellular-network
        CellularService.unregisterNetwork(this)
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

    private fun updateButton(isEnable: Boolean) {
        if (isEnable) {
            binding.loginBtn.post {
                binding.loginBtn.isEnabled = true
                binding.loginBtn.alpha = 1f
                binding.loginBtn.isEnabled = true
                binding.loginBtn.alpha = 1f
            }
//            sendLog()
        } else {
            binding.loginBtn.post {
                binding.loginBtn.isEnabled = false
                binding.loginBtn.alpha = 0.2f
                binding.loginBtn.isEnabled = false
                binding.loginBtn.alpha = 0.2f
            }
        }
    }

    private fun showlog() {
//        logTextView.post {
//            logTextView.text = log
//        }
//        Constant.LOG = log
    }

}


//extension
fun Activity.hideKeyboard() {
    if (currentFocus != null) {
        val inputMethodManager: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }
}

fun View.hideKeyboard() {
    val inputMethodManager: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}
