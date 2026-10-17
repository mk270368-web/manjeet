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
    primary = Color(0xFF93B5F5),
    onPrimary = Color(0xFF002A67),
    primaryContainer = VidyutBlueLight,
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = ElectricGold,
    onSecondary = Color(0xFF3F2E00),
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = ElectricGoldContainer,
    tertiary = Color(0xFF7DD3FC),
    onTertiary = Color(0xFF00354E),
    background = Color(0xFF0B1322),
    surface = Color(0xFF101B2E),
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFFCBD5E1),
)

private val LightColorScheme = lightColorScheme(
    primary = VidyutBluePrimary,
    onPrimary = Color.White,
    primaryContainer = VidyutBlueContainer,
    onPrimaryContainer = OnVidyutBlueContainer,
    secondary = ElectricGoldDark,
    onSecondary = Color.Black,
    secondaryContainer = ElectricGoldContainer,
    onSecondaryContainer = OnElectricGoldContainer,
    tertiary = CircuitCyan,
    onTertiary = Color.White,
    tertiaryContainer = CircuitCyanContainer,
    onTertiaryContainer = OnCircuitCyanContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onBackground = SlateDark,
    onSurface = SlateDark,
    onSurfaceVariant = SlateMedium,
    outline = CardBorder
)

@Composable
fun VidyutGyanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded electrical educational theme consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
