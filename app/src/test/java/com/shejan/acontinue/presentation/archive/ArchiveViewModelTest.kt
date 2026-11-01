package com.shejan.acontinue.presentation.archive

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeNoteRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArchiveViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var viewModel: ArchiveViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        noteRepository = FakeNoteRepository()
        viewModel = ArchiveViewModel(activityRepository, noteRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test uiState loads archived notes and filters by query`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        // Non-archived note
        noteRepository.insertNote(
            ActivityNote(
                id = 1L,
                activityId = 0L,
                title = "Active Note",
                content = "General content",
                isArchived = false
            )
        )

        // Archived Note 1
        noteRepository.insertNote(
            ActivityNote(
                id = 2L,
                activityId = 0L,
                title = "Secret Project Plan",
                content = "Confidential archive",
                isArchived = true,
                archivedAtMillis = now - 2000
            )
        )

        // Archived Note 2 (Activity note)
        activityRepository.insertActivity(
            ActivityItem(
                id = 20L,
                title = "Meeting Notes 2025",
                description = "Architecture decisions",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                isArchived = true,
                archivedAtMillis = now - 1000
            )
        )

        viewModel.uiState.test {
            val state = awaitItem()
            testScheduler.advanceUntilIdle()
            val loadedState = if (state.isLoading) awaitItem() else state

            assertEquals(2, loadedState.notes.size)
            assertEquals("Meeting Notes 2025", loadedState.notes[0].title)
            assertEquals("Secret Project Plan", loadedState.notes[1].title)

            // Test search
            viewModel.onSearchQueryChange("Secret")
            testScheduler.advanceUntilIdle()
            val searchState = awaitItem()

            assertEquals(1, searchState.notes.size)
            assertEquals("Secret Project Plan", searchState.notes[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test unarchiveNote marks note as unarchived`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val noteId = noteRepository.insertNote(
            ActivityNote(
                id = 10L,
                activityId = 0L,
                title = "To Unarchive",
                content = "Content",
                isArchived = true,
                archivedAtMillis = now
            )
        )

        val item = ArchiveNoteItem(
            activityId = 0L,
            noteId = noteId,
            title = "To Unarchive",
            previewText = "Content",
            archivedAtMillis = now
        )

        viewModel.unarchiveNote(item)
        testScheduler.advanceUntilIdle()

        val updated = noteRepository.notesFlow.value.first { it.id == noteId }
        assertFalse(updated.isArchived)
    }

    @Test
    fun `test deleteNote moves archived note to bin`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val actId = activityRepository.insertActivity(
            ActivityItem(
                id = 30L,
                title = "Archived to delete",
                description = "",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                isArchived = true,
                archivedAtMillis = now
            )
        )

        val item = ArchiveNoteItem(
            activityId = actId,
            noteId = 0L,
            title = "Archived to delete",
            previewText = "",
            archivedAtMillis = now
        )

        viewModel.deleteNote(item)
        testScheduler.advanceUntilIdle()

        val updated = activityRepository.activitiesFlow.value.first { it.id == actId }
        assertTrue(updated.isDeleted)
    }
}

