import 'dart:convert';

import 'package:http/http.dart' as http;

class Constant {
  // Mirrors the Android SDK demo app BuildConfig.BASE_URL + CONFIG_PATH setup.
  static const String configBaseUrl = String.fromEnvironment(
    "CONFIG_BASE_URL",
    defaultValue: "https://showcase.stage.ipification.com",
  );
  static const String configPath = String.fromEnvironment(
    "CONFIG_PATH",
    defaultValue: "/api/config",
  );
  static const String configUrlOverride = String.fromEnvironment(
    "CONFIG_URL",
    defaultValue: "",
  );
  static const String tokenExchangePath = String.fromEnvironment(
    "TOKEN_EXCHANGE_PATH",
    defaultValue: "/auth/mobile/login",
  );

  static String get configUrl {
    if (configUrlOverride.isNotEmpty) return configUrlOverride;
    return "$configBaseUrl$configPath";
  }

  static const String environment = String.fromEnvironment(
    "ENVIRONMENT",
    defaultValue: "sandbox",
  );
  static const bool isSandbox = environment == "sandbox";

  static const String clientId = String.fromEnvironment(
    "CLIENT_ID",
    defaultValue: "webclient3",
  );
  static const String clientSecret = String.fromEnvironment(
    "CLIENT_SECRET",
    defaultValue: "webclient3",
  );
  static const String redirectUri = String.fromEnvironment(
    "REDIRECT_URI",
    defaultValue: "https://test.ipification.com/auth",
  );

  static const String stageHost = String.fromEnvironment(
    "STAGE_HOST",
    defaultValue: "https://api.stage.ipification.com",
  );
  static const String productionHost = String.fromEnvironment(
    "PRODUCTION_HOST",
    defaultValue: "https://api.ipification.com",
  );
  static const String _host = isSandbox ? stageHost : productionHost;

  // API Endpoints.
  static const String tokenUrl = String.fromEnvironment(
    "TOKEN_URL",
    defaultValue:
        "$_host/auth/realms/ipification/protocol/openid-connect/token",
  );
  static const String userInfoUrl = String.fromEnvironment(
    "USER_INFO_URL",
    defaultValue:
        "$_host/auth/realms/ipification/protocol/openid-connect/userinfo",
  );

  // IM AUTH - Device registration endpoint.
  static const String registerDeviceUrl = String.fromEnvironment(
    "REGISTER_DEVICE_URL",
    defaultValue: "",
  );

  // TS43/SMS backend configuration. This mirrors the Android Kotlin sample:
  // one stage/live backend pair with fixed TS43 and SMS paths.
  static const String stageBackendUrl = String.fromEnvironment(
    "STAGE_BACKEND_URL",
    defaultValue: "",
  );
  static const String productionBackendUrl = String.fromEnvironment(
    "PRODUCTION_BACKEND_URL",
    defaultValue: "",
  );

  static const String ts43AuthPath = "/ts43/auth";
  static const String ts43TokenPath = "/ts43/token";
  static const String smsAuthPath = "/sms/auth";
  static const String smsTokenPath = "/sms/token";
  static const String smsServerId = environment;
}

class DemoConfig {
  const DemoConfig({
    required this.environment,
    required this.clientId,
    required this.clientSecret,
    required this.redirectUri,
    required this.scope,
    required this.tokenUrl,
    required this.userInfoUrl,
    required this.registerDeviceUrl,
    required this.stageBackendUrl,
    required this.productionBackendUrl,
    required this.ts43AuthPath,
    required this.ts43TokenPath,
    required this.smsAuthPath,
    required this.smsTokenPath,
    required this.smsServerId,
    required this.backendBaseUrl,
    required this.authServerBaseUrl,
    required this.realm,
    required this.selectedServerId,
    required this.clients,
    required this.usesBackendTokenExchange,
  });

  factory DemoConfig.fallback() {
    return const DemoConfig(
      environment: Constant.environment,
      clientId: Constant.clientId,
      clientSecret: Constant.clientSecret,
      redirectUri: Constant.redirectUri,
      scope: "openid ip:phone_verify",
      tokenUrl: Constant.tokenUrl,
      userInfoUrl: Constant.userInfoUrl,
      registerDeviceUrl: Constant.registerDeviceUrl,
      stageBackendUrl: Constant.stageBackendUrl,
      productionBackendUrl: Constant.productionBackendUrl,
      ts43AuthPath: Constant.ts43AuthPath,
      ts43TokenPath: Constant.ts43TokenPath,
      smsAuthPath: Constant.smsAuthPath,
      smsTokenPath: Constant.smsTokenPath,
      smsServerId: Constant.smsServerId,
      backendBaseUrl: Constant.configBaseUrl,
      authServerBaseUrl: "",
      realm: "ipification",
      selectedServerId: "stage",
      clients: [],
      usesBackendTokenExchange: false,
    );
  }

