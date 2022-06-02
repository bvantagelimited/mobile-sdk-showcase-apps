package com.ipification.demoapp.callback

interface IPAuthorizationCallback {
    fun result(code: String?, errorMessage: String?)
}
