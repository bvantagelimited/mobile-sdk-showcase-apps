package com.ipification.ts43sample.network

import android.util.Log
import com.ipification.ts43sample.Helper
import okhttp3.Call
import okhttp3.EventListener

/**
 * OkHttp event listener to track network events
 */
internal class PrintingEventListener : EventListener() {
    private var callStartNanos: Long = 0
    
    private fun printEvent(name: String) {
        val nowNanos = System.nanoTime()
        if (name == "callStart") {
            callStartNanos = nowNanos
        }
        val elapsedNanos = nowNanos - callStartNanos
        val str = String.format("%.3f %s%n", elapsedNanos / 1000000000.0, name)
        Helper.LOG += "$str\n"
        Log.d("NetworkEvent", str)
    }

    override fun callStart(call: Call) {
        printEvent("callStart")
    }

    override fun callEnd(call: Call) {
        printEvent("callEnd")
    }
}