  final String environment;
  final String clientId;
  final String clientSecret;
  final String redirectUri;
  final String scope;
  final String tokenUrl;
  final String userInfoUrl;
  final String registerDeviceUrl;
  final String stageBackendUrl;
  final String productionBackendUrl;
  final String ts43AuthPath;
  final String ts43TokenPath;
  final String smsAuthPath;
  final String smsTokenPath;
  final String smsServerId;
  final String backendBaseUrl;
  final String authServerBaseUrl;
  final String realm;
  final String selectedServerId;
  final List<DemoClientConfig> clients;
  final bool usesBackendTokenExchange;

  bool get isSandbox => environment == "sandbox";
  String get tokenExchangeUrl => "$backendBaseUrl${Constant.tokenExchangePath}";

  DemoConfig copyWith({
    String? environment,
    String? clientId,
    String? clientSecret,
    String? redirectUri,
    String? scope,
    String? tokenUrl,
    String? userInfoUrl,
    String? registerDeviceUrl,
    String? stageBackendUrl,
    String? productionBackendUrl,
    String? ts43AuthPath,
    String? ts43TokenPath,
    String? smsAuthPath,
    String? smsTokenPath,
    String? smsServerId,
    String? backendBaseUrl,
    String? authServerBaseUrl,
    String? realm,
    String? selectedServerId,
    List<DemoClientConfig>? clients,
    bool? usesBackendTokenExchange,
  }) {
    return DemoConfig(
      environment: environment ?? this.environment,
      clientId: clientId ?? this.clientId,
      clientSecret: clientSecret ?? this.clientSecret,
      redirectUri: redirectUri ?? this.redirectUri,
      scope: scope ?? this.scope,
      tokenUrl: tokenUrl ?? this.tokenUrl,
      userInfoUrl: userInfoUrl ?? this.userInfoUrl,
      registerDeviceUrl: registerDeviceUrl ?? this.registerDeviceUrl,
      stageBackendUrl: stageBackendUrl ?? this.stageBackendUrl,
      productionBackendUrl: productionBackendUrl ?? this.productionBackendUrl,
      ts43AuthPath: ts43AuthPath ?? this.ts43AuthPath,
      ts43TokenPath: ts43TokenPath ?? this.ts43TokenPath,
      smsAuthPath: smsAuthPath ?? this.smsAuthPath,
      smsTokenPath: smsTokenPath ?? this.smsTokenPath,
      smsServerId: smsServerId ?? this.smsServerId,
      backendBaseUrl: backendBaseUrl ?? this.backendBaseUrl,
      authServerBaseUrl: authServerBaseUrl ?? this.authServerBaseUrl,
      realm: realm ?? this.realm,
      selectedServerId: selectedServerId ?? this.selectedServerId,
      clients: clients ?? this.clients,
      usesBackendTokenExchange:
          usesBackendTokenExchange ?? this.usesBackendTokenExchange,
    );
  }

  DemoClientConfig? clientForFlow(String userFlow) {
    return clients.where((client) => client.userFlow == userFlow).firstOrNull;
  }

  DemoClientConfig? clientForAuthFlow(DemoAuthFlowKey flow) {
    switch (flow) {
      case DemoAuthFlowKey.ip:
      case DemoAuthFlowKey.multiChannel:
        return clientForFlow("pvn_ip") ??
            clients
                .where((client) => client.userFlow.startsWith("pvn_ip"))
                .firstOrNull;
      case DemoAuthFlowKey.ts43:
        return clientForFlow("pvn_sim") ??
            clients
                .where(
                  (client) =>
                      client.userFlow.contains("ts43") ||
                      client.userFlow.endsWith("_sim"),
                )
                .firstOrNull;
      case DemoAuthFlowKey.sms:
        return clientForFlow("pvn_sms");
    }
  }

