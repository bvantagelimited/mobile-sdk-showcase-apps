import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl_phone_number_input/intl_phone_number_input.dart';
import 'package:ip_sdk_example/failed.dart';
import 'package:ip_sdk_example/network.dart';
import 'package:ip_sdk_example/success.dart';
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
        title: const Text('IPification SDK example'),
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
                          selectorTextStyle: TextStyle(color: Colors.black),
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
                          child: const Text('Authenticating'),
                          onPressed: startFlow,
                        ),
                        SizedBox(height: 30),
                        Text('Result: $alertMessage\n',
                            textAlign: TextAlign.center),
                      ],
                    ))
              ])),
    );
  }

  @override
  void deactivate() {
    if (Platform.isAndroid) {
      IPificationPlugin.unregisterNetwork();
    }
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

  Future<void> startFlow() async {
    //init
    init();

    FocusScope.of(context).requestFocus(new FocusNode());

    String errMessage = "your Telco is not supported IPification";
    try {
      setState(() {
        alertMessage = "Checking Coverage";
      });
      // if (Platform.isAndroid) {
      //   IpSdk.setAuthorizationServiceConfiguration("ipification_services");
      // }

      // isAvailable = await IpSdk.checkCoverage;
      var coverageResponse = await IPificationPlugin.checkCoverage();
      isAvailable = coverageResponse.isAvailable;
      print("isAvailable $isAvailable");

      var operatorCode = coverageResponse.operatorCode;
      print("operatorCode $operatorCode");

      showMessage("supported network: $isAvailable $operatorCode");
    } on PlatformException catch (e) {
      isAvailable = false;
      errMessage = e.code + "\n" + (e.message ?? "");
    }
    if (isAvailable == false) {
      doAuthorization();
    } else {
      errMessage = errMessage;
      showMessage(errMessage);
    }
  }

  void doAuthorization() async {
    String errMessage;
    try {
      // if (Platform.isAndroid) {
      //   IpSdk.setAuthorizationServiceConfiguration("ipification_services");
      // }
      if (_phoneNum.isEmpty) {
        showMessage("please input your phone number");
        return;
      }
      _phoneNum = _phoneNum.replaceAll("+", "");
      print(_phoneNum);

      showMessage("call authentication service");
      IPificationPlugin.setScope(value: "openid ip:phone_verify");

      // authenCode = await IpSdk.doAuthentication(loginHint: _phoneNum);
      var authResponse =
          await IPificationPlugin.doAuthentication(loginHint: _phoneNum);
      authCode = authResponse.code;
      print(authResponse.responseString);
      print(authCode);
      print(authResponse.state);
      showMessage("authCode $authCode");

      if (authCode?.isNotEmpty == true) {
        await IPNetwork.doTokenExchange(
            authCode,
            (success) => {checkResult(success)},
            (fail) => {
                  errMessage = fail,
                  showMessage(errMessage),
                  checkResult(fail)
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
          builder: (context) =>
              FailScreen(responseMessage: responseMessage)),
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
