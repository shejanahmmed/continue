package com.shejan.acontinue.domain.model

import java.util.UUID

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

data class CustomAlert(
    val id: String = UUID.randomUUID().toString(),
    val value: Int,
    val unit: String
) {
    val minutesBefore: Int get() = when (unit.lowercase()) {
        "minutes" -> value
        "hours" -> value * 60
        "days" -> value * 1440
        "weeks" -> value * 10080
        else -> value
    }

    val displayName: String get() {
        val singular = when (unit.lowercase()) {
            "minutes" -> "minute"
            "hours" -> "hour"
            "days" -> "day"
            "weeks" -> "week"
            else -> unit
        }
        val unitStr = if (value == 1) singular else "${singular}s"
        return "$value $unitStr before"
    }
}

data class ReminderCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: Long,
    val iconName: String,
    val isDefault: Boolean = false
) {
    companion object {
        val DEFAULT = ReminderCategory(
            id = "default_my_reminders",
            name = "My reminders",
            colorHex = 0xFF9D7BFC,
            iconName = "bell",
            isDefault = true
        )
    }
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentPalette: AccentPalette = AccentPalette.MONOCHROME_RED,
    val hapticFeedbackEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val recentEmojis: List<String> = emptyList(),
    val customAlerts: List<CustomAlert> = emptyList(),
    val reminderCategories: List<ReminderCategory> = listOf(ReminderCategory.DEFAULT),
    val noteLabels: List<String> = emptyList()
)
