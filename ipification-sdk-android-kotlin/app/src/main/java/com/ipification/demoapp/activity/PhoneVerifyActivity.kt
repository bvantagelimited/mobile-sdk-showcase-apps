package com.ipification.demoapp.activity


import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.facebook.stetho.Stetho
import com.ipification.demoapp.data.TokenInfo
import com.ipification.demoapp.databinding.ActivityPhoneVerifyBinding
import com.ipification.demoapp.manager.APIManager
import com.ipification.demoapp.manager.TokenCallback
import com.ipification.demoapp.util.*
import com.ipification.mobile.sdk.android.CellularService
//import com.ipification.mobile.sdk.android.IPIMServices
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.callback.IPificationCallback
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.AuthResponse
//import com.ipification.mobile.sdk.android.utils.Constant
import com.ipification.mobile.sdk.im.IMService
import com.mukesh.countrypicker.CountryPicker
import org.json.JSONObject
import java.lang.reflect.Method


class PhoneVerifyActivity : AppCompatActivity() {
    val TAG = "PhoneVerifyActivity"
    lateinit var binding: ActivityPhoneVerifyBinding
    var log = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Stetho.initializeWithDefaults(this)
        binding = ActivityPhoneVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.countryCodeEditText.hideKeyboard()

        binding.loginBtn.setOnClickListener {
            requestIPification()
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



    private fun requestIPification() {
        hideKeyboard()

        updateButton(isEnable = false)
        log += "\n"
        log += "#####################################\n"
        log += "1. DO AUTHORIZATION - start\n\n"

        val phoneNumber  = "${binding.countryCodeEditText.text}${binding.phoneCodeEditText.text}"
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.setScope("openid ip:phone_verify")
        authRequestBuilder.addQueryParam("login_hint", phoneNumber)
        //add channel
        authRequestBuilder.addQueryParam("channel", "ip wa viber telegram")

        IPificationServices.startAuthentication(this, authRequestBuilder.build(), object: IPificationCallback{
            override fun onSuccess(response: AuthResponse) {
                //check auth_code
                val code = response.getCode()
                if(code != null){
                    callTokenExchange(response.getCode()!!)
                }else{
                    binding.result1.post {
                        log += "Result: AUTH ERROR : ${response.responseData}"
                        log += "\n2. DO AUTHORIZATION - end\n"
                        showlog()
                        binding.result1.text = "auth error: ${response.responseData}"
                        updateButton(isEnable = true)
                    }
                }

            }
            override fun onError(error: IPificationError) {
                Log.e(TAG,"IPificationError " + error.getErrorMessage())
                binding.result1.post {
                    log += "Result: AUTH ERROR : ${error.getErrorMessage()}"
                    log += "\n2. DO AUTHORIZATION - end\n"
                    showlog()
                    binding.result1.text = "auth error: ${error.getErrorMessage()}"
                    updateButton(isEnable = true)
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        IMService.onActivityResult(requestCode, resultCode, data)
    }



    private fun callTokenExchange(code: String) {
        APIManager.doPostToken(code, callback = object: TokenCallback {
            override fun onSuccess(response: String) {
                handleTokenExchangeSuccess(response)
            }
            override fun onError(error: String) {
                handleTokenExchangeError(error)
            }
        })
    }

    private fun handleTokenExchangeSuccess(response: String) {
        try{
            val jObject = JSONObject(response)
            val accessToken = jObject.getString("access_token")
            val tokenInfo = Util.parseAccessToken(accessToken)
            if(tokenInfo != null && tokenInfo.phoneNumberVerified){
                openSuccessActivity(tokenInfo)
            }else{
                openErrorActivity("token null", tokenInfo)
            }
        }catch (error: Exception){
            openErrorActivity(error.localizedMessage ?: "unknown error")
        }
    }

    private fun handleTokenExchangeError(error: String) {
        if(error != "USER_CANCELED"){
            openErrorActivity(error)
        }
    }
    private fun openSuccessActivity(tokenInfo: TokenInfo) {
        val intent = Intent(this, SuccessResultActivity::class.java)
        intent.putExtra("tokenInfo", tokenInfo)
        startActivity(intent)

    }
    private fun openErrorActivity(error: String, tokenInfo: TokenInfo? = null){
        val intent = Intent(this, FailResultActivity::class.java)
        intent.putExtra(
            "error",
            error
        )
        if(tokenInfo != null){
            intent.putExtra("tokenInfo", tokenInfo)
        }
        startActivity(intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        CellularService.unregisterNetwork(this)
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
//        Constant.getInstance().LOG = log

    }

}

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


fun Activity.showAlert(title: String? = "", message: String?): AlertDialog{
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
