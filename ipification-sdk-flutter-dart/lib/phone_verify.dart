import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl_phone_number_input/intl_phone_number_input.dart';
import 'package:ipification_demo_app/constant.dart';
import 'package:ipification_demo_app/failed.dart';
import 'package:ipification_demo_app/network.dart';
import 'package:ipification_demo_app/success.dart';
import 'package:ipification_plugin/ipification.dart';
import 'package:ipification_plugin/multi_authentication_response.dart';

enum DemoAuthFlow { ip, ts43, sms, multiChannelTs43IpSms, multiChannelTs43Ip }

class PhoneVerifyScreen extends StatefulWidget {
  const PhoneVerifyScreen({Key? key}) : super(key: key);

  @override
  State<PhoneVerifyScreen> createState() => _PhoneVerifyState();
}

class _PhoneVerifyState extends State<PhoneVerifyScreen> {
  final TextEditingController _phoneController = TextEditingController();
  final TextEditingController _otpController = TextEditingController();

  DemoAuthFlow _selectedFlow = DemoAuthFlow.ip;
  String _phoneNum = "+381123456789";
  String _alertMessage = "";
  String? _pendingAuthReqId;
  String? _pendingNonce;
  bool _isLoading = false;

  final PhoneNumber _initialPhoneNumber = PhoneNumber(
    isoCode: 'RS',
    phoneNumber: "+381123456789",
  );

  @override
  void dispose() {
    _phoneController.dispose();
    _otpController.dispose();
    super.dispose();
  }

  @override
  void deactivate() {
    if (Platform.isAndroid) {
      IPificationPlugin().unregisterNetwork();
    }
    super.deactivate();
  }

