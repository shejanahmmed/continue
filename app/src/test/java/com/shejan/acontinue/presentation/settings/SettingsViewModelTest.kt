package com.shejan.acontinue.presentation.settings

import app.cash.turbine.test
import com.shejan.acontinue.FakeSettingsRepository
import com.shejan.acontinue.domain.model.AccentPalette
import com.shejan.acontinue.domain.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = FakeSettingsRepository()
        viewModel = SettingsViewModel(settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state reflects default user settings`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(ThemeMode.DARK, state.settings.themeMode)
            assertEquals(AccentPalette.MONOCHROME_RED, state.settings.accentPalette)
            assertTrue(state.settings.hapticFeedbackEnabled)
            assertTrue(state.settings.notificationsEnabled)
        }
    }

    @Test
    fun `test updating theme mode updates repository and state`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.setThemeMode(ThemeMode.LIGHT)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(ThemeMode.LIGHT, state.settings.themeMode)
        }
    }

    @Test
    fun `test updating accent palette updates repository and state`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.setAccentPalette(AccentPalette.MATRIX_GREEN)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(AccentPalette.MATRIX_GREEN, state.settings.accentPalette)
        }
    }

    @Test
    fun `test toggling haptic feedback updates repository and state`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.setHapticFeedbackEnabled(false)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.settings.hapticFeedbackEnabled)
        }
    }

    @Test
    fun `test onboarding completion status updates repository and state`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        assertFalse(settingsRepository.getCurrentSettings().isOnboardingCompleted)

        settingsRepository.setOnboardingCompleted(true)
        testScheduler.advanceUntilIdle()

        assertTrue(settingsRepository.getCurrentSettings().isOnboardingCompleted)
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.settings.isOnboardingCompleted)
        }
    }
}
