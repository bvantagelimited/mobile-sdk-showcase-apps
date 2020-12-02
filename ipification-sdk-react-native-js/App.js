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
export default class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      activated: '',
      authentication:''
    };
    
  }
  
  checkCoverage = () => {
    console.log("checkCoverage")
    RNCoverageService.checkCoverage((error, success) => {
      console.log(" success ", error, success)
      if(success != null){
        this.setState({activated: success});
      }
      if(error != null){
        this.setState({activated: error});
      }
      
    }, (error, failed) => {
      console.log(" failed ", failed)
      if(failed != null){
        this.setState({activated: failed});
      }
      if(error != null){
        this.setState({activated: error});
      }
    });
  };

  doAuthentication = () => {
    console.log("doAuthentication")
    RNAuthenticationService.doAuthorizationWithParams({login_hint: "381692023534"}, (error, success) => {
      console.log(error, success)
      if(success != null){
        this.setState({authentication: success});
      }
      if(error != null){
        this.setState({authentication: error});
      }
      
    }, (error, failed) => {
      console.log(error, failed)
      if(failed != null){
        this.setState({authentication: failed});
      }
      if(error != null){
        this.setState({authentication: error});
      }
    });
  };
  render() {
    return (
      <View style={styles.container}>
        <Text style={{fontSize: 24}}>
        CoverageApi Result: {`${this.state.activated}`}
        </Text>
        <Text style={{fontSize: 24, margin: 10}}>
          DoAuthentcation Result: <Text style={{fontSize: 11}}> {`${this.state.authentication}`}</Text>
        </Text>
        <View style={{height:30}}/>
        <Button onPress={this.checkCoverage} title="Check Coverage " color="green" />
        <View style={{height:10}}/>
        <Button onPress={this.doAuthentication} title="Do Authentication" color="blue" />
      </View>
    );
  }
}
const styles = StyleSheet.create({
  container: {flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#d3d3d3',},
});
