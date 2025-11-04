package com.ipification.demoapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipification.demoapp.R
import com.ipification.demoapp.activity.im.IMAuthActivity
import com.ipification.demoapp.activity.ip.IPificationAuthActivity
import com.ipification.demoapp.ui.components.IMButton
import com.ipification.demoapp.ui.components.IPificationButton
import com.ipification.demoapp.ui.theme.IPDarkGray
import com.ipification.demoapp.ui.theme.IPificationTheme

class MainActivity : ComponentActivity() {
    private val TAG: String = "MainActivity"

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
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.select_authentication_option),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = IPDarkGray,
                modifier = Modifier.padding(bottom = 30.dp)
            )
            
            IPificationButton(
                text = stringResource(id = R.string.login_with_ipification),
                onClick = { 
                    val intent = Intent(context, IPificationAuthActivity::class.java)
                    startActivity(intent)
                },
                modifier = Modifier
                    .width(300.dp)
                    .padding(bottom = 20.dp)
            )
            
            IMButton(
                text = stringResource(id = R.string.login_with_instant_message),
                onClick = { 
                    val intent = Intent(context, IMAuthActivity::class.java)
                    startActivity(intent)
                    // Uncomment the line below to open a specific IM activity
                    // val intent = Intent(context, IMAuthAutoModeActivity::class.java)
                    // val intent = Intent(context, IMAuthManualActivity::class.java)
                },
                modifier = Modifier
                    .width(300.dp)
                    .padding(bottom = 100.dp)
            )
            
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


