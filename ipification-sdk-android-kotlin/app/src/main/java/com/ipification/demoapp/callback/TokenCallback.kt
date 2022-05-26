package com.ipification.demoapp.callback

interface TokenCallback {
    fun result(response: String?, errorMessage: String?)
}