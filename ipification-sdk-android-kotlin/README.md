
<h1 align="center">IPification Sample Project</h1>

#### 1. IP Authentication
<p align="center">
<img src='https://user-images.githubusercontent.com/4114159/176865959-8c16cbd7-cdee-4cb4-bd37-7bdd2fce7659.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176863776-8961c9d7-a64f-4b14-965e-1ddc222bd96e.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176863792-ee7ffc89-600e-42f8-ad75-2475726c5929.png' width='220'>

</p>


#### 2. IM Authentication
<p align="center">
<img src='https://user-images.githubusercontent.com/4114159/176865974-427bad75-1993-4d25-ba2e-c3f742575d84.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176865227-d9b565c4-ec0e-44f3-80a4-c39d960ae066.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176865253-856df6fd-a951-4ba0-bf76-22d47d276743.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176865288-c842e3ce-7d9f-45bc-93c8-15f370d48961.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176865314-04082643-c9fc-475d-99b4-c873e1d90152.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176865326-b7eb2c08-0c3f-466c-aa88-712e42eb782f.png' width='220'>
</p>


<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple example steps:

### I. IP Authentication:

#### 1. Update with your credentials in `app/build.gradle`
```
stage {
    buildConfigField "String", "ENVIRONMENT", "\"sandbox\""
    buildConfigField "String", "CLIENT_ID", "\"\""
    buildConfigField "String", "REDIRECT_URI", "\"\""
    buildConfigField "String", "CLIENT_SECRET", "\"\""

}
```

#### 2. Use correct `variant` (stageDebug, stageRelease, productionDebug, productionRelease)

#### 3. Run the project on the device



------------------------------------------------------------------------

### II. IM Authentication

#### 1. Update with your credentials in `app/build.gradle`
```
stage {
    buildConfigField "String", "ENVIRONMENT", "\"sandbox\""
    buildConfigField "String", "CLIENT_ID", "\"\""
    buildConfigField "String", "REDIRECT_URI", "\"\""
    buildConfigField "String", "CLIENT_SECRET", "\"\""

}
```

#### 2. Update `DEVICE_TOKEN_REGISTRATION_URL` (for Push Notification)
Set up your FCM and override `google-services.json`

We offer 3 types of implementation for IM Flow
```MainActivity.kt
private fun openIMActivity() {
    val intent = Intent(applicationContext, IMAuthActivity::class.java) 
//        val intent = Intent(applicationContext, IMAuthAutoModeActivity::class.java) // https://developer.ipification.com/#/android-automode/latest/
//        val intent = Intent(applicationContext, IMAuthManualActivity::class.java) // https://developer.ipification.com/#/android/latest/?id=_3-instant-message-im-authentication-flow-manual-implementation
    startActivity(intent)
}
```
#### 3. Use correct `variant` (stageDebug, stageRelease, productionDebug, productionRelease)

#### 4. Update your client backend URL to check authorize result (automode):
```Constant.kt
const val AUTOMODE_SIGN_IN_URL = ''
```

#### 5. Run the project on the device

# Document

https://developer.ipification.com/#/android/latest/
