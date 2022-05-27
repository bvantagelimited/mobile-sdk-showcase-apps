import { NavigationContainer } from "@react-navigation/native";
import React, { useEffect } from "react";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import PhoneVerifyScreen from "./PhoneVerifyScreen";
import HomeScreen from "./Home";
import ResultScreen from "./Result";
import { Platform } from "react-native";
import Constants from "./Constants";
import {
  SafeAreaView,
  StyleSheet,
  View,
  NativeModules,
  ToastAndroid,
} from "react-native";

const Stack = createNativeStackNavigator();

const App = () => {
  
  return (
    <NavigationContainer>
      <Stack.Navigator>
        <Stack.Screen name="IPification Verification" component={HomeScreen} />

        <Stack.Screen
          name="PhoneVerifyScreen"
          component={PhoneVerifyScreen}
          options={{ title: "Phone Number Verify" }}
        />
        <Stack.Screen
          name="ResultScreen"
          component={ResultScreen}
          options={{ title: "Result" }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
};

export default App;
