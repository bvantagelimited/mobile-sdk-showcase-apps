
<h1 align="center">IPification Sample Project</h1>

<!-- GETTING STARTED -->
## Getting Started

### I. PNV Authentication:
<p align="center">
    <img src='https://github.com/bvantagelimited/mobile-sdk-showcase-apps/assets/4114159/f563302c-d54c-402a-90b9-27053f13a7bd' width='220'>
    <img src='https://github.com/bvantagelimited/mobile-sdk-showcase-apps/assets/4114159/8d9ddc09-a96e-4185-a093-23d721f7e967' width='220'>
</p>


To get a local copy up and running, follow these simple steps:

#### 1. Update with your credentials in `app/build.gradle`:
```
stage {
    buildConfigField "String", "ENVIRONMENT", "\"sandbox\""
    buildConfigField "String", "CLIENT_ID", "\"\""
    buildConfigField "String", "REDIRECT_URI", "\"\""
    buildConfigField "String", "CLIENT_SECRET", "\"\"" (for demo only)

}

``` 
#### 2. Use correct `variant` (stageDebug, stageRelease, productionDebug, productionRelease).

#### 3. Build and Run the project.
---------

#### Core functions:
#### 1. Initial IP configuration
```IPificationAuthActivity.kt
private fun initIPification() {
    IPConfiguration.getInstance().ENV = if(BuildConfig.ENVIRONMENT == "sandbox") IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
    IPConfiguration.getInstance().CLIENT_ID = BuildConfig.CLIENT_ID
    IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI)
}
```
#### 2. CheckCoverage:
```IPificationAuthActivity.kt
private fun startIPAuthenticationFlow() {

    val phoneNumber  = "${binding.countryCodeEditText.text}${binding.phoneCodeEditText.text}"

    // start checking coverage with user phone number
    val coverageCallback = object : IPCoverageCallback
    {
        override fun onSuccess(response: CoverageResponse) {
            if(response.isAvailable()) {
                // supported Telco. call IP Auth function
                callIPAuthentication(phoneNumber)
            } else {
                // unsupported Telco. Fallback to SMS authentication service flow
            }
        }
        override fun onError(error: IPificationError) {
            // error, handle it with SMS authentication service flow
        }
    }
    IPificationServices.startCheckCoverage( phoneNumber = phoneNumber , context = this,  callback = coverageCallback)
}
```
#### 3. Authentication:

```IPificationAuthActivity.kt
private fun callIPAuthentication(phoneNumber: String) {
    val authCallback = object: IPAuthCallback {
        override fun onSuccess(response: IPAuthResponse) {
            // call backend with {response.code}
            IPHelper.callTokenExchangeAPI(this@IPificationAuthActivity, response.code)
        }
        override fun onError(error: IPificationError) {
            // error, handle it with SMS authentication service flow
        }
    }
    val authRequestBuilder = AuthRequest.Builder()
    authRequestBuilder.addQueryParam("login_hint", phoneNumber)
    val authRequest = authRequestBuilder.build()
    IPificationServices.startAuthentication(this, authRequest, authCallback)
}
```
#### 4. Call Token Exchange API with Authorized Code (S2S api)
https://developer.ipification.com/#/android/latest/?id=_14-call-your-backend-service-with-authorization_code-

------------------------------------------------------------------------
------------------------------------------------------------------------

### II. Instant Message Authentication
<p align="center">
<img src='https://user-images.githubusercontent.com/4114159/176865227-d9b565c4-ec0e-44f3-80a4-c39d960ae066.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176865253-856df6fd-a951-4ba0-bf76-22d47d276743.png' width='220'>
    <br/>
<img src='https://user-images.githubusercontent.com/4114159/176865288-c842e3ce-7d9f-45bc-93c8-15f370d48961.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176865314-04082643-c9fc-475d-99b4-c873e1d90152.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176865326-b7eb2c08-0c3f-466c-aa88-712e42eb782f.png' width='220'>
</p>


#### 1. Update with your credentials in `app/build.gradle`
```
stage {
    buildConfigField "String", "ENVIRONMENT", "\"sandbox\""
    buildConfigField "String", "CLIENT_ID", "\"\""
    buildConfigField "String", "REDIRECT_URI", "\"\""
    buildConfigField "String", "CLIENT_SECRET", "\"\"" (for demo only)

}
```

#### 2. Update `DEVICE_TOKEN_REGISTRATION_URL` (for Push Notification)
Set up your FCM and override `google-services.json`
#### 3. Init IP
```IMAuthAutoModeActivity.kt
private fun initIPification(){
    IPConfiguration.getInstance().ENV = if(BuildConfig.ENVIRONMENT == "sandbox" ) IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
    IPConfiguration.getInstance().CLIENT_ID = BuildConfig.CLIENT_ID
    IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI) // this uri should be do S2S to exchange token

    //enable Auto Mode
    // IPConfiguration.getInstance().IM_AUTO_MODE = true // false
    // IPConfiguration.getInstance().IM_PRIORITY_APP_LIST = arrayOf("wa")

}
```
#### 4. Update IM Flow
We offer 3 types of implementation for IM Flow
```MainActivity.kt
private fun openIMActivity() {
    val intent = Intent(applicationContext, IMAuthActivity::class.java) 
//        val intent = Intent(applicationContext, IMAuthAutoModeActivity::class.java) // https://developer.ipification.com/#/android-automode/latest/
//        val intent = Intent(applicationContext, IMAuthManualActivity::class.java) // https://developer.ipification.com/#/android/latest/?id=_3-instant-message-im-authentication-flow-manual-implementation
    startActivity(intent)
}
```

#### 5. Update your client backend URL to check authorize result (automode):
```Constant.kt
const val AUTOMODE_SIGN_IN_URL = 'your-backend-api'
```

#### 6. Use correct `variant` (stageDebug, stageRelease, productionDebug, productionRelease)

#### 7. Run the project on the device

# Document

https://developer.ipification.com/#/android/latest/
