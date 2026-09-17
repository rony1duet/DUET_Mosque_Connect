package com.duet.mosque.connect.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * ============================================================================
 * TYPOGRAPHY: MATERIAL 3 TEXT STYLES
 * ============================================================================
 *
 * WHAT IS TYPOGRAPHY?
 * -------------------
 * Material 3 defines text roles such as:
 * - `displayLarge`, `displayMedium`, `displaySmall` (Huge headings)
 * - `headlineLarge`, `headlineMedium`, `headlineSmall` (Section headers)
 * - `titleLarge`, `titleMedium`, `titleSmall` (Card titles & subtitles)
 * - `bodyLarge`, `bodyMedium`, `bodySmall` (Main body paragraph text)
 * - `labelLarge`, `labelMedium`, `labelSmall` (Buttons & badges)
 *
 * `sp` (Scale-independent Pixels):
 * Unlike fixed pixels (`px`), `sp` automatically respects the user's font size
 * preferences set in Android system settings, ensuring accessibility.
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    )
)

