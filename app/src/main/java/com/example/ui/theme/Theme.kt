package com.example.ui.theme

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

private val GraffitiDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color.Black,
    secondary = NeonPink,
    onSecondary = Color.White,
    tertiary = AcidYellow,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC7CBD8),
    outline = OutlineColor
)

private val GraffitiLightColorScheme = lightColorScheme(
    primary = Color(0xFF007A99),
    onPrimary = Color.White,
    secondary = Color(0xFFB8006E),
    onSecondary = Color.White,
    tertiary = Color(0xFF7A6400),
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) GraffitiDarkColorScheme else GraffitiDarkColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
