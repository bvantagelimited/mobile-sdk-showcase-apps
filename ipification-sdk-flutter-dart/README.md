# ip_sdk_example

Demonstrates how to use the ip_sdk plugin.

## Getting Started


1. Download IPification plugin, copy its path and update `ip_sdk` location in `pubspec.yaml`

```
dependencies:
  ip_sdk:
    path: ./IPification-Flutter-Plugin
```

2. Save or Run `pub get` to update the dependencies


3. In `lib/main.dart`, let update init() function with your configuration:

```
IpSdk.setCheckCoverageUrl(
        "https://stage.ipification.com/auth/realms/ipification/coverage");
IpSdk.setAuthorizationUrl(
    "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/auth");

IpSdk.setClientId("your-client-id");
IpSdk.setRedirectUri("your-redirect-uri");
    
```


4. In `lib/constant.dart`, update `YOUR_CLIENT_SECRET` with your provided `client_secret`

5. Run `flutter clean / flutter run` to start project

