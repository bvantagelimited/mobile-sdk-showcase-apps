package com.ipification.demoapp.activity.pnv

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
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
import com.ipification.mobile.sdk.sms.response.SMSAuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PnvUiState(
    val countryCode: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false
)

enum class PnvAuthFlow(val label: String, val channels: List<AuthChannel>) {
    // PNV flows map directly to SDK AuthChannel priority order.
    // Multi-channel flows are tried left-to-right by the SDK configuration below.
    IP("IP Verify", listOf(AuthChannel.IP)),
    TS43("TS43 Verify", listOf(AuthChannel.TS43)),
    SMS("SMS Verify", listOf(AuthChannel.SMS)),
    MULTI_CHANNEL_TS43_IP_SMS("Multi Channel (TS43 -> IP -> SMS) Verify", listOf(AuthChannel.TS43, AuthChannel.IP, AuthChannel.SMS)),
    MULTI_CHANNEL_TS43_IP("Multi Channel (TS43 -> IP) Verify", listOf(AuthChannel.TS43, AuthChannel.IP))
}

class PnvViewModel : ViewModel() {
    companion object {
        private const val TAG = "PnvViewModel"
        private const val STAGE_BACKEND_URL = ""
        private const val PRODUCTION_BACKEND_URL = ""
    }

    private val _uiState = MutableStateFlow(PnvUiState())
    val uiState: StateFlow<PnvUiState> = _uiState

    fun setCountryCode(code: String) {
        _uiState.update { it.copy(countryCode = code) }
    }

    fun setPhoneNumber(number: String) {
        _uiState.update { it.copy(phoneNumber = number) }
    }

    private fun setLoading(loading: Boolean) {
        _uiState.update { it.copy(isLoading = loading) }
    }

    fun onPhoneNumberFromHint(fullNumber: String, countryISO: String? = null) {
        Log.d(TAG, "onPhoneNumberFromHint - fullNumber: $fullNumber, countryISO: $countryISO")

        val phoneUtil = PhoneNumberUtil.getInstance()
        try {
            val parsedNumber = phoneUtil.parse(fullNumber, countryISO)
            val countryDialCode = "+${parsedNumber.countryCode}"
            val nationalNumber = parsedNumber.nationalNumber.toString()

            _uiState.update { it.copy(countryCode = countryDialCode, phoneNumber = nationalNumber) }
        } catch (e: NumberParseException) {
            Log.e(TAG, "Error parsing phone number: ${e.message}", e)
            _uiState.update { it.copy(countryCode = fullNumber) }
        }
    }

    fun startVerification(
        activity: Activity,
        flow: PnvAuthFlow,
        onOpenError: (String) -> Unit
    ) {
        val phoneNumber = "${_uiState.value.countryCode}${_uiState.value.phoneNumber}"
        setLoading(true)
        configureAuthChannels(flow)

        if (flow != PnvAuthFlow.IP) {
            callAuthentication(activity, phoneNumber, flow, onOpenError)
            return
        }

        IPificationServices.startCheckCoverage(
            phoneNumber = phoneNumber,
            context = activity.applicationContext,
            callback = object : IPCoverageCallback {
                override fun onSuccess(response: CoverageResponse) {
                    if (response.isAvailable()) {
                        callAuthentication(activity, phoneNumber, flow, onOpenError)
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
        )
    }

    private fun configureAuthChannels(flow: PnvAuthFlow) {
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
        activity: Activity,
        phoneNumber: String,
        flow: PnvAuthFlow,
        onOpenError: (String) -> Unit
    ) {
        val authRequestBuilder = AuthRequest.Builder()
        authRequestBuilder.addQueryParam("login_hint", phoneNumber)
        val authRequest = authRequestBuilder.build()

        IPificationServices.startAuthentication(activity, authRequest, object : MultiAuthCallback {
            override fun onSuccess(response: IPAuthResponse) {
                viewModelScope.launch {
                    when {
                        response.ts43TokenResponse != null -> Util.openSuccessActivity(activity, response.fullResponse)
                        response.fullResponse.isNotEmpty() -> Util.openSuccessActivity(activity, response.fullResponse)
                        response.code.isNotEmpty() -> IPHelper.callTokenExchangeAPI(activity, response.code)
                        else -> onOpenError("${flow.label} completed without a token response")
                    }
                    setLoading(false)
                }
            }

            override fun onOTPRequired(response: SMSAuthResponse) {
                onOpenError("SMS OTP is required. Use the SMS sample screen if OTP handling is needed.")
                setLoading(false)
            }

            override fun onError(error: IPificationError) {
                onOpenError("${flow.label} error: ${error.getErrorMessage()}")
                setLoading(false)
            }
        })
    }
}
