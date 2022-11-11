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
import com.ipification.demoapp.callback.IPAuthorizationCallback
import com.ipification.demoapp.callback.IPCheckCoverageCallback
import com.ipification.demoapp.databinding.ActivityIpAuthenticationBinding
import com.ipification.demoapp.manager.APIManager
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.CellularService
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
import com.ipification.mobile.sdk.android.InternalService
import com.ipification.mobile.sdk.android.callback.CellularCallback
import com.ipification.mobile.sdk.android.exception.CellularException
import com.ipification.mobile.sdk.android.response.CellularResponse
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
        checkIP()
    }
    private fun initView() {
        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)
        initPhoneInputView()
        binding.loginBtn.setOnClickListener {
            startIPAuthentication()
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

        binding.countryCodeEditText.setText(country?.dialCode)
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
    }


    private fun startIPAuthentication() {
        hideKeyboard()
        updateButton(isEnable = false)
        // start checking coverage
        val phoneNumber  = "${binding.countryCodeEditText.text}${binding.phoneCodeEditText.text}"

        APIManager.checkCoverage(phoneNumber, context = this, callback = object: IPCheckCoverageCallback {
            override fun result(isAvailable: Boolean, operatorCode: String?, errorMessage: String?) {
                if(isAvailable){
                    // call Authorization API
                    callIPAuth()
                }else{
                    updateButton(true)
                    // TODO fallback to other service
                    Log.e("IP CheckCoverage", "not supported : $errorMessage")
                    Util.openErrorActivity(this@IPificationAuthActivity, "$errorMessage")
                }
            }
        })
    }
    private fun callIPAuth() {

        val phoneNumber  = "${binding.countryCodeEditText.text}${binding.phoneCodeEditText.text}"
        val callback = object: IPAuthorizationCallback{
            override fun result(code: String?, errorMessage: String?) {
                if(code != null){ // success
                    Util.callTokenExchangeAPI(this@IPificationAuthActivity, code)
                } else{
                    binding.result1.post {
                        binding.result1.text = "auth error: $errorMessage"

                    }
                    Util.openErrorActivity(this@IPificationAuthActivity, "auth error:  $errorMessage")
                }
                updateButton(isEnable = true)
            }
        }
        APIManager.callAuthorization(activity = this, phoneNumber = phoneNumber, callback = callback)
    }


    private fun checkIP(){
        val cellularService = InternalService<CellularResponse>(this)
        val callback = object :
            CellularCallback<CellularResponse> {
            override fun onSuccess(response: CellularResponse) {
                Log.d("checkIP", "response " + response.responseData)
            }

            override fun onError(error: CellularException) {
                Log.e("checkIP", "response " + error.getErrorMessage())
            }
        }
        cellularService.checkRequestedIP("http://checkip.amazonaws.com/", callback)
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
