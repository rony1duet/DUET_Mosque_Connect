package com.duet.mosque.connect.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ============================================================================
 * COLOR SYSTEM: DUET MOSQUE CONNECT DESIGN TOKENS
 * ============================================================================
 *
 * WHAT IS THIS FILE?
 * ------------------
 * In Jetpack Compose, colors are defined as immutable `Color` objects using
 * 32-bit ARGB hex integers: `Color(0xAARRGGBB)`
 * - `AA` = Alpha (transparency): FF is 100% opaque, 00 is completely transparent.
 * - `RR` = Red (00 to FF)
 * - `GG` = Green (00 to FF)
 * - `BB` = Blue (00 to FF)
 *
 * BRANDING PALETTE:
 * - Emerald Green (0xFF03A052): Primary Islamic & mosque identity color.
 * - Notice Red (0xFFEB332C): Urgent alerts, janaza, and important notifications.
 * - Gold Accent (0xFFFFD700): Premium highlights, active prayer markers, Kaaba icon.
 */

// --- Primary Brand Colors (Emerald Green) ---
val EmeraldGreen = Color(0xFF03A052)
val EmeraldGreenLight = Color(0xFF1FD075)
val EmeraldGreenDark = Color(0xFF01733B)
val EmeraldGreenContainerLight = Color(0xFFE2F7ED)
val EmeraldGreenContainerDark = Color(0xFF01381E)

// --- Alert & Notice Colors (Red) ---
val NoticeRed = Color(0xFFEB332C)
val NoticeRedLight = Color(0xFFFDE8E7)
val NoticeRedDark = Color(0xFF3F0B09)
val NoticeRedTextLight = Color(0xFF9E1B15)
val NoticeRedTextDark = Color(0xFFFFB4AB)

// --- Accent & Highlight Colors (Gold) ---
val GoldAccent = Color(0xFFFFD700)
val GoldAccentLight = Color(0xFFFFF2B2)
val GoldAccentDark = Color(0xFFC5A000)

// --- High-contrast Neutrals for Light & Dark Modes ---
val BackgroundLight = Color(0xFFF6F8F6)
val BackgroundDark = Color(0xFF101411)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF181D1A)

val TextDark = Color(0xFF0E1310)          // High contrast primary text for Light Mode
val TextLight = Color(0xFFF0F4F1)         // High contrast primary text for Dark Mode
val TextMutedLight = Color(0xFF424E47)    // Clear secondary text for Light Mode
val TextMutedDark = Color(0xFFA2B3A8)     // Clear secondary text for Dark Mode

val CardBorderLight = Color(0xFFD8E2DC)
val CardBorderDark = Color(0xFF27312B)
val CreamAccent = Color(0xFFFFFBEB)