  Future<void> _startFlow() async {
    AppConfig.applyFlow(_flowConfigKey(_selectedFlow));
    await _initIPification();
    FocusScope.of(context).unfocus();

    setState(() {
      _isLoading = true;
      _pendingAuthReqId = null;
      _pendingNonce = null;
    });

    try {
      switch (_selectedFlow) {
        case DemoAuthFlow.ip:
          await _startIPFlow();
          break;
        case DemoAuthFlow.ts43:
          await _startTS43Flow();
          break;
        case DemoAuthFlow.sms:
          await _startSMSFlow();
          break;
        case DemoAuthFlow.multiChannelTs43IpSms:
          await _startMultiChannelFlow(includeSms: true);
          break;
        case DemoAuthFlow.multiChannelTs43Ip:
          await _startMultiChannelFlow(includeSms: false);
          break;
      }
    } on PlatformException catch (e) {
      _showMessage("${e.code}\n${e.message ?? ""}");
      _goFailPage("${e.code}\n${e.message ?? ""}");
    } catch (e) {
      _showMessage(e.toString());
      _goFailPage(e.toString());
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _startIPFlow() async {
    final ipPlugin = IPificationPlugin();
    _showMessage("Checking IP coverage");

    final coverageResponse = await ipPlugin.checkCoverage();
    final operatorCode = coverageResponse.operatorCode;
    _showMessage(
      "IP coverage: ${coverageResponse.isAvailable}, operator: $operatorCode",
    );

    if (coverageResponse.isAvailable != true) {
      _goFailPage("Your Telco is not supported by IPification.");
      return;
    }

    ipPlugin.setScope(value: AppConfig.current.scope);
    ipPlugin.setState(value: "ip-demo-state");
    final authResponse = await ipPlugin.doAuthentication(loginHint: _phoneNum);
    await _handleAuthenticationResult(
      authResponse.code,
      authResponse.responseString,
    );
  }

  Future<void> _startTS43Flow() async {
    if (Platform.isIOS) {
      _showMessage("TS43 is available only on Android in this Flutter demo.");
      return;
    }

    final ipPlugin = IPificationPlugin();
    await _configureTS43(ipPlugin);
    await ipPlugin.setAuthChannels(channels: [AuthChannel.TS43]);
    ipPlugin.setScope(value: AppConfig.current.scope);
    ipPlugin.setState(value: "ts43-demo-state");

    _showMessage("Starting TS43 authentication");
    final response = await ipPlugin.doAuthenticationWithChannels(
      loginHint: _phoneNum,
    );
    await _handleMultiAuthResponse(response);
  }

  Future<void> _startSMSFlow() async {
    final ipPlugin = IPificationPlugin();
    await _configureSMS(ipPlugin);
    _showMessage("Starting SMS authentication");

    final response = await ipPlugin.startSMSAuthentication(
      phoneNumber: _phoneNum,
    );
    setState(() {
      _pendingAuthReqId = response.authReqId;
      _pendingNonce = response.nonce;
    });
    _showMessage("OTP required. auth_req_id: ${response.authReqId}");
  }

  Future<void> _startMultiChannelFlow({required bool includeSms}) async {
    final ipPlugin = IPificationPlugin();
    if (Platform.isAndroid) {
      await _configureTS43(ipPlugin);
    }
    if (includeSms) {
      await _configureSMS(ipPlugin);
    }
    await ipPlugin.setAuthChannels(
      channels: Platform.isAndroid
          ? [
              if (_hasTS43Client()) AuthChannel.TS43,
              AuthChannel.IP,
              if (includeSms && _hasSMSClient()) AuthChannel.SMS,
            ]
          : [AuthChannel.IP, if (includeSms) AuthChannel.SMS],
    );
    ipPlugin.setScope(value: AppConfig.current.scope);
    ipPlugin.setState(
      value: includeSms ? "multi-sms-demo-state" : "multi-state",
    );

    _showMessage("Starting multi-channel authentication");
    final response = await ipPlugin.doAuthenticationWithChannels(
      loginHint: _phoneNum,
    );
    await _handleMultiAuthResponse(response);
  }

  Future<void> _verifySMSOTP() async {
    final authReqId = _pendingAuthReqId;
    final nonce = _pendingNonce;
    final otpCode = _otpController.text.trim();

    if (authReqId == null || nonce == null) {
      _showMessage("Start SMS or multi-channel SMS first.");
      return;
    }
    if (otpCode.isEmpty) {
      _showMessage("Enter OTP code.");
      return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      final response = await IPificationPlugin().verifySMSOTP(
        otpCode: otpCode,
        authReqId: authReqId,
        nonce: nonce,
      );
      final responseJson = jsonEncode({
        "sub": response.sub,
        "phone_number": response.phoneNumber,
        "phone_number_verified": response.phoneNumberVerified,
        "login_hint": response.loginHint,
        "raw_response": response.rawResponse,
      });
      _checkResult(
        response.rawResponse?.isNotEmpty == true
            ? response.rawResponse!
            : responseJson,
      );
    } on PlatformException catch (e) {
      _showMessage("${e.code}\n${e.message ?? ""}");
      _goFailPage("${e.code}\n${e.message ?? ""}");
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _handleMultiAuthResponse(
    MultiAuthenticationResponse response,
  ) async {
    switch (response.type) {
      case MultiAuthenticationResultType.authentication:
        await _handleAuthenticationResult(
          response.authenticationResponse?.code,
          response.authenticationResponse?.responseString,
        );
        break;
      case MultiAuthenticationResultType.otpRequired:
        final smsResponse = response.smsAuthResponse;
        setState(() {
          _pendingAuthReqId = smsResponse?.authReqId;
          _pendingNonce = smsResponse?.nonce;
        });
        _showMessage("OTP required. auth_req_id: ${smsResponse?.authReqId}");
        break;
      case MultiAuthenticationResultType.unknown:
        _checkResult(response.responseString ?? "");
        break;
    }
  }

  Future<void> _handleAuthenticationResult(
    String? authCode,
    String? rawResponse,
  ) async {
    _showMessage("Auth response: ${rawResponse ?? ""}");

    if (authCode?.isNotEmpty == true) {
      await IPNetwork.doTokenExchange(authCode!, _checkResult, (fail) {
        _showMessage(fail);
        _checkResult(fail);
      });
      return;
    }

    _checkResult(rawResponse ?? "");
  }

  Future<void> _initIPification() async {
    final ipificationPlugin = IPificationPlugin();
    final config = AppConfig.current;
    ipificationPlugin.setEnv(config.isSandbox ? ENV.SANDBOX : ENV.PRODUCTION);
    ipificationPlugin.setClientId(config.clientId);
    ipificationPlugin.setRedirectUri(config.redirectUri);
    if (config.authServerBaseUrl.isNotEmpty) {
      await ipificationPlugin.setBaseUrl(config.authServerBaseUrl);
    }
    ipificationPlugin.enableLog();
  }

  Future<void> _configureTS43(IPificationPlugin ipPlugin) {
    final config = AppConfig.current;
    return ipPlugin.setTS43Configuration(
      sandboxBackendUrl: config.stageBackendUrl,
      productionBackendUrl: config.productionBackendUrl,
      authPath: config.ts43AuthPath,
      tokenPath: config.ts43TokenPath,
      scopeVerifyPhone: config.scope,
      scopeGetPhone: "openid ip:phone",
    );
  }

  Future<void> _configureSMS(IPificationPlugin ipPlugin) {
    final config = AppConfig.current;
    return ipPlugin.setSMSConfiguration(
      sandboxBackendUrl: config.stageBackendUrl,
      productionBackendUrl: config.productionBackendUrl,
      authPath: config.smsAuthPath,
      tokenPath: config.smsTokenPath,
      scope: config.scope,
      serverId: config.smsServerId,
    );
  }

  DemoAuthFlowKey _flowConfigKey(DemoAuthFlow flow) {
    switch (flow) {
      case DemoAuthFlow.ip:
        return DemoAuthFlowKey.ip;
      case DemoAuthFlow.ts43:
        return DemoAuthFlowKey.ts43;
      case DemoAuthFlow.sms:
        return DemoAuthFlowKey.sms;
      case DemoAuthFlow.multiChannelTs43IpSms:
      case DemoAuthFlow.multiChannelTs43Ip:
        return DemoAuthFlowKey.multiChannel;
    }
  }

  bool _hasTS43Client() {
    final clients = AppConfig.current.clients;
    if (clients.isEmpty) return true;
    return clients.any(
      (client) =>
          client.userFlow == "pvn_sim" ||
          client.userFlow.contains("ts43") ||
          client.userFlow.endsWith("_sim"),
    );
  }

  bool _hasSMSClient() {
    final clients = AppConfig.current.clients;
    if (clients.isEmpty) return true;
    return clients.any((client) => client.userFlow == "pvn_sms");
  }

  void _showMessage(String message) {
    if (!mounted) return;
    setState(() {
      _alertMessage = message;
    });
  }

  void _goSuccessPage(String responseMessage) {
    if (!mounted) return;
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => SuccessScreen(responseMessage: responseMessage),
      ),
    );
  }

  void _goFailPage(String responseMessage) {
    if (!mounted) return;
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => FailScreen(responseMessage: responseMessage),
      ),
    );
  }

