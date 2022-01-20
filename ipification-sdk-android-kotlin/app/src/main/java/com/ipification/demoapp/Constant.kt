package com.ipification.demoapp

class Constant {

    companion object Factory {
        const val CLIENT_SECRET = "CLIENT_SECRET"
        const val TOKEN_URL =
            "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token"
        const val DEVICE_TOKEN_REGISTRATION_URL =
            "your_backend_register_token_api"
    }
}