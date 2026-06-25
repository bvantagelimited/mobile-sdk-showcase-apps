# IPification Plugin Demo App

Demonstrates how to use the IPification plugin.

## Getting Started

This project is a starting point for a IPification Demo application on Flutter.


1. A few resources to get you started
- [Document](https://developer.ipification.com/#/flutter-plugin/latest/)

2. Update project:
- Load config from the same API shape as the `ipification-android-sdk-public` demo app:

```bash
flutter run \
  --dart-define=CONFIG_BASE_URL=https://showcase.stage.ipification.com \
  --dart-define=CONFIG_PATH=/api/config
```

- `CONFIG_URL` can be used instead when the full config URL should override `CONFIG_BASE_URL + CONFIG_PATH`.
- The app reads `auth_servers`, `realm`, `clients`, and `app_config.default_environment`.
- The first `auth_servers` entry is selected by default. Its `url` is applied to the SDK with `setBaseUrl(url.replace("/auth", ""))`.
- Flow client selection follows the Android demo: `pvn_ip` for IP and multi-channel, `pvn_sim` or another TS43/SIM client for TS43, and `pvn_sms` for SMS.
- TS43 and SMS backend base URLs use `CONFIG_BASE_URL`, with `/ts43/auth`, `/ts43/token`, `/sms/auth`, and `/sms/token`.
- Token exchange uses `CONFIG_BASE_URL + /auth/mobile/login`, matching the Android demo's `TOKEN_EXCHANGE_PATH`.

Example config response:

```json
{
  "auth_servers": [
    {
      "id": "stage",
      "url": "https://api.stage.ipification.com/auth"
    }
  ],
  "realm": "ipification",
  "clients": [
    {
      "user_flow": "pvn_ip",
      "title": "Phone Verify",
      "scope": "openid ip:phone_verify",
      "client_id": "PUT_YOUR_VALUE_HERE",
      "redirect_uri": "PUT_YOUR_VALUE_HERE"
    },
    {
      "user_flow": "pvn_sim",
      "title": "TS43 Verify",
      "scope": "openid ip:phone_verify",
      "client_id": "PUT_YOUR_VALUE_HERE",
      "redirect_uri": "PUT_YOUR_VALUE_HERE"
    },
    {
      "user_flow": "pvn_sms",
      "title": "SMS Verify",
      "scope": "openid ip:phone_verify",
      "client_id": "PUT_YOUR_VALUE_HERE",
      "redirect_uri": "PUT_YOUR_VALUE_HERE"
    }
  ],
  "app_config": {
    "default_environment": "sandbox"
  }
}
```

** Configuration for IM testing (FOR IM AUTH ONLY):
- Update the configuration file for FCM service:
  + Android: `google-services.json`
  + iOS: `GoogleService-Info.plist`

3. Run project
- `flutter clean`
- `flutter run`
