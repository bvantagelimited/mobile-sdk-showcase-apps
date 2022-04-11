/**
 * Sample IPification React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */
import React, { useEffect, useState } from "react";
import {
  NativeModules,
  Platform,
  SafeAreaView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from "react-native";
import { Colors } from "react-native/Libraries/NewAppScreen";
import Constants from "./Constants";

const { RNAuthenticationService, RNIPConfiguration } = NativeModules;

const HomeScreen = ({ navigation }) => {
  const [authorizationResult, setAuthorizationResult] = useState();
  const [disabled, setDisabled] = useState(false);

  useEffect(() => {
    localize();
  }, []);
  localize = async () => {
    if (Platform.OS === "android") {
      RNAuthenticationService.updateAndroidLocale({
        mainTitle: "Phone Number Verification",
        description:
          "Please tap on the preferred messaging app then follow instructions on the screen",
        whatsappBtnText: "Login with WhatsApp C",
        telegramBtnText: "Login with Telegram",
        viberBtnText: "Login with Viber",
      });
      RNAuthenticationService.updateAndroidTheme({
        backgroundColor: "#ffffff",
        toolbarTextColor: "#000000",
        toolbarColor: "#000000",
        toolbarTitle: "toolbarTitle",
        isVisible: false,
      });
    }

    // set configuration runtime
    // RNIPConfiguration.setCheckCoverageUrl("https://stage.ipification.com/auth/realms/ipification/coverage/202.175.50.128")
    // RNIPConfiguration.setAuthorizationUrl("https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/auth")
    // RNIPConfiguration.setClientId("your-clien-id")
    // RNIPConfiguration.setRedirectUri("your-redirect-uri")
    
    // android only
    // RNIPConfiguration.setConfigFileName("ipification-services_dev.json")
    
    // RNIPConfiguration.enableValidateIMApps(false);

    // var clientId = await RNIPConfiguration.getClientId()
    // var redirectUri = await RNIPConfiguration.getRedirectUri()

    if (Platform.OS === "ios") {
      RNAuthenticationService.updateIOSLocale({
        titleBar: "IPification",
        description:
          "Please tap on the preferred messaging app then follow instructions on the screen",
        whatsappBtnText: "Login with WhatsApp",
        telegramBtnText: "Login with Telegram",
        viberBtnText: "Login with Viber",
        cancelBtnText: "Cancel",
      });
      RNAuthenticationService.updateIOSTheme({
        toolbarTitleColor: "#000000",
        cancelBtnColor: "#000000",
        titleColor: "#000000",
        descColor: "#000000",
        backgroundColor: "#ffffff",
      });
    }
  };

  doIMAuthentication = () => {
    var state = Constants.CURRENT_STATE;
    console.log("2. do IM Authentication with state", state);
    RNAuthenticationService.startAuthorization(
      {
        scope: "openid ip:phone",
        channel: "wa viber telegram",
        state: state,
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

    var client_id = await RNIPConfiguration.getClientId();
    var redirect_uri = await RNIPConfiguration.getRedirectUri();

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
        console.log("exchange response", responseJson);
        if (responseJson["access_token"]) {
          navigation.navigate("ResultScreen", {
            name: "ResultScreen",
            success: true,
            accessToken: responseJson["access_token"],
          });
        } else {
          navigation.navigate("ResultScreen", {
            name: "Result",
            success: false,
            error: JSON.stringify(responseJson),
          });
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
        <Text>Select your login option</Text>
        <TouchableOpacity
          style={styles.ipbutton}
          onPress={() =>
            navigation.navigate("PhoneVerifyScreen", {
              name: "PhoneVerifyScreen",
            })
          }
        >
          <Text style={styles.IPbuttonText}>Phone Verify</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.imbutton}
          onPress={() => {
            doIMAuthentication();
          }}
        >
          <Text style={styles.IMbuttonText}>IM Login</Text>
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
  imbutton: {
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
    borderRadius: 5,
  },
  ipbutton: {
    marginTop: 20,
    height: 50,
    width: 300,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#FD5D5D",
    shadowColor: "rgba(0,0,0,0.4)",
    shadowOffset: {
      width: 1,
      height: 5,
    },
    shadowOpacity: 0.34,
    shadowRadius: 6.27,
    elevation: 10,
    borderRadius: 5,
  },
  IMbuttonText: {
    color: "white",
    fontSize: 18,
  },
  IPbuttonText: {
    color: "white",
    fontSize: 18,
  },
});

export default HomeScreen;
