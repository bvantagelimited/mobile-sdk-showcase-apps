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
    companion object {
        private const val TAG = "IPAuthAct"
        private const val DEFAULT_COUNTRY_CODE = "+999"
    }

    private lateinit var binding: ActivityIpAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIpAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initIPification()
        initView()
    }

    private fun initIPification() {
        val environment = if (BuildConfig.ENVIRONMENT == "sandbox") IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
        with(IPConfiguration.getInstance()) {
            ENV = environment
            CLIENT_ID = BuildConfig.CLIENT_ID
            REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI)
        }
    }

    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initPhoneInputView()
        binding.loginBtn.setOnClickListener {
            startIPAuthenticationFlow()
        }
    }

    private fun initPhoneInputView() {
        // hide autofocus
        binding.countryCodeEditText.hideKeyboard()

        val builder = CountryPicker.Builder().with(this)
            .listener { country ->
                Log.d(TAG, "Country code = ${country?.dialCode}")
                binding.countryCodeEditText.setText(country?.dialCode)
                binding.phoneCodeEditText.requestFocus()
            }

        binding.phoneCodeEditText.requestFocus()

        val picker = builder.build()
        val country = picker.countryFromSIM

        binding.countryCodeEditText.setText(if (BuildConfig.ENVIRONMENT == "sandbox") DEFAULT_COUNTRY_CODE else country?.dialCode)
        binding.countryCodeEditText.isEnabled = BuildConfig.ENVIRONMENT != "sandbox"
        if(BuildConfig.ENVIRONMENT == "sandbox"){
            binding.phoneCodeEditText.setText("123456789")
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
        binding.countryCodeEditText.setOnFocusChangeListener { _, b ->
            if (b) {
                picker.showBottomSheet(self)
                binding.countryCodeEditText.hideKeyboard()
            }
        }
    }

    private fun startIPAuthenticationFlow() {
        hideKeyboard()
        updateButton(isEnable = false)

        val phoneNumber = "${binding.countryCodeEditText.text}${binding.phoneCodeEditText.text}"

        val coverageCallback = object : IPCoverageCallback {
            override fun onSuccess(response: CoverageResponse) {
                if (response.isAvailable()) {
                    callIPAuthentication(phoneNumber)
                } else {
                    Util.openErrorActivity(this@IPificationAuthActivity, "unsupported")
                    updateButton(isEnable = true)
                }
            }

            override fun onError(error: IPificationError) {
                Util.openErrorActivity(this@IPificationAuthActivity, error.getErrorMessage())
                updateButton(isEnable = true)
            }
        }
        IPificationServices.startCheckCoverage(phoneNumber = phoneNumber, context = this, callback = coverageCallback)
    }

    private fun callIPAuthentication(phoneNumber: String) {
        val authCallback = object : IPAuthCallback {
            override fun onSuccess(response: IPAuthResponse) {
                IPHelper.callTokenExchangeAPI(this@IPificationAuthActivity, response.code)
                updateButton(isEnable = true)
            }

            override fun onError(error: IPificationError) {
                Util.openErrorActivity(this@IPificationAuthActivity, "auth error:  ${error.getErrorMessage()}")
                updateButton(isEnable = true)
            }
        }
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.addQueryParam("login_hint", phoneNumber)
        val authRequest = authRequestBuilder.build()
        IPificationServices.startAuthentication(this, authRequest, authCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        IPificationServices.unregisterNetwork(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateButton(isEnable: Boolean) {
        binding.loginBtn.post {
            with(binding.loginBtn) {
                isEnabled = isEnable
                alpha = if (isEnable) 1f else 0.2f
            }
        }
    }
}

// extension
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