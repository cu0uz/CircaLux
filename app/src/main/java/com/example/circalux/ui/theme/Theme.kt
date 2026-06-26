package com.example.circalux.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CircadianColorScheme = darkColorScheme(
    primary = SolarYellow,
    onPrimary = DeepBlue,
    primaryContainer = NightViolet,
    onPrimaryContainer = Color.White,
    secondary = SolarOrange,
    onSecondary = Color.Black,
    tertiary = NightViolet,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface,
    error = ErrorRed
)

@Composable
fun CircaLuxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // We focus on the Circadian Dark theme for a premium feel
    val colorScheme = CircadianColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
