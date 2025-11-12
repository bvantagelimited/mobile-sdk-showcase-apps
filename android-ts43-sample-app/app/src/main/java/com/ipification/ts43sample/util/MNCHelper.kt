package com.ipification.ts43sample.util

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * Helper class to get MNC/MCC information from the active SIM
 */
object MNCHelper {
    
    private const val TAG = "MNCHelper"
    
    /**
     * Get MNCMCC from the active VOICE/DIAL SIM
     * This is the SIM card used for making phone calls
     * 
     * @param context Application context
     * @return String in format "MCCMNC" (e.g. "310260" for T-Mobile US)
     */
    @SuppressLint("MissingPermission")
    fun getVoiceSimMNCMCC(context: Context): String {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            
            return when {
                // Android N (API 24) and above - Use getDefaultVoiceSubscriptionId
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
                    getVoiceSimMNCMCCFromSubscription(context, telephonyManager)
                }
                // Older versions - Use default telephony manager
                else -> {
                    getDefaultMNCMCC(telephonyManager)
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Missing permission to read phone state: ${e.message}")
            return ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting VOICE SIM MNCMCC: ${e.message}")
            return ""
        }
    }
    
    /**
     * Get MNCMCC from voice subscription (Android N+)
     */
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    private fun getVoiceSimMNCMCCFromSubscription(context: Context, telephonyManager: TelephonyManager): String {
        try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            
            if (subscriptionManager != null) {
                // Get default voice call subscription ID (the SIM used for calls/USSD)
                val defaultVoiceSubId = SubscriptionManager.getDefaultVoiceSubscriptionId()
                
                Log.d(TAG, "Default Voice SubId: $defaultVoiceSubId")
                
                if (defaultVoiceSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                    // Create TelephonyManager for the voice SIM
                    val voiceSimTelephonyManager = telephonyManager.createForSubscriptionId(defaultVoiceSubId)
                    
                    // Get network operator (MCCMNC)
                    val networkOperator = voiceSimTelephonyManager.networkOperator
                    
                    if (!networkOperator.isNullOrEmpty() && networkOperator.length >= 5) {
                        Log.d(TAG, "Voice SIM MNCMCC: $networkOperator")
                        return networkOperator
                    } else {
                        // Try SIM operator as fallback
                        val simOperator = voiceSimTelephonyManager.simOperator
                        if (!simOperator.isNullOrEmpty() && simOperator.length >= 5) {
                            Log.d(TAG, "Voice SIM MNCMCC (from simOperator): $simOperator")
                            return simOperator
                        }
                    }
                }
            }
            
            // Fallback to default
            return getDefaultMNCMCC(telephonyManager)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in getVoiceSimMNCMCCFromSubscription: ${e.message}")
            return getDefaultMNCMCC(telephonyManager)
        }
    }
    
    /**
     * Get MNCMCC from default telephony manager (fallback)
     */
    @SuppressLint("MissingPermission")
    private fun getDefaultMNCMCC(telephonyManager: TelephonyManager): String {
        return try {
            val networkOperator = telephonyManager.networkOperator
            if (!networkOperator.isNullOrEmpty() && networkOperator.length >= 5) {
                Log.d(TAG, "Default MNCMCC: $networkOperator")
                networkOperator
            } else {
                val simOperator = telephonyManager.simOperator
                if (!simOperator.isNullOrEmpty() && simOperator.length >= 5) {
                    Log.d(TAG, "Default MNCMCC (from simOperator): $simOperator")
                    simOperator
                } else {
                    Log.w(TAG, "No valid MNCMCC found")
                    ""
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in getDefaultMNCMCC: ${e.message}")
            ""
        }
    }
    
    /**
     * Parse MNCMCC string into MCC and MNC components
     * 
     * @param mncmcc Combined MCCMNC string (e.g. "310260")
     * @return Pair of (MCC, MNC) or ("", "") if invalid
     */
    fun parseMNCMCC(mncmcc: String): Pair<String, String> {
        return if (mncmcc.length >= 5) {
            val mcc = mncmcc.substring(0, 3)
            val mnc = mncmcc.substring(3)
            Pair(mcc, mnc)
        } else {
            Pair("", "")
        }
    }
}
