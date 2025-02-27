import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ipification_plugin/error_codes.dart';
import 'package:ipification_plugin/ipification.dart';
import 'package:loader_overlay/loader_overlay.dart';
import 'package:ipification_demo_app/constant.dart';
import 'package:ipification_demo_app/failed.dart';
import 'package:ipification_demo_app/network.dart';
import 'package:ipification_demo_app/phone_verify.dart';
import 'package:ipification_demo_app/success.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);
  
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'IPification Demo App',
      theme: ThemeData(brightness: Brightness.dark, primaryColor: Colors.red),
      home: const IPificationHome(),
    );
  }
}

class IPificationHome extends StatefulWidget {
  const IPificationHome({Key? key}) : super(key: key);

  @override
  _IPificationHomeState createState() => _IPificationHomeState();
}

class _IPificationHomeState extends State<IPificationHome> {
  String? _authCode = '';
  String _alertMessage = '';
  String _fcmToken = "";

  @override
  void initState() {
    super.initState();
    _initIPification();
  }

  /// Initializes IPification settings, FCM and theme/locale.
  Future<void> _initIPification() async {
    IPificationPlugin.setEnv(ENV.SANDBOX);
    IPificationPlugin.setClientId(Constant.clientId);
    IPificationPlugin.setRedirectUri(Constant.redirectUri);
    _registerFCM();
    _updateThemeAndLocale();
  }

  /// Initiates the IPification authentication flow via IM channels.
  Future<void> _doAuthorization() async {
    context.loaderOverlay.show();
    IPificationPlugin.enableLog();

    try {
      final state = await IPificationPlugin.generateState();
      await _registerDeviceWithState(state);

      // Configure authentication parameters.
      IPificationPlugin.setScope(value: "openid ip:phone");
      IPificationPlugin.setState(value: state);

      // Begin IM authentication.
      final authResponse = await IPificationPlugin.doIMAuthentication(
        channel: "wa telegram viber",
      );
      _authCode = authResponse.code;
      _showMessage("authCode $_authCode");

      if (_authCode?.isNotEmpty ?? false) {
        await IPNetwork.doTokenExchange(
          authResponse.code!,
          (success) => _checkResult(success),
          (fail) {
            _showMessage(fail);
            _checkResult(fail);
          },
        );
      } else {
        _checkResult("");
      }
    } on PlatformException catch (e) {
      final errorMsg = "${e.code} - ${e.message ?? ""}";
      _showMessage(errorMsg);
      if (e.code == IP_AUTHENTICATE_IM_CANCEL) {
        context.loaderOverlay.hide();
        return;
      }
      _checkResult(errorMsg);
    }
  }

  /// Checks the token exchange response and navigates accordingly.
  void _checkResult(String response) {
    context.loaderOverlay.hide();
    try {
      final result = jsonDecode(response);
      if (result["phone_number"] != null) {
        _navigateToSuccess(response);
      } else {
        _navigateToFailure(response);
      }
    } catch (_) {
      _navigateToFailure(response);
    }
  }

  void _navigateToSuccess(String responseMessage) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => SuccessScreen(responseMessage: responseMessage),
      ),
    );
  }

  void _navigateToFailure(String responseMessage) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => FailScreen(responseMessage: responseMessage),
      ),
    );
  }

  /// Displays a message on the screen.
  void _showMessage(String message) {
    if (!mounted) return;
    setState(() {
      _alertMessage = message;
    });
  }

  Future<void> _registerDeviceWithState(String state) async {
    await IPNetwork.registerDevice(
      _fcmToken,
      state,
      Platform.isIOS ? "ios" : "android",
      (response) => print(response),
      (error) => print(error),
    );
  }

  /// Stub for registering Firebase Cloud Messaging.
  void _registerFCM() {
    // Place Firebase messaging registration code here.
  }

  /// Updates the theme and locale settings for iOS and Android.
  void _updateThemeAndLocale() {
    // iOS configuration.
    IPificationPlugin.updateIOSLocale(
      "IPification",
      "Phone Number Verify",
      "Please tap on the preferred messaging app then follow instructions on the screen",
      "Login with WhatsApp",
      "Login with Telegram",
      "Login with Viber",
      "Cancel",
    );
    IPificationPlugin.updateIOSTheme(
      "#000000", "#000000", "#000000", "#000000", "#ffffff",
    );

    // Android configuration.
    IPificationPlugin.updateAndroidLocale(
      "IPification",
      "Phone Number Verification",
      "Please tap on the preferred messaging app then follow instructions on the screen",
      "Login with WhatsApp",
      "Login with Telegram",
      "Login with Viber",
    );
    IPificationPlugin.updateAndroidTheme("#ffffff", "#ffffff", "#c91636");
  }

  /// Helper method to create a standardized button.
  Widget _buildButton({
    required Color color,
    required String text,
    required VoidCallback onPressed,
  }) {
    return SizedBox(
      width: MediaQuery.of(context).size.width * 0.8,
      child: ElevatedButton(
        style: ElevatedButton.styleFrom(
          backgroundColor: color,
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 15),
          textStyle:
              const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
        ),
        onPressed: onPressed,
        child: Text(text),
      ),
    );
  }

  Future<void> _startIPFlow() async {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => PhoneVerifyScreen()),
    );
  }

  Future<void> _startIMFlow() async {
    FocusScope.of(context).unfocus();
    _doAuthorization();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('IPification Demo App'),
      ),
      body: LoaderOverlay(
        child: ConstrainedBox(
          constraints: const BoxConstraints.expand(),
          child: Stack(
            children: [
              Center(
                child: SingleChildScrollView(
                  child: Padding(
                    padding: const EdgeInsets.all(10.0),
                    child: Column(
                      children: [
                        const SizedBox(height: 50),
                        const Text(
                          'Choose your login option:',
                          style: TextStyle(fontSize: 16),
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 20),
                        _buildButton(
                          color: Colors.red,
                          text: 'Phone Number Verify',
                          onPressed: _startIPFlow,
                        ),
                        const SizedBox(height: 30),
                        _buildButton(
                          color: Colors.green,
                          text: 'Login via IM',
                          onPressed: _startIMFlow,
                        ),
                        const SizedBox(height: 30),
                        Text(
                          _alertMessage,
                          textAlign: TextAlign.center,
                        ),
                        const SizedBox(height: 100),
                      ],
                    ),
                  ),
                ),
              ),
              Positioned(
                bottom: 20,
                left: 0,
                right: 0,
                child: Center(
                  child: const Text(
                    'Powered By : IPification',
                    textAlign: TextAlign.center,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}