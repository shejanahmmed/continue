package com.shejan.acontinue.presentation.diary

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeNoteRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
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
class DiaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var viewModel: DiaryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        noteRepository = FakeNoteRepository()
        viewModel = DiaryViewModel(activityRepository, noteRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test diary excludes activities and reminders and shows only notes`() = runTest(testDispatcher) {
        // Insert standard activity/event
        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Software Engineering Event",
                entryType = EntryType.ACTIVITY,
                startTimeMillis = 1000L,
                endTimeMillis = 2000L
            )
        )
        // Insert reminder
        activityRepository.insertActivity(
            ActivityItem(
                id = 2L,
                title = "Pay Utility Bill Reminder",
                entryType = EntryType.REMINDER,
                startTimeMillis = 3000L,
                endTimeMillis = 3000L
            )
        )
        // Insert note activity and note item
        activityRepository.insertActivity(
            ActivityItem(
                id = 3L,
                title = "Architecture Decision Note",
                description = "Decided to use Clean Architecture",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                reminderCategory = "Architecture",
                createdAtMillis = 4000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 100L,
                activityId = 3L,
                title = "Architecture Decision Note",
                content = "Decided to use Clean Architecture",
                updatedAtMillis = 4000L
            )
        )

        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            // Events and reminders must not be shown on the Diary page
            assertEquals(1, state.notes.size)
            assertEquals("Architecture Decision Note", state.notes.first().title)
            assertEquals("Decided to use Clean Architecture", state.notes.first().previewText)
            assertEquals(3L, state.notes.first().activityId)
        }
    }

    @Test
    fun `test diary search filters notes by title, content, and labels`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 10L,
                title = "Project Roadmap",
                description = "Sprint planning details for upcoming release",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                reminderCategory = "Sprint,Planning",
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 101L,
                activityId = 10L,
                title = "Project Roadmap",
                content = "Sprint planning details for upcoming release",
                updatedAtMillis = 1000L
            )
        )

        activityRepository.insertActivity(
            ActivityItem(
                id = 20L,
                title = "Groceries",
                description = "[ ] Oat milk\n[ ] Coffee beans",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                reminderCategory = "Personal",
                createdAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 102L,
                activityId = 20L,
                title = "Groceries",
                content = "[ ] Oat milk\n[ ] Coffee beans",
                updatedAtMillis = 2000L
            )
        )

        testScheduler.advanceUntilIdle()

        // Filter by title
        viewModel.onSearchQueryChange("Roadmap")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.notes.size)
            assertEquals("Project Roadmap", state.notes.first().title)
        }

        // Filter by content
        viewModel.onSearchQueryChange("Coffee")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.notes.size)
            assertEquals("Groceries", state.notes.first().title)
            assertTrue(state.notes.first().isChecklist)
        }

        // Filter by label
        viewModel.onSearchQueryChange("Planning")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.notes.size)
            assertEquals("Project Roadmap", state.notes.first().title)
        }
    }

    @Test
    fun `test diary note metadata parsing and descending chronological sorting`() = runTest(testDispatcher) {
        // Older note
        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Older Note",
                description = "Simple plain text note",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 11L,
                activityId = 1L,
                title = "Older Note",
                content = "Simple plain text note",
                updatedAtMillis = 1000L
            )
        )

        // Newer note with voice recording and photo
        val mediaContent = "![audio](content://media/audio/1)\n\n![photo](content://media/photo/1)\n\nMeeting audio notes"
        activityRepository.insertActivity(
            ActivityItem(
                id = 2L,
                title = "Newer Note with Audio",
                description = mediaContent,
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 5000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 12L,
                activityId = 2L,
                title = "Newer Note with Audio",
                content = mediaContent,
                updatedAtMillis = 5000L
            )
        )

        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.notes.size)
            // Most recent first
            assertEquals("Newer Note with Audio", state.notes[0].title)
            assertEquals(1, state.notes[0].audioCount)
            assertEquals(1, state.notes[0].photoCount)
            assertEquals("Meeting audio notes", state.notes[0].previewText)
            assertEquals("Older Note", state.notes[1].title)
        }
    }

    @Test
    fun `test note with Untitled Note or blank title normalizes to empty string for card display`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 5L,
                title = "Untitled Note",
                description = "Just some content without title",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 50L,
                activityId = 5L,
                title = "",
                content = "Just some content without title",
                updatedAtMillis = 1000L
            )
        )

        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.notes.size)
            assertEquals("", state.notes.first().title)
            assertEquals("Just some content without title", state.notes.first().previewText)
        }
    }
}
