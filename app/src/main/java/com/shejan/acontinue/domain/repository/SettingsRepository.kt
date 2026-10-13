package com.shejan.acontinue.domain.repository

import com.shejan.acontinue.domain.model.AccentPalette
import com.shejan.acontinue.domain.model.CustomAlert
import com.shejan.acontinue.domain.model.ThemeMode
import com.shejan.acontinue.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccentPalette(palette: AccentPalette)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun addRecentEmoji(emoji: String)
    suspend fun addCustomAlert(value: Int, unit: String): CustomAlert
    suspend fun updateCustomAlert(id: String, value: Int, unit: String)
    suspend fun deleteCustomAlert(id: String)
}
