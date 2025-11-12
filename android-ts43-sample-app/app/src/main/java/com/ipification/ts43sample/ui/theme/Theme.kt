package com.ipification.ts43sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Material Design 3 - Light Theme Colors
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFfc1c4b),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF775652),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBD1),
    onSecondaryContainer = Color(0xFF2C1512),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A19),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF201A19),
    surfaceVariant = Color(0xFFF5DDDA),
    onSurfaceVariant = Color(0xFF534341),
    outline = Color(0xFF857370),
    outlineVariant = Color(0xFFD8C2BE)
)

// Material Design 3 - Dark Theme Colors
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFfc1c4b),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFE7BDB7),
    onSecondary = Color(0xFF442925),
    secondaryContainer = Color(0xFF5D3F3B),
    onSecondaryContainer = Color(0xFFFFDAD6),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033)
)

@Composable
fun TS43SampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
