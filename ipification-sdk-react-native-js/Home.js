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
  StyleSheet,
  SafeAreaView,
  View,
  Text,
  TouchableOpacity
} from "react-native";
import Constants from "./Constants";
import NetworkManger from "./NetworkManger";
import messaging from "@react-native-firebase/messaging";
import Spinner from 'react-native-loading-spinner-overlay';

const { RNAuthenticationService, RNIPConfiguration, RNIPNotification, RNIPEnvironment } = NativeModules;

const HomeScreen = ({ navigation }) => {
  const [loading, setLoading] = useState(false);
  useEffect(() => {
    initIPification();
  }, []);
  
  initIPification = async () =>{
    
    const {SANDBOX, PRODUCTION} = RNIPEnvironment.getConstants();
    RNIPConfiguration.setENV(SANDBOX)
    RNIPConfiguration.setClientId(Constants.CLIENT_ID)
    RNIPConfiguration.setRedirectUri(Constants.REDIRECT_URI)

    Constants.TOKEN_EXCHANGE_HOST = "https://api.stage.ipification.com" // for demo only
    //TODO production: Constants.TOKEN_EXCHANGE_HOST =  = "https://api.ipification.com" // for demo only
    
  }


  // localize = async () => {
  //   RNAuthenticationService.updateAndroidLocale({
  //     mainTitle: "Phone Number Verification",
  //     description:
  //       "Please tap on the preferred messaging app then follow instructions on the screen",
  //     whatsappBtnText: "Login with WhatsApp",
  //     telegramBtnText: "Login with Telegram",
  //     viberBtnText: "Login with Viber",
  //     toolbarTitle: "IPification Verification",
  //     isVisible: true,
  //   });
  //   RNAuthenticationService.updateAndroidTheme({
  //     backgroundColor: "#ffffff",
  //     toolbarTextColor: "#000000",
  //     toolbarColor: "#000000"
  //   });

  //   RNAuthenticationService.updateIOSLocale({
  //     titleBar: "IPification",
  //     description:
  //       "Please tap on the preferred messaging app then follow instructions on the screen",
  //     whatsappBtnText: "Login with WhatsApp",
  //     telegramBtnText: "Login with Telegram",
  //     viberBtnText: "Login with Viber",
  //     cancelBtnText: "Cancel",
  //   });
  //   RNAuthenticationService.updateIOSTheme({
  //     toolbarTitleColor: "#000000",
  //     cancelBtnColor: "#000000",
  //     titleColor: "#000000",
  //     descColor: "#000000",
  //     backgroundColor: "#ffffff",
  //   });
  // };

  doIMAuthentication = async ()  => {
    setLoading(true)


    await NetworkManger.registerDevice();

    console.log("2. do IM Authentication with state : " +  Constants.CURRENT_STATE + " .");
    RNAuthenticationService.startAuthorization(
      {
        scope: "openid ip:phone",
        channel: "wa viber telegram",
        state: Constants.CURRENT_STATE,
      },
      (error, code, state, fullResponse, userCanceled) => {
        console.log(code, state, fullResponse, error, userCanceled);
        if (code != null) {
         
          var successResult = (result) => {
            setLoading(false);
            navigation.navigate("ResultScreen", {
              name: "Result",
              success: true,
              response: result,
            });
          }
          var failedResult = (result) => {
            setLoading(false);
            navigation.navigate("ResultScreen", {
              name: "Result",
              success: false,
              response: result,
            });
          }
          NetworkManger.doTokenExchange(code, successResult, failedResult);
        }
        else if(userCanceled){
          setLoading(false);
          console.log("userCanceled", userCanceled)
        } 
        else {
          setLoading(false);
          navigation.navigate("ResultScreen", {
            name: "Result",
            success: false,
            response: fullResponse,
          });
        }
        
      }
    );
  };





  // SET UP FCM NOTIFICATION
  // START

  useEffect(() => {
    initNotification();
    receiveNotificationFromQuitState();

    //android
    receiveBackgroundNotification();
    backgroundThread();

    //iOS
    requestUserPermission();

    return () => {};
  }, []);

  async function requestUserPermission() {
    const authorizationStatus = await messaging().requestPermission();

    if (authorizationStatus) {
      // console.log("Permission status:", authorizationStatus);
    }
  }
  async function initNotification() {
    // Register the device with FCM
    if(Platform.OS == "android"){
      await messaging().registerDeviceForRemoteMessages();
    }
    

    // Get the token
    const token = await messaging().getToken();
    Constants.CURRENT_DEVICE_TOKEN = token;

  }
  const receiveNotificationFromQuitState = () => {
    messaging()
      .getInitialNotification()
      .then(async (remoteMessage) => {});
  };
  const receiveBackgroundNotification = () => {
    messaging().onNotificationOpenedApp(async (remoteMessage) => {});
  };
  const backgroundThread = () => {
    //It's called when the app is in the background or terminated
    messaging().setBackgroundMessageHandler(async (remoteMessage) => {
      display(remoteMessage);
    });
  };

  const display = (remoteMessage) => {
    console.log(remoteMessage.data.body)
    if(Platform.OS == "android"){
      RNIPNotification.showAndroidNotification(
        "IPification",
        remoteMessage.data.body,
        "mipmap",
        "ic_notification"
      );
    }
  };
  
  // SET UP FCM NOTIFICATION
  // END



  
  return (
    <View style={styles.container}>
       
      <SafeAreaView style={styles.wrapper}>
       
        <Text>Select your login option</Text>
        <TouchableOpacity
          disabled={loading}
          style={styles.ipButton}
          onPress={() =>
            {
              navigation.navigate("PhoneVerifyScreen", {
                name: "PhoneVerifyScreen",
              })
            }
          }
        >
          <Text style={styles.IPbuttonText}>Phone Verify</Text>
        </TouchableOpacity>
        <TouchableOpacity
          disabled={loading}
          style={styles.imButton}
          onPress={() => {
            doIMAuthentication();
          }}
        >
          <Text style={styles.IMbuttonText}>IM Login</Text>
        </TouchableOpacity>
      </SafeAreaView>
      <Spinner
            visible={loading}
            textStyle={styles.spinnerTextStyle}
          />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    // backgroundColor: "#000000",
  },
  wrapper: {
    marginTop: 30,
    flex: 1,
    alignItems: "center",
  },
  imButton: {
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
  ipButton: {
    marginTop: 20,
    height: 50,
    width: 300,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#ed1e26",
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
  spinnerTextStyle: {
    color: '#FFF'
  },
});

export default HomeScreen;
