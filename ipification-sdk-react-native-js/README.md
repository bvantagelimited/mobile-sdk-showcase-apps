

<h1 align="center">IPification Sample RN Project</h1>


<!-- GETTING STARTED -->
# Getting Started

## Setup
To get a local copy up and running follow these simple example steps:
#### 1. Extract the IPification plugin and correct the plugin path in `packages.js`:
    "react-native-ipification-library": "./ReactNativeIPificationLibrary_2.0.16",

#### 2. Update `Constants.js`
Update with your `ENV`, `CLIENT_ID` and `REDIRECT_URI` values.
Update with your `CLIENT_SECRET` (for testing only)

#### 3. Home.js -> initIPification() is the function to init our variables
#### 3. Check `PhoneVerifyScreen.js` to see the IP flow


## How to run the project:

1. Run `yarn` to install packages
2. cd ios, run `pod install`
3. cd .. 
4. to run for android app: run `yarn android`
5. to run for iOS app: run `yarn ios`

`Requirements: RN 0.65+`


## Document
For more detail, please check our document: https://developer.ipification.com/#/react-native-plugin/latest/

