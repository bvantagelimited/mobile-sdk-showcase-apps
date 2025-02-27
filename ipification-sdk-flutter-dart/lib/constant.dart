class Constant {
  // Base URL for the API.
  static const String _host = "https://api.stage.ipification.com";

  // API Endpoints.
  static const String checkCoverageUrl = "$_host/auth/realms/ipification/coverage/";
  static const String authUrl =
      "$_host/auth/realms/ipification/protocol/openid-connect/auth";
  static const String tokenUrl =
      "$_host/auth/realms/ipification/protocol/openid-connect/token";
  static const String userInfoUrl =
      "$_host/auth/realms/ipification/protocol/openid-connect/userinfo";

  // Credentials and redirect URI.
  static const String clientId = "201ccc790ebc4498b6f30c1d06826a16";
  static const String clientSecret = "4rMBROeFNe7TmeM6E35yi3wDTuyjWtUZ";
  static const String redirectUri = "https://bakong.nbc.gov.kh";

  // Device registration endpoint.
  // TODO: update with your value.
  static const String registerDeviceUrl = "";
}