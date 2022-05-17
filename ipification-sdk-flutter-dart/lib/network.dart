
import 'constant.dart';
import 'package:jwt_decoder/jwt_decoder.dart';
import 'package:http/http.dart' as http;
import 'package:ip_sdk/ip_sdk.dart';
import 'package:flutter/services.dart';
import 'dart:convert';
import "dart:developer";
import 'dart:io';

class IPNetwork {
  
  static Future<void> doTokenExchange(var authCode,
      Function(Map<String, dynamic>) success, Function(String) fail) async {
    
    var clientID = await IpSdk.getClientId();
    var redirectURI = await IpSdk.getRedirectUri();
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


    var client = http.Client();
    try {
      var responseJson = await client.post(Uri.parse(Constant.TOKEN_URL), body: details);

      Map<String, dynamic> parse = jsonDecode(responseJson.body);
      if (responseJson.statusCode == 200) {
        Map<String, dynamic> decodedToken =
            JwtDecoder.decode(parse["access_token"]);
        log("responseJson: ${decodedToken.toString()}");
        decodedToken["access_token"] = parse["access_token"];
        success(decodedToken);
      } else {
        fail(responseJson.body);
      }
    } finally {
      client.close();
    }
  }
}