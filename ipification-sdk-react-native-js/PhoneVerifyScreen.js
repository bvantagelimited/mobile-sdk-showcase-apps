/**
 * Sample IPification React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */
import React, { useState, useRef, useEffect } from "react";
import { Keyboard } from 'react-native'

import {
  SafeAreaView,
  StyleSheet,
  View,
  TouchableOpacity,
  Text,
  NativeModules,
} from "react-native";
const { RNCoverageService, RNAuthenticationService , RNIPConfiguration} = NativeModules;
import { Platform } from "react-native";
import { Colors } from "react-native/Libraries/NewAppScreen";
import PhoneInput from "react-native-phone-number-input";

import Constants from "./Constants";

const PhoneVerifyScreen = ({navigation}) => {
 
  const [phoneNumber, setPhoneNumnber] = useState("");
  const [token, setToken] = useState("");
  const [formattedValue, setFormattedValue] = useState("");
  const [coverageResult, setCoverageResult] = useState(false);
  const [authorizationResult, setAuthorizationResult] = useState();
  const [disabled, setDisabled] = useState(false);
  const [showMessage, setShowMessage] = useState(false);
  const phoneInput = useRef<PhoneInput>(null);

  useEffect(() => {
    
  }, []);
  
  checkCoverage = () => {
    Keyboard.dismiss()

    
    console.log("1. check Coverage");
    RNCoverageService.checkCoverage((error, isAvailable, operatorCode) => {
      console.log("Check Coverage Result - isAvailable ", isAvailable, operatorCode, error);
      if (isAvailable) {
        setCoverageResult(isAvailable);
        doIPAuthentication();
       
      } else {
        setCoverageResult(isAvailable || error);
        setDisabled(false);
      }
    });
  };

  
  doIPAuthentication = () => {
    // var state = getRandomValues(); // optional
    console.log("2. do IM Authentication");
    RNAuthenticationService.startAuthorization(
      {
        scope: "openid ip:phone_verify",
        login_hint: formattedValue
      },
      (error, code, state, fullResponse) => {
        console.log(code, state, fullResponse, error);
        if (code != null) {
          setAuthorizationResult(code);
          doTokenExchange();
        } else {
          setAuthorizationResult(error);
        }
        setDisabled(false);
      }
    );
  };
  // do at your backend server
  doTokenExchange = async () => {
    console.log("3. do Token Exchange (call from your backend service)");

    var client_id = await RNIPConfiguration.getClientId()
    console.log("client_id,", client_id);
    var redirect_uri = await RNIPConfiguration.getRedirectUri()
    var details = {
      client_id: client_id,
      grant_type: "authorization_code",
      client_secret: Constants.CLIENT_SECRET,
      redirect_uri: redirect_uri,
      code: authorizationResult,
    };
    var formBody = [];
    for (var property in details) {
      var encodedKey = encodeURIComponent(property);
      var encodedValue = encodeURIComponent(details[property]);
      formBody.push(encodedKey + "=" + encodedValue);
    }
    formBody = formBody.join("&");
    // console.log(formBody)
    fetch(Constants.TOKEN_URL, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: formBody,
    })
      .then((response) => response.json())
      .then((responseJson) => {
        console.log(responseJson)
        if (responseJson["access_token"]) {
          setToken(responseJson["access_token"])
          navigation.navigate("ResultScreen", {
            name: "ResultScreen",
            success: true,
            accessToken: responseJson["access_token"]
          })
        } else {
          navigation.navigate("ResultScreen", {
            name: "Result",
            success: false,
            error: JSON.stringify(responseJson)
          })
        }
      })
      .catch((error) => {
        console.error(error);
        setToken(JSON.stringify(error));
      });
  };

  useEffect(() => {
    return () => {
      if (Platform.OS === "android") {
        console.log("componentWillUnmount android");
        RNAuthenticationService.unregisterNetwork();
      }
    };
  }, []);
  //util
  // getRandomStateValues = () => {
  //   const validChars =
  //     "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
  //   var result = "";
  //   for (var i = 0; i < 40; i++) {
  //     result += validChars.charAt(
  //       Math.floor(Math.random() * validChars.length)
  //     );
  //   }
  //   return result;
  // };

  
  return (
    <View style={styles.container}>
      <SafeAreaView style={styles.wrapper}>
       
        <PhoneInput
          ref={phoneInput}
          defaultValue={phoneNumber}
          defaultCode="RS"
          layout="first"
          onChangeText={(text) => {
            setPhoneNumnber(text);
          }}
          onChangeFormattedText={(text) => {
            setFormattedValue(text);
          }}
          countryPickerProps={{ withAlphaFilter: true }}
          disabled={disabled}
          withDarkTheme
          withShadow
          autoFocus
        />

        {showMessage && (
          <View style={styles.message}>
            <Text>Formatted Phone Number : {formattedValue}</Text>
            <Text>
              1. Supported Telco :{" "}
              {coverageResult == true
                ? "true"
                : coverageResult == false
                ? "false"
                : "false - " +coverageResult}
            </Text>
            <Text>2. Do Authentication - Result : {authorizationResult}</Text>
            <Text>3. Token Exchange - Access Token : {token}</Text>
          </View>
        )}
        <TouchableOpacity
          style={styles.button}
          onPress={() => {
            const checkValid = phoneInput.current?.isValidNumber(phoneNumber);
            console.log("checkValid Phone ", checkValid);
            setShowMessage(true);
            checkCoverage();
          }}
        >
          <Text style={styles.buttonText}>Login</Text>
        </TouchableOpacity>
      </SafeAreaView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.lighter,
  },
  wrapper: {
    marginTop: 30,
    flex: 1,
    alignItems: "center",
  },
  button: {
    marginTop: 20,
    height: 50,
    width: 300,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#7CDB8A",
    shadowColor: "rgba(0,0,0,0.4)",
    shadowOffset: {
      width: 1,
      height: 5,
    },
    shadowOpacity: 0.34,
    shadowRadius: 6.27,
    elevation: 10,
  },
  buttonText: {
    color: "white",
    fontSize: 14,
  },
  redColor: {
    backgroundColor: "#F57777",
  },
  message: {
    borderWidth: 1,
    borderRadius: 5,
    padding: 20,
    marginBottom: 20,
    justifyContent: "center",
    alignItems: "flex-start",
  },
});

export default PhoneVerifyScreen;
