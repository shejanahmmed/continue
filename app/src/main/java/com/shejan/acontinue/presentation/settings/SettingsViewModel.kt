package com.shejan.acontinue.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.AccentPalette
import com.shejan.acontinue.domain.model.ThemeMode
import com.shejan.acontinue.domain.model.UserSettings
import com.shejan.acontinue.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings()
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        settingsRepository.getUserSettings()
            .onEach { userSettings ->
                _uiState.value = SettingsUiState(settings = userSettings)
            }
            .launchIn(viewModelScope)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun setAccentPalette(palette: AccentPalette) {
        viewModelScope.launch {
            settingsRepository.setAccentPalette(palette)
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setHapticFeedbackEnabled(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }
}
