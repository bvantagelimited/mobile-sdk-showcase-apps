import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:ipification_plugin/ipification_plugin.dart';
import 'constant.dart';

typedef SuccessCallback = void Function(String);
typedef ErrorCallback = void Function(String);

class IPNetwork {
  static Future<void> doTokenExchange(
    String authCode,
    SuccessCallback success,
    ErrorCallback fail,
  ) async {
    final ipPlugin = IPificationPlugin();
    final clientId = await ipPlugin.getClientId();
    final redirectURI = await ipPlugin.getRedirectUri();

    print('Auth Code: $authCode');
    print('client_id: $clientId');
    print('redirect_uri: $redirectURI');

    final details = {
      'client_id': clientId,
      'grant_type': 'authorization_code',
      'client_secret': Constant.clientSecret,
      'redirect_uri': redirectURI,
      'code': authCode,
    };
    print('Token exchange details: $details');

    final client = http.Client();
    try {
      final response = await client.post(
        Uri.parse(Constant.tokenUrl),
        body: details,
      );
      print('Token exchange response: ${response.body}');

      if (response.statusCode == 200) {
        final Map<String, dynamic> data = jsonDecode(response.body);
        final String accessToken = data["access_token"];
        // Optionally wait before requesting user info.
        await Future.delayed(const Duration(milliseconds: 500));
        await getUserInfo(accessToken, success, fail);
      } else {
        fail(response.body);
      }
    } catch (e) {
      print('Error in doTokenExchange: $e');
      fail(e.toString());
    } finally {
      client.close();
    }
  }

  static Future<void> getUserInfo(
    String accessToken,
    SuccessCallback success,
    ErrorCallback fail,
  ) async {
    print('Getting user info...');
    final Map<String, String> details = {'access_token': accessToken};
    final Map<String, String> headers = {
      'Content-Type': 'application/x-www-form-urlencoded'
    };

    final client = http.Client();
    try {
      final response = await client.post(
        Uri.parse(Constant.userInfoUrl),
        headers: headers,
        body: details,
      );
      print('User info response: ${response.body}');
      if (response.statusCode == 200) {
        success(response.body);
      } else {
        fail(response.body);
      }
    } catch (e) {
      print('Error in getUserInfo: $e');
      fail(e.toString());
    } finally {
      client.close();
    }
  }

  static Future<void> registerDevice(
    String deviceToken,
    String state,
    String deviceType,
    SuccessCallback success,
    ErrorCallback fail,
  ) async {
    print('Registering device: $deviceToken, $state, $deviceType');
    final Map<String, dynamic> details = {
      'device_token': deviceToken,
      'device_id': state,
      'device_type': deviceType,
    };
    final Map<String, String> headers = {'Content-Type': 'application/json'};

    final client = http.Client();
    try {
      final response = await client.post(
        Uri.parse(Constant.registerDeviceUrl),
        headers: headers,
        body: jsonEncode(details),
      );
      if (response.statusCode == 200) {
        success(response.body);
      } else {
        fail(response.body);
      }
    } catch (e) {
      print('Error in registerDevice: $e');
      fail(e.toString());
    } finally {
      client.close();
    }
  }
}