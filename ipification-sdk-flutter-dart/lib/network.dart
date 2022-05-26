import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:ipification_plugin/ipification.dart';
import 'constant.dart';

class IPNetwork {
  static Future<void> doTokenExchange(
      var authCode, Function(String) success, Function(String) fail) async {
    var clientID = await IPificationPlugin.getClientId();
    var redirectURI = await IPificationPlugin.getRedirectUri();
    print(authCode);
    print("client_id:$clientID");
    print("redirect_uri:$redirectURI");

    var details = {
      'client_id': clientID,
      'grant_type': 'authorization_code',
      'client_secret': Constant.YOUR_CLIENT_SECRET,
      'redirect_uri': redirectURI,
      'code': authCode
    };
    print(details);
    var client = http.Client();
    try {
      print(Constant.TOKEN_URL);

      var responseJson =
          await client.post(Uri.parse(Constant.TOKEN_URL), body: details);
      print(responseJson.body);
      if (responseJson.statusCode == 200) {
        Map<String, dynamic> parse = jsonDecode(responseJson.body);
        Future.delayed(const Duration(milliseconds: 500), () async {
          await getUserInfo(parse["access_token"], success, fail);
        });
      } else {
        fail(responseJson.body);
      }
    } catch (e) {
      print(e);
      fail(e.toString());
    } finally {
      client.close();
    }
  }

  static Future<void> getUserInfo(
      var accessToken, Function(String) success, Function(String) fail) async {
    print("getUserInfo");
    var details = {'access_token': accessToken};
    var headers = {'Content-Type': 'application/x-www-form-urlencoded'};

    var client = http.Client();
    try {
      var responseJson = await client.post(Uri.parse(Constant.USER_INFO_URL),
          headers: headers, body: details);
      print(responseJson.body);
      if (responseJson.statusCode == 200) {
        success(responseJson.body);
      } else {
        fail(responseJson.body);
      }
    } catch (e) {
      print(e);
      fail(e.toString());
    } finally {
      client.close();
    }
  }

  static Future<void> registerDevice(var deviceToken, var state, var deviceType,
      Function(String) success, Function(String) fail) async {
    print("call registerDevice" + deviceToken + " " + state + deviceType);
    var details = {
      "device_token": deviceToken,
      "device_id": state,
      "device_type": deviceType
    };
    var headers = {'Content-Type': 'application/json'};
    var client = http.Client();

    try {
      var responseJson = await client.post(
          Uri.parse(Constant.REGISTER_DEVICE_URL),
          headers: headers,
          body: jsonEncode(details));

      if (responseJson.statusCode == 200) {
        success(responseJson.body);
      } else {
        fail(responseJson.body);
      }
    } catch (e) {
      print(e);
    } finally {
      client.close();
    }
  }
}
