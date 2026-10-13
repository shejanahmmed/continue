package com.shejan.acontinue.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.shejan.acontinue.domain.model.AccentPalette
import com.shejan.acontinue.domain.model.ThemeMode
import com.shejan.acontinue.domain.model.UserSettings
import com.shejan.acontinue.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

        return UserSettings(
            themeMode = themeMode,
            accentPalette = accentPalette,
            hapticFeedbackEnabled = haptic,
            notificationsEnabled = notifs,
            recentEmojis = recentEmojis
        )
    }

    companion object {
        private const val PREFS_NAME = "continue_user_settings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_PALETTE = "accent_palette"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_RECENT_EMOJIS = "recent_emojis"
        private const val DELIMITER = "|||"
    }
}
