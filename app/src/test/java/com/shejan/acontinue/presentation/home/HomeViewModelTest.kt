package com.shejan.acontinue.presentation.home

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeTaskRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.model.EntryType
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

    @Test
    fun `test home view model calculates distinct event and reminder counts for today`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val event1 = ActivityItem(
            id = 1L,
            title = "Team Standup",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = now + 1000L,
            endTimeMillis = now + 2000L
        )
        val event2 = ActivityItem(
            id = 2L,
            title = "Architecture Review",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = now + 3000L,
            endTimeMillis = now + 4000L
        )
        val reminder1 = ActivityItem(
            id = 3L,
            title = "Submit Report",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 5000L,
            endTimeMillis = now + 5000L,
            isCompleted = false
        )
        val reminder2 = ActivityItem(
            id = 4L,
            title = "Water Plants",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 6000L,
            endTimeMillis = now + 6000L,
            isCompleted = true
        )

        activityRepository.insertActivity(event1)
        activityRepository.insertActivity(event2)
        activityRepository.insertActivity(reminder1)
        activityRepository.insertActivity(reminder2)

        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.todayEventCount)
            assertEquals(0, state.todayCompletedEventCount)
            assertEquals(0f, state.eventProgress, 0.001f)
            assertEquals(2, state.todayReminderCount)
            assertEquals(1, state.todayCompletedReminderCount)
            assertEquals(1, state.todayPendingReminderCount)
            assertEquals(0.5f, state.reminderProgress, 0.001f)
            assertEquals(2, state.todayTotalCount)
            assertEquals(1, state.todayCompletedCount)
            assertEquals(0.5f, state.completionProgress, 0.001f)
        }
    }

    @Test
    fun `test empty day calculates zero metrics`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.todayEventCount)
            assertEquals(0, state.todayCompletedEventCount)
            assertEquals(0f, state.eventProgress, 0.001f)
            assertEquals(0, state.todayReminderCount)
            assertEquals(0, state.todayCompletedReminderCount)
            assertEquals(0, state.todayPendingReminderCount)
            assertEquals(0f, state.reminderProgress, 0.001f)
            assertEquals(0, state.todayTotalCount)
            assertEquals(0, state.todayCompletedCount)
            assertEquals(0f, state.completionProgress, 0.001f)
        }
    }

    @Test
    fun `test reminder point in time semantics and focal card representation`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val reminder = ActivityItem(
            id = 50L,
            title = "Drink Water",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 5000L,
            endTimeMillis = now + 5000L,
            isCompleted = false
        )
        activityRepository.insertActivity(reminder)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val focal = state.timeline.nextActivity
            assertNotNull(focal)
            assertEquals(50L, focal!!.id)
            assertEquals(EntryType.REMINDER, focal.entryType)
            assertEquals(focal.startTimeMillis, focal.endTimeMillis)
        }
    }
}
