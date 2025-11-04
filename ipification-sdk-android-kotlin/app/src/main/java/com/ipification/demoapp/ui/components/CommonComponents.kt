package com.ipification.demoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ipification.demoapp.ui.theme.IPGreen
import com.ipification.demoapp.ui.theme.IPPrimary
import com.ipification.demoapp.ui.theme.IPWhite

@Composable
fun IPificationTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = if (onBackClick != null) {
            {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        } else null,
        backgroundColor = IPPrimary,
        contentColor = IPWhite
    )
}

@Composable
fun IPificationButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = IPPrimary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(50.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = backgroundColor,
            contentColor = IPWhite,
            disabledBackgroundColor = backgroundColor.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = IPWhite
        )
    }
}

@Composable
fun IMButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(50.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = IPGreen,
            contentColor = IPWhite,
            disabledBackgroundColor = IPGreen.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = IPWhite
        )
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = IPPrimary
        )
    }
}

@Composable
fun CenteredText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 16
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Center,
        fontSize = fontSize.sp,
        color = Color(0xFF444444)
    )
}

