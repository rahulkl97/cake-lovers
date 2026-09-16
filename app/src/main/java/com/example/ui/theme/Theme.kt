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

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = TextWhitePrimary,
    primaryContainer = DeepVelvetWine,
    onPrimaryContainer = SoftRoseCream,
    secondary = GoldenCaramel,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF2E2416),
    onSecondaryContainer = HoneyButter,
    tertiary = PistachioGreen,
    background = DarkCanvasBackground,
    onBackground = TextWhitePrimary,
    surface = DarkCardSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = DarkCardElevated,
    onSurfaceVariant = TextGreySecondary,
    outline = DarkBorderSubtle
)

private val LightColorScheme = DarkColorScheme // Keep app styled with dark luxury format from user mockup

@Composable
fun CakeLoversTheme(
    darkTheme: Boolean = true, // Luxury dark aesthetic by default as requested
    dynamicColor: Boolean = false, // Keep distinctive artisanal branding
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backwards-compatible alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CakeLoversTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
