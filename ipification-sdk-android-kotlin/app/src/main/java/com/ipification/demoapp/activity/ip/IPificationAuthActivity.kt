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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.ui.components.IPificationButton
import com.ipification.demoapp.ui.components.IPificationTopBar
import com.ipification.demoapp.ui.theme.IPDarkGray
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
                PhoneNumberHelper.fetchPhoneNumberNow(context) { msisdn, iso ->
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
                    PhoneNumberHelper.fetchPhoneNumberNow(context) { msisdn, iso ->
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "Please enter your phone number:",
                    fontSize = 18.sp,
                    color = IPDarkGray
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Country Code Field
                    OutlinedTextField(
                        value = uiState.countryCode,
                        onValueChange = { },
                        modifier = Modifier
                            .width(90.dp)
                            .clickable(
                                enabled = BuildConfig.ENVIRONMENT != "sandbox"
                            ) {
                                if (BuildConfig.ENVIRONMENT != "sandbox") {
                                    (activity as? AppCompatActivity)?.let { act ->
                                        CountryPicker.Builder().with(act)
                                            .listener { selectedCountry ->
                                                Log.d(TAG, "Country code = ${selectedCountry?.dialCode}")
                                                vm.setCountryCode(selectedCountry?.dialCode ?: "")
                                            }
                                            .build()
                                            .showBottomSheet(act)
                                    }
                                }
                            },
                        enabled = false, // Always disabled to prevent keyboard
                        singleLine = true,
                        readOnly = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = IPDarkGray,
                            disabledBorderColor = IPDarkGray
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                    )
                    
                    // Phone Number Field
                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = { vm.setPhoneNumber(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("phone number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                IPificationButton(
                    text = "Phone Number Verify",
                    onClick = {
                        hideKeyboard()
                        activity?.let { act ->
                            vm.startVerification(act) { msg ->
                                Util.openErrorActivity(act, msg)
                            }
                        }
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .align(Alignment.CenterHorizontally),
                    enabled = !uiState.isLoading
                )
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