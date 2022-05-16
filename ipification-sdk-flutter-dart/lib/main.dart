import 'dart:async';
import 'dart:convert';
import "dart:developer";
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:intl_phone_number_input/intl_phone_number_input.dart';
import 'package:ip_sdk/ip_sdk.dart';
import 'package:jwt_decoder/jwt_decoder.dart';

void main() {
  runApp(MaterialApp(home: MyApp()));
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String authCode = '';
  String alertMessage = '';
  bool isAvailable = false;
  final String TOKEN_URL =
      "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token";

  final String YOUR_CLIENT_SECRET = '';

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
      IpSdk.unregisterNetwork();
    }
    super.deactivate();
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

      showMessage("call authentication service");
      print(_phoneNum);
      IpSdk.setScope(value: "openid");

      // authenCode = await IpSdk.doAuthentication(loginHint: _phoneNum);
      var authResponse = await IpSdk.doAuthentication(loginHint: _phoneNum);
      authCode = authResponse.code;
      print(authCode);
      print(authResponse.state);
      showMessage("authCode $authCode");

      if (authCode.isNotEmpty) {
        var successMessage = "";
        await doTokenExchange(
            authCode,
            (success) => {
                  successMessage =
                      "got accessToken with Phone Number verified: ${success['phone_number_verified']}" +
                          "\n\n" +
                          "MobileId: ${success['mobile_id']}" +
                          "\n\n" +
                          "Sub: ${success['sub']}",
                  showMessage(successMessage)
                },
            (fail) => {errMessage = fail, showMessage(errMessage)});
      }
    } on PlatformException catch (e) {
      errMessage = e.code + "\n" + (e.message ?? "");
      showMessage(errMessage);
    }
  }

  Future<void> doTokenExchange(var authCode,
      Function(Map<String, dynamic>) success, Function(String) fail) async {
    var clientID = await IpSdk.getConfigurationByName("client_id");
    String redirectURI = await IpSdk.getConfigurationByName("redirect_uri");
    print("client_id:$clientID");
    print("redirect_uri:$redirectURI");
    var details = {
      'client_id': clientID,
      'grant_type': 'authorization_code',
      'client_secret': YOUR_CLIENT_SECRET,
      'redirect_uri': redirectURI,
      'code': authCode
    };
    var client = http.Client();
    try {
      var responseJson = await client.post(Uri.parse(TOKEN_URL), body: details);
      // responseJson["access_token"]

      Map<String, dynamic> parse = jsonDecode(responseJson.body);
      if (responseJson.statusCode == 200) {
        Map<String, dynamic> decodedToken =
            JwtDecoder.decode(parse["access_token"]);
        log("responseJson: ${decodedToken.toString()}");
        decodedToken["access_token"] = parse["access_token"];
        success(decodedToken);
      } else {
        fail(parse["error_description"]);
      }
    } finally {
      client.close();
    }
  }

  void nextPage() {}

  void showMessage(String message) {
    if (!mounted) return;
    setState(() {
      alertMessage = message;
    });
  }

  void init() {
    IpSdk.setCheckCoverageUrl(
        "https://stage.ipification.com/auth/realms/ipification/coverage/202.175.50.128");
    IpSdk.setAuthorizationUrl(
        "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/auth");

    IpSdk.setClientId("6f2026a683bc439ebb414a03f9012f27");
    IpSdk.setRedirectUri("https://api.dev.ipification.com/api/v1/callback");
  }

  void init2() {
    IpSdk.setAuthorizationServiceConfiguration("ipification-services-dev.json");
  }

  Future<void> startFlow() async {
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
      var coverageResponse = await IpSdk.checkCoverage();
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
}
