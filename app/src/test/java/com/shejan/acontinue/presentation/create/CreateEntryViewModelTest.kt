package com.shejan.acontinue.presentation.create

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeNoteRepository
import com.shejan.acontinue.FakeScheduler
import com.shejan.acontinue.FakeSettingsRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.EntryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateEntryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var scheduler: FakeScheduler
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: CreateEntryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        noteRepository = FakeNoteRepository()
        scheduler = FakeScheduler()
        settingsRepository = FakeSettingsRepository()
        viewModel = CreateEntryViewModel(activityRepository, noteRepository, scheduler, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test empty title emits error event`() = runTest(testDispatcher) {
        viewModel.updateTitle("")

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CreateEntryEvent.ShowError)
        }
    }

    @Test
    fun `test saving valid activity schedules reminder and creates initial note`() = runTest(testDispatcher) {
        viewModel.updateTitle("CS 101 Lecture")
        viewModel.updateCategory(ActivityCategory.CLASS)
        viewModel.updateNotes("Bring textbook Chapter 4")

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertEquals(CreateEntryEvent.SavedSuccessfully, event)

            val activities = activityRepository.activitiesFlow.value
            assertEquals(1, activities.size)
            assertEquals("CS 101 Lecture", activities.first().title)
            assertEquals(ActivityCategory.CLASS, activities.first().category)

            val notes = noteRepository.notesFlow.value
            assertEquals(1, notes.size)
            assertEquals("Bring textbook Chapter 4", notes.first().content)

            assertEquals(1, scheduler.scheduledActivities.size)
            assertEquals("CS 101 Lecture", scheduler.scheduledActivities.first().title)
        }
    }

    @Test
    fun `test saving reminder entry type`() = runTest(testDispatcher) {
        viewModel.updateEntryType(EntryType.REMINDER)
        viewModel.updateTitle("Submit Assignment")

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            val saved = activityRepository.activitiesFlow.value.first()
            assertEquals(EntryType.REMINDER, saved.entryType)
            assertEquals("Submit Assignment", saved.title)
        }
    }

    @Test
    fun `test appending emoji updates title and state`() = runTest(testDispatcher) {
        assertTrue(viewModel.state.value.recentEmojis.isEmpty())

        viewModel.appendEmojiToTitle("🎂")
        testScheduler.advanceUntilIdle()

        assertEquals("🎂", viewModel.state.value.title)
        assertEquals("🎂", viewModel.state.value.emoji)
        assertEquals(listOf("🎂"), viewModel.state.value.recentEmojis)
        assertEquals(listOf("🎂"), settingsRepository.settingsFlow.value.recentEmojis)

        viewModel.updateTitle("Birthday Party")
        viewModel.appendEmojiToTitle("🎉")
        testScheduler.advanceUntilIdle()

        assertEquals("🎉 Birthday Party", viewModel.state.value.title)
        assertEquals("🎉", viewModel.state.value.emoji)
        assertEquals(listOf("🎉", "🎂"), viewModel.state.value.recentEmojis)
        assertEquals(listOf("🎉", "🎂"), settingsRepository.settingsFlow.value.recentEmojis)
    }
}
