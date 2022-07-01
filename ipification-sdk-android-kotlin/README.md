
<h1 align="center">IPification Sample Project</h1>

<p align="center">
<img src='https://user-images.githubusercontent.com/4114159/176865959-8c16cbd7-cdee-4cb4-bd37-7bdd2fce7659.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176863776-8961c9d7-a64f-4b14-965e-1ddc222bd96e.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/176863792-ee7ffc89-600e-42f8-ad75-2475726c5929.png' width='220'>

</p>
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


### 1. Update `app/build.gradle`
```
stage {
    buildConfigField "String", "ENVIRONMENT", "\"sandbox\""
    buildConfigField "String", "CLIENT_ID", "\"\""
    buildConfigField "String", "REDIRECT_URI", "\"\""
}
```
### 2 Update Constant.kt
Update `CLIENT_SECRET`

### 2. IM Authentication
Update `DEVICE_TOKEN_REGISTRATION_URL` (for Push Notification)
Set up your FCM and override `google-services.json`

We offer 3 types of implementation for IM Flow
```
private fun openIMActivity() {
    val intent = Intent(applicationContext, IMAuthActivity::class.java)
//        val intent = Intent(applicationContext, IMAuthAutoModeActivity::class.java)
//        val intent = Intent(applicationContext, IMAuthManualActivity::class.java)
    startActivity(intent)
}
```

# Document

https://developer.ipification.com/#/android/latest/
