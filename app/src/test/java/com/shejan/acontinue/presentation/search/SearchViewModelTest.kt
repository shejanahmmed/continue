package com.shejan.acontinue.presentation.search

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
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        noteRepository = FakeNoteRepository()
        viewModel = SearchViewModel(activityRepository, noteRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state has empty query and empty results`() = runTest(testDispatcher) {
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun `test query matching note title returns result`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 101L,
                title = "Hardware Architecture Notes",
                description = "Details about modular bus and system clocks",
                entryType = EntryType.NOTE,
                startTimeMillis = 1000L,
                endTimeMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("Architecture")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Architecture", state.searchQuery)
        assertEquals(1, state.results.size)
        assertEquals("Hardware Architecture Notes", state.results[0].title)
    }

    @Test
    fun `test query matching content returns result`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 201L,
                activityId = 0L,
                title = "Ideas",
                content = "Investigate dot-matrix display driver algorithms",
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("algorithms")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("Ideas", state.results[0].title)
    }

    @Test
    fun `test query matching label returns result`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 301L,
                activityId = 0L,
                title = "Project Plan",
                content = "Timeline and budget milestones",
                labels = listOf("Engineering", "Urgent"),
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("Engineering")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("Project Plan", state.results[0].title)
    }

    @Test
    fun `test deleted and archived notes are excluded from search results`() = runTest(testDispatcher) {
        // Deleted note
        noteRepository.insertNote(
            ActivityNote(
                id = 401L,
                activityId = 0L,
                title = "Deleted Secret Notes",
                content = "Should not appear in search",
                isDeleted = true,
                updatedAtMillis = 2000L
            )
        )
        // Archived note
        noteRepository.insertNote(
            ActivityNote(
                id = 402L,
                activityId = 0L,
                title = "Archived Research Notes",
                content = "Should not appear in search",
                isArchived = true,
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("Notes")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun `test clearing search query clears results`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 501L,
                activityId = 0L,
                title = "Meeting Minutes",
                content = "Standup notes",
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("Meeting")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.results.size)

        viewModel.onSearchQueryChange("")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("", viewModel.uiState.value.searchQuery)
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }
}
