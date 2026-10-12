package com.shejan.acontinue.presentation.home

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeTaskRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.usecase.GetTodayTimelineUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var getTodayTimelineUseCase: GetTodayTimelineUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        taskRepository = FakeTaskRepository()
        getTodayTimelineUseCase = GetTodayTimelineUseCase(activityRepository)
        viewModel = HomeViewModel(getTodayTimelineUseCase, taskRepository, activityRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test home view model loads activities and pending tasks`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val activity = ActivityItem(
            id = 1L,
            title = "University Class",
            category = ActivityCategory.CLASS,
            startTimeMillis = now + 1000L,
            endTimeMillis = now + 3600000L
        )
        val task = ActivityTask(
            id = 10L,
            activityId = 1L,
            title = "Review Lecture 1",
            isCompleted = false
        )

        activityRepository.insertActivity(activity)
        taskRepository.insertTask(task)

        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.timeline.activities.size)
            assertEquals("University Class", state.timeline.activities.first().title)
            assertEquals(1, state.pendingTasks.size)
            assertEquals("Review Lecture 1", state.pendingTasks.first().title)
            assertNotNull(state.timeline.nextActivity)
        }
    }

    @Test
    fun `test toggle task marks task completed`() = runTest(testDispatcher) {
        val task = ActivityTask(
            id = 1L,
            activityId = null,
            title = "Standalone Task",
            isCompleted = false
        )
        taskRepository.insertTask(task)
        testScheduler.advanceUntilIdle()

        viewModel.toggleTask(1L, true)
        testScheduler.advanceUntilIdle()

        val allTasks = taskRepository.tasksFlow.value
        assertEquals(true, allTasks.first().isCompleted)
    }
}
