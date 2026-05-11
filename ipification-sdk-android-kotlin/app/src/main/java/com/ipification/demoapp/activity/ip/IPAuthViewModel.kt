package com.ipification.demoapp.activity.ip

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.manager.IPHelper
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.AuthChannel
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.callback.IPCoverageCallback
import com.ipification.mobile.sdk.android.callback.MultiAuthCallback
import com.ipification.mobile.sdk.android.exception.IPificationError
import com.ipification.mobile.sdk.android.request.AuthRequest
import com.ipification.mobile.sdk.android.response.CoverageResponse
import com.ipification.mobile.sdk.android.response.IPAuthResponse
import com.ipification.mobile.sdk.sms.callback.SMSCallback
import com.ipification.mobile.sdk.sms.response.SMSAuthResponse
import com.ipification.mobile.sdk.sms.response.SMSTokenResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IPAuthUiState(
    val countryCode: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val pendingSmsAuth: SMSAuthResponse? = null,
    val otpCode: String = ""
)

enum class DemoAuthFlow(val label: String, val channels: List<AuthChannel>) {
    IP("Phone Number Verify", listOf(AuthChannel.IP)),
    TS43("TS43 Verify", listOf(AuthChannel.TS43)),
    SMS("SMS Verify", listOf(AuthChannel.SMS)),
    MULTI_CHANNEL("Multi-channel Verify", listOf(AuthChannel.TS43, AuthChannel.IP, AuthChannel.SMS))
}

class IPAuthViewModel : ViewModel() {

    companion object {
        private const val TAG = "IPAuthViewModel"
        private const val AUTH_SCOPE = "openid ip:phone_verify"
        private const val STAGE_BACKEND_URL = ""
        private const val PRODUCTION_BACKEND_URL = ""
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

    fun setOtpCode(code: String) {
        _uiState.update { it.copy(otpCode = code) }
    }

    fun cancelOtp() {
        _uiState.update { it.copy(pendingSmsAuth = null, otpCode = "", isLoading = false) }
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
        flow: DemoAuthFlow,
        onOpenError: (String) -> Unit
    ) {
        val full = "${_uiState.value.countryCode}${_uiState.value.phoneNumber}"
        setLoading(true)

        configureAuthChannels(flow)

        if (flow != DemoAuthFlow.IP) {
            callAuthentication(act, full, flow, onOpenError)
            return
        }

        val coverageCallback = object : IPCoverageCallback {
            override fun onSuccess(response: CoverageResponse) {
                if (response.isAvailable()) {
                    callAuthentication(act, full, flow, onOpenError)
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

    private fun configureAuthChannels(flow: DemoAuthFlow) {
        with(IPConfiguration.getInstance()) {
            AUTH_CHANNELS = flow.channels
            TS43_BACKEND_URL_SANDBOX = STAGE_BACKEND_URL
            TS43_BACKEND_URL_PRODUCTION = PRODUCTION_BACKEND_URL
            TS43_AUTH_PATH = "/ts43/auth"
            TS43_TOKEN_PATH = "/ts43/token"
            SMS_BACKEND_URL_SANDBOX = STAGE_BACKEND_URL
            SMS_BACKEND_URL_PRODUCTION = PRODUCTION_BACKEND_URL
            SMS_AUTH_PATH = "/sms/auth"
            SMS_TOKEN_PATH = "/sms/token"
        }
    }

    private fun callAuthentication(
        act: Activity,
        phoneNumber: String,
        flow: DemoAuthFlow,
        onOpenError: (String) -> Unit
    ) {
        val authCallback = object : MultiAuthCallback {
            override fun onSuccess(response: IPAuthResponse) {
                viewModelScope.launch {
                    when {
                        response.ts43TokenResponse != null -> {
                            Util.openSuccessActivity(act, response.fullResponse)
                        }
                        response.fullResponse.isNotEmpty() -> {
                            Util.openSuccessActivity(act, response.fullResponse)
                        }
                        response.code.isNotEmpty() -> {
                            IPHelper.callTokenExchangeAPI(act, response.code)
                        }
                        else -> {
                            onOpenError("${flow.label} completed without a token response")
                        }
                    }
                    setLoading(false)
                }
            }

            override fun onOTPRequired(response: SMSAuthResponse) {
                _uiState.update { it.copy(isLoading = false, pendingSmsAuth = response, otpCode = "") }
            }

            override fun onError(error: IPificationError) {
                onOpenError("${flow.label} error: ${error.getErrorMessage()}")
                setLoading(false)
            }
        }
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.addQueryParam("login_hint", phoneNumber)
        val authRequest = authRequestBuilder.build()
        IPificationServices.startAuthentication(act, authRequest, authCallback)
    }

    fun verifyOtp(act: Activity, onOpenError: (String) -> Unit) {
        val pendingAuth = _uiState.value.pendingSmsAuth
        val otp = _uiState.value.otpCode
        if (pendingAuth == null || otp.isBlank()) {
            onOpenError("SMS OTP, auth_req_id, or nonce is empty")
            return
        }

        setLoading(true)
        IPificationServices.verifySMSOTP(act, otp, pendingAuth.authReqId, pendingAuth.nonce, object : SMSCallback {
            override fun onAuthInitiated(response: SMSAuthResponse) {
                _uiState.update { it.copy(isLoading = false, pendingSmsAuth = response, otpCode = "") }
            }

            override fun onSuccess(response: SMSTokenResponse) {
                _uiState.update { it.copy(isLoading = false, pendingSmsAuth = null, otpCode = "") }
                Util.openSuccessActivity(act, response.rawResponse ?: "")
            }

            override fun onError(error: IPificationError) {
                setLoading(false)
                onOpenError("SMS OTP error: ${error.getErrorMessage()}")
            }
        })
    }
}