  factory DemoConfig.fromJson(
    Map<String, dynamic> json,
    DemoConfig fallback, {
    required String backendBaseUrl,
  }) {
    if (json.containsKey("auth_servers") || json.containsKey("clients")) {
      return DemoConfig.fromAndroidDemoJson(
        json,
        fallback,
        backendBaseUrl: backendBaseUrl,
      );
    }

    final environment =
        _stringValue(json, const ["environment", "env", "ENVIRONMENT"]) ??
        fallback.environment;
    final environmentConfig = _mapValue(json, [
      environment,
      environment.toLowerCase(),
      environment.toUpperCase(),
    ]);

    String? value(List<String> keys) {
      return _stringValue(environmentConfig, keys) ?? _stringValue(json, keys);
    }

    final ts43Config = _mapValue(json, const ["ts43", "TS43"]);
    final smsConfig = _mapValue(json, const ["sms", "SMS"]);

    return fallback.copyWith(
      environment: environment,
      clientId: value(const ["client_id", "clientId", "CLIENT_ID"]),
      clientSecret: value(const [
        "client_secret",
        "clientSecret",
        "CLIENT_SECRET",
      ]),
      redirectUri: value(const ["redirect_uri", "redirectUri", "REDIRECT_URI"]),
      scope: value(const ["scope", "SCOPE"]),
      tokenUrl: value(const ["token_url", "tokenUrl", "TOKEN_URL"]),
      userInfoUrl: value(const [
        "user_info_url",
        "userInfoUrl",
        "USER_INFO_URL",
      ]),
      registerDeviceUrl: value(const [
        "register_device_url",
        "registerDeviceUrl",
        "REGISTER_DEVICE_URL",
      ]),
      stageBackendUrl: value(const [
        "stage_backend_url",
        "stageBackendUrl",
        "STAGE_BACKEND_URL",
      ]),
      productionBackendUrl: value(const [
        "production_backend_url",
        "productionBackendUrl",
        "PRODUCTION_BACKEND_URL",
      ]),
      ts43AuthPath:
          _stringValue(ts43Config, const ["auth_path", "authPath"]) ??
          value(const ["ts43_auth_path", "ts43AuthPath", "TS43_AUTH_PATH"]),
      ts43TokenPath:
          _stringValue(ts43Config, const ["token_path", "tokenPath"]) ??
          value(const ["ts43_token_path", "ts43TokenPath", "TS43_TOKEN_PATH"]),
      smsAuthPath:
          _stringValue(smsConfig, const ["auth_path", "authPath"]) ??
          value(const ["sms_auth_path", "smsAuthPath", "SMS_AUTH_PATH"]),
      smsTokenPath:
          _stringValue(smsConfig, const ["token_path", "tokenPath"]) ??
          value(const ["sms_token_path", "smsTokenPath", "SMS_TOKEN_PATH"]),
      smsServerId:
          _stringValue(smsConfig, const ["server_id", "serverId"]) ??
          value(const ["sms_server_id", "smsServerId", "SMS_SERVER_ID"]),
      backendBaseUrl: backendBaseUrl,
      usesBackendTokenExchange: true,
    );
  }

  factory DemoConfig.fromAndroidDemoJson(
    Map<String, dynamic> json,
    DemoConfig fallback, {
    required String backendBaseUrl,
  }) {
    final authServers = _listValue(json, const ["auth_servers"]);
    final firstAuthServer = authServers.firstOrNull;
    final selectedServerId =
        _stringValue(firstAuthServer, const ["id"]) ??
        fallback.selectedServerId;
    final selectedServerUrl =
        _stringValue(firstAuthServer, const ["url"]) ?? "";
    final authServerBaseUrl = selectedServerUrl.endsWith("/auth")
        ? selectedServerUrl.substring(0, selectedServerUrl.length - 5)
        : selectedServerUrl;

    final clients = _listValue(json, const [
      "clients",
    ]).map(DemoClientConfig.fromJson).whereType<DemoClientConfig>().toList();
    final appConfig = _mapValue(json, const ["app_config"]);
    final environment =
        _stringValue(appConfig, const ["default_environment"]) ??
        fallback.environment;

    final config = fallback.copyWith(
      environment: environment,
      backendBaseUrl: backendBaseUrl,
      authServerBaseUrl: authServerBaseUrl,
      realm: _stringValue(json, const ["realm"]) ?? fallback.realm,
      selectedServerId: selectedServerId,
      clients: clients,
      stageBackendUrl: backendBaseUrl,
      productionBackendUrl: backendBaseUrl,
      smsServerId: selectedServerId,
      tokenUrl: "$backendBaseUrl${Constant.tokenExchangePath}",
      usesBackendTokenExchange: true,
    );

    final defaultClient = config.clientForFlow("pvn_ip") ?? clients.firstOrNull;
    if (defaultClient == null) return config;
    return config.withClient(defaultClient);
  }

