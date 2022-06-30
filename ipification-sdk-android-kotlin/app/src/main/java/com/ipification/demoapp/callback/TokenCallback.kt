package com.ipification.demoapp.callback

interface TokenCallback {
    fun onSuccess(response: String)
    fun onError(errorMessage: String)
}