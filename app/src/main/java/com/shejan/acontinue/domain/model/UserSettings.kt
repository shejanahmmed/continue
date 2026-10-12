package com.shejan.acontinue.domain.model

enum class ThemeMode(val displayName: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System")
}

enum class AccentPalette(
    val displayName: String,
    val colorHex: Long,
    val mutedHex: Long
) {
    MONOCHROME_RED("Monochrome Red", 0xFFD71921, 0xFF4A1012),
    MATRIX_GREEN("Matrix Green", 0xFF00E676, 0xFF0A3D1E),
    CYBER_AMBER("Cyber Amber", 0xFFFFB300, 0xFF4A3400),
    TACTILE_WHITE("Tactile White", 0xFFFFFFFF, 0xFF333333)
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentPalette: AccentPalette = AccentPalette.MONOCHROME_RED,
    val hapticFeedbackEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)
