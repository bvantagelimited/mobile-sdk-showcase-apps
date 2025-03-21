import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl_phone_number_input/intl_phone_number_input.dart';
import 'package:ipification_demo_app/constant.dart';
import 'package:ipification_demo_app/failed.dart';
import 'package:ipification_demo_app/network.dart';
import 'package:ipification_demo_app/success.dart';
import 'package:ipification_plugin/ipification.dart';

class PhoneVerifyScreen extends StatefulWidget {
  @override
  _PhoneVerifyState createState() => _PhoneVerifyState();
}

class _PhoneVerifyState extends State<PhoneVerifyScreen> {
  String? authCode = '';
  String alertMessage = '';
  bool isAvailable = false;

  final String countryCode = "381";
  final String phoneNumber = "123456789";
  String _phoneNum = "381123456789";

  final TextEditingController controller = TextEditingController();
  String initialCountry = 'RS';
  PhoneNumber number = PhoneNumber(isoCode: 'RS', phoneNumber: "123456789");

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Phone Number Verify Showcase'),
      ),
      body: ConstrainedBox(
          constraints: const BoxConstraints.expand(),
          child: Column(
              mainAxisAlignment: MainAxisAlignment.start,
              children: <Widget>[
                Padding(
                    padding: EdgeInsets.all(10),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        SizedBox(height: 50),
                        InternationalPhoneNumberInput(
                          onInputChanged: (PhoneNumber number) {
                            setState(() {
                              _phoneNum = number.phoneNumber ?? "";
                            });
                          },
                          hintText: "123456789",
                          onInputValidated: (bool value) {
                            // print(value);
                          },
                          selectorConfig: SelectorConfig(
                            selectorType: PhoneInputSelectorType.BOTTOM_SHEET,
                          ),
                          ignoreBlank: true,
                          autoValidateMode: AutovalidateMode.disabled,
                          // selectorTextStyle: TextStyle(color: Colors.black),
                          initialValue: number,
                          textFieldController: controller,
                          formatInput: true,
                          keyboardType: TextInputType.numberWithOptions(
                              signed: true, decimal: true),
                          inputBorder: OutlineInputBorder(),
                          onSaved: (PhoneNumber number) {
                            // print('On Saved: $number');
                          },
                        ),
                        SizedBox(height: 30),
                        ElevatedButton(
                          child: const Text('Login'),
                          onPressed: startFlow,
                        ),
                        SizedBox(height: 30),
                        Text('$alertMessage\n', textAlign: TextAlign.center),
                      ],
                    ))
              ])),
    );
  }

  @override
  void deactivate() {
    if (Platform.isAndroid) {
      final ipificationPlugin = IPificationPlugin();
      ipificationPlugin.unregisterNetwork();
    }
    super.deactivate();
  }

  void initIPification() {
    final ipificationPlugin = IPificationPlugin();
    ipificationPlugin.setEnv(ENV.SANDBOX);
    ipificationPlugin.setClientId(Constant.clientId);
    ipificationPlugin.setRedirectUri(Constant.redirectUri);
  }

  Future<void> startFlow() async {
    //init
    initIPification();

    FocusScope.of(context).requestFocus(new FocusNode());

    String errMessage = "your Telco is not supported IPification";
    try {
      setState(() {
        alertMessage = "Checking Coverage";
      });
      // if (Platform.isAndroid) {
      //   IpSdk.setAuthorizationServiceConfiguration("ipification_services");
      // }
      final ipificationPlugin = IPificationPlugin();
      var coverageResponse = await ipificationPlugin.checkCoverage();
      isAvailable = coverageResponse.isAvailable;
      print("isAvailable $isAvailable");

      var operatorCode = coverageResponse.operatorCode;
      print("operatorCode $operatorCode");

      showMessage("supported network: $isAvailable $operatorCode");
    } on PlatformException catch (e) {
      isAvailable = false;
      errMessage = e.code + "\n" + (e.message ?? "");
    }
    if (isAvailable == true) {
      doAuthorization();
    } else {
      errMessage = errMessage;
      showMessage(errMessage);
    }
  }

  void doAuthorization() async {
    final ipificationPlugin = IPificationPlugin();
    String errMessage;
    try {
      showMessage("call authentication service");
      ipificationPlugin.setScope(value: "openid ip:phone_verify");
      ipificationPlugin.setState(value: "abcd1234");
      // authenCode = await IpSdk.doAuthentication(loginHint: _phoneNum);
      var authResponse =
          await ipificationPlugin.doAuthentication(loginHint: _phoneNum);
      authCode = authResponse.code;
      print(authResponse.responseString);
      print(authCode);
      print(authResponse.state);
      showMessage("authCode $authCode");

      if (authCode?.isNotEmpty == true) {
        await IPNetwork.doTokenExchange(
            authCode!,
            (success) {checkResult(success);},
            (fail) {
                  errMessage = fail;
                  showMessage(errMessage);
                  checkResult(fail);
                });
      } else {
        showMessage("errMessage");
      }
    } on PlatformException catch (e) {
      errMessage = e.code + "\n" + (e.message ?? "");
      print(errMessage);
      showMessage(errMessage);
      checkResult(errMessage);
    }
  }

  void nextPage() {}

  void showMessage(String message) {
    if (!mounted) return;
    setState(() {
      alertMessage = message;
    });
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

  checkResult(String response) {
    print("checkResult");
    try {
      var result = jsonDecode(response);
      if (result["phone_number_verified"] == "true" ||
          result["phone_number_verified"] == true) {
        goSuccessPage(response);
      } else {
        goFailPage(response);
      }
    } catch (e) {
      goFailPage(response);
    }
  }
}
