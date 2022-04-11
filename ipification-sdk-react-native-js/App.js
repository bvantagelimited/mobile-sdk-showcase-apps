import { NavigationContainer } from "@react-navigation/native";
import React, { useEffect } from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import PhoneVerifyScreen from "./PhoneVerifyScreen";
import HomeScreen from "./Home";
import ResultScreen from "./Result";
import messaging from "@react-native-firebase/messaging";
import { Platform } from "react-native";
import Constants from "./Constants";
import {
  SafeAreaView,
  StyleSheet,
  View,
  NativeModules,
  ToastAndroid,
} from "react-native";
const { RNIPNotification } = NativeModules;

const Stack = createNativeStackNavigator();

const App = () => {
  useEffect(() => {
    initNotification();
    receiveNotificationFromQuitState();
    receiveBackgroundNotification();
    backgroundThread();
    //iOS
    requestUserPermission();
    return () => {};
  }, []);

  async function requestUserPermission() {
    const authorizationStatus = await messaging().requestPermission();

    if (authorizationStatus) {
      console.log("Permission status:", authorizationStatus);
    }
  }
  async function initNotification() {
    // Register the device with FCM
    await messaging().registerDeviceForRemoteMessages();

    // Get the token
    const token = await messaging().getToken();
    console.log(token);
    var state = Constants.getRandomStateValues();
    Constants.CURRENT_STATE = state;
    await registerDeviceToServer(state, token);
    // Save the token
    // await postToApi('/users/1234/tokens', { token });
  }
  const receiveNotificationFromQuitState = () => {
    messaging()
      .getInitialNotification()
      .then(async (remoteMessage) => {
        // if (remoteMessage) {
        //  showToast(
        //  'getInitialNotification:' +
        //  'Notification caused app to open from quit state',
        //  );
        // }
      });
  };
  const receiveBackgroundNotification = () => {
    messaging().onNotificationOpenedApp(async (remoteMessage) => {
      //  if (remoteMessage) {
      //   showToast(
      //   'onNotificationOpenedApp: ' +
      //   'Notification caused app to open from background state',
      //   );
      //  }
    });
  };
  const backgroundThread = () => {
    //It's called when the app is in the background or terminated
    messaging().setBackgroundMessageHandler(async (remoteMessage) => {
      //  showToast("Background notification" +  JSON.stringify(remoteMessage));
      display(remoteMessage);
    });
  };
  const showToast = (message) => {
    if (Platform.OS == "ios") {
      alert(message);
    } else {
      ToastAndroid.show(message, ToastAndroid.SHORT);
    }
  };

  const display = (remoteMessage) => {
    console.log(remoteMessage)
    if(Platform.OS == "android"){
      RNIPNotification.showNotification(
        "IPification",
        remoteMessage.data.body,
        "mipmap",
        "ic_notification"
      );
    }
  };
  // do at your backend server
  registerDeviceToServer = async (device_id, device_token) => {
    console.log("1. register FCM device token to your backend server ");

    var details = {
      device_id: device_id,
      device_token: device_token,
      device_type: Platform.OS,
    };

    fetch(Constants.REGISTER_DEVICE_TOKEN_URL, {
      method: "POST",
      headers: { "Content-Type": "application/json; charset=utf-8" },
      body: JSON.stringify(details),
    })
      .then((response) => response.json())
      .then((responseJson) => {
        console.log("REGISTER_DEVICE_TOKEN_URL response", responseJson);
      })
      .catch((error) => {
        console.error(error);
      });
  };
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="Home Screen" component={HomeScreen} />

        <Stack.Screen
          name="PhoneVerifyScreen"
          component={PhoneVerifyScreen}
          options={{ title: "Phone Verify Screen" }}
        />
        <Stack.Screen
          name="ResultScreen"
          component={ResultScreen}
          options={{ title: "Result Screen" }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;
