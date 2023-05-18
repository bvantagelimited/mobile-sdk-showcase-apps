
<h1 align="center">IPification Sample Project</h1>

### IPification Verification:
<p align="center">
    <img src='https://user-images.githubusercontent.com/4114159/224629567-189dcace-a895-4fad-8588-93b955960452.jpg' width='220'>
    <img src='https://user-images.githubusercontent.com/4114159/153834085-686378a6-97e2-4c49-9871-f12db5fc16e1.png' width='220'>
    <img src='https://user-images.githubusercontent.com/4114159/153834072-52712b4e-a3fc-43e4-a07a-0df018f7374f.jpg' width='220'>
</p>

### IM Authentication:
<p align="center">
  <img src='https://user-images.githubusercontent.com/4114159/224629326-9a1c850a-4cce-47d3-9cbc-6de49036cc04.png' width='180'>
<img src='https://user-images.githubusercontent.com/4114159/153820778-b4e6cb13-e4b9-4eb9-96ec-b7596617d906.jpg' width='180'>
<img src='https://user-images.githubusercontent.com/4114159/153820768-fe862eb3-01b2-46de-b140-5ac55e6008bf.jpg' width='180'>
  <img src='https://user-images.githubusercontent.com/4114159/153827230-b896b66b-5b82-421d-b639-755b50d218c8.png' width='180'>

  <img src='https://user-images.githubusercontent.com/4114159/153826264-74a50ef3-9847-4ca4-bed0-5278863a3222.png' width='180'>

</p>

<!-- GETTING STARTED -->
## Getting Started

To get a local copy up and running follow these simple example steps:


### IPIFICATION AUTHENTICATION

### 1. Update IP's Configuration in HomeViewController.swift
```
IPConfiguration.sharedInstance.ENV = IPEnvironment.PRODUCTION // 1. SANDBOX or PRODUCTION
```

### 2. Update Constants.swift
- Update endpoint of IP's HOST // production or sandbox
```
static let HOST = "https://api.ipification.com" // sandbox: https://stage.ipification.com or production: https://api.ipification.com

```
- Update with your `CLIENT_ID` `CLIENT_SECRET`, and `REDIRECT_URI` values
```
static let CLIENT_ID = "your_client_id"
static let REDIRECT_URI = "your_redirect_uri"
// warning: put here only for demo
static let CLIENT_SECRET = "your_client_secret" // 
```

----------------------

### IM AUTHENTICATION

### 1. Update IP's Configuration in HomeViewController.swift
```
IPConfiguration.sharedInstance.ENV = IPEnvironment.PRODUCTION // 1. SANDBOX or PRODUCTIOn
```

### 2. Update Constants.swift
- Update endpoint of IP's HOST // production or sandbox
```
static let HOST = "https://api.ipification.com" // sandbox: https://stage.ipification.com or production: https://api.ipification.com

```
- Update with your `CLIENT_ID` `CLIENT_SECRET`, and `REDIRECT_URI` values
```
static let CLIENT_ID = "your_client_id"
static let REDIRECT_URI = "your_redirect_uri"
// warning: put here only for demo
static let CLIENT_SECRET = "your_client_secret" // 
```

- Update `REGISTER_DEVICE_URL` (for Push Notification)
- Set up your FCM and override `GoogleService-Info.plist`

  

# Document

https://developer.ipification.com/#/ios/latest/
