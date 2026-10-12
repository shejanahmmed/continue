package com.shejan.acontinue.presentation.diary

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeNoteRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun `test diary search filters activities and notes`() = runTest(testDispatcher) {
        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Software Engineering Project",
                category = ActivityCategory.PROJECT,
                startTimeMillis = 1000L,
                endTimeMillis = 2000L
            )
        )
        activityRepository.insertActivity(
            ActivityItem(
                id = 2L,
                title = "Gym Session",
                category = ActivityCategory.PERSONAL,
                startTimeMillis = 3000L,
                endTimeMillis = 4000L
            )
        )
        noteRepository.insertNote(
            ActivityNote(
                id = 10L,
                activityId = 1L,
                title = "Architecture Decision",
                content = "Decided to use Room with clean architecture"
            )
        )

        testScheduler.advanceUntilIdle()

        viewModel.onSearchQueryChange("Software")
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.activities.size)
            assertEquals("Software Engineering Project", state.activities.first().title)
        }
    }
}
