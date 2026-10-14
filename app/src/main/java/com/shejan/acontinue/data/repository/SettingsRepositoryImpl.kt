package com.shejan.acontinue.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.shejan.acontinue.domain.model.AccentPalette
import com.shejan.acontinue.domain.model.CustomAlert
import com.shejan.acontinue.domain.model.ReminderCategory
import com.shejan.acontinue.domain.model.ThemeMode
import com.shejan.acontinue.domain.model.UserSettings
import com.shejan.acontinue.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class SettingsRepositoryImpl(
    context: Context
) : SettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    private val settingsFlow = _settings.asStateFlow()

    override fun getUserSettings(): Flow<UserSettings> = settingsFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _settings.update { it.copy(themeMode = mode) }
    }

    override suspend fun setAccentPalette(palette: AccentPalette) {
        prefs.edit().putString(KEY_ACCENT_PALETTE, palette.name).apply()
        _settings.update { it.copy(accentPalette = palette) }
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _settings.update { it.copy(hapticFeedbackEnabled = enabled) }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS, enabled).apply()
        _settings.update { it.copy(notificationsEnabled = enabled) }
    }

    override suspend fun addRecentEmoji(emoji: String) {
        if (emoji.isBlank()) return
        val current = _settings.value.recentEmojis
        val updated = (listOf(emoji) + current).distinct().take(30)
        prefs.edit().putString(KEY_RECENT_EMOJIS, updated.joinToString(DELIMITER)).apply()
        _settings.update { it.copy(recentEmojis = updated) }
    }

    override suspend fun addCustomAlert(value: Int, unit: String): CustomAlert {
        val newAlert = CustomAlert(id = UUID.randomUUID().toString(), value = value, unit = unit)
        val current = _settings.value.customAlerts
        val updated = current + newAlert
        saveCustomAlerts(updated)
        _settings.update { it.copy(customAlerts = updated) }
        return newAlert
    }

    override suspend fun updateCustomAlert(id: String, value: Int, unit: String) {
        val current = _settings.value.customAlerts
        val updated = current.map { alert ->
            if (alert.id == id) alert.copy(value = value, unit = unit) else alert
        }
        saveCustomAlerts(updated)
        _settings.update { it.copy(customAlerts = updated) }
    }

    override suspend fun deleteCustomAlert(id: String) {
        val current = _settings.value.customAlerts
        val updated = current.filterNot { it.id == id }
        saveCustomAlerts(updated)
        _settings.update { it.copy(customAlerts = updated) }
    }

    override suspend fun addReminderCategory(name: String, colorHex: Long, iconName: String): ReminderCategory {
        val newCategory = ReminderCategory(
            name = name,
            colorHex = colorHex,
            iconName = iconName,
            isDefault = false
        )
        val current = _settings.value.reminderCategories
        val updated = current + newCategory
        saveReminderCategories(updated)
        _settings.update { it.copy(reminderCategories = updated) }
        return newCategory
    }

    private fun saveCustomAlerts(alerts: List<CustomAlert>) {
        val serialized = alerts.joinToString(DELIMITER) { "${it.id}:::${it.value}:::${it.unit}" }
        prefs.edit().putString(KEY_CUSTOM_ALERTS, serialized).apply()
    }

    private fun parseCustomAlerts(raw: String): List<CustomAlert> {
        if (raw.isBlank()) return emptyList()
        return raw.split(DELIMITER).mapNotNull { item ->
            val parts = item.split(":::")
            if (parts.size >= 3) {
                val value = parts[1].toIntOrNull() ?: return@mapNotNull null
                CustomAlert(id = parts[0], value = value, unit = parts[2])
            } else null
        }
    }

    private fun saveReminderCategories(categories: List<ReminderCategory>) {
        val serialized = categories.joinToString(DELIMITER) {
            "${it.id}:::${it.name}:::${it.colorHex}:::${it.iconName}:::${it.isDefault}"
        }
        prefs.edit().putString(KEY_REMINDER_CATEGORIES, serialized).apply()
    }

    private fun parseReminderCategories(raw: String): List<ReminderCategory> {
        if (raw.isBlank()) return listOf(ReminderCategory.DEFAULT)
        val list = raw.split(DELIMITER).mapNotNull { item ->
            val parts = item.split(":::")
            if (parts.size >= 5) {
                val color = parts[2].toLongOrNull() ?: ReminderCategory.DEFAULT.colorHex
                val isDefault = parts[4].toBooleanStrictOrNull() ?: false
                ReminderCategory(
                    id = parts[0],
                    name = parts[1],
                    colorHex = color,
                    iconName = parts[3],
                    isDefault = isDefault
                )
            } else null
        }
        return if (list.none { it.isDefault || it.name == ReminderCategory.DEFAULT.name }) {
            listOf(ReminderCategory.DEFAULT) + list
        } else {
            list
        }
    }

    private fun loadSettings(): UserSettings {
        val themeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.name)
        val themeMode = try {
            ThemeMode.valueOf(themeStr ?: ThemeMode.DARK.name)
        } catch (_: Exception) {
            ThemeMode.DARK
        }

        val paletteStr = prefs.getString(KEY_ACCENT_PALETTE, AccentPalette.MONOCHROME_RED.name)
        val accentPalette = try {
            AccentPalette.valueOf(paletteStr ?: AccentPalette.MONOCHROME_RED.name)
        } catch (_: Exception) {
            AccentPalette.MONOCHROME_RED
        }

        val haptic = prefs.getBoolean(KEY_HAPTIC, true)
        val notifs = prefs.getBoolean(KEY_NOTIFICATIONS, true)
        val recentEmojisRaw = prefs.getString(KEY_RECENT_EMOJIS, "") ?: ""
        val recentEmojis = if (recentEmojisRaw.isBlank()) {
            emptyList()
        } else {
            recentEmojisRaw.split(DELIMITER).filter { it.isNotBlank() }
        }

        val customAlertsRaw = prefs.getString(KEY_CUSTOM_ALERTS, "") ?: ""
        val customAlerts = parseCustomAlerts(customAlertsRaw)

        val reminderCategoriesRaw = prefs.getString(KEY_REMINDER_CATEGORIES, "") ?: ""
        val reminderCategories = parseReminderCategories(reminderCategoriesRaw)

        return UserSettings(
            themeMode = themeMode,
            accentPalette = accentPalette,
            hapticFeedbackEnabled = haptic,
            notificationsEnabled = notifs,
            recentEmojis = recentEmojis,
            customAlerts = customAlerts,
            reminderCategories = reminderCategories
        )
    }

    companion object {
        private const val PREFS_NAME = "continue_user_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_PALETTE = "accent_palette"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_RECENT_EMOJIS = "recent_emojis"
        private const val KEY_CUSTOM_ALERTS = "custom_alerts"
        private const val KEY_REMINDER_CATEGORIES = "reminder_categories"
        private const val DELIMITER = "|||"
    }
}
