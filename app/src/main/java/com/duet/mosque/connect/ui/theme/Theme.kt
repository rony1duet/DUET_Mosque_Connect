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

/**
 * ============================================================================
 * THEME SYSTEM: MATERIAL 3 COLOR SCHEMES & THEME PROVIDER
 * ============================================================================
 *
 * HOW DOES THEMING WORK IN JETPACK COMPOSE?
 * -----------------------------------------
 * 1. We create two `ColorScheme` objects: `DarkColorScheme` and `LightColorScheme`.
 *    Each maps Material 3 standard color tokens (primary, surface, background, error, etc.)
 *    to our custom brand colors.
 *
 * 2. `@Composable fun MyApplicationTheme(...)`:
 *    A wrapper Composable that surrounds our app UI with `MaterialTheme(...)`.
 *    Any child Composable inside `MyApplicationTheme` can access these colors via:
 *    `MaterialTheme.colorScheme.primary`, `MaterialTheme.colorScheme.surface`, etc.
 *
 * 3. `isSystemInDarkTheme()`:
 *    A Compose utility that automatically checks if the Android device is currently
 *    set to System Dark Mode or Light Mode.
 */

// --- Dark Mode Color Scheme ---
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

// --- Light Mode Color Scheme ---
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

/**
 * Root Theme Wrapper for DUET Mosque Connect.
 *
 * @param darkTheme Whether to render dark mode colors (defaults to system setting).
 * @param dynamicColor Android 12+ wallpaper dynamic theming. Kept `false` by default
 *                     to preserve the custom DUET Mosque emerald brand identity.
 * @param content The composable UI tree to render inside this theme.
 */
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


