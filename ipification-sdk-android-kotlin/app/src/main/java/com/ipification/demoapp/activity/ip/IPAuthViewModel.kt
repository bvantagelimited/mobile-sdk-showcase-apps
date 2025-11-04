package com.ipification.demoapp.activity.ip

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ipification.demoapp.manager.IPHelper
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.callback.IPAuthCallback
import com.ipification.mobile.sdk.android.callback.IPCoverageCallback
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.CoverageResponse
import com.ipification.mobile.sdk.android.response.IPAuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IPAuthUiState(
    val countryCode: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false
)

class IPAuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "IPAuthViewModel"
    }

    private val _uiState = MutableStateFlow(IPAuthUiState())
    val uiState: StateFlow<IPAuthUiState> = _uiState

    fun setCountryCode(code: String) {
        _uiState.update { it.copy(countryCode = code) }
    }

    fun setPhoneNumber(number: String) {
        _uiState.update { it.copy(phoneNumber = number) }
    }

    fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun onPhoneNumberFromHint(fullNumber: String, countryISO: String? = null) {
        Log.d(TAG, "onPhoneNumberFromHint - fullNumber: $fullNumber, countryISO: $countryISO")
        
        val phoneUtil = PhoneNumberUtil.getInstance()
        
        try {
            val parsedNumber = phoneUtil.parse(fullNumber, countryISO)
            val countryDialCode = "+${parsedNumber.countryCode}"
            val nationalNumber = parsedNumber.nationalNumber.toString()
            
            Log.d(TAG, "Parsed - countryDialCode: $countryDialCode, nationalNumber: $nationalNumber")
            
            _uiState.update { it.copy(countryCode = countryDialCode, phoneNumber = nationalNumber) }
        } catch (e: NumberParseException) {
            Log.e(TAG, "Error parsing phone number: ${e.message}", e)
            _uiState.update { it.copy(countryCode = fullNumber) }
        }
    }

    fun startVerification(
        act: Activity,
        onOpenError: (String) -> Unit
    ) {
        val full = "${_uiState.value.countryCode}${_uiState.value.phoneNumber}"
        setLoading(true)

        val coverageCallback = object : IPCoverageCallback {
            override fun onSuccess(response: CoverageResponse) {
                if (response.isAvailable()) {
                    callIPAuthentication(act, full, onOpenError)
                } else {
                    onOpenError("unsupported")
                    setLoading(false)
                }
            }

            override fun onError(error: IPificationError) {
                onOpenError(error.getErrorMessage())
                setLoading(false)
            }
        }
        IPificationServices.startCheckCoverage(
            phoneNumber = full,
            context = act.applicationContext,
            callback = coverageCallback
        )
    }

    private fun callIPAuthentication(
        act: Activity,
        phoneNumber: String,
        onOpenError: (String) -> Unit
    ) {
        val authCallback = object : IPAuthCallback {
            override fun onSuccess(response: IPAuthResponse) {
                viewModelScope.launch {
                    IPHelper.callTokenExchangeAPI(act, response.code)
                    setLoading(false)
                }
            }

            override fun onError(error: IPificationError) {
                onOpenError("auth error:  ${error.getErrorMessage()}")
                setLoading(false)
            }
        }
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.addQueryParam("login_hint", phoneNumber)
        val authRequest = authRequestBuilder.build()
        IPificationServices.startAuthentication(act, authRequest, authCallback)
    }
}


