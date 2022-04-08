/**
 * Sample IPification React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */
import React, { useEffect, useState } from "react";
import {
  SafeAreaView,
  StyleSheet, Text, View
} from "react-native";
import { Colors } from "react-native/Libraries/NewAppScreen";
import Constants from "./Constants";




const ResultScreen = ({route, navigation}) => {
  const [userInfo, setUserInfo] = useState("");
  const { success, accessToken, error } = route.params;

  useEffect(() => {
    getUserInfo()
  }, []);
  
  getUserInfo = async () => {
    console.log("4. getUserInfo (call from your backend service)");

  
    var details = {
      access_token: accessToken,
    };
    var formBody = [];
    for (var property in details) {
      var encodedKey = encodeURIComponent(property);
      var encodedValue = encodeURIComponent(details[property]);
      formBody.push(encodedKey + "=" + encodedValue);
    }
    formBody = formBody.join("&");
    // console.log(formBody)
    fetch(Constants.GET_USER_INFO_URL, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: formBody,
    })
      .then((response) => response.json())
      .then((responseJson) => {
        console.log("user info", responseJson);
        if (responseJson["access_token"]) {

          setUserInfo(JSON.stringify(decoded));
        } else {
          setUserInfo(JSON.stringify(responseJson));
        }
      })
      .catch((error) => {
        console.error(error);
        setUserInfo(JSON.stringify(error));
      });
  };
  
  return (
    <View style={styles.container}>
      <SafeAreaView style={styles.wrapper}>
        <View style={styles.message}>
        
         {error && <Text>UserInfo : {error}</Text> }
         <Text>UserInfo : {userInfo}</Text>   
        </View>
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
  
  message: {
    borderWidth: 1,
    borderRadius: 5,
    padding: 20,
    marginBottom: 20,
    justifyContent: "center",
    alignItems: "flex-start",
  },
});

export default ResultScreen;