  void _checkResult(String response) {
    try {
      final result = jsonDecode(response);
      if (result["phone_number_verified"] == "true" ||
          result["phone_number_verified"] == true ||
          result["phone_number"] != null) {
        _goSuccessPage(response);
      } else {
        _goFailPage(response);
      }
    } catch (_) {
      _goFailPage(response);
    }
  }

  String _flowTitle(DemoAuthFlow flow) {
    switch (flow) {
      case DemoAuthFlow.ip:
        return "IP";
      case DemoAuthFlow.ts43:
        return "TS43";
      case DemoAuthFlow.sms:
        return "SMS";
      case DemoAuthFlow.multiChannelTs43IpSms:
        return "Multi";
      case DemoAuthFlow.multiChannelTs43Ip:
        return "TS43+IP";
    }
  }

  String _buttonTitle() {
    switch (_selectedFlow) {
      case DemoAuthFlow.ip:
        return "Test IP";
      case DemoAuthFlow.ts43:
        return "Test TS43";
      case DemoAuthFlow.sms:
        return "Start SMS";
      case DemoAuthFlow.multiChannelTs43IpSms:
        return "Test Multi Channels";
      case DemoAuthFlow.multiChannelTs43Ip:
        return "Test TS43 -> IP";
    }
  }

  @override
  Widget build(BuildContext context) {
    final hasPendingOtp = _pendingAuthReqId != null && _pendingNonce != null;

    return Scaffold(
      appBar: AppBar(title: const Text('PNV Test Console')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              SegmentedButton<DemoAuthFlow>(
                segments: DemoAuthFlow.values.map((flow) {
                  return ButtonSegment<DemoAuthFlow>(
                    value: flow,
                    label: Text(_flowTitle(flow)),
                  );
                }).toList(),
                selected: {_selectedFlow},
                onSelectionChanged: _isLoading
                    ? null
                    : (selection) {
                        setState(() {
                          _selectedFlow = selection.first;
                          _pendingAuthReqId = null;
                          _pendingNonce = null;
                        });
                      },
              ),
              const SizedBox(height: 24),
              InternationalPhoneNumberInput(
                onInputChanged: (PhoneNumber number) {
                  _phoneNum = number.phoneNumber ?? "";
                },
                hintText: "123456789",
                selectorConfig: const SelectorConfig(
                  selectorType: PhoneInputSelectorType.BOTTOM_SHEET,
                ),
                ignoreBlank: true,
                autoValidateMode: AutovalidateMode.disabled,
                initialValue: _initialPhoneNumber,
                textFieldController: _phoneController,
                formatInput: true,
                keyboardType: const TextInputType.numberWithOptions(
                  signed: true,
                  decimal: true,
                ),
                inputBorder: const OutlineInputBorder(),
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: _isLoading ? null : _startFlow,
                child: Text(_isLoading ? "Please wait..." : _buttonTitle()),
              ),
              const SizedBox(height: 24),
              TextField(
                controller: _otpController,
                enabled: hasPendingOtp && !_isLoading,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  border: OutlineInputBorder(),
                  labelText: "SMS OTP",
                ),
              ),
              const SizedBox(height: 12),
              ElevatedButton(
                onPressed: hasPendingOtp && !_isLoading ? _verifySMSOTP : null,
                child: const Text("Verify SMS OTP"),
              ),
              const SizedBox(height: 24),
              SelectableText(_alertMessage, textAlign: TextAlign.center),
              const SizedBox(height: 12),
              SelectableText(
                AppConfig.loadMessage,
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
