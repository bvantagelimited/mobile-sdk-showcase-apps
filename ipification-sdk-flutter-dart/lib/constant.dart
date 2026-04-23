class Constant {
  // Base URL for the API.
  static const String _host = "https://api.stage.ipification.com";

  // API Endpoints.
  static const String tokenUrl =
      "$_host/auth/realms/ipification/protocol/openid-connect/token";
  static const String userInfoUrl =
      "$_host/auth/realms/ipification/protocol/openid-connect/userinfo";

  // Credentials and redirect URI.
  // TODO: update with your value.
  static const String clientId = "webclient3";
  static const String clientSecret = "webclient3";
  static const String redirectUri = "https://test.ipification.com/auth";

  // IM AUTH - Device registration endpoint.
  // TODO: update with your value.
  static const String registerDeviceUrl = "";
}
