package com.ipification.demoapp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipification.demoapp.R
import com.ipification.demoapp.ui.components.IPificationTopBar
import com.ipification.demoapp.ui.theme.IPGreen
import com.ipification.demoapp.ui.theme.IPificationTheme
import org.json.JSONObject

class ResultSuccessActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenInfo = intent.getStringExtra("responseStr")
        val formattedJson = try {
            JSONObject(tokenInfo ?: "").toString(4)
        } catch (e: Exception) {
            tokenInfo ?: ""
        }

        setContent {
            IPificationTheme {
                SuccessResultScreen(
                    result = formattedJson,
                    onBackClick = { finish() }
                )
            }
        }
    }

    @Composable
    fun SuccessResultScreen(
        result: String,
        onBackClick: () -> Unit
    ) {
        Scaffold(
            topBar = {
                IPificationTopBar(
                    title = "Result",
                    onBackClick = onBackClick
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                
                Text(
                    text = "Success",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = IPGreen,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                
                Spacer(modifier = Modifier.height(25.dp))
                
                Image(
                    painter = painterResource(id = R.drawable.ic_success),
                    contentDescription = "Success",
                    modifier = Modifier.size(78.dp)
                )
                
                Spacer(modifier = Modifier.height(25.dp))
                
                Text(
                    text = result,
                    fontSize = 16.sp,
                    color = IPGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}