/**
* Sample React Native App
* https://github.com/facebook/react-native
*
* @format
* @flow strict-local
*/
import React, {Component} from 'react';
import {StyleSheet, Text, View, NativeModules, Button} from 'react-native';
const {RNCoverageService, RNAuthenticationService} = NativeModules;
import {Platform} from 'react-native';

export default class App extends Component {
 constructor(props) {
   super(props);
   this.state = {
     activated: '',
     authentication: '',
   };
 }
 componentWillUnmount = () => {
   if (Platform.OS === 'android') {
     console.log("componentWillUnmount android")
     RNCoverageService.unregisterNetwork();
   }
 };
 checkCoverage = () => {
   console.log('checkCoverage');
   RNCoverageService.setAuthorizationServiceConfiguration("ipification-services.json")
   RNCoverageService.checkCoverage(
     (error, isAvailable) => {
       console.log(' isAvailable ',isAvailable,  error);
       if (isAvailable) {
         this.setState({activated: isAvailable});
       } else {
         this.setState({activated: error});
       }
     }
   );
 };

 doAuthentication = () => {
   console.log('doAuthentication');
   RNAuthenticationService.doAuthorization(
     {login_hint: '85263480857', state: '0829332e-3c3f-46ff-8de3-221c'},
     (error, code) => {
       console.log(error, code);
       if (code != null) {
         this.setState({authentication: code});
       }
       else{
         this.setState({authentication: error});
       }
     }
   );
 };
 render() {
   return (
     <View style={styles.container}>
       <Text style={{fontSize: 24}}>
         CoverageApi Result: {`${this.state.activated}`}
       </Text>
       <Text style={{fontSize: 24, margin: 10}}>
         DoAuthentcation Result:{' '}
         <Text style={{fontSize: 11}}> {`${this.state.authentication}`}</Text>
       </Text>
       <View style={{height: 30}} />
       <Button
         onPress={this.checkCoverage}
         title="Check Coverage "
         color="green"
       />
       <View style={{height: 10}} />
       <Button
         onPress={this.doAuthentication}
         title="Do Authentication"
         color="blue"
       />
     </View>
   );
 }
}
const styles = StyleSheet.create({
 container: {
   flex: 1,
   justifyContent: 'center',
   alignItems: 'center',
   backgroundColor: '#d3d3d3',
 },
});