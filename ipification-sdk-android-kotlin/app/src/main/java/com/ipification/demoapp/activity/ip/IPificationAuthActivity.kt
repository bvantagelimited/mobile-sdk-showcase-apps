package com.ipification.demoapp.activity.ip

import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TextButton
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.ui.components.IPificationButton
import com.ipification.demoapp.ui.components.IPificationTopBar
import com.ipification.demoapp.ui.theme.IPDarkGray
import com.ipification.demoapp.ui.theme.IPPrimary
import com.ipification.demoapp.ui.theme.IPificationTheme
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
import com.ipification.mobile.sdk.android.IPificationServices
import com.ipification.mobile.sdk.android.utils.PhoneNumberHelper
import com.mukesh.countrypicker.CountryPicker

class IPificationAuthActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "IPAuthAct"
        private const val DEFAULT_COUNTRY_CODE = "+999"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIPification()
        
        setContent {
            IPificationTheme {
                IPAuthenticationScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }

    private fun initIPification() {
        val environment = if (BuildConfig.ENVIRONMENT == "sandbox") IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
        with(IPConfiguration.getInstance()) {
            ENV = environment
            CLIENT_ID = BuildConfig.CLIENT_ID
            REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI)
        }
    }

    @Composable
    fun IPAuthenticationScreen(onBackClick: () -> Unit) {
        val context = LocalContext.current
        val activity = context as? AppCompatActivity
        val vm: com.ipification.demoapp.activity.ip.IPAuthViewModel = viewModel()
        val uiState by vm.uiState.collectAsState()
        
        // Get country from SIM (requires AppCompatActivity for the picker)
        val countryFromSim = remember(activity) { activity?.let { CountryPicker.Builder().with(it).build().countryFromSIM } }
        
        // Initialize VM state once
        if (uiState.countryCode.isEmpty()) {
            vm.setCountryCode(
                if (BuildConfig.ENVIRONMENT == "sandbox") DEFAULT_COUNTRY_CODE else (countryFromSim?.dialCode ?: "")
            )
        }
        if (uiState.phoneNumber.isEmpty() && BuildConfig.ENVIRONMENT == "sandbox") {
            vm.setPhoneNumber("123456789")
        }

        // Phone number hint permissions (optional auto-fill)
        val phonePerms = remember { PhoneNumberHelper.getRuntimeRequiredPermissions() }
        val permLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            Log.d(TAG, "Permission grants: $grants")
            val granted = phonePerms.all { grants[it] == true }
            if (granted) {
                PhoneNumberHelper.fetchPhoneNumberNow(context) { phoneNumbers ->
                    val phoneNumber = phoneNumbers.firstOrNull()
                    val msisdn = phoneNumber?.first
                    val iso = phoneNumber?.second
                    Log.d(TAG, "Phone hint received - msisdn: $msisdn, iso: $iso")
                    if (!msisdn.isNullOrBlank()) {
                        vm.onPhoneNumberFromHint(msisdn, iso)
                    }
                }
            } else {
                Log.d(TAG, "Phone permissions not granted")
            }
        }

        LaunchedEffect(Unit) {
            // Skip auto-fill in sandbox mode
            if (BuildConfig.ENVIRONMENT != "sandbox") {
                val notGranted = phonePerms.filter { 
                    ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED 
                }
                if (notGranted.isNotEmpty()) {
                    Log.d(TAG, "Requesting permissions: $notGranted")
                    permLauncher.launch(notGranted.toTypedArray())
                } else {
                    Log.d(TAG, "All permissions already granted, fetching phone number")
                    PhoneNumberHelper.fetchPhoneNumberNow(context) { phoneNumbers ->
                        val phoneNumber = phoneNumbers.firstOrNull()
                        val msisdn = phoneNumber?.first
                        val iso = phoneNumber?.second
                        Log.d(TAG, "Phone hint received - msisdn: $msisdn, iso: $iso")
                        if (!msisdn.isNullOrBlank()) {
                            vm.onPhoneNumberFromHint(msisdn, iso)
                        }
                    }
                }
            } else {
                Log.d(TAG, "Sandbox mode - skipping phone auto-fill")
            }
        }

        // UI
        
        Scaffold(
            topBar = {
                IPificationTopBar(
                    title = "IPification Auth - ${BuildConfig.VERSION_NAME}",
                    onBackClick = onBackClick
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFF4F6), Color.White)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 8.dp,
                        backgroundColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Verify your phone",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = IPPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Enter your phone number and choose an authentication flow.",
                                fontSize = 15.sp,
                                lineHeight = 21.sp,
                                color = IPDarkGray,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = uiState.countryCode,
                                    onValueChange = { },
                                    modifier = Modifier
                                        .width(118.dp)
                                        .height(64.dp)
                                        .clickable(
                                            enabled = BuildConfig.ENVIRONMENT != "sandbox"
                                        ) {
                                            countryFromSim?.let { country ->
                                                vm.setCountryCode(country.dialCode)
                                            }
                                        },
                                    label = { Text("Code") },
                                    singleLine = true,
                                    enabled = BuildConfig.ENVIRONMENT != "sandbox",
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        disabledTextColor = IPDarkGray,
                                        disabledBorderColor = IPDarkGray.copy(alpha = 0.35f),
                                        disabledLabelColor = IPDarkGray.copy(alpha = 0.65f)
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                                )

                                OutlinedTextField(
                                    value = uiState.phoneNumber,
                                    onValueChange = { vm.setPhoneNumber(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp),
                                    label = { Text("Phone number") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        backgroundColor = Color(0xFFFFFBFC),
                                        textColor = IPDarkGray,
                                        cursorColor = IPPrimary,
                                        focusedBorderColor = IPPrimary,
                                        unfocusedBorderColor = IPDarkGray.copy(alpha = 0.35f),
                                        focusedLabelColor = IPPrimary,
                                        unfocusedLabelColor = IPDarkGray.copy(alpha = 0.7f)
                                    ),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            IPificationButton(
                                text = "Verify with IPification",
                                onClick = {
                                    hideKeyboard()
                                    activity?.let { act ->
                                        vm.startVerification(act, DemoAuthFlow.IP) { msg ->
                                            Util.openErrorActivity(act, msg)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isLoading
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            IPificationButton(
                                text = DemoAuthFlow.TS43.label,
                                onClick = {
                                    hideKeyboard()
                                    activity?.let { act ->
                                        vm.startVerification(act, DemoAuthFlow.TS43) { msg ->
                                            Util.openErrorActivity(act, msg)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isLoading
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            IPificationButton(
                                text = DemoAuthFlow.SMS.label,
                                onClick = {
                                    hideKeyboard()
                                    activity?.let { act ->
                                        vm.startVerification(act, DemoAuthFlow.SMS) { msg ->
                                            Util.openErrorActivity(act, msg)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isLoading
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            IPificationButton(
                                text = DemoAuthFlow.MULTI_CHANNEL.label,
                                onClick = {
                                    hideKeyboard()
                                    activity?.let { act ->
                                        vm.startVerification(act, DemoAuthFlow.MULTI_CHANNEL) { msg ->
                                            Util.openErrorActivity(act, msg)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isLoading
                            )

                            uiState.pendingSmsAuth?.let {
                                Spacer(modifier = Modifier.height(22.dp))
                                OutlinedTextField(
                                    value = uiState.otpCode,
                                    onValueChange = { vm.setOtpCode(it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("SMS OTP") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    TextButton(onClick = { vm.cancelOtp() }) {
                                        Text("Cancel")
                                    }
                                    TextButton(
                                        onClick = {
                                            activity?.let { act ->
                                                vm.verifyOtp(act) { msg -> Util.openErrorActivity(act, msg) }
                                            }
                                        },
                                        enabled = !uiState.isLoading
                                    ) {
                                        Text("Verify OTP")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    // Auth call moved into ViewModel

    override fun onDestroy() {
        super.onDestroy()
        IPificationServices.unregisterNetwork(this)
    }
}

// extension
fun Activity.hideKeyboard() {
    if (currentFocus != null) {
        val inputMethodManager: InputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    }
}

fun View.hideKeyboard() {
    val inputMethodManager: InputMethodManager =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}
