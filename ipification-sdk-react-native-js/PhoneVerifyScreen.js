/**
 * Sample IPification React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */
import React, { useState, useRef, useEffect } from "react";
import { Keyboard } from "react-native";

import {
  SafeAreaView,
  StyleSheet,
  View,
  TouchableOpacity,
  Text,
  NativeModules,
} from "react-native";
const { RNCoverageService, RNAuthenticationService, RNIPConfiguration } =
  NativeModules;
import { Platform } from "react-native";
import { Colors } from "react-native/Libraries/NewAppScreen";
import PhoneInput from "react-native-phone-number-input";

import Constants from "./Constants";
import NetworkManger from "./NetworkManger";

const PhoneVerifyScreen = ({ navigation }) => {
  const [phoneNumber, setPhoneNumnber] = useState("");
  const [formattedValue, setFormattedValue] = useState("");
  const [disabled, setDisabled] = useState(false);
  const phoneInput = useRef<PhoneInput>(null);

  useEffect(() => {}, []);

  checkCoverage = () => {
    Keyboard.dismiss();

    console.log("1. check Coverage");
    RNCoverageService.checkCoverage((error, isAvailable, operatorCode) => {
      console.log(
        "Check Coverage Result - isAvailable ",
        isAvailable,
        operatorCode,
        error
      );
      if (isAvailable) {
        doIPAuthentication();
      } else {
        setDisabled(false);
        navigation.navigate("ResultScreen", {
          name: "Result",
          success: false,
          response: "checkCoverage return false",
        });
      }
    });
  };

  doIPAuthentication = () => {
    setDisabled(true)
    
    console.log("2. do IM Authentication");
    RNAuthenticationService.startAuthorization(
      {
        scope: "openid ip:phone_verify",
        login_hint: formattedValue,
      },
      (error, code, state, fullResponse, userCancelIM) => {
        console.log(code, state, fullResponse, error, userCancelIM);
        if (code != null) {
          doTokenExchange(code);
        }
        else if(userCancelIM){
            // do nothing
        }

        else {
          setDisabled(false);
          navigation.navigate("ResultScreen", {
            name: "Result",
            success: false,
            response: fullResponse,
          });
        }
        setDisabled(false);
      }
    );
  };
  // do at your backend server
  doTokenExchange = async (code) => {
    console.log("3. do Token Exchange (call from your backend service)");

    var successResult = (result) => {
      setDisabled(false);
      navigation.navigate("ResultScreen", {
        name: "Result",
        success: true,
        response: result,
      });
    }
    var failedResult = (result) => {
      setDisabled(false);
      navigation.navigate("ResultScreen", {
        name: "Result",
        success: false,
        response: result,
      });
    }
    NetworkManger.doTokenExchange(code, successResult, failedResult);
  };

  useEffect(() => {
    return () => {
      RNAuthenticationService.unregisterNetwork();
    };
  }, []);


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

        
        <TouchableOpacity
          style={styles.button}
          disabled={disabled}
          onPress={() => {
            checkCoverage();
          }}
        >
          <Text style={styles.buttonText}>Verify</Text>
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
