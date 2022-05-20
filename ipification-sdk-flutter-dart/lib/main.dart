import 'dart:async';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ip_sdk_example/failed.dart';
import 'package:ip_sdk_example/network.dart';
import 'package:ip_sdk_example/phone_verify.dart';
import 'package:ip_sdk_example/success.dart';
import 'package:ipification_plugin/ipification.dart';

void main() {
  runApp(MaterialApp(
    home: MyApp(),
    theme: ThemeData(brightness: Brightness.dark, primaryColor: Colors.red),
  ));
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String? authCode = '';
  String alertMessage = '';
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('IPification Demo App'),
      ),
      body: ConstrainedBox(
          constraints: const BoxConstraints.expand(),
          child: Stack(children: [
            Align(
              alignment: Alignment.center,
              child: Column(
                  mainAxisAlignment: MainAxisAlignment.start,
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: <Widget>[
                    Padding(
                        padding: EdgeInsets.all(10),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            SizedBox(height: 50),
                            Text('Choose your login option:',
                                style: TextStyle(fontSize: 16),
                                textAlign: TextAlign.center),
                            SizedBox(height: 20),
                            ButtonTheme(
                              height: 40.0,
                              child: Container(
                                width: MediaQuery.of(context).size.width * 0.7,
                                child: ElevatedButton(
                                  style: ElevatedButton.styleFrom(
                                      primary: Colors.red,
                                      padding: EdgeInsets.symmetric(
                                          horizontal: 50, vertical: 15),
                                      textStyle: TextStyle(
                                          fontSize: 16,
                                          fontWeight: FontWeight.bold)),
                                  child: const Text('Phone Number Verify'),
                                  onPressed: startIPFlow,
                                ),
                              ),
                            ),
                            SizedBox(height: 30),
                            ButtonTheme(
                                minWidth:
                                    MediaQuery.of(context).size.width * 0.8,
                                height: 40.0,
                                child: Container(
                                  width:
                                      MediaQuery.of(context).size.width * 0.7,
                                  child: ElevatedButton(
                                    style: ElevatedButton.styleFrom(
                                        primary: Colors.green,
                                        padding: EdgeInsets.symmetric(
                                            horizontal: 50, vertical: 15),
                                        textStyle: TextStyle(
                                            fontSize: 16,
                                            fontWeight: FontWeight.bold)),
                                    child: const Text('Login via IM'),
                                    onPressed: startIMFlow,
                                  ),
                                )),
                            SizedBox(height: 30),
                            Text('$alertMessage\n',
                                textAlign: TextAlign.center),
                            SizedBox(height: 100),
                          ],
                        ))
                  ]),
            ),
            Align(
                alignment: Alignment.bottomCenter,
                child: Padding(
                  padding: EdgeInsets.all(20),
                  child: Text('Powered By : IPification',
                      textAlign: TextAlign.center),
                ))
          ])),
    );
  }

  @override
  void deactivate() {
    // if (Platform.isAndroid) {
    //   IPificationPlugin.unregisterNetwork();
    // }
    super.deactivate();
  }

  void init() {
    IPificationPlugin.setCheckCoverageUrl(
        "https://stage.ipification.com/auth/realms/ipification/coverage/");
    IPificationPlugin.setAuthorizationUrl(
        "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/auth");
    IPificationPlugin.setClientId("6f2026a683bc439ebb414a03f9012f27");
    IPificationPlugin.setRedirectUri(
        "https://api.dev.ipification.com/api/v1/callback");
  }

  void doAuthorization() async {
    String errMessage;
    try {
      showMessage("call authentication service");
      IPificationPlugin.setScope(value: "openid ip:phone");

      // authenCode = await IpSdk.doAuthentication(loginHint: _phoneNum);
      var authResponse = await IPificationPlugin.doIMAuthentication(
          channel: "wa telegram viber");
      authCode = authResponse.code;
      print(authCode);
      print(authResponse.state);
      showMessage("authCode $authCode");

      if (authCode?.isNotEmpty != null) {
        var successMessage = "";
        await IPNetwork.doTokenExchange(
            authCode,
            (success) => {checkResult(success)},
            (fail) => {errMessage = fail, showMessage(errMessage)});
      }
    } on PlatformException catch (e) {
      errMessage = e.code + "\n" + (e.message ?? "");
      showMessage(errMessage);
    }
  }

  void goSuccessPage(String responseMessage) {
    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) =>
              SuccessScreen(responseMessage: responseMessage)),
    );
  }

  void goFailPage(String responseMessage) {
    Navigator.push(
      context,
      MaterialPageRoute(
          builder: (context) => FailScreen(responseMessage: responseMessage)),
    );
  }

  void showMessage(String message) {
    if (!mounted) return;
    setState(() {
      alertMessage = message;
    });
  }

  Future<void> startIPFlow() async {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => PhoneVerifyScreen()),
    );
  }

  Future<void> startIMFlow() async {
    init();

    FocusScope.of(context).requestFocus(new FocusNode());

    doAuthorization();
  }

  checkResult(String success) {
    goSuccessPage(success);
  }
}
