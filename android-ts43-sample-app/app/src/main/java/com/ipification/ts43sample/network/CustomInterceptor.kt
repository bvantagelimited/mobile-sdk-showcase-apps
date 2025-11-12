package com.ipification.ts43sample.network

import com.ipification.ts43sample.Helper
import com.ipification.ts43sample.util.Util
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor to log network requests and responses
 */
class CustomInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val t1 = System.nanoTime()
        
        Helper.printLog(
            String.format(
                "--> Sending request %s on %s\n",
                request.url,
                Util.getCurrentDate()
            ),
            "NetworkRequest"
        )
        
        val response = chain.proceed(request)
        val t2 = System.nanoTime()
        
        Helper.printLog(
            String.format(
                "<-- Received response for %s in %.1fms%n%s",
                response.request.url,
                (t2 - t1) / 1e6,
                response.headers
            ),
            "NetworkResponse"
        )
        
        return response
    }
}
