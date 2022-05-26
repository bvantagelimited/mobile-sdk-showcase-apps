class Constant {
  static const String HOST = "https://stage.ipification.com";
  static const String CHECK_COVERAGE_URL =
      "$HOST/auth/realms/ipification/coverage/";
  static const String AUTH_URL =
      "$HOST/auth/realms/ipification/protocol/openid-connect/auth";

  static const String TOKEN_URL =
      "$HOST/auth/realms/ipification/protocol/openid-connect/token";
  static const String USER_INFO_URL =
      "$HOST/auth/realms/ipification/protocol/openid-connect/userinfo";

  // TODO
  // update with your value
  static const String CLIENT_ID = "";
  static const String CLIENT_SECRET = "";
  static const String REDIRECT_URI = "";
  // update with your value
  static const String REGISTER_DEVICE_URL = "";
}
