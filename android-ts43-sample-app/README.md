# TS43 Phone Verification Sample App

This sample app demonstrates **two complete TS43 authentication flows**:

### Flow 1: Get Phone Number 
Retrieve the phone number associated with the active SIM card without user input.

### Flow 2: Verify Phone Number 
Verify that a given phone number matches the active SIM card.

### Common Steps (Both Flows)
1. **TS43 Auth Request** - Initiates authentication with appropriate scope and operation
2. **Digital Credential Request** - Receives digital_request and auth_req_id from backend
3. **Credential Manager Integration** - Uses Android Credential Manager API to obtain verification credential
4. **Token Exchange** - Exchanges the credential for final authentication token with phone number data

## Project Structure

```
app/
├── ui/
│   ├── TS43Screen.kt             # Main verification screen
│   ├── ResultScreen.kt           # Results display
├── util/
│   ├── MNCHelper.kt              # SIM operator detection
│   └── Util.kt                   # Utility functions
├── viewmodel/
│   └── TS43ViewModel.kt          # Business logic and state management
├── Helper.kt                     # Global configuration
└── MainActivity.kt               
```

## Getting Started

### Prerequisites

- Android Studio 
- Android SDK 24 (Android 7.0) or higher
- TS43 backend endpoint access
- Valid client credentials

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/bvantagelimited/mobile-sdk-showcase-apps/new/ts43/android-ts43-sample-app
   cd android-ts43-sample-app
   ```

2. **Configure your credentials**
   
   Open `app/src/main/java/com/ipification/ts43sample/Helper.kt` and update:
   ```kotlin
   var TS43_ENDPOINT = "https://your-backend-endpoint.com"
   var CLIENT_ID_GET_PHONE_NUMBER = "your-get-phone-client-id"
   var CLIENT_ID_VERIFY_PHONE_NUMBER = "your-verify-phone-client-id"
   ```

## Usage

The app supports **two TS43 flows**: GetPhoneNumber and VerifyPhoneNumber

**Install Wallet App : CMWalletApp**

### Flow 1: Get Phone Number

**Purpose:** Retrieve the phone number associated with the active SIM card

**Use Case:** When you need to retrieve the user's phone number without asking them to enter it

**Scope:** `openid ip:phone`  
**Operation:** `GetPhoneNumber`  
**Login Hint:** `anonymous` (no phone number required)

**Steps:**
1. **Set up the PNV Token**
2. **App Launch**
3. **Click "Get Phone Number"**
   - App initiates TS43 auth request with `scope: "openid ip:phone" and operation: "GetPhoneNumber"`
   - Receives digital credential request
   - Opens Credential Manager UI
   - User approves the request
4. **View Results**

---

### Flow 2: Verify Phone Number 

**Purpose:** Verify that a given phone number matches the active SIM card

**Use Case:** When you need to verify a phone number belongs to the user

**Scope:** `openid ip:phone_verify`  
**Operation:** `VerifyPhoneNumber`  
**Login Hint:** The phone number to verify (`6281288122111`)

**Steps:**
1. **Enter Phone Number**
2. **Click "Verify Phone Number"**
   - App initiates CIBA auth request with `scope: "openid ip:phone_verify"`
   - Includes the phone number as `login_hint`
   - Receives digital credential request
   - Opens Credential Manager UI
   - User approves verification
3. **View Results**

---

### Common Steps (Both Flows)

4. **Credential Manager Integration**
   - System UI displays verification request
   - User reviews and approves
   - Digital credential returned to app

5. **Token Exchange**
   - App extracts `vp_token` from credential
   - Exchanges token with backend
   - Receives final authentication response


## 🔍 Code Flow

### TS43ViewModel.kt

The ViewModel manages both authentication flows:

```kotlin
// Flow 1: Get Phone Number
fun startGetPhoneNumber(context: Context) {
    callGetPhoneNumberAuth(clientId)
}

// Flow 2: Verify Phone Number
fun startVerifyPhoneNumber(context: Context) {
    callVerifyPhoneNumberAuth(phoneNumber, clientId)
}

// Common methods for both flows:

// 1. Call TS43 auth endpoint
private suspend fun performTS43Auth(
    loginHint: String?,
    clientId: String,
    scope: String,
    operation: String,
    flowType: String
): String

// 2. Parse response
private fun parseTS43Response(response: String)

// 3. Handle credential from Credential Manager
fun onCredentialReceived(credentialJson: String)

// 4. Exchange token
private suspend fun performTS43TokenExchange(
    vpToken: String, 
    authReqId: String, 
    clientId: String
): Pair<Int, String>
```

### TS43Screen.kt

The UI handles Credential Manager integration:

```kotlin
// Launch Credential Manager when navigation state changes
LaunchedEffect(state.navigation) {
    if (state.navigation is TS43Navigation.ToCredentialManager) {
        val credentialManager = CredentialManager.create(context)
        val option = GetDigitalCredentialOption(requestJson)
        val request = GetCredentialRequest(listOf(option))
        val result = credentialManager.getCredential(context, request)
        // Handle result...
    }
}
```

## 📖 API Reference

### TS43 Auth Endpoint

**POST** `/ts43/auth`

#### Flow 1: Get Phone Number

Request:
```json
{
  "login_hint": "anonymous",
  "carrier_hint": "51004",
  "client_id": "your-client-id",
  "scope": "openid ip:phone",
  "operation": "GetPhoneNumber"
}
```

Response:
```json
{
  "auth_req_id": "abc123...",
  "digital_request": {
    "protocol": "openid4vp-v1-unsigned",
    "request": "..."
  }
}
```

#### Flow 2: Verify Phone Number

Request:
```json
{
  "login_hint": "6281288122111",
  "carrier_hint": "51004",
  "client_id": "your-client-id",
  "scope": "openid ip:phone_verify",
  "operation": "VerifyPhoneNumber"
}
```

Response:
```json
{
  "auth_req_id": "abc123...",
  "digital_request": {
    "protocol": "openid4vp-v1-unsigned",
    "request": "..."
  }
}
```

### Token Exchange Endpoint

**POST** `/ts43/token`

Request (same for both flows):
```json
{
  "vp_token": "eyJhbGc...",
  "auth_req_id": "abc123...",
  "client_id": "your-client-id"
}
```

Response for Get Phone Number:
```json
{
  "access_token": "...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "login_hint": "6281288122111",
}
```

Response for Verify Phone Number:
```json
{
  "access_token": "...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "login_hint": "6281288122111",
  "phone_number_verified": true
}
```
