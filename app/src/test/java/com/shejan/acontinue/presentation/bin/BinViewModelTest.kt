package com.shejan.acontinue.presentation.bin

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BinViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var viewModel: BinViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        noteRepository = FakeNoteRepository()
        viewModel = BinViewModel(activityRepository, noteRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test uiState loads deleted activities and notes in sorted order`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        // Active item (should not be in Bin)
        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Active Event",
                entryType = EntryType.ACTIVITY,
                startTimeMillis = now,
                endTimeMillis = now + 1000
            )
        )

        // Deleted Event
        val delEventId = activityRepository.insertActivity(
            ActivityItem(
                id = 2L,
                title = "Deleted Event",
                entryType = EntryType.ACTIVITY,
                startTimeMillis = now,
                endTimeMillis = now + 1000,
                isDeleted = true,
                deletedAtMillis = now - 5000
            )
        )

        // Deleted Note
        val delNoteId = noteRepository.insertNote(
            ActivityNote(
                id = 10L,
                activityId = 0L,
                title = "Deleted Standalone Note",
                content = "Some note content",
                isDeleted = true,
                deletedAtMillis = now - 1000
            )
        )

        viewModel.uiState.test {
            val state = awaitItem()
            testScheduler.advanceUntilIdle()
            val loadedState = if (state.isLoading) awaitItem() else state

            assertEquals(2, loadedState.items.size)
            assertEquals("Deleted Standalone Note", loadedState.items[0].title)
            assertEquals("Deleted Event", loadedState.items[1].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test deduplicates deleted activity note and child note sharing activityId`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()

        // When a note is saved, both an ActivityItem and an ActivityNote are created.
        // When deleted, both rows are soft-deleted.
        val actId = activityRepository.insertActivity(
            ActivityItem(
                id = 100L,
                title = "Meeting Notes",
                description = "Discussion on project milestones",
                entryType = EntryType.NOTE,
                startTimeMillis = now,
                endTimeMillis = now,
                isDeleted = true,
                deletedAtMillis = now - 2000
            )
        )

        noteRepository.insertNote(
            ActivityNote(
                id = 200L,
                activityId = actId,
                title = "Meeting Notes",
                content = "Discussion on project milestones",
                labels = listOf("Work"),
                isDeleted = true,
                deletedAtMillis = now - 1500
            )
        )

        viewModel.uiState.test {
            val state = awaitItem()
            testScheduler.advanceUntilIdle()
            val loadedState = if (state.isLoading) awaitItem() else state

            // Must produce exactly ONE item instead of duplicate cards
            assertEquals(1, loadedState.items.size)
            val item = loadedState.items[0]
            assertEquals(actId, item.id)
            assertTrue(item.isActivity)
            assertEquals(200L, item.linkedNoteId)
            assertEquals("Meeting Notes", item.title)
            assertEquals("Discussion on project milestones", item.previewText)
            assertEquals("Work", item.categoryName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test restoreItem restores both activity and linked child note`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val actId = activityRepository.insertActivity(
            ActivityItem(
                id = 101L,
                title = "Sprint Planning Note",
                description = "Plan items",
                entryType = EntryType.NOTE,
                startTimeMillis = now,
                endTimeMillis = now,
                isDeleted = true,
                deletedAtMillis = now
            )
        )

        val noteId = noteRepository.insertNote(
            ActivityNote(
                id = 201L,
                activityId = actId,
                title = "Sprint Planning Note",
                content = "Plan items",
                isDeleted = true,
                deletedAtMillis = now
            )
        )

        val trashItem = TrashItem(
            id = actId,
            isActivity = true,
            title = "Sprint Planning Note",
            previewText = "Plan items",
            entryType = EntryType.NOTE,
            deletedAtMillis = now,
            linkedNoteId = noteId,
            activityId = actId
        )

        viewModel.restoreItem(trashItem)
        testScheduler.advanceUntilIdle()

        val restoredAct = activityRepository.activitiesFlow.value.first { it.id == actId }
        val restoredNote = noteRepository.notesFlow.value.first { it.id == noteId }

        assertTrue(!restoredAct.isDeleted)
        assertEquals(0L, restoredAct.deletedAtMillis)
        assertTrue(!restoredNote.isDeleted)
        assertEquals(0L, restoredNote.deletedAtMillis)
    }

    @Test
    fun `test permanentlyDeleteItem removes both activity and linked child note`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val actId = activityRepository.insertActivity(
            ActivityItem(
                id = 102L,
                title = "Throwaway Note",
                entryType = EntryType.NOTE,
                startTimeMillis = now,
                endTimeMillis = now,
                isDeleted = true,
                deletedAtMillis = now
            )
        )

        val noteId = noteRepository.insertNote(
            ActivityNote(
                id = 202L,
                activityId = actId,
                title = "Throwaway Note",
                content = "Trash me",
                isDeleted = true,
                deletedAtMillis = now
            )
        )

        val trashItem = TrashItem(
            id = actId,
            isActivity = true,
            title = "Throwaway Note",
            previewText = "Trash me",
            entryType = EntryType.NOTE,
            deletedAtMillis = now,
            linkedNoteId = noteId,
            activityId = actId
        )

        viewModel.permanentlyDeleteItem(trashItem)
        testScheduler.advanceUntilIdle()

        assertTrue(activityRepository.activitiesFlow.value.none { it.id == actId })
        assertTrue(noteRepository.notesFlow.value.none { it.id == noteId })
    }

    @Test
    fun `test restoreItem on standalone note with parent activityId restores parent activity`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val actId = activityRepository.insertActivity(
            ActivityItem(
                id = 103L,
                title = "Linked Event",
                entryType = EntryType.ACTIVITY,
                startTimeMillis = now,
                endTimeMillis = now,
                isDeleted = true,
                deletedAtMillis = now
            )
        )

        val noteId = noteRepository.insertNote(
            ActivityNote(
                id = 203L,
                activityId = actId,
                title = "Event Attached Note",
                content = "Details",
                isDeleted = true,
                deletedAtMillis = now
            )
        )

        val trashItem = TrashItem(
            id = noteId,
            isActivity = false,
            title = "Event Attached Note",
            previewText = "Details",
            entryType = EntryType.NOTE,
            deletedAtMillis = now,
            activityId = actId
        )

        viewModel.restoreItem(trashItem)
        testScheduler.advanceUntilIdle()

        val restoredNote = noteRepository.notesFlow.value.first { it.id == noteId }
        val restoredAct = activityRepository.activitiesFlow.value.first { it.id == actId }

        assertTrue(!restoredNote.isDeleted)
        assertTrue(!restoredAct.isDeleted)
    }

    @Test
    fun `test restoreItem moves standalone note from trash back to active`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val delNoteId = noteRepository.insertNote(
            ActivityNote(
                id = 10L,
                activityId = 0L,
                title = "Restorable Note",
                content = "Test content",
                isDeleted = true,
                deletedAtMillis = now
            )
        )

        val trashItem = TrashItem(
            id = delNoteId,
            isActivity = false,
            title = "Restorable Note",
            previewText = "Test content",
            entryType = EntryType.NOTE,
            deletedAtMillis = now
        )

        viewModel.restoreItem(trashItem)
        testScheduler.advanceUntilIdle()

        val note = noteRepository.notesFlow.value.first { it.id == delNoteId }
        assertTrue(!note.isDeleted)
        assertEquals(0L, note.deletedAtMillis)
    }

    @Test
    fun `test permanentlyDeleteItem permanently removes item from repository`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val delEventId = activityRepository.insertActivity(
            ActivityItem(
                id = 5L,
                title = "To Purge",
                entryType = EntryType.REMINDER,
                startTimeMillis = now,
                endTimeMillis = now,
                isDeleted = true,
                deletedAtMillis = now
            )
        )

        val trashItem = TrashItem(
            id = delEventId,
            isActivity = true,
            title = "To Purge",
            previewText = "",
            entryType = EntryType.REMINDER,
            deletedAtMillis = now
        )

        viewModel.permanentlyDeleteItem(trashItem)
        testScheduler.advanceUntilIdle()

        assertTrue(activityRepository.activitiesFlow.value.none { it.id == delEventId })
    }

    @Test
    fun `test emptyTrash permanently purges all deleted activities and notes`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Active Reminder",
                entryType = EntryType.REMINDER,
                startTimeMillis = now,
                endTimeMillis = now,
                isDeleted = false
            )
        )
        activityRepository.insertActivity(
            ActivityItem(
                id = 2L,
                title = "Deleted Event",
                entryType = EntryType.ACTIVITY,
                startTimeMillis = now,
                endTimeMillis = now,
                isDeleted = true,
                deletedAtMillis = now
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 10L,
                activityId = 0L,
                title = "Deleted Note",
                content = "xyz",
                isDeleted = true,
                deletedAtMillis = now
            )
        )

        viewModel.emptyTrash()
        testScheduler.advanceUntilIdle()

        assertEquals(1, activityRepository.activitiesFlow.value.size)
        assertEquals(1L, activityRepository.activitiesFlow.value[0].id)
        assertTrue(noteRepository.notesFlow.value.isEmpty())
    }
}
