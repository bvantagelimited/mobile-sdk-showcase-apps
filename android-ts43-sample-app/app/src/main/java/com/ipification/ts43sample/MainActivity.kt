package com.ipification.ts43sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ipification.ts43sample.ui.ResultScreen
import com.ipification.ts43sample.ui.TS43Screen
import com.ipification.ts43sample.ui.theme.TS43SampleTheme
//import com.ipification.ts43sample.util.MNCHelper
import com.ipification.ts43sample.viewmodel.TS43ViewModel

/**
 * Main Activity for TS43 Sample App
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get MNC/MCC from active SIM
//        val activeSIM = MNCHelper(this).getActiveDataSimOperator()
//        Helper.MNCMCC = "${activeSIM.getMCC()}${activeSIM.getMNC()}"
        
        setContent {
            TS43SampleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TS43SampleApp()
                }
            }
        }
    }
}

/**
 * Main composable for navigation between screens
 */
@Composable
fun TS43SampleApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.TS43) }
    var resultResponse by remember { mutableStateOf<String?>(null) }
    var resultError by remember { mutableStateOf<String?>(null) }
    
    val viewModel: TS43ViewModel = viewModel()

    when (currentScreen) {
        is Screen.TS43 -> {
            TS43Screen(
                viewModel = viewModel,
                onResult = { response, error ->
                    resultResponse = response
                    resultError = error
                    currentScreen = Screen.Result
                }
            )
        }
        is Screen.Result -> {
            ResultScreen(
                response = resultResponse,
                error = resultError,
                onBack = {
                    currentScreen = Screen.TS43
                },
                viewModel = viewModel
            )
        }
    }
}

/**
 * Screen navigation sealed class
 */
sealed class Screen {
    data object TS43 : Screen()
    data object Result : Screen()
}
