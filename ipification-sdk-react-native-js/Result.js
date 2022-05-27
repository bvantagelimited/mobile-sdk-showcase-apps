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




const ResultScreen = ({route, navigation}) => {
  const { success, accessToken, response } = route.params;

  useEffect(() => {
 
  }, []);
  
  return (
    <View style={styles.container}>
      <SafeAreaView style={styles.wrapper}>
        <View style={styles.message}>
          <Text style={styles.title}>{success ? "AUTHENTICATED" : "FAILED"}</Text>
          <View style={styles.space}></View>
          <Text>{JSON.stringify(response)}</Text>
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
    // borderWidth: 1,
    borderRadius: 5,
    padding: 20,
    marginBottom: 20,
    justifyContent: "center",
    alignItems: "flex-start",
  },
  space:{ 
    width: "100%",
    height: 20

  },
  title:{
      fontWeight: "600",
      alignContent:'center'
  }
});

export default ResultScreen;