  DemoConfig withFlow(DemoAuthFlowKey flow) {
    final client = clientForAuthFlow(flow);
    return client == null ? this : withClient(client);
  }

  DemoConfig withClient(DemoClientConfig client) {
    return copyWith(
      clientId: client.clientId,
      redirectUri: "${client.redirectUri}/$selectedServerId",
      scope: client.scope,
    );
  }

  static Map<String, dynamic>? _mapValue(
    Map<String, dynamic>? source,
    List<String> keys,
  ) {
    if (source == null) return null;
    for (final key in keys) {
      final value = source[key];
      if (value is Map) {
        return Map<String, dynamic>.from(value);
      }
    }
    return null;
  }

  static List<Map<String, dynamic>> _listValue(
    Map<String, dynamic>? source,
    List<String> keys,
  ) {
    if (source == null) return [];
    for (final key in keys) {
      final value = source[key];
      if (value is List) {
        return value
            .whereType<Map>()
            .map((item) => Map<String, dynamic>.from(item))
            .toList();
      }
    }
    return [];
  }

  static String? _stringValue(Map<String, dynamic>? source, List<String> keys) {
    if (source == null) return null;
    for (final key in keys) {
      final value = source[key];
      if (value is String && value.isNotEmpty) return value;
      if (value != null && value is! Map && value.toString().isNotEmpty) {
        return value.toString();
      }
    }
    return null;
  }
}

class DemoClientConfig {
  const DemoClientConfig({
    required this.userFlow,
    required this.title,
    required this.scope,
    required this.clientId,
    required this.redirectUri,
    this.channel,
  });

  final String userFlow;
  final String title;
  final String scope;
  final String clientId;
  final String redirectUri;
  final String? channel;

  static DemoClientConfig? fromJson(Map<String, dynamic> json) {
    final userFlow = DemoConfig._stringValue(json, const ["user_flow"]);
    final clientId = DemoConfig._stringValue(json, const ["client_id"]);
    final redirectUri = DemoConfig._stringValue(json, const ["redirect_uri"]);
    if (userFlow == null || clientId == null || redirectUri == null) {
      return null;
    }

    return DemoClientConfig(
      userFlow: userFlow,
      title: DemoConfig._stringValue(json, const ["title"]) ?? userFlow,
      scope:
          DemoConfig._stringValue(json, const ["scope"]) ??
          "openid ip:phone_verify",
      clientId: clientId,
      redirectUri: redirectUri,
      channel: DemoConfig._stringValue(json, const ["channel"]),
    );
  }
}

enum DemoAuthFlowKey { ip, ts43, sms, multiChannel }

class AppConfig {
  static DemoConfig current = DemoConfig.fallback();
  static String loadMessage = "Config not loaded yet";

  static Future<void> loadFromApi() async {
    final configUrl = Constant.configUrl;
    final backendBaseUrl = _backendBaseUrl(configUrl);

    final client = http.Client();
    try {
      final response = await client.get(Uri.parse(configUrl));
      if (response.statusCode < 200 || response.statusCode >= 300) {
        loadMessage = "Config API failed: ${response.statusCode}";
        return;
      }

      final decoded = jsonDecode(response.body);
      if (decoded is! Map<String, dynamic>) {
        loadMessage = "Config API returned invalid JSON";
        return;
      }

      current = DemoConfig.fromJson(
        decoded,
        DemoConfig.fallback(),
        backendBaseUrl: backendBaseUrl,
      );
      loadMessage = "Loaded config from API";
    } catch (e) {
      loadMessage = "Config API error: $e";
    } finally {
      client.close();
    }
  }

  static void applyFlow(DemoAuthFlowKey flow) {
    current = current.withFlow(flow);
  }

  static String _backendBaseUrl(String configUrl) {
    if (Constant.configUrlOverride.isNotEmpty &&
        Constant.configBaseUrl.isNotEmpty) {
      return Constant.configBaseUrl;
    }
    if (configUrl.endsWith(Constant.configPath)) {
      return configUrl.substring(
        0,
        configUrl.length - Constant.configPath.length,
      );
    }
    return Constant.configBaseUrl;
  }
}
