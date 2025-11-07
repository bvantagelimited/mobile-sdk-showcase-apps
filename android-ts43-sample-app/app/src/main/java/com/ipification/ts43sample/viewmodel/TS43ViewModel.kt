package com.ipification.ts43sample.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.ipification.mobile.sdk.android.utils.DeviceUtils
import com.ipification.ts43sample.Helper
import com.ipification.ts43sample.network.CustomInterceptor
import com.ipification.ts43sample.network.PrintingEventListener
import com.ipification.ts43sample.util.MNCHelper
import com.ipification.ts43sample.util.Util
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * Navigation states for TS43 flow
 */
sealed class TS43Navigation {
    data class ToCredentialManager(val digitalRequest: String, val authReqId: String) : TS43Navigation()
    data class ToResult(val response: String?, val error: String?) : TS43Navigation()
    data object Idle : TS43Navigation()
}

/**
 * UI State for TS43 flow
 */
data class TS43State(
    val isLoading: Boolean = false,
    val message: String = "",
    val navigation: TS43Navigation = TS43Navigation.Idle,
    val ts43AuthReqId: String? = null,
    val countryIso: String? = "",
    val phoneNumber: String = "",
    val countryCode: String = "",
    val clientId: String = "",
    val phonePermissionTrigger: Int = 0  // Counter to trigger permission request
)

/**
 * ViewModel for TS43 authentication flow
 */
