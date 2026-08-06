package com.duet.mosque.connect.ui.theme

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
    primary = EmeraldGreen,
    onPrimary = Color.White,
    primaryContainer = EmeraldGreenContainerDark,
    onPrimaryContainer = TextLight,
    secondary = GoldAccent,
    onSecondary = TextDark,
    tertiary = EmeraldGreenLight,
    onTertiary = TextDark,
    error = NoticeRed,
    onError = Color.White,
    errorContainer = NoticeRedDark,
    onErrorContainer = NoticeRedTextDark,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
    surfaceVariant = CardBorderDark,
    onSurfaceVariant = TextMutedDark,
    outline = CardBorderDark
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = Color.White,
    primaryContainer = EmeraldGreenContainerLight,
    onPrimaryContainer = EmeraldGreenDark,
    secondary = GoldAccentDark,
    onSecondary = Color.White,
    tertiary = EmeraldGreenDark,
    onTertiary = Color.White,
    error = NoticeRed,
    onError = Color.White,
    errorContainer = NoticeRedLight,
    onErrorContainer = NoticeRedTextLight,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
    surfaceVariant = CardBorderLight,
    onSurfaceVariant = TextMutedLight,
    outline = CardBorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to enforce DUET Mosque Connect branding
    content: @Composable () -> Unit,
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

