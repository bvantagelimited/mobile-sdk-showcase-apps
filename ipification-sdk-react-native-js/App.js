/**
* Sample React Native App
* https://github.com/facebook/react-native
*
* @format
* @flow strict-local
*/
import React, {useState, useRef, useEffect} from 'react';

import {
  SafeAreaView,
  StyleSheet,
  View,
  StatusBar,
  TouchableOpacity,
  Text,
  NativeModules
} from 'react-native';
const {RNCoverageService, RNAuthenticationService} = NativeModules;
import {Platform} from 'react-native';
import {Colors} from 'react-native/Libraries/NewAppScreen';
import PhoneInput from 'react-native-phone-number-input';

import jwt_decode from "jwt-decode";

const App = () => {
  const TOKEN_URL = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token"

  const [phoneNumber, setPhoneNumnber] = useState('123456789');
  const [token, setToken] = useState('');
  const [formattedValue, setFormattedValue] = useState('381123456789');
  const [coverageResult, setCoverageResult] = useState(false);
  const [authorizationResult, setAuthorizationResult] = useState();
  const [disabled, setDisabled] = useState(false);
  const [showMessage, setShowMessage] = useState(false);
  const phoneInput = useRef<PhoneInput>(null);
  

 useEffect(() => {
   
   return () => {
    if (Platform.OS === 'android') {
      console.log("componentWillUnmount android")
      RNCoverageService.unregisterNetwork();
    }
   }
 }, [])
 checkCoverage = () => {
   
  //  if (Platform.OS === 'android') {
  //   RNCoverageService.setAuthorizationServiceConfiguration("ipification-services.json")
  //  }
   
   console.log('1. check Coverage');
   RNCoverageService.checkCoverage(
     (error, isAvailable, operatorCode) => {
       console.log('isAvailable ',isAvailable,  operatorCode, error);
       if (isAvailable) {
         setCoverageResult(isAvailable);
         doAuthentication()
       } else {
        setCoverageResult(isAvailable || error);
        setDisabled(false)
       }
     }
   );
 };

 doAuthentication = () => {
  //  if (Platform.OS === 'android') {
  //   RNCoverageService.setAuthorizationServiceConfiguration("ipification-services.json")
  //  }
   var state = getRandomValues(); // optional
   console.log('2. do Authentication');
   RNAuthenticationService.doAuthorization(
     {login_hint: formattedValue, scope:"openid ip:phone_verify ip:mobile_id", state: state},
     (error, code, state, fullResponse) => {
       console.log(error, code, state, fullResponse);
       if (code != null) {
         setAuthorizationResult(code)
         doTokenExchange()
       }
       else{
        setAuthorizationResult(error)
       }
       setDisabled(false)
     }
   );
 };

 // do at your backend server
 doTokenExchange = async () =>{
  console.log("3. do Token Exchange")

  var client_id =  await RNAuthenticationService.getConfigurationByName("client_id")
  var redirect_uri =  await RNAuthenticationService.getConfigurationByName("redirect_uri")
  console.log("client_id,", client_id)
  var details = {
    'client_id': client_id,
    'grant_type': 'authorization_code',
    'client_secret': 'your_client_secret',
    'redirect_uri': redirect_uri, 
    'code': authorizationResult
  };
  var formBody = [];
  for (var property in details) {
    var encodedKey = encodeURIComponent(property);
    var encodedValue = encodeURIComponent(details[property]);
    formBody.push(encodedKey + "=" + encodedValue);
  }
  formBody = formBody.join("&");
  // console.log(formBody)
  fetch(TOKEN_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: formBody,
  }).then((response) => response.json())
  .then((responseJson) => {
      console.log("exchange response",responseJson)
      if(responseJson["access_token"]){
        var decoded = jwt_decode(responseJson["access_token"]);
        // console.log(decoded)
        setToken(JSON.stringify(decoded))
      }else{
        setToken(JSON.stringify(responseJson))
      }
  })
  .catch((error) => {
    console.error(error);
    setToken(JSON.stringify(error))
  });;
 }
  getRandomValues = () =>{
    const validChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    var result = '';
      for ( var i = 0; i < 40; i++ ) {
          result += validChars.charAt(Math.floor(Math.random() * validChars.length));
      }
      return result;
  }
   return (
    <View style={styles.container}>
    <SafeAreaView style={styles.wrapper}>
      {showMessage && (
        <View style={styles.message}>
          <Text>Formatted Phone Number : {formattedValue}</Text>
          <Text>1. Supported Telco : {coverageResult == true ? "true" : coverageResult == false ? "false" : coverageResult}</Text>
          <Text>2. Do Authentication - Result : {authorizationResult}</Text>
          <Text>3. Token Exchange - Access Token : {token}</Text>
          
        </View>
      )}
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
          // setCountryCode(phoneInput.current?.getCountryCode() || '');
        }}
        countryPickerProps={{withAlphaFilter:true}}
        disabled={disabled}
        withDarkTheme
        withShadow
        autoFocus
      />
      <TouchableOpacity
        style={styles.button}
        onPress={() => {
          const checkValid = phoneInput.current?.isValidNumber(phoneNumber);
          console.log(checkValid)
          // setDisabled(true)
          setShowMessage(true);
          checkCoverage()
        }}>
        <Text style={styles.buttonText}>Authorize</Text>
      </TouchableOpacity>
      
    </SafeAreaView>
  </View>
   );
 }


const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.lighter,
  },
  wrapper: {
    marginTop: 30,
    flex: 1,
    alignItems: 'center',
  },
  button: {
    marginTop: 20,
    height: 50,
    width: 300,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#7CDB8A',
    shadowColor: 'rgba(0,0,0,0.4)',
    shadowOffset: {
      width: 1,
      height: 5,
    },
    shadowOpacity: 0.34,
    shadowRadius: 6.27,
    elevation: 10,
  },
  buttonText:{
    color: 'white',
    fontSize: 14,
  },
  redColor: {
    backgroundColor: '#F57777'
  },
  message: {
    borderWidth: 1,
    borderRadius: 5,
    padding: 20,
    marginBottom: 20,
    justifyContent: 'center',
    alignItems: 'flex-start',
  },
});


export default App;
