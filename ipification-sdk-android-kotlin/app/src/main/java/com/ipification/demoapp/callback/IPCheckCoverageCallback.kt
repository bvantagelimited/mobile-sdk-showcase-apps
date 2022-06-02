package com.ipification.demoapp.callback

interface IPCheckCoverageCallback {
    fun result(isAvailable: Boolean, operatorCode: String?, errorMessage: String?)
}
