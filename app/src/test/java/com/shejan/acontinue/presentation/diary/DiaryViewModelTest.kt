package com.shejan.acontinue.presentation.diary

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeNoteRepository
import com.shejan.acontinue.FakeSettingsRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.DiarySortOption
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
    fun `test sort option is persisted in settingsRepository and restored on app restart`() = runTest(testDispatcher) {
        assertEquals(DiarySortOption.DEFAULT, viewModel.uiState.value.sortOption)
        assertEquals(DiarySortOption.DEFAULT, settingsRepository.diarySortOptionFlow.value)

        viewModel.onSortOptionChange(DiarySortOption.DATE_MODIFIED)
        testScheduler.advanceUntilIdle()

        assertEquals(DiarySortOption.DATE_MODIFIED, viewModel.uiState.value.sortOption)
        assertEquals(DiarySortOption.DATE_MODIFIED, settingsRepository.diarySortOptionFlow.value)

        val restoredViewModel = DiaryViewModel(activityRepository, noteRepository, settingsRepository)
        testScheduler.advanceUntilIdle()

        assertEquals(DiarySortOption.DATE_MODIFIED, restoredViewModel.uiState.value.sortOption)
    }

    @Test
    fun `test multi-selection toggle and clearSelection`() = runTest(testDispatcher) {
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
    fun `test pinned notes persist across ViewModel reload with key alias synchronization`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 5L,
                title = "Meeting Note",
                description = "Architecture discussion",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 55L,
                activityId = 5L,
                title = "Meeting Note",
                content = "Architecture discussion",
                updatedAtMillis = 1000L
            )
        )
        testScheduler.advanceUntilIdle()

        // Pin the note via DiaryViewModel
        viewModel.toggleSelection("act_5")
        testScheduler.advanceUntilIdle()
        viewModel.togglePinSelectedNotes()
        testScheduler.advanceUntilIdle()

        // Verify both key aliases ('act_5' and 'note_55') are persisted in SettingsRepository
        val pinnedKeys = settingsRepository.pinnedNotesFlow.value
        assertTrue("act_5 must be in pinned keys", "act_5" in pinnedKeys)
        assertTrue("note_55 must be in pinned keys", "note_55" in pinnedKeys)

        // Simulate app restart / ViewModel re-instantiation with same settingsRepository
        val reloadedViewModel = DiaryViewModel(activityRepository, noteRepository, settingsRepository)
        testScheduler.advanceUntilIdle()

        val reloadedNotes = reloadedViewModel.uiState.value.notes
        assertEquals(1, reloadedNotes.size)
        assertTrue("Note must still be pinned after restart", reloadedNotes[0].isPinned)
    }

    @Test
    fun `test note saved with note_ prefix is recognized as pinned on Diary reload`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 8L,
                title = "Idea Note",
                description = "Important idea",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 88L,
                activityId = 8L,
                title = "Idea Note",
                content = "Important idea",
                updatedAtMillis = 1000L
            )
        )

        // Pre-seed settingsRepository with note_88 (as created in NoteEditor)
        settingsRepository.setNotesPinned(setOf("note_88"), true)
        testScheduler.advanceUntilIdle()

        // Create new DiaryViewModel simulating app launch
        val newViewModel = DiaryViewModel(activityRepository, noteRepository, settingsRepository)
        testScheduler.advanceUntilIdle()

        val notes = newViewModel.uiState.value.notes
        assertEquals(1, notes.size)
        assertTrue("Note must be recognized as pinned via note_ prefix", notes[0].isPinned)
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

    @Test
    fun `test toggleLayoutMode alternates between single and two column mode`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isGridMode)

        // Toggle to two-column (grid)
        viewModel.toggleLayoutMode()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isGridMode)
        assertTrue("SettingsRepository must store true for diary grid mode", settingsRepository.diaryGridModeFlow.value)

        // Simulate navigating away and coming back / reopening app
        val reloadedViewModel = DiaryViewModel(activityRepository, noteRepository, settingsRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue("Reloaded ViewModel must retain two-column grid mode from SettingsRepository", reloadedViewModel.uiState.value.isGridMode)

        // Toggle back to single-column (list)
        reloadedViewModel.toggleLayoutMode()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(reloadedViewModel.uiState.value.isGridMode)
        assertFalse("SettingsRepository must store false after toggling back", settingsRepository.diaryGridModeFlow.value)
    }

    @Test
    fun `test archiveSelectedNotes sets isArchived on selected activities and notes`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 301L,
                title = "Note to Archive",
                description = "Content to archive",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 601L,
                activityId = 301L,
                title = "Note to Archive",
                content = "Content to archive",
                updatedAtMillis = 1000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSelection("act_301")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSelectionMode)

        viewModel.archiveSelectedNotes()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSelectionMode)
        assertTrue(viewModel.uiState.value.notes.isEmpty())
        assertTrue(activityRepository.activitiesFlow.value.first { it.id == 301L }.isArchived)
        assertTrue(noteRepository.notesFlow.value.first { it.id == 601L }.isArchived)
    }

    @Test
    fun `test diary preserves palette custom colorHex for note items`() = runTest(testDispatcher) {
        val customColor = 0xFF5C1324L // Crimson / Burgundy from NOTE_COLORS
        activityRepository.insertActivity(
            ActivityItem(
                id = 401L,
                title = "Colored Note Activity",
                description = "Note with palette color",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                reminderCategory = "Notes",
                reminderCategoryColorHex = customColor,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 701L,
                activityId = 401L,
                title = "Colored Note Activity",
                content = "Note with palette color",
                colorHex = customColor,
                updatedAtMillis = 1000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val noteItem = state.notes.firstOrNull { it.activityId == 401L }
            org.junit.Assert.assertNotNull(noteItem)
            org.junit.Assert.assertEquals(customColor, noteItem?.colorHex)
        }
    }

    @Test
    fun `test undoDeleteNotes restores activities and notes after deletion`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 501L,
                title = "Note To Delete And Undo",
                description = "Content",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 801L,
                activityId = 501L,
                title = "Note To Delete And Undo",
                content = "Content",
                updatedAtMillis = 1000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSelection("act_501")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSelectionMode)

        val deletedNotes = viewModel.deleteSelectedNotes()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, deletedNotes.size)
        assertTrue(activityRepository.activitiesFlow.value.first { it.id == 501L }.isDeleted)
        assertTrue(noteRepository.notesFlow.value.first { it.id == 801L }.isDeleted)

        // Undo deletion
        viewModel.undoDeleteNotes(deletedNotes)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(activityRepository.activitiesFlow.value.first { it.id == 501L }.isDeleted)
        assertFalse(noteRepository.notesFlow.value.first { it.id == 801L }.isDeleted)
        assertEquals(1, viewModel.uiState.value.notes.size)
        assertEquals("Note To Delete And Undo", viewModel.uiState.value.notes.first().title)
    }

    @Test
    fun `test undoArchiveNotes restores isArchived to false after archiving`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 502L,
                title = "Note To Archive And Undo",
                description = "Content",
                entryType = EntryType.NOTE,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                createdAtMillis = 1000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 802L,
                activityId = 502L,
                title = "Note To Archive And Undo",
                content = "Content",
                updatedAtMillis = 1000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleSelection("act_502")
        testDispatcher.scheduler.advanceUntilIdle()

        val archivedNotes = viewModel.archiveSelectedNotes()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, archivedNotes.size)
        assertTrue(activityRepository.activitiesFlow.value.first { it.id == 502L }.isArchived)
        assertTrue(noteRepository.notesFlow.value.first { it.id == 802L }.isArchived)
        assertTrue(viewModel.uiState.value.notes.isEmpty())

        // Undo archiving
        viewModel.undoArchiveNotes(archivedNotes)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(activityRepository.activitiesFlow.value.first { it.id == 502L }.isArchived)
        assertFalse(noteRepository.notesFlow.value.first { it.id == 802L }.isArchived)
        assertEquals(1, viewModel.uiState.value.notes.size)
        assertEquals("Note To Archive And Undo", viewModel.uiState.value.notes.first().title)
    }
}

