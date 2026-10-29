package com.shejan.acontinue.presentation.diary

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeNoteRepository
import com.shejan.acontinue.FakeSettingsRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: DiaryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        noteRepository = FakeNoteRepository()
        settingsRepository = FakeSettingsRepository()
        viewModel = DiaryViewModel(activityRepository, noteRepository, settingsRepository)
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

    @Test
    fun `test diary card sorting options Default, Date created, and Date modified`() = runTest(testDispatcher) {
        // Note 1: Created at 1000L, modified at 1000L
        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Note Alpha (Created First)",
                description = "Content 1",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 101L,
                activityId = 1L,
                title = "Note Alpha (Created First)",
                content = "Content 1",
                updatedAtMillis = 1000L
            )
        )

        // Note 2: Created at 2000L, modified at 2000L
        activityRepository.insertActivity(
            ActivityItem(
                id = 2L,
                title = "Note Beta (Created Second)",
                description = "Content 2",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 102L,
                activityId = 2L,
                title = "Note Beta (Created Second)",
                content = "Content 2",
                updatedAtMillis = 2000L
            )
        )

        // Now edit Note 1 at 3000L (so Note 1 was created first, but modified last)
        noteRepository.updateNote(
            ActivityNote(
                id = 101L,
                activityId = 1L,
                title = "Note Alpha (Created First)",
                content = "Content 1 Modified",
                updatedAtMillis = 3000L
            )
        )

        testScheduler.advanceUntilIdle()

        // 1. DEFAULT: Newest created first (Note Beta at 2000L is before Note Alpha at 1000L).
        // Note Alpha's position does not change despite being modified at 3000L!
        viewModel.onSortOptionChange(DiarySortOption.DEFAULT)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(DiarySortOption.DEFAULT, state.sortOption)
            assertEquals("Note Beta (Created Second)", state.notes[0].title)
            assertEquals("Note Alpha (Created First)", state.notes[1].title)
        }

        // 2. DATE_CREATED: Oldest created first (Note Alpha at 1000L is before Note Beta at 2000L)
        viewModel.onSortOptionChange(DiarySortOption.DATE_CREATED)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(DiarySortOption.DATE_CREATED, state.sortOption)
            assertEquals("Note Alpha (Created First)", state.notes[0].title)
            assertEquals("Note Beta (Created Second)", state.notes[1].title)
        }

        // 3. DATE_MODIFIED: Most recently modified first (Note Alpha at 3000L is before Note Beta at 2000L)
        viewModel.onSortOptionChange(DiarySortOption.DATE_MODIFIED)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(DiarySortOption.DATE_MODIFIED, state.sortOption)
            assertEquals("Note Alpha (Created First)", state.notes[0].title)
            assertEquals("Note Beta (Created Second)", state.notes[1].title)
        }
    }

    @Test
    fun `test multi-selection toggle selectAll and clearSelection`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Note 1",
                description = "Content 1",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        activityRepository.insertActivity(
            ActivityItem(
                id = 2L,
                title = "Note 2",
                description = "Content 2",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 2000L
            )
        )
        testScheduler.advanceUntilIdle()

        val key1 = "act_1"
        val key2 = "act_2"

        // Initially no selection
        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedKeys.isEmpty())

        // Toggle select key 1
        viewModel.toggleSelection(key1)
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSelectionMode)
        assertEquals(setOf(key1), viewModel.uiState.value.selectedKeys)

        // Toggle select key 2
        viewModel.toggleSelection(key2)
        testScheduler.advanceUntilIdle()
        assertEquals(setOf(key1, key2), viewModel.uiState.value.selectedKeys)

        // Toggle unselect key 1
        viewModel.toggleSelection(key1)
        testScheduler.advanceUntilIdle()
        assertEquals(setOf(key2), viewModel.uiState.value.selectedKeys)

        // Select all
        viewModel.selectAll()
        testScheduler.advanceUntilIdle()
        assertEquals(setOf(key1, key2), viewModel.uiState.value.selectedKeys)

        // Clear selection
        viewModel.clearSelection()
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.selectedKeys.isEmpty())
    }

    @Test
    fun `test togglePinSelectedNotes pins note and places it at top across sorting`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Older Note",
                description = "Content 1",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        activityRepository.insertActivity(
            ActivityItem(
                id = 2L,
                title = "Newer Note",
                description = "Content 2",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 2000L
            )
        )
        testScheduler.advanceUntilIdle()

        // By default (newest first), Newer Note is first
        assertEquals("Newer Note", viewModel.uiState.value.notes[0].title)
        assertEquals("Older Note", viewModel.uiState.value.notes[1].title)

        // Select Older Note and pin it
        viewModel.toggleSelection("act_1")
        testScheduler.advanceUntilIdle()
        viewModel.togglePinSelectedNotes()
        testScheduler.advanceUntilIdle()

        // Selection should be cleared after pinning
        assertFalse(viewModel.uiState.value.isSelectionMode)

        // Now Older Note is pinned and MUST appear at index 0 (top of the list)
        val state = viewModel.uiState.value
        assertEquals("Older Note", state.notes[0].title)
        assertTrue(state.notes[0].isPinned)
        assertEquals("Newer Note", state.notes[1].title)
        assertFalse(state.notes[1].isPinned)
    }

    @Test
    fun `test togglePinSelectedNotes unpins notes when all selected are pinned`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Note 1",
                description = "Content 1",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        testScheduler.advanceUntilIdle()

        // Pin Note 1
        viewModel.toggleSelection("act_1")
        testScheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.areAllSelectedPinned)
        viewModel.togglePinSelectedNotes()
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.notes[0].isPinned)

        // Select Note 1 again and toggle pin -> should unpin
        viewModel.toggleSelection("act_1")
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.areAllSelectedPinned)
        viewModel.togglePinSelectedNotes()
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.notes[0].isPinned)
    }

    @Test
    fun `test deleteSelectedNotes removes activities notes and clears selection`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 10L,
                title = "To Delete 1",
                description = "Desc 1",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 101L,
                activityId = 10L,
                title = "To Delete 1",
                content = "Desc 1",
                updatedAtMillis = 1000L
            )
        )
        activityRepository.insertActivity(
            ActivityItem(
                id = 20L,
                title = "Keep This Note",
                description = "Desc 2",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 2000L
            )
        )
        testScheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.notes.size)

        // Select Note 10 and delete
        viewModel.toggleSelection("act_10")
        testScheduler.advanceUntilIdle()
        viewModel.deleteSelectedNotes()
        testScheduler.advanceUntilIdle()

        // Selection cleared and note deleted
        assertFalse(viewModel.uiState.value.isSelectionMode)
        val remainingNotes = viewModel.uiState.value.notes
        assertEquals(1, remainingNotes.size)
        assertEquals("Keep This Note", remainingNotes[0].title)
    }

    @Test
    fun `test event workspace notes do not leak into standalone diary list`() = runTest(testDispatcher) {
        // Insert an Event (EntryType.ACTIVITY)
        activityRepository.insertActivity(
            ActivityItem(
                id = 100L,
                title = "Backend Architecture Workshop",
                description = "Event overview description",
                entryType = EntryType.ACTIVITY,
                startTimeMillis = 1000L,
                endTimeMillis = 5000L
            )
        )
        // Insert a note taken during the Event inside its Workspace
        noteRepository.insertNote(
            ActivityNote(
                id = 501L,
                activityId = 100L,
                title = "Workshop Note 1",
                content = "Notes taken during the running event",
                updatedAtMillis = 2000L
            )
        )

        // Insert a standalone Diary note (EntryType.NOTE)
        activityRepository.insertActivity(
            ActivityItem(
                id = 200L,
                title = "Standalone Diary Note",
                description = "Personal diary reflection",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 3000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 502L,
                activityId = 200L,
                title = "Standalone Diary Note",
                content = "Personal diary reflection",
                updatedAtMillis = 3000L
            )
        )

        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.notes.size)
            assertEquals("Standalone Diary Note", state.notes.first().title)
            assertEquals(200L, state.notes.first().activityId)
        }
    }
}

