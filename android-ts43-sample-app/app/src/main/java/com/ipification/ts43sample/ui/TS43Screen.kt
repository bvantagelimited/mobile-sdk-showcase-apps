package com.ipification.ts43sample.ui

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetDigitalCredentialOption
import androidx.credentials.GetCredentialResponse
import androidx.credentials.DigitalCredential
import androidx.credentials.ExperimentalDigitalCredentialApi
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialInterruptedException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipification.mobile.sdk.android.utils.PhoneNumberHelper
import com.ipification.ts43sample.Helper
import com.ipification.ts43sample.util.Util
import com.ipification.ts43sample.viewmodel.TS43Navigation
import com.ipification.ts43sample.viewmodel.TS43ViewModel
import kotlinx.coroutines.launch


/**
 * Main TS43 Screen with phone number input and verification button
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalDigitalCredentialApi::class)
@Composable
fun TS43Screen(
    viewModel: TS43ViewModel = viewModel(),
    onResult: (response: String?, error: String?) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val phonePerms = remember { PhoneNumberHelper.getRuntimeRequiredPermissions() }

    // Permission launcher for reading phone number
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = phonePerms.all { grants[it] == true }
        if (granted) {
            PhoneNumberHelper.fetchPhoneNumberNow(context) { msisdn, iso ->
                if (!msisdn.isNullOrBlank()) {
                    viewModel.onPhoneNumberFromHint(msisdn, iso)
                } else {
                    val dial = Util.getSystemDialCode(context)
                    viewModel.onCountryCodeFromHint(dial)
                }
            }
        }
    }

    // Function to request permissions and fetch phone number
    fun requestPermsThenFetch() {
        val notGranted = phonePerms.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
        if (notGranted.isNotEmpty()) {
            permLauncher.launch(notGranted.toTypedArray())
        } else {
            PhoneNumberHelper.fetchPhoneNumberNow(context) { msisdn, iso ->
                if (!msisdn.isNullOrBlank()) {
                    viewModel.onPhoneNumberFromHint(msisdn, iso)
                } else {
                    val dial = Util.getSystemDialCode(context)
                    viewModel.onCountryCodeFromHint(dial)
                }
            }
        }
    }

    // Auto-fetch phone number when screen loads
    LaunchedEffect(Unit) {
        requestPermsThenFetch()
    }

    // Handle navigation to Credential Manager
    LaunchedEffect(state.navigation) {
        if (state.navigation is TS43Navigation.ToCredentialManager) {
            val nav = state.navigation as TS43Navigation.ToCredentialManager
            viewModel.onNavigationHandled()
            
            coroutineScope.launch {
                try {
                    val credentialManager = CredentialManager.create(context)
                    
                    // Wrap the digital_request in the expected format
                    val requestJson = """{"requests": [${nav.digitalRequest}]}"""
                    
                    // Log Credential Manager request
                    Helper.printLog("\n========== CREDENTIAL MANAGER REQUEST ==========\n")
                    Helper.printLog("[APIManager] ${Util.getCurrentDate()} - CREDENTIAL_MANAGER - START\n")
                    Helper.printLog("[Auth Request ID] ${nav.authReqId}\n")
                    Helper.printLog("[Digital Request] ${nav.digitalRequest}\n")
                    Helper.printLog("[Full Request JSON] $requestJson\n")
                    Helper.printLog("================================================\n\n")

                    val option = GetDigitalCredentialOption(requestJson = requestJson)
                    val request = GetCredentialRequest(listOf(option))
                    
                    // This shows system/wallet UI and returns the response directly
                    val result: GetCredentialResponse = credentialManager.getCredential(
                        context = context,
                        request = request
                    )
                    
                    // Handle the credential
                    when (val cred = result.credential) {
                        is DigitalCredential -> {
                            val responseJson = cred.credentialJson

                            // Log Credential Manager response
                            Helper.printLog("\n========== CREDENTIAL MANAGER RESPONSE ==========\n")
                            Helper.printLog("[APIManager] ${Util.getCurrentDate()} - CREDENTIAL_MANAGER - SUCCESS\n")
                            Helper.printLog("[Credential Type] ${cred.type}\n")
                            Helper.printLog("[Response JSON] $responseJson\n")
                            Helper.printLog("=================================================\n\n")
                            
                            Log.d("OID4VP", "Success JSON: $responseJson")
                            viewModel.onCredentialReceived(responseJson)
                        }
                        else -> {
                            Helper.printLog("\n========== CREDENTIAL MANAGER ERROR ==========\n")
                            Helper.printLog("[APIManager] ${Util.getCurrentDate()} - CREDENTIAL_MANAGER - FAILED\n")
                            Helper.printLog("[Error] Unexpected credential type: ${cred.type}\n")
                            Helper.printLog("==============================================\n\n")
                            
                            Log.e("OID4VP", "Unexpected credential type: ${cred.type}")
                            onResult(null, "Unexpected credential type: ${cred.type}")
                        }
                    }
                } catch (e: GetCredentialException) {
                    val errorMessage = when (e) {
                        is GetCredentialCancellationException -> {
                            Log.w("OID4VP", "User cancelled.")
                            "User cancelled the verification process"
                        }
                        is GetCredentialInterruptedException -> {
                            Log.w("OID4VP", "Retryable interruption.", e)
                            "The verification process was interrupted. Please try again"
                        }
                        is NoCredentialException -> {
                            Log.w("OID4VP", "No matching credential available.")
                            "No matching credential was found on your device"
                        }
                        else -> {
                            Log.e("OID4VP", "GetCredential failed.", e)
                            "Verification failed: ${e.message}"
                        }
                    }
                    
                    // Log credential exception
                    Helper.printLog("\n========== CREDENTIAL MANAGER ERROR ==========\n")
                    Helper.printLog("[APIManager] ${Util.getCurrentDate()} - CREDENTIAL_MANAGER - EXCEPTION\n")
                    Helper.printLog("[Exception Type] ${e::class.simpleName}\n")
                    Helper.printLog("[Error Message] $errorMessage\n")
                    Helper.printLog("[Stack Trace] ${e.stackTraceToString()}\n")
                    Helper.printLog("==============================================\n\n")
                    
                    onResult(null, errorMessage)
                } catch (e: Exception) {
                    Helper.printLog("\n========== CREDENTIAL MANAGER UNEXPECTED ERROR ==========\n")
                    Helper.printLog("[APIManager] ${Util.getCurrentDate()} - CREDENTIAL_MANAGER - UNEXPECTED_ERROR\n")
                    Helper.printLog("[Exception] ${e::class.simpleName}\n")
                    Helper.printLog("[Error Message] ${e.message}\n")
                    Helper.printLog("[Stack Trace] ${e.stackTraceToString()}\n")
                    Helper.printLog("=========================================================\n\n")
                    
                    Log.e("OID4VP", "Unexpected error", e)
                    onResult(null, "Unexpected error: ${e.message}")
                }
            }
        }
    }

    // Handle navigation to result
    LaunchedEffect(state.navigation) {
        if (state.navigation is TS43Navigation.ToResult) {
            val nav = state.navigation as TS43Navigation.ToResult
            onResult(nav.response, nav.error)
            viewModel.onNavigationHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TS43 Phone Verification") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
//            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TS43 Authentication Sample",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))


            Button(
                onClick = { viewModel.startGetPhoneNumber(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (state.isLoading) "Processing..." else "Get Phone Number",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }


            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color(0xFFF2F2F2)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.phoneNumber,
                onValueChange = { },
                label = { Text("Phone Number") },
                placeholder = { Text("e.g., +999123456789") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                singleLine = true,
//                trailingIcon = {
//                    IconButton(
//                        onClick = {
//                            Log.d("PhonePermission", "Phone icon clicked!")
//                            requestPermsThenFetch()
//                        },
//                        enabled = !state.isLoading
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Phone,
//                            contentDescription = "Get phone number from device",
//                            tint = MaterialTheme.colorScheme.primary
//                        )
//                    }
//                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.startVerifyPhoneNumber(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (state.isLoading) "Processing..." else "Verify Phone Number",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Show validation message in the same UI
            if (state.message.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.message.contains("❌") || state.message.contains("Error") || state.message.contains("Failed"))
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = state.message,
                        color = if (state.message.contains("❌") || state.message.contains("Error") || state.message.contains("Failed"))
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
