/**
 * Sample GMIDBOX React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */
import React, { useEffect, useState } from "react";
import { Keyboard } from "react-native";

import {
  NativeModules,
  StyleSheet,
  SafeAreaView,
  View,
  Text,
  TextInput,
  TouchableOpacity,
} from "react-native";
import Spinner from "react-native-loading-spinner-overlay";

const { RNCellularService} = NativeModules;

const GMIDBOXScreen = ({ navigation }) => {
  const [url, onChangeUrl] = React.useState("https://api.ipify.org");
  const [result, setResult] = useState();
  const [isError, setError] = useState(false);
  const [loading, setLoading] = useState(false);

  doRequest = async () => {
    Keyboard.dismiss();
    setLoading(true);

    var seconds = new Date().getSeconds();
    console.log(`call startRequest start at - second:${seconds}`);

    RNCellularService.startRequest(url, (error, responseData) => {
      console.log(
        `RNCellularService - responseData: ${responseData} - error: ${error}`
      );

      var seconds = new Date().getSeconds();
      setResult(`response=${responseData} - error=${error}`);
      if (error) {
        setError(true);
      } else {
        setError(false);
      }
      console.log(`call startRequest end at - second:${seconds}`);
      setLoading(false);
    });
  };
  doRequestWithParams = async () => {
    Keyboard.dismiss();
    setLoading(true);

    var seconds = new Date().getSeconds();
    console.log(`call startRequest start at - second:${seconds}`);

    RNCellularService.startRequestWithParams(url, {format: "json"}, (error, responseData) => {
      console.log(
        `RNCellularService - responseData: ${responseData} - error: ${error}`
      );

      var seconds = new Date().getSeconds();
      setResult(`response=${responseData} - error=${error}`);
      if (error) {
        setError(true);
      } else {
        setError(false);
      }
      console.log(`call startRequest end at - second:${seconds}`);
      setLoading(false);
    });
  };
  
  componentWillUnmount = () => {
    RNCellularService.unregisterNetwork();
  }
  return (
    <View style={styles.container}>
      <SafeAreaView style={styles.wrapper}>
        <Text>Enter your Url</Text>
        <View style={{ backgroundColor: "transparent", padding: 10 }}></View>

        <TextInput
          style={styles.input}
          onChangeText={onChangeUrl}
          value={url}
        />

        <View style={{ backgroundColor: "transparent", padding: 20 }}></View>

        <TouchableOpacity
          disabled={loading}
          style={styles.ipButton}
          onPress={() => {
            doRequest();
          }}
        >
          <Text style={styles.IPbuttonText}>Request</Text>
        </TouchableOpacity>
        <View style={{ backgroundColor: "transparent", padding: 30 }}></View>
        <Text style={isError ? styles.error : styles.response}>{result}</Text>
      </SafeAreaView>
      <Spinner visible={loading} textStyle={styles.spinnerTextStyle} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#ffffff",
  },
  wrapper: {
    marginTop: 30,
    flex: 1,
    alignItems: "center",
  },
  input: {
    fontSize: 22,
    backgroundColor: "#f8f8f8",
    borderRadius: 5,
    minWidth: 300,
    paddingLeft: 40,
    paddingRight: 40,
    paddingTop: 10,
    paddingBottom: 10,
    textAlign: "center",
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
    color: "#FFF",
  },
  error: {
    color: "#ed1e26",
    fontSize: 18,
  },
  response: {
    color: "#7CDB8A",
    fontSize: 18,
  },
});

export default GMIDBOXScreen;
