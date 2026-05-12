package com.ipification.demoapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipification.demoapp.R
import com.ipification.demoapp.activity.im.IMAuthActivity
import com.ipification.demoapp.activity.pnv.PnvActivity
import com.ipification.demoapp.ui.components.IMButton
import com.ipification.demoapp.ui.components.IPificationButton
import com.ipification.demoapp.ui.theme.IPDarkGray
import com.ipification.demoapp.ui.theme.IPPrimary
import com.ipification.demoapp.ui.theme.IPificationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPificationTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFF4F6), Color.White)
                    )
                )
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "IPification Demo",
                    textAlign = TextAlign.Center,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = IPPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = stringResource(id = R.string.select_authentication_option),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = IPDarkGray
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PNV integration entry point: open the sample Phone Number Verification screen.
                IPificationButton(
                    text = stringResource(id = R.string.phone_number_verification),
                    onClick = {
                        startActivity(Intent(context, PnvActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                IMButton(
                    text = stringResource(id = R.string.login_with_instant_message),
                    onClick = {
                        startActivity(Intent(context, IMAuthActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = stringResource(id = R.string.powered_by_ipification),
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = IPDarkGray
            )
        }
    }
}
