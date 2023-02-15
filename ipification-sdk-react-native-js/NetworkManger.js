

import {
  NativeModules,
  Platform,
  
} from "react-native";
import Constants from "./Constants";
const { RNAuthenticationService , RNIPConfiguration} = NativeModules;

const registerDevice = async () => {
  //generate State
  var state = await RNAuthenticationService.generateState();
  Constants.CURRENT_STATE = state
  console.log("1. register FCM device token to your backend server ");

  var details = {
    device_id: state,
    device_token: Constants.CURRENT_DEVICE_TOKEN,
    device_type: Platform.OS ,
  };

  fetch(Constants.REGISTER_DEVICE_TOKEN_URL, {
    method: "POST",
    headers: { "Content-Type": "application/json; charset=utf-8" },
    body: JSON.stringify(details),
  })
    .then((response) => response.json())
    .then((responseJson) => {
      console.log("registerDevice response", responseJson);
    })
    .catch((error) => {
      console.error(error);
    });
};
// do at your backend server
const doTokenExchange = async (code, successBlock, failedBlock) => {
  console.log("3. do Token Exchange (call from your backend service)");

  var client_id = await RNIPConfiguration.getClientId();
  var redirect_uri = await RNIPConfiguration.getRedirectUri();

  var details = {
    grant_type: "authorization_code",
    client_secret: Constants.CLIENT_SECRET,
    client_id: client_id,
    redirect_uri: redirect_uri,
    code: code,
  };

  var formBody = [];
  for (var property in details) {
    var encodedKey = encodeURIComponent(property);
    var encodedValue = encodeURIComponent(details[property]);
    formBody.push(encodedKey + "=" + encodedValue);
  }
  formBody = formBody.join("&");
  // console.log(formBody)

  fetch(`${Constants.TOKEN_EXCHANGE_HOST}${Constants.TOKEN_PATH}`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: formBody,
  })
    .then((response) => response.json())
    .then((responseJson) => {
      console.log(responseJson)
      if (responseJson["access_token"]) {
        doPostUserInfo(responseJson["access_token"], successBlock, failedBlock)
      } else {
        failedBlock(responseJson)
      }
    })
    .catch((error) => {
      failedBlock(error)
    });
};
const doPostUserInfo = async (accessToken, successBlock, failedBlock) => {
  console.log("3. do Token Exchange (call from your backend service)");

  var details = {
    "access_token": accessToken
  };

  var formBody = [];
  for (var property in details) {
    var encodedKey = encodeURIComponent(property);
    var encodedValue = encodeURIComponent(details[property]);
    formBody.push(encodedKey + "=" + encodedValue);
  }
  formBody = formBody.join("&");

  fetch(`${Constants.TOKEN_EXCHANGE_HOST}${Constants.GET_USER_INFO_PATH}`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: formBody,
  })
    .then((response) => response.json())
    .then((responseJson) => {
      if (responseJson["phone_number_verified"] == "true" || responseJson["phone_number"]) {
        successBlock(responseJson)
      } else {
        failedBlock(responseJson)
      }
    })
    .catch((error) => {
      failedBlock(error)
    });
};
export default { registerDevice, doTokenExchange };
