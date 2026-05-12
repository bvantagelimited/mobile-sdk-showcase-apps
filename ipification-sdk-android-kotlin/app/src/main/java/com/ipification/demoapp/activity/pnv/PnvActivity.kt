package com.ipification.demoapp.activity.pnv

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipification.demoapp.BuildConfig
import com.ipification.demoapp.ui.theme.IPPrimary
import com.ipification.demoapp.ui.theme.IPificationTheme
import com.ipification.demoapp.util.Util
import com.ipification.mobile.sdk.android.IPConfiguration
import com.ipification.mobile.sdk.android.IPEnvironment
import com.mukesh.countrypicker.CountryPicker

class PnvActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initIPification()

        setContent {
            IPificationTheme {
                PnvScreen(onBack = { finish() })
            }
        }
    }

    private fun initIPification() {
        // SDK setup required before starting any PNV flow.
        // Replace CLIENT_ID and REDIRECT_URI in build.gradle product flavors with client values from the docs.
        val environment = if (BuildConfig.ENVIRONMENT == "sandbox") IPEnvironment.SANDBOX else IPEnvironment.PRODUCTION
        with(IPConfiguration.getInstance()) {
            ENV = environment
            CLIENT_ID = BuildConfig.CLIENT_ID
            REDIRECT_URI = Uri.parse(BuildConfig.REDIRECT_URI)
        }
    }

    @Composable
    private fun PnvScreen(onBack: () -> Unit) {
        val viewModel: PnvViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()
        val activity = LocalContext.current as? Activity
        val focusManager = LocalFocusManager.current

        // Auto-fill the dial code from the device SIM when possible.
        // Sandbox keeps the docs-friendly test value so the sample works without a SIM.
        val countryFromSim = remember(activity) {
            (activity as? AppCompatActivity)?.let { CountryPicker.Builder().with(it).build().countryFromSIM }
        }

        if (state.countryCode.isEmpty()) {
            viewModel.setCountryCode(
                if (BuildConfig.ENVIRONMENT == "sandbox") "+999" else countryFromSim?.dialCode.orEmpty()
            )
        }
        if (state.phoneNumber.isEmpty() && BuildConfig.ENVIRONMENT == "sandbox") {
            viewModel.setPhoneNumber("123456789")
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    backgroundColor = Color.White,
                    contentColor = Color.Black,
                    elevation = 0.dp
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(start = 25.dp, end = 25.dp)
                    .verticalScroll(rememberScrollState())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { focusManager.clearFocus() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Phone Number Verification",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Please enter your phone number to continue",
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.countryCode.removePrefix("+"),
                        onValueChange = { viewModel.setCountryCode(if (it.startsWith("+")) it else "+$it") },
                        modifier = Modifier
                            .weight(0.3f)
                            .height(64.dp),
                        label = { Text("Code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = viewModel::setPhoneNumber,
                        modifier = Modifier
                            .weight(0.7f)
                            .height(64.dp),
                        label = { Text("Phone Number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                // IP Verify uses the IP channel only.
                PnvButton(text = "IP Verify", enabled = !state.isLoading) {
                    focusManager.clearFocus()
                    hideKeyboard()
                    activity?.let { viewModel.startVerification(it, PnvAuthFlow.IP) { msg -> Util.openErrorActivity(it, msg) } }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // TS43 Verify uses the TS43 channel only. Intended for supported ID TSEL numbers.
                PnvButton(text = "TS43 Verify (ID TSEL only)", color = Color(0xFF4CAF50), enabled = !state.isLoading) {
                    focusManager.clearFocus()
                    hideKeyboard()
                    activity?.let { viewModel.startVerification(it, PnvAuthFlow.TS43) { msg -> Util.openErrorActivity(it, msg) } }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // SMS Verify uses the SMS channel only. Intended for supported ID TSEL numbers.
                PnvButton(text = "SMS Verify (ID TSEL only)", color = Color(0xFFFF9800), enabled = !state.isLoading) {
                    focusManager.clearFocus()
                    hideKeyboard()
                    activity?.let { viewModel.startVerification(it, PnvAuthFlow.SMS) { msg -> Util.openErrorActivity(it, msg) } }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Multi-channel fallback order: TS43 first, then IP, then SMS.
                PnvButton(text = "Multi Channel (TS43 -> IP -> SMS) Verify", color = Color(0xFF673AB7), enabled = !state.isLoading) {
                    focusManager.clearFocus()
                    hideKeyboard()
                    activity?.let { viewModel.startVerification(it, PnvAuthFlow.MULTI_CHANNEL_TS43_IP_SMS) { msg -> Util.openErrorActivity(it, msg) } }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Multi-channel fallback order: TS43 first, then IP. SMS is not used in this flow.
                PnvButton(text = "Multi Channel (TS43 -> IP) Verify", color = Color(0xFF673AB7), enabled = !state.isLoading) {
                    focusManager.clearFocus()
                    hideKeyboard()
                    activity?.let { viewModel.startVerification(it, PnvAuthFlow.MULTI_CHANNEL_TS43_IP) { msg -> Util.openErrorActivity(it, msg) } }
                }

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(36.dp))
                PrivacyPolicyText()
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

private fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

@Composable
private fun PnvButton(
    text: String,
    color: Color = IPPrimary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(backgroundColor = color, contentColor = Color.White)
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PrivacyPolicyText(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val annotatedString = buildAnnotatedString {
        append("By continuing this verification you are\nagreeing to the ")
        pushStringAnnotation(tag = "URL", annotation = "https://www.ipification.com/legal")
        withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
            append("Privacy policy")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        style = androidx.compose.ui.text.TextStyle(
            color = Color(0xFF999999),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        ),
        modifier = modifier.fillMaxWidth(),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item)))
                }
        }
    )
}
