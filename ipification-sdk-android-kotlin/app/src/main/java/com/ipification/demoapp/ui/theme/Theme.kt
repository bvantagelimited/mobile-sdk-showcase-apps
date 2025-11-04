package com.ipification.demoapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val DarkColorPalette = darkColors(
    primary = IPPrimary,
    primaryVariant = IPPrimaryDark,
    secondary = IPAccent
)

private val LightColorPalette = lightColors(
    primary = IPPrimary,
    primaryVariant = IPPrimaryDark,
    secondary = IPAccent,
    background = IPBackground,
    surface = IPWhite,
    onPrimary = IPWhite,
    onSecondary = IPWhite,
    onBackground = IPDarkGray,
    onSurface = IPDarkGray
)

@Composable
fun IPificationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

