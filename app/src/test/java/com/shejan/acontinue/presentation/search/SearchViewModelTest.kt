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

    @Test
    fun `test available labels combines settings labels and note labels deduplicated`() = runTest(testDispatcher) {
        val settingsRepo = com.shejan.acontinue.FakeSettingsRepository()
        settingsRepo.addNoteLabel("Work")
        settingsRepo.addNoteLabel("Ideas")

        noteRepository.insertNote(
            ActivityNote(
                id = 601L,
                activityId = 0L,
                title = "Brainstorm",
                content = "Some ideas",
                labels = listOf("ideas", "Hardware"),
                updatedAtMillis = 2000L
            )
        )
        val vmWithSettings = SearchViewModel(activityRepository, noteRepository, settingsRepo)
        testDispatcher.scheduler.advanceUntilIdle()

        val labels = vmWithSettings.uiState.value.availableLabels
        assertEquals(listOf("Hardware", "Ideas", "Work"), labels)
    }

    @Test
    fun `test toggle LISTS type filter returns only notes with lists or checklists`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 701L,
                activityId = 0L,
                title = "Shopping",
                content = "[ ] Apples\n[x] Milk",
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 702L,
                activityId = 0L,
                title = "Regular Note",
                content = "Just plain narrative text without any checklist items",
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleTypeFilter(SearchNoteType.LISTS)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.selectedTypes.contains(SearchNoteType.LISTS))
        assertEquals(1, state.results.size)
        assertEquals("Shopping", state.results[0].title)
    }

    @Test
    fun `test toggle URLS type filter returns only notes with web links`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 801L,
                activityId = 0L,
                title = "Reference Link",
                content = "Docs located at https://developer.android.com/compose",
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 802L,
                activityId = 0L,
                title = "Plain Notes",
                content = "Standard notes text without links",
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleTypeFilter(SearchNoteType.URLS)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("Reference Link", state.results[0].title)
    }

    @Test
    fun `test toggle AUDIO type filter returns only notes with audio attachments`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 901L,
                activityId = 0L,
                title = "Meeting Recording",
                content = "Audio attached below:\n![audio](content://media/audio1.m4a)",
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 902L,
                activityId = 0L,
                title = "Written Summary",
                content = "Standard text transcript without audio",
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleTypeFilter(SearchNoteType.AUDIO)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("Meeting Recording", state.results[0].title)
    }

    @Test
    fun `test toggle PHOTOS type filter returns only notes with photo attachments`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 1001L,
                activityId = 0L,
                title = "Whiteboard Photo",
                content = "Architecture diagram:\n![photo](content://media/photo1.jpg)",
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 1002L,
                activityId = 0L,
                title = "Diagram Description",
                content = "Blocks connected via local bus",
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleTypeFilter(SearchNoteType.PHOTOS)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("Whiteboard Photo", state.results[0].title)
    }

    @Test
    fun `test multi-selection filter returns only notes satisfying all active types`() = runTest(testDispatcher) {
        // Note with list only
        noteRepository.insertNote(
            ActivityNote(
                id = 1101L,
                activityId = 0L,
                title = "List Only",
                content = "[ ] Task 1\n[ ] Task 2",
                updatedAtMillis = 2000L
            )
        )
        // Note with photo only
        noteRepository.insertNote(
            ActivityNote(
                id = 1102L,
                activityId = 0L,
                title = "Photo Only",
                content = "Photo attached:\n![photo](content://media/photo2.jpg)",
                updatedAtMillis = 2000L
            )
        )
        // Note with both list and photo
        noteRepository.insertNote(
            ActivityNote(
                id = 1103L,
                activityId = 0L,
                title = "List And Photo",
                content = "[ ] Inspect board\n![photo](content://media/photo3.jpg)",
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleTypeFilter(SearchNoteType.LISTS)
        viewModel.toggleTypeFilter(SearchNoteType.PHOTOS)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(setOf(SearchNoteType.LISTS, SearchNoteType.PHOTOS), state.selectedTypes)
        assertEquals(1, state.results.size)
        assertEquals("List And Photo", state.results[0].title)
    }

    @Test
    fun `test query combined with type filter filters by both query and type`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 1201L,
                activityId = 0L,
                title = "Grocery Shopping",
                content = "[ ] Milk\n[ ] Bread",
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 1202L,
                activityId = 0L,
                title = "Workout Checklist",
                content = "[ ] Bench press\n[ ] Squats",
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("Grocery")
        viewModel.toggleTypeFilter(SearchNoteType.LISTS)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("Grocery Shopping", state.results[0].title)
    }

    @Test
    fun `test toggling active filter off deselects and restores state`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 1301L,
                activityId = 0L,
                title = "Checklist Note",
                content = "[ ] Task item",
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Toggle on
        viewModel.toggleTypeFilter(SearchNoteType.LISTS)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.results.size)

        // Toggle off
        viewModel.toggleTypeFilter(SearchNoteType.LISTS)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.selectedTypes.isEmpty())
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun `test toggle single label filter returns only notes with that label`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 1401L,
                activityId = 0L,
                title = "Work Task",
                content = "Prepare quarterly review",
                labels = listOf("Work"),
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 1402L,
                activityId = 0L,
                title = "Personal Errand",
                content = "Buy groceries",
                labels = listOf("Personal"),
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleLabelFilter("Work")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.selectedLabels.contains("Work"))
        assertEquals(1, state.results.size)
        assertEquals("Work Task", state.results[0].title)
    }

    @Test
    fun `test multi-selection label filter returns only notes having all selected labels`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 1501L,
                activityId = 0L,
                title = "Urgent Work",
                content = "Server outage report",
                labels = listOf("Work", "Urgent"),
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 1502L,
                activityId = 0L,
                title = "General Work",
                content = "Weekly sprint planning",
                labels = listOf("Work"),
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 1503L,
                activityId = 0L,
                title = "Personal Urgent",
                content = "Call plumber",
                labels = listOf("Urgent"),
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleLabelFilter("Work")
        viewModel.toggleLabelFilter("Urgent")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(setOf("Work", "Urgent"), state.selectedLabels)
        assertEquals(1, state.results.size)
        assertEquals("Urgent Work", state.results[0].title)
    }

    @Test
    fun `test combining type filter and label filter returns notes with both`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 1601L,
                activityId = 0L,
                title = "Project Checklist",
                content = "[ ] Deploy to staging\n[x] Run smoke tests",
                labels = listOf("DevOps"),
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 1602L,
                activityId = 0L,
                title = "General DevOps Guide",
                content = "Architecture setup instructions without checklist items",
                labels = listOf("DevOps"),
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 1603L,
                activityId = 0L,
                title = "Other Checklist",
                content = "[ ] Grocery shopping",
                labels = listOf("Personal"),
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleTypeFilter(SearchNoteType.LISTS)
        viewModel.toggleLabelFilter("DevOps")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("Project Checklist", state.results[0].title)
    }

    @Test
    fun `test combining search query, type filter, and label filter`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 1701L,
                activityId = 0L,
                title = "Critical Security Checklist",
                content = "[ ] Audit open ports\n[ ] Rotate secrets",
                labels = listOf("Security"),
                updatedAtMillis = 2000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 1702L,
                activityId = 0L,
                title = "Routine Security Guidelines",
                content = "Standard security best practices documentation",
                labels = listOf("Security"),
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("Critical")
        viewModel.toggleTypeFilter(SearchNoteType.LISTS)
        viewModel.toggleLabelFilter("Security")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.results.size)
        assertEquals("Critical Security Checklist", state.results[0].title)
    }

    @Test
    fun `test toggling active label filter off restores unselected state`() = runTest(testDispatcher) {
        noteRepository.insertNote(
            ActivityNote(
                id = 1801L,
                activityId = 0L,
                title = "Draft Note",
                content = "Early draft concept",
                labels = listOf("Draft"),
                updatedAtMillis = 2000L
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleLabelFilter("Draft")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.results.size)

        viewModel.toggleLabelFilter("Draft")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.selectedLabels.isEmpty())
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }
}
