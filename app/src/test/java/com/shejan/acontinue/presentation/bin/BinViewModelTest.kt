package com.shejan.acontinue.presentation.bin

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
    fun `test restoreItem moves item from trash back to active`() = runTest(testDispatcher) {
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

