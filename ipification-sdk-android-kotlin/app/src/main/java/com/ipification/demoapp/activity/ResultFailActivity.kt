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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipification.demoapp.R
import com.ipification.demoapp.ui.components.IPificationTopBar
import com.ipification.demoapp.ui.theme.IPAccent
import com.ipification.demoapp.ui.theme.IPificationTheme
import org.json.JSONObject

class ResultFailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val errorMessage = intent.getStringExtra("error")
        val formattedError = try {
            JSONObject(errorMessage ?: "").toString(4)
        } catch (e: Exception) {
            errorMessage ?: ""
        }

        setContent {
            IPificationTheme {
                FailResultScreen(
                    error = formattedError,
                    onBackClick = { finish() }
                )
            }
        }
    }

    @Composable
    fun FailResultScreen(
        error: String,
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
                Spacer(modifier = Modifier.height(45.dp))
                
                Text(
                    text = "Fail",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = IPAccent,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                
                Spacer(modifier = Modifier.height(25.dp))
                
                Image(
                    painter = painterResource(id = R.drawable.ic_fail),
                    contentDescription = "Fail",
                    modifier = Modifier.wrapContentSize()
                )
                
                Spacer(modifier = Modifier.height(25.dp))
                
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = IPAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}