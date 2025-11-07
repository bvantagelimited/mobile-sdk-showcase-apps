package com.ipification.ts43sample

import android.util.Log

/**
 * Helper class to store global configuration and logs for TS43 flow
 */
class Helper {
    companion object {
        private const val TAG = "TS43_LOG"
        
        // TS43 endpoint - change this to your backend endpoint
        var TS43_ENDPOINT = "https://test.ipification.com"
        
        // MNC/MCC from active SIM
        var MNCMCC: String = ""
        
        // Logs for debugging and tracking
        var LOG = ""
        var HEADER_LOG = ""

        var SUGGEST_PHONE = ""

        var CLIENT_ID_GET_PHONE_NUMBER = "webclient2"
        var CLIENT_ID_VERIFY_PHONE_NUMBER = "webclient3"
        
        /**
         * Print log message to both Helper.LOG (for display) and console (for debugging)
         * @param message The log message to print
         * @param tag Optional custom tag for console logging (defaults to "TS43_LOG")
         */
        fun printLog(message: String, tag: String = TAG) {
            LOG += message
            Log.d(tag, message.trim())
        }
    }
}
