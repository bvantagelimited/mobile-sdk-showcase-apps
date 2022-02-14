
<h1 align="center">IPification Sample Project</h1>

<p align="center">
<img src='https://user-images.githubusercontent.com/4114159/153820731-6cf4d6ed-cc37-4cc2-8a16-8baccb41b9d3.jpg' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/153820778-b4e6cb13-e4b9-4eb9-96ec-b7596617d906.jpg' width='220'>
<img src='https://user-images.githubusercontent.com/4114159/153820768-fe862eb3-01b2-46de-b140-5ac55e6008bf.jpg' width='220'>
</p>
<p align="center">
  
<img src='https://user-images.githubusercontent.com/4114159/153825820-4fed7e23-439b-406d-a323-690bb6376b72.png' width='220'>
  
  <img src='https://user-images.githubusercontent.com/4114159/153826264-74a50ef3-9847-4ca4-bed0-5278863a3222.png' width='220'>

</p>

<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple example steps:

### 1. Edit Debug.xcconfig
Update your `client_id` and `redirect_uri`values

  ```json
COVERAGE_URL = https:\/\/stage.ipification.com/auth/realms/ipification/coverage
AUTHORIZATION_URL = https:\/\/stage.ipification.com/auth/realms/ipification/protocol/openid-connect/auth
REDIRECT_URI = your-redirect-uri
CLIENT_ID = your-client-id
  ```
  
## 2. Constants.swift
Update your `CLIENT_SECRET` value
  <br/><br/>
  
## 3. IM Authentication
Set up your FCM and override `google-services.json`

Update `REGISTER_DEVICE_URL` (for IM Authentication)


# Document

https://developer.ipification.com/#/ios/latest/
