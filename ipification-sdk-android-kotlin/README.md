
<h1 align="center">IPification Sample Project</h1>

<p align="center">

<img src='https://user-images.githubusercontent.com/4114159/152466467-55a3d411-9206-4975-95fd-868f7b0ed081.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/152466836-9da8ee53-7bd3-4c1e-b4c5-4b10dc328d44.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/152466843-85e0da80-4337-4ef8-a1a3-54df9189de2b.png' width='220'>

</p>
<p align="center">

<img src='https://user-images.githubusercontent.com/4114159/152466467-55a3d411-9206-4975-95fd-868f7b0ed081.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/152466933-805eedac-9414-48f7-821c-f1dc21be4dfe.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/152467595-8e5c7933-01b6-4b9f-b2f5-dee9a133fdf7.png' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/152466843-85e0da80-4337-4ef8-a1a3-54df9189de2b.png' width='220'>
</p>

<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple example steps:

### 1. /assets/ipification-services.json
Update your `client_id` and `redirect_uri`values

  ```json
  {
  "coverage_url": "https://stage.ipification.com/auth/realms/ipification/coverage",
  "authorization_url": "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/auth",
  "client_id": "your_client_id",
  "redirect_uri": "your_redirect_uri"
  }
  ```
  
## 2. Constant.kt
Update your `CLIENT_SECRET` value
  <br/><br/>
  
## 3. IM Authentication
Set up your FCM and override `google-services.json`

Update `DEVICE_TOKEN_REGISTRATION_URL` (for IM Authentication)


# Document

https://developer.ipification.com/#/android/latest/
