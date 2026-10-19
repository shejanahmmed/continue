package com.shejan.acontinue.domain.repository

import com.shejan.acontinue.domain.model.AccentPalette
import com.shejan.acontinue.domain.model.CustomAlert
import com.shejan.acontinue.domain.model.ThemeMode
import com.shejan.acontinue.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getUserSettings(): Flow<UserSettings>
    fun getCurrentSettings(): UserSettings
    suspend fun setOnboardingCompleted(completed: Boolean = true)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccentPalette(palette: AccentPalette)
    suspend fun setHapticFeedbackEnabled(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun addRecentEmoji(emoji: String)
    suspend fun addCustomAlert(value: Int, unit: String): CustomAlert
    suspend fun updateCustomAlert(id: String, value: Int, unit: String)
    suspend fun deleteCustomAlert(id: String)
    suspend fun addReminderCategory(name: String, colorHex: Long, iconName: String): com.shejan.acontinue.domain.model.ReminderCategory
    suspend fun updateReminderCategory(id: String, newName: String, newColorHex: Long, newIconName: String): com.shejan.acontinue.domain.model.ReminderCategory
    suspend fun deleteReminderCategory(id: String)
    fun getNoteLabels(): Flow<List<String>>
    suspend fun addNoteLabel(name: String)
    suspend fun updateNoteLabel(oldName: String, newName: String)
    suspend fun deleteNoteLabel(name: String)
}