class TS43ViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(TS43State())
    val state = _state.asStateFlow()

    /**
     * Update phone number
     */
    fun onPhoneNumberFromHint(fullNumber: String, countryISO: String? = null) {
        Helper.SUGGEST_PHONE += "onPhoneNumberFromHint fullNumber:$fullNumber countryISO:$countryISO\n"
        val phoneUtil = PhoneNumberUtil.getInstance()
        try {
            val parsedNumber = phoneUtil.parse(fullNumber, countryISO)
            val countryDialCode = "+${parsedNumber.countryCode}"
            val nationalNumber = parsedNumber.nationalNumber.toString()
            Helper.SUGGEST_PHONE += "onPhoneNumberFromHint nationalNumber:$nationalNumber countryDialCode:$countryDialCode\n"
            _state.update { it.copy(countryCode = countryDialCode, phoneNumber = "${parsedNumber.countryCode}${nationalNumber}") }
        } catch (e: NumberParseException) {
            _state.update { it.copy(countryCode = fullNumber) }
            Helper.SUGGEST_PHONE += "onPhoneNumberFromHint nationalNumber:$fullNumber error: ${e.message}\n"
        }
    }
    fun onCountryCodeFromHint(countryCode: String) {
        _state.update { it.copy(countryCode = countryCode) }
    }

    /**
     * Start Verify Phone Number flow
     */
    fun startVerifyPhoneNumber(context: Context) {
        val phoneNumber = state.value.phoneNumber
        val clientId = Helper.CLIENT_ID_VERIFY_PHONE_NUMBER
        
        if (phoneNumber.isBlank()) {
            _state.update { it.copy(message = "❌ Please enter a phone number") }
            return
        }
        
        // Validate phone number format (basic E.164 check)
        if (phoneNumber.length < 10) {
            _state.update { it.copy(message = "❌ Phone number must be in E.164 format (e.g., +1234567890)") }
            return
        }
        
        if (clientId.isBlank()) {
            handleError("Client ID is required")
            return
        }
        
        // Reset logs
        Helper.LOG = ""
        Helper.HEADER_LOG = "TS43 FLOW - VERIFY PHONE NUMBER - VERSION 1.0.0\n\n"
        
        // Get MNCMCC from voice SIM
        Helper.MNCMCC = MNCHelper.getVoiceSimMNCMCC(context)
        Helper.HEADER_LOG += "VOICE SIM MNCMCC: ${Helper.MNCMCC}\n\n"
        
        // Add device info to header logs
        Helper.HEADER_LOG += DeviceUtils.getInstance(context).generateHeaderLogs(phoneNumber)
        
        callVerifyPhoneNumberAuth(phoneNumber, clientId)
    }

    /**
     * Start Get Phone Number flow
     */
    fun startGetPhoneNumber(context: Context) {
        val clientId = Helper.CLIENT_ID_GET_PHONE_NUMBER
        
        if (clientId.isBlank()) {
            handleError("Client ID is required")
            return
        }
        
        // Reset logs
        Helper.LOG = ""
        Helper.HEADER_LOG = "TS43 FLOW - GET PHONE NUMBER - VERSION 1.0.0\n\n"
        
        // Get MNCMCC from voice SIM
        Helper.MNCMCC = MNCHelper.getVoiceSimMNCMCC(context)
        Helper.HEADER_LOG += "VOICE SIM MNCMCC: ${Helper.MNCMCC}\n\n"
        
        // Add device info to header logs (empty phone number for GetPhoneNumber flow)
        Helper.HEADER_LOG += DeviceUtils.getInstance(context).generateHeaderLogs("")
        
        // No phone number required for GetPhoneNumber flow - use empty login_hint
        callGetPhoneNumberAuth(clientId)
    }

    /**
     * Call TS43 CIBA Auth endpoint (Verify Phone Number)
     */
    private fun callVerifyPhoneNumberAuth(loginHint: String, clientId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = "Verifying phone number...") }
            try {
                val response = performTS43Auth(
                    loginHint = loginHint,
                    clientId = clientId,
                    scope = "ip:phone_verify",
                    operation = "VerifyPhoneNumber",
                    flowType = "VERIFY_PHONE"
                )
                parseTS43Response(response)
            } catch (e: IOException) {
                handleError("Verify Phone Number Failed: ${e.message}")
            }
        }
    }

    /**
     * Call TS43 CIBA Auth endpoint (Get Phone Number)
     */
    private fun callGetPhoneNumberAuth(clientId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = "Getting phone number...") }
            try {
                val response = performTS43Auth(
                    loginHint = null,
                    clientId = clientId,
                    scope = "openid ip:phone",
                    operation = "GetPhoneNumber",
                    flowType = "GET_PHONE"
                )
                parseTS43Response(response)
            } catch (e: IOException) {
                handleError("Get Phone Number Failed: ${e.message}")
            }
        }
    }

    /**
     * Unified method to perform TS43 Auth API call
     * Used by both GetPhoneNumber and VerifyPhoneNumber flows
     */
    private suspend fun performTS43Auth(
        loginHint: String?,
        clientId: String,
        scope: String,
        operation: String,
        flowType: String
    ): String = withContext(Dispatchers.IO) {
        val url = "${Helper.TS43_ENDPOINT}/ts43/auth"
        val carrierHint = Helper.MNCMCC.ifEmpty { "51004" } // Default for testing

        val json = if(loginHint != null) {
            """{"login_hint":"$loginHint", "carrier_hint":"$carrierHint", "client_id":"$clientId", "scope":"$scope", "operation":"$operation"}"""
        } else """{"login_hint":"anonymous", "carrier_hint":"$carrierHint", "client_id":"$clientId", "scope":"$scope", "operation":"$operation"}"""

        // Log request
        val flowName = if (flowType == "GET_PHONE") "GET PHONE NUMBER" else "VERIFY PHONE NUMBER"
        Helper.printLog("\n========== TS43 $flowName REQUEST ==========\n")
        Helper.printLog("[APIManager] ${Util.getCurrentDate()} - TS43_$flowType - START\n")
        Helper.printLog("[URL] $url\n")
        Helper.printLog("[Method] POST\n")
        Helper.printLog("[Headers] Content-Type: application/json\n")
        Helper.printLog("[Request Body] $json\n")
        Helper.printLog("[Parameters] loginHint=$loginHint, scope=$scope, clientId=$clientId, operation=$operation\n")
        Helper.printLog("======================================================\n\n")
        
        Log.d("TS43_$flowType", "========== REQUEST ==========")
        Log.d("TS43_$flowType", "URL: $url")
        Log.d("TS43_$flowType", "Body: $json")
        
        val requestBody = json.toRequestBody("application/json".toMediaType())
        val client = OkHttpClient.Builder()
            .addInterceptor(CustomInterceptor())
            .eventListener(PrintingEventListener())
            .build()

        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            val responseCode = response.code
            val responseHeaders = response.headers.toString()
            
            // Log response
            Helper.printLog("\n========== TS43 $flowName RESPONSE ==========\n")
            Helper.printLog("[APIManager] ${Util.getCurrentDate()} - TS43_$flowType - ${if (response.isSuccessful) "SUCCESS" else "FAILED"}\n")
            Helper.printLog("[Status Code] $responseCode\n")
            Helper.printLog("[Headers]\n$responseHeaders\n")
            Helper.printLog("[Response Body] $responseBody\n")
            Helper.printLog("======================================================\n\n")
            
            Log.d("TS43_$flowType", "========== RESPONSE ==========")
            Log.d("TS43_$flowType", "Status: ${if (response.isSuccessful) "SUCCESS" else "FAILED"}")
            Log.d("TS43_$flowType", "Code: $responseCode")
            Log.d("TS43_$flowType", "Body: $responseBody")
            
            if (!response.isSuccessful) {
                throw IOException("Unexpected code $responseCode: $responseBody")
            }
            responseBody
        }
    }

    /**
     * Parse TS43 response to extract digital_request and auth_req_id
     */
    private fun parseTS43Response(response: String) {
        try {
            Helper.printLog("[APIManager] ${Util.getCurrentDate()} - TS43_PARSE - Parsing response.\n")

            val root = JSONObject(response)
            val authReqId = root.optString("auth_req_id", "")
            val digitalRequestObj = root.optJSONObject("digital_request")
            val digitalRequest = digitalRequestObj?.toString() ?: ""

            if (authReqId.isBlank() || digitalRequest.isBlank()) {
                handleError("Failed to parse TS43 response: missing 'auth_req_id' or 'digital_request'")
                return
            }

            Helper.printLog("[APIManager] ${Util.getCurrentDate()} - TS43_PARSE - Success. AuthReqId: $authReqId\n")
            _state.update {
                it.copy(
                    navigation = TS43Navigation.ToCredentialManager(digitalRequest, authReqId),
                    ts43AuthReqId = authReqId,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Helper.printLog("[APIManager] ${Util.getCurrentDate()} - TS43_PARSE - Error: ${e.message}\n")
            handleError("Failed to parse TS43 response: ${e.message}")
        }
    }

    /**
     * Handle credential received from Credential Manager
     */
    fun onCredentialReceived(credentialJson: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = "Processing credential...") }
            try {
                Helper.printLog("[APIManager] ${Util.getCurrentDate()} - CREDENTIAL_RECEIVED - Success\n")
                Helper.printLog("[APIManager] Credential JSON: $credentialJson\n")
                
                Log.d("CREDENTIAL", "========== RECEIVED ==========")
                Log.d("CREDENTIAL", "Full JSON: $credentialJson")
                
                val vpToken = extractVpToken(credentialJson)
                
                Log.d("CREDENTIAL", "Extracted vp_token: ${vpToken?.take(100)}...")
                Log.d("CREDENTIAL", "auth_req_id: ${state.value.ts43AuthReqId}")
                
                if (vpToken != null && state.value.ts43AuthReqId != null) {
                    Helper.printLog("[APIManager] ${Util.getCurrentDate()} - CREDENTIAL_PARSE - Extracted vp_token (length: ${vpToken.length})\n")
                    val tokenResponse = performTS43TokenExchange(vpToken, state.value.ts43AuthReqId!!, state.value.clientId)
                    
                    // Success - show message in same UI
                    _state.update { 
                        it.copy(
                            navigation = TS43Navigation.Idle,
                            isLoading = false,
                            message = "✅ Authentication successful! Phone number verified."
                        )
                    }
                    Log.d("TS43_SUCCESS", "Token response: $tokenResponse")
                } else {
                    Helper.printLog("[APIManager] ${Util.getCurrentDate()} - CREDENTIAL_PARSE - Error: Missing vp_token or auth_req_id\n")
                    Log.e("CREDENTIAL", "ERROR: Missing vp_token or auth_req_id")
                    handleError("Missing vp_token or auth_req_id")
                }
            } catch (e: Exception) {
                Log.e("CREDENTIAL", "ERROR processing credential", e)
                handleError("Failed to process credential: ${e.message}")
            }
        }
    }

    /**
     * Extract vp_token from credential JSON
     */
    private fun extractVpToken(credentialJson: String): String? {
        return try {
            val ipificationPattern = "\"ipification\\.com\"\\s*:\\s*\\[\\s*\"([^\"]+)\"".toRegex()
            val match = ipificationPattern.find(credentialJson)
            match?.groupValues?.get(1)
        } catch (e: Exception) {
            Helper.printLog("[APIManager] ${Util.getCurrentDate()} - EXTRACT_VP_TOKEN - Error: ${e.message}\n")
            null
        }
    }

    /**
     * Perform TS43 token exchange
     */
    private suspend fun performTS43TokenExchange(vpToken: String, authReqId: String, clientid: String): String = withContext(Dispatchers.IO) {
        val url = "${Helper.TS43_ENDPOINT}/ts43/token"
        val json = """{"vp_token":"$vpToken", "auth_req_id":"$authReqId", "client_id":"$clientid"}"""
        
        // Log token exchange request
        Helper.printLog("\n========== TS43 TOKEN EXCHANGE REQUEST ==========\n")
        Helper.printLog("[APIManager] ${Util.getCurrentDate()} - TS43_TOKEN_EXCHANGE - START\n")
        Helper.printLog("[URL] $url\n")
        Helper.printLog("[Method] POST\n")
        Helper.printLog("[Headers] Content-Type: application/json\n")
        Helper.printLog("[Request Body] $json\n")
        Helper.printLog("=================================================\n\n")
        
        Log.d("TS43_TOKEN", "========== REQUEST ==========")
        Log.d("TS43_TOKEN", "URL: $url")
        Log.d("TS43_TOKEN", "Body: $json")
        
        val requestBody = json.toRequestBody("application/json".toMediaType())
        val client = OkHttpClient.Builder()
            .addInterceptor(CustomInterceptor())
            .eventListener(PrintingEventListener())
            .build()
        
        val request = Request.Builder().url(url).post(requestBody).build()
        
        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            val responseCode = response.code
            val responseHeaders = response.headers.toString()
            
            // Log token exchange response
            Helper.printLog("\n========== TS43 TOKEN EXCHANGE RESPONSE ==========\n")
            Helper.printLog("[APIManager] ${Util.getCurrentDate()} - TS43_TOKEN_EXCHANGE - ${if (response.isSuccessful) "SUCCESS" else "FAILED"}\n")
            Helper.printLog("[Status Code] $responseCode\n")
            Helper.printLog("[Headers]\n$responseHeaders\n")
            Helper.printLog("[Response Body] $responseBody\n")
            Helper.printLog("==================================================\n\n")
            
            Log.d("TS43_TOKEN", "========== RESPONSE ==========")
            Log.d("TS43_TOKEN", "Status: ${if (response.isSuccessful) "SUCCESS" else "FAILED"}")
            Log.d("TS43_TOKEN", "Code: $responseCode")
            Log.d("TS43_TOKEN", "Body: $responseBody")
            
            if (!response.isSuccessful) {
                handleError("Token Exchange - Unexpected code $responseCode: $responseBody")
            }
            responseBody
        }
    }

    /**
     * Handle errors
     */
    private fun handleError(error: String) {
        _state.update { 
            it.copy(
                isLoading = false,
                message = "❌ Error: $error",
                navigation = TS43Navigation.Idle
            )
        }
    }

    /**
     * Reset navigation state and clear message
     */
    fun onNavigationHandled() {
        _state.update { it.copy(navigation = TS43Navigation.Idle, message = "") }
    }

    /**
     * Get full logs
     */
    fun getLogs(): String {
        return "${Helper.HEADER_LOG}\n${Helper.LOG}"
    }
}
