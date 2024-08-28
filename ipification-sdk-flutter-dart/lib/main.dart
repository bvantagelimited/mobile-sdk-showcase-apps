import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:ipification_plugin/error_codes.dart';
import 'package:ipification_plugin_demo_app/constant.dart';
import 'package:loader_overlay/loader_overlay.dart';
// import 'package:firebase_core/firebase_core.dart';
// import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ipification_plugin_demo_app/failed.dart';
import 'package:ipification_plugin_demo_app/network.dart';
import 'package:ipification_plugin_demo_app/phone_verify.dart';
import 'package:ipification_plugin_demo_app/success.dart';
import 'package:ipification_plugin/ipification.dart';

void main() async {
  // setup FCM
  WidgetsFlutterBinding.ensureInitialized();
  // await Firebase.initializeApp();
  // FirebaseMessaging messaging = FirebaseMessaging.instance;

  // ask permission
  // NotificationSettings settings = await messaging.requestPermission(
  //   alert: true,
  //   announcement: false,
  //   badge: true,
  //   carPlay: false,
  //   criticalAlert: false,
  //   provisional: false,
  //   sound: true,
  // );
  // print('User granted permission: ${settings.authorizationStatus}');

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
  String fcmToken = "";
  // late final FirebaseMessaging _messaging;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('IPification Demo App'),
      ),
      body: LoaderOverlay(
        child: ConstrainedBox(
            constraints: const BoxConstraints.expand(),
            child: Stack(children: [
              Align(
                alignment: Alignment.center,
                child: SingleChildScrollView(
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
                                    width:
                                        MediaQuery.of(context).size.width * 0.8,
                                    child: ElevatedButton(
                                      style: ElevatedButton.styleFrom(
                                          primary: Colors.red,
                                          padding: EdgeInsets.symmetric(
                                              horizontal: 20, vertical: 15),
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
                                      width: MediaQuery.of(context).size.width *
                                          0.8,
                                      child: ElevatedButton(
                                        style: ElevatedButton.styleFrom(
                                            primary: Colors.green,
                                            padding: EdgeInsets.symmetric(
                                                horizontal: 20, vertical: 15),
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
              ),
              Align(
                  alignment: Alignment.bottomCenter,
                  child: Padding(
                    padding: EdgeInsets.all(20),
                    child: Text('Powered By : IPification',
                        textAlign: TextAlign.center),
                  ))
            ])),
      ),
    );
  }

  @override
  void initState() {
    super.initState();
    initIPification();
  }

  void initIPification() async {
    print("init -- start");

    IPificationPlugin.setEnv(ENV.SANDBOX);
    // custom urls
    // IPificationPlugin.setCheckCoverageUrl(
    //     Constant.CHECK_COVERAGE_URL);
    // IPificationPlugin.setAuthorizationUrl(
    //     Constant.AUTH_URL);

    IPificationPlugin.setClientId(Constant.CLIENT_ID);
    IPificationPlugin.setRedirectUri(Constant.REDIRECT_URI);

    // final token = await FirebaseMessaging.instance.getToken();
    // if (token != null) {
    //   fcmToken = token;
    // }
    // FirebaseMessaging.instance.onTokenRefresh.listen((newToken) {
    //   fcmToken = newToken;
    // });

    registerFCM();

    updateThemeAndLocale();
  }

  void doAuthorization() async {
    context.loaderOverlay.show();
    IPificationPlugin.enableLog();
    String state = await IPificationPlugin.generateState();
    await registerDeviceWithState(state);

    String errMessage;
    try {
      showMessage("call authentication service");
      IPificationPlugin.setScope(value: "openid ip:phone");
      IPificationPlugin.setState(value: state);
      var authResponse = await IPificationPlugin.doIMAuthentication(
          channel: "wa telegram viber");
      authCode = authResponse.code;
      print(authCode);
      print(authResponse.state);
      showMessage("authCode $authCode");

      if (authCode?.isNotEmpty != null) {
        await IPNetwork.doTokenExchange(
            authResponse.code!,
            (success) => {checkResult(success)},
            (fail) => {
                  errMessage = fail,
                  showMessage(errMessage),
                  checkResult(errMessage)
                });
      } else {
        checkResult("");
      }
    } on PlatformException catch (e) {
      errMessage = e.code + " -  " + (e.message ?? "");
      print(errMessage);
      showMessage(errMessage);
      if (e.code == IP_AUTHENTICATE_IM_CANCEL) {
        // dismiss loadingView
        context.loaderOverlay.hide();
        return;
      }

      checkResult(errMessage);
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
    FocusScope.of(context).requestFocus(new FocusNode());

    doAuthorization();
  }

  checkResult(String response) {
    context.loaderOverlay.hide();
    print("checkResult " + response);
    try {
      var result = jsonDecode(response);
      if (result["phone_number"] != null) {
        goSuccessPage(response);
      } else {
        goFailPage(response);
      }
    } catch (e) {
      goFailPage(response);
    }
  }

  Future<void> registerDeviceWithState(String state) async {
    await IPNetwork.registerDevice(
        fcmToken,
        state,
        Platform.isIOS ? "ios" : "android",
        (p0) => {print(p0)},
        (p0) => {print(p0)});
  }

  void registerFCM() async {
    // FirebaseMessaging.onMessageOpenedApp
    //     .listen((RemoteMessage message) async {});
    // FirebaseMessaging.onMessage.listen((RemoteMessage message) {
    //   print(message.data);
    // });
    // FirebaseMessaging.onBackgroundMessage(firebaseMessagingBackgroundHandler);
  }

  void updateThemeAndLocale() {
    IPificationPlugin.updateIOSLocale(
        "IPification",
        "Phone Number Verify",
        "Please tap on the preferred messaging app then follow instructions on the screen",
        "Login with WhatsApp",
        "Login with Telegram",
        "Login with Viber",
        "Cancel");
    IPificationPlugin.updateIOSTheme(
        "#000000", "#000000", "#000000", "#000000", "#ffffff");

    IPificationPlugin.updateAndroidLocale(
        "IPification",
        "Phone Number Verification",
        "Please tap on the preferred messaging app then follow instructions on the screen",
        "Login with WhatsApp",
        "Login with Telegram",
        "Login with Viber");

    IPificationPlugin.updateAndroidTheme("#ffffff", "#ffffff", "#c91636");
  }
}

// Future<void> firebaseMessagingBackgroundHandler(RemoteMessage message) async {
//   print("firebaseMessagingBackgroundHandler ${message.data}");

//   IPificationPlugin.showNotification(
//       "Demo App", message.data["body"] ?? "", "mipmap", "ic_launcher");
// }
