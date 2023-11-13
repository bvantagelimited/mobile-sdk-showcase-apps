package com.ipification.demoapp.activity

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.databinding.ActivityIpAuthenticationBinding
import com.ipification.demoapp.manager.IPHelper
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.callback.IPAuthCallback
import com.ipification.mobile.sdk.android.callback.IPCoverageCallback
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.CoverageResponse
import com.ipification.mobile.sdk.android.response.IPAuthResponse
import com.mukesh.countrypicker.CountryPicker
import java.lang.reflect.Method


class IPificationAuthActivity : AppCompatActivity() {
    val TAG = "IPAuthAct"
    lateinit var binding: ActivityIpAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIpAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initIPification()
        initView()
    }

    private fun initIPification() {
        IPConfiguration.getInstance().ENV = if(BuildConfig.ENVIRONMENT == "sandbox" ) IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
        IPConfiguration.getInstance().CLIENT_ID = BuildConfig.CLIENT_ID
        IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI)
    }
    private fun initView() {
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        initPhoneInputView()
        binding.loginBtn.setOnClickListener {
            startIPAuthenticationFlow()
        }
    }

    private fun initPhoneInputView() {
        //hide autofocus
        binding.countryCodeEditText.hideKeyboard()

        val builder = CountryPicker.Builder().with(this)
            .listener { country ->
                Log.d(TAG, "country code = " + country?.dialCode)
                binding.countryCodeEditText.setText(country?.dialCode)
                binding.phoneCodeEditText.requestFocus()
            }

        binding.phoneCodeEditText.requestFocus()

        val picker = builder.build()
        val country = picker.countryFromSIM

        //TODO
        if(BuildConfig.ENVIRONMENT == "sandbox"){
            binding.countryCodeEditText.setText("+999")
            binding.countryCodeEditText.isEnabled = false
            binding.phoneCodeEditText.setText("123456789")
        }else{
            binding.countryCodeEditText.setText(country.dialCode)
            binding.countryCodeEditText.isEnabled = true
            binding.phoneCodeEditText.setText("")
        }

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
        val self = this
        binding.countryCodeEditText.setOnFocusChangeListener { view: View, b: Boolean ->
            if (b) {
                picker.showBottomSheet(self)
                binding.countryCodeEditText.hideKeyboard()
            }
        }
    }


    private fun startIPAuthenticationFlow() {
        hideKeyboard()
        updateButton(isEnable = false)

        val phoneNumber  = "${binding.countryCodeEditText.text}${binding.phoneCodeEditText.text}"

        // start checking coverage with user phone number
        val coverageCallback = object : IPCoverageCallback
        {
            override fun onSuccess(response: CoverageResponse) {
                if(response.isAvailable()) {
                    // supported Telco. call IP Auth function
                    callIPAuthentication(phoneNumber)
                } else {
                    // unsupported Telco. Fallback to another authentication service flow
                    Util.openErrorActivity(this@IPificationAuthActivity, "unsupported")
                }
            }
            override fun onError(error: IPificationError) {
                // error, handle it with another authentication service flow
                Util.openErrorActivity(this@IPificationAuthActivity, error.getErrorMessage())
            }
        }
        IPificationServices.startCheckCoverage( phoneNumber = phoneNumber , context = this,  callback = coverageCallback)
    }
    private fun callIPAuthentication(phoneNumber: String) {
        val authCallback = object: IPAuthCallback {
            override fun onSuccess(response: IPAuthResponse) {
                // call backend with {response.code}
                IPHelper.callTokenExchangeAPI(this@IPificationAuthActivity, response.code)
            }
            override fun onError(error: IPificationError) {
                Util.openErrorActivity(this@IPificationAuthActivity, "auth error:  ${error.getErrorMessage()}")
            }
        }
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.addQueryParam("login_hint", phoneNumber)
        val authRequest = authRequestBuilder.build()
        IPificationServices.startAuthentication(this, authRequest, authCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        // unregister cellular network. See https://developer.ipification.com/#/android/latest/?id=_5-unregister-cellular-network
        IPificationServices.unregisterNetwork(this)
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
