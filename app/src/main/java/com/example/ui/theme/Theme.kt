package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IndustrialColorScheme = lightColorScheme(
    primary = TsmcBlue,
    onPrimary = Color.White,
    primaryContainer = TsmcBlueLight,
    onPrimaryContainer = Color.White,
    secondary = Cyan600,
    onSecondary = Color.White,
    tertiary = NeonBlue,
    onTertiary = Color.White,
    background = Slate50,
    onBackground = Slate800,
    surface = Color.White,
    onSurface = Slate800,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate200,
    error = NeonRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // We enforce our highly branded Industrial Bright scale
    MaterialTheme(
        colorScheme = IndustrialColorScheme,
        typography = Typography,
        content = content
    )
}
