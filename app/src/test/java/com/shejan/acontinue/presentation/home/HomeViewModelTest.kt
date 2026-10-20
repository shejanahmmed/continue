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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var getTodayTimelineUseCase: GetTodayTimelineUseCase
    private lateinit var testTickerFlow: MutableStateFlow<Long>
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        taskRepository = FakeTaskRepository()
        getTodayTimelineUseCase = GetTodayTimelineUseCase(activityRepository)
        testTickerFlow = MutableStateFlow(System.currentTimeMillis())
        viewModel = HomeViewModel(
            getTodayTimelineUseCase = getTodayTimelineUseCase,
            taskRepository = taskRepository,
            activityRepository = activityRepository,
            tickerFlow = testTickerFlow
        )
    }

    private fun getTodayMiddayMillis(): Long {
        return java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 12)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test home view model loads activities and pending tasks`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        testTickerFlow.value = now
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
        testTickerFlow.value = now
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
    fun `test event timetable progression automatically updates elapsed events based on end time`() = runTest(testDispatcher) {
        val baseTime = System.currentTimeMillis()
        testTickerFlow.value = baseTime

        // Event 1 has already finished today (ended 10s before baseTime)
        val pastEvent = ActivityItem(
            id = 1L,
            title = "Morning Standup",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = baseTime - 30_000L,
            endTimeMillis = baseTime - 10_000L
        )
        // Event 2 is still upcoming today (ends 50s after baseTime)
        val futureEvent = ActivityItem(
            id = 2L,
            title = "Design Sync",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = baseTime + 10_000L,
            endTimeMillis = baseTime + 50_000L
        )

        activityRepository.insertActivity(pastEvent)
        activityRepository.insertActivity(futureEvent)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.todayEventCount)
            assertEquals(1, state.todayCompletedEventCount)
            assertEquals(0.5f, state.eventProgress, 0.001f)

            // Dynamic progression: advance ticker past Event 2's end time
            testTickerFlow.value = baseTime + 60_000L
            testScheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(2, updatedState.todayEventCount)
            assertEquals(2, updatedState.todayCompletedEventCount)
            assertEquals(1.0f, updatedState.eventProgress, 0.001f)
        }
    }

    @Test
    fun `test reminder tick mark completion updates count interactively`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        testTickerFlow.value = now

        val reminder = ActivityItem(
            id = 10L,
            title = "Buy Groceries",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 10_000L,
            endTimeMillis = now + 10_000L,
            isCompleted = false
        )
        activityRepository.insertActivity(reminder)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(1, initialState.todayReminderCount)
            assertEquals(0, initialState.todayCompletedReminderCount)
            assertEquals(0f, initialState.reminderProgress, 0.001f)

            // User toggles tick mark completion
            viewModel.toggleActivityCompletion(10L, true)
            testScheduler.advanceUntilIdle()

            val completedState = awaitItem()
            assertEquals(1, completedState.todayReminderCount)
            assertEquals(1, completedState.todayCompletedReminderCount)
            assertEquals(1.0f, completedState.reminderProgress, 0.001f)
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
            assertEquals(0f, state.reminderProgress, 0.001f)
            assertEquals(0, state.todayPendingReminderCount)
            assertEquals(0, state.todayTotalCount)
            assertEquals(0, state.todayCompletedCount)
            assertEquals(0f, state.completionProgress, 0.001f)
        }
    }

    @Test
    fun `test reminder point in time semantics and focal card representation`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        testTickerFlow.value = now
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

    @Test
    fun `test deleteActivity removes activity from timeline`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        testTickerFlow.value = now
        val reminder = ActivityItem(
            id = 55L,
            title = "Disposable Reminder",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 5000L,
            endTimeMillis = now + 5000L
        )
        activityRepository.insertActivity(reminder)
        testScheduler.advanceUntilIdle()

        viewModel.deleteActivity(55L)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(0, state.timeline.activities.size)
            assertEquals(0, state.todayReminderCount)
        }
    }

    @Test
    fun `test currentEvent and nextReminder separation and dynamic transition to NOW`() = runTest(testDispatcher) {
        val baseTime = System.currentTimeMillis()
        testTickerFlow.value = baseTime

        val upcomingEvent = ActivityItem(
            id = 101L,
            title = "Distributed Systems Lecture",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = baseTime + 10_000L,
            endTimeMillis = baseTime + 50_000L
        )
        val pendingReminder = ActivityItem(
            id = 201L,
            title = "Check Lab Assignment",
            entryType = EntryType.REMINDER,
            startTimeMillis = baseTime + 20_000L,
            endTimeMillis = baseTime + 20_000L,
            isCompleted = false
        )

        activityRepository.insertActivity(upcomingEvent)
        activityRepository.insertActivity(pendingReminder)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            // Initial state: before start time -> upcomingEvent is nextEvent, currentEvent is null
            val stateBefore = awaitItem()
            assertNull(stateBefore.currentEvent)
            assertNotNull(stateBefore.nextEvent)
            assertEquals(101L, stateBefore.nextEvent?.id)
            assertNotNull(stateBefore.nextReminder)
            assertEquals(201L, stateBefore.nextReminder?.id)

            // Dynamic progression: advance time into event window -> transitions to currentEvent (NOW)
            testTickerFlow.value = baseTime + 15_000L
            testScheduler.advanceUntilIdle()

            val stateDuring = awaitItem()
            assertNotNull(stateDuring.currentEvent)
            assertEquals(101L, stateDuring.currentEvent?.id)
            assertNull(stateDuring.nextEvent)
            assertEquals(201L, stateDuring.nextReminder?.id)

            // Dynamic progression: advance time past event end time -> event concludes
            testTickerFlow.value = baseTime + 60_000L
            testScheduler.advanceUntilIdle()

            val stateAfter = awaitItem()
            assertNull(stateAfter.currentEvent)
            assertNull(stateAfter.nextEvent)
            assertEquals(201L, stateAfter.nextReminder?.id)
        }
    }

    @Test
    fun `test completed reminders are excluded from nextReminder`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        testTickerFlow.value = now

        val completedReminder = ActivityItem(
            id = 301L,
            title = "Already Done",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 5000L,
            endTimeMillis = now + 5000L,
            isCompleted = true
        )
        val pendingReminder = ActivityItem(
            id = 302L,
            title = "Still To Do",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 15000L,
            endTimeMillis = now + 15000L,
            isCompleted = false
        )

        activityRepository.insertActivity(completedReminder)
        activityRepository.insertActivity(pendingReminder)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.nextReminder)
            assertEquals(302L, state.nextReminder?.id)
            assertEquals("Still To Do", state.nextReminder?.title)
        }
    }

    @Test
    fun `test missed reminders count increments when overdue and decrements upon completion`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        testTickerFlow.value = now

        // Past uncompleted reminder -> should count as missed
        val overdueReminder = ActivityItem(
            id = 401L,
            title = "Overdue Medicine",
            entryType = EntryType.REMINDER,
            startTimeMillis = now - 60_000L,
            endTimeMillis = now - 60_000L,
            isCompleted = false
        )
        // Future uncompleted reminder -> not missed
        val upcomingReminder = ActivityItem(
            id = 402L,
            title = "Upcoming Call",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 60_000L,
            endTimeMillis = now + 60_000L,
            isCompleted = false
        )

        activityRepository.insertActivity(overdueReminder)
        activityRepository.insertActivity(upcomingReminder)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(2, initialState.todayReminderCount)
            assertEquals(1, initialState.todayMissedReminderCount)
            assertEquals(0, initialState.todayCompletedReminderCount)
            assertEquals(true, initialState.isReminderMissed)
            assertEquals(401L, initialState.nextReminder?.id)

            // When user completes the overdue reminder
            viewModel.toggleActivityCompletion(401L, true)
            testScheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(2, updatedState.todayReminderCount)
            assertEquals(0, updatedState.todayMissedReminderCount)
            assertEquals(1, updatedState.todayCompletedReminderCount)
            assertEquals(false, updatedState.isReminderMissed)
            assertEquals(402L, updatedState.nextReminder?.id)
        }
    }

    @Test
    fun `test overdue reminder from past day is excluded and current date upcoming reminder is shown`() = runTest(testDispatcher) {
        val now = getTodayMiddayMillis()
        testTickerFlow.value = now

        // Uncompleted reminder from yesterday - must be excluded as it is not the current date
        val yesterdayReminder = ActivityItem(
            id = 501L,
            title = "Yesterday Medicine",
            entryType = EntryType.REMINDER,
            startTimeMillis = now - (24 * 3600 * 1000L),
            endTimeMillis = now - (24 * 3600 * 1000L),
            isCompleted = false
        )
        // Future reminder scheduled for today
        val todayUpcomingReminder = ActivityItem(
            id = 502L,
            title = "Tonight Dinner",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + (2 * 3600 * 1000L),
            endTimeMillis = now + (2 * 3600 * 1000L),
            isCompleted = false
        )

        activityRepository.insertActivity(yesterdayReminder)
        activityRepository.insertActivity(todayUpcomingReminder)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initialState = awaitItem()
            // Strictly current date activities
            assertEquals(1, initialState.timeline.activities.size)
            assertEquals(502L, initialState.timeline.activities.first().id)
            // Yesterday's reminder is ignored on the current date's focus card
            assertNotNull(initialState.nextReminder)
            assertEquals(502L, initialState.nextReminder?.id)
            assertEquals("Tonight Dinner", initialState.nextReminder?.title)
            assertEquals(false, initialState.isReminderMissed)
            assertEquals(0, initialState.todayMissedReminderCount)
        }
    }

    @Test
    fun `test skipping current date missed reminder transitions to upcoming reminder`() = runTest(testDispatcher) {
        val now = getTodayMiddayMillis()
        testTickerFlow.value = now

        // Overdue reminder from earlier today
        val todayOverdueReminder = ActivityItem(
            id = 601L,
            title = "Morning Medicine",
            entryType = EntryType.REMINDER,
            startTimeMillis = now - (3600 * 1000L),
            endTimeMillis = now - (3600 * 1000L),
            isCompleted = false
        )
        // Upcoming reminder later today
        val todayUpcomingReminder = ActivityItem(
            id = 602L,
            title = "Afternoon Walk",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + (3600 * 1000L),
            endTimeMillis = now + (3600 * 1000L),
            isCompleted = false
        )

        activityRepository.insertActivity(todayOverdueReminder)
        activityRepository.insertActivity(todayUpcomingReminder)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(601L, initialState.nextReminder?.id)
            assertEquals(true, initialState.isReminderMissed)
            assertEquals(1, initialState.todayMissedReminderCount)

            // User taps SKIP on the missed reminder
            viewModel.skipReminder(601L)
            testScheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(602L, updatedState.nextReminder?.id)
            assertEquals("Afternoon Walk", updatedState.nextReminder?.title)
            assertEquals(false, updatedState.isReminderMissed)
            assertEquals(0, updatedState.todayMissedReminderCount)
        }

        // Verify it was persisted in the repository as isSkipped = true
        val persistedList = activityRepository.activitiesFlow.value
        val skippedItem = persistedList.first { it.id == 601L }
        assertEquals(true, skippedItem.isSkipped)

        // Simulate app reopen with a fresh ViewModel instance
        val freshViewModel = HomeViewModel(
            getTodayTimelineUseCase = getTodayTimelineUseCase,
            taskRepository = taskRepository,
            activityRepository = activityRepository,
            tickerFlow = testTickerFlow
        )
        testScheduler.advanceUntilIdle()

        freshViewModel.uiState.test {
            val freshState = awaitItem()
            // Fresh ViewModel reads from repository: skipped reminder stays skipped and does not re-alert
            assertEquals(602L, freshState.nextReminder?.id)
            assertEquals(false, freshState.isReminderMissed)
            assertEquals(0, freshState.todayMissedReminderCount)

            // When user marks the skipped reminder done
            freshViewModel.toggleActivityCompletion(601L, true)
            testScheduler.advanceUntilIdle()

            val completedState = awaitItem()
            val completedItem = activityRepository.activitiesFlow.value.first { it.id == 601L }
            assertEquals(true, completedItem.isCompleted)
            assertEquals(false, completedItem.isSkipped)
            assertEquals(0, completedState.todayMissedReminderCount)
        }
    }

    @Test
    fun `test activities are correctly partitioned into todayActivities and pastActivities based on current time`() = runTest(testDispatcher) {
        val now = getTodayMiddayMillis()
        testTickerFlow.value = now

        // Concluded event earlier today
        val pastEvent = ActivityItem(
            id = 701L,
            title = "Morning Standup",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = now - 7200_000L,
            endTimeMillis = now - 3600_000L
        )
        // Active ongoing event right now
        val activeEvent = ActivityItem(
            id = 702L,
            title = "Current Workshop",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = now - 1800_000L,
            endTimeMillis = now + 1800_000L
        )
        // Future upcoming event later today
        val futureEvent = ActivityItem(
            id = 703L,
            title = "Evening Lecture",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = now + 7200_000L,
            endTimeMillis = now + 10800_000L
        )
        // Completed reminder from earlier
        val pastReminder = ActivityItem(
            id = 704L,
            title = "Pay Tuition Fee",
            entryType = EntryType.REMINDER,
            startTimeMillis = now - 5400_000L,
            endTimeMillis = now - 5400_000L,
            isCompleted = true
        )
        // Upcoming reminder later today
        val futureReminder = ActivityItem(
            id = 705L,
            title = "Call Home",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 3600_000L,
            endTimeMillis = now + 3600_000L,
            isCompleted = false
        )

        activityRepository.insertActivity(pastEvent)
        activityRepository.insertActivity(activeEvent)
        activityRepository.insertActivity(futureEvent)
        activityRepository.insertActivity(pastReminder)
        activityRepository.insertActivity(futureReminder)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()

            // Ongoing and upcoming items must be in todayActivities
            val todayIds = state.todayActivities.map { it.id }
            assertEquals(listOf(702L, 705L, 703L), todayIds)

            // Concluded events and past/completed reminders must be in pastActivities
            val pastIds = state.pastActivities.map { it.id }
            assertEquals(listOf(701L, 704L), pastIds)
        }
    }

    @Test
    fun `test toggling completion on reminder transitions it from todayActivities to pastActivities`() = runTest(testDispatcher) {
        val now = getTodayMiddayMillis()
        testTickerFlow.value = now

        val futureReminder = ActivityItem(
            id = 801L,
            title = "Submit Assignment",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 3600_000L,
            endTimeMillis = now + 3600_000L,
            isCompleted = false
        )

        activityRepository.insertActivity(futureReminder)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertEquals(1, initialState.todayActivities.size)
            assertEquals(801L, initialState.todayActivities.first().id)
            assertEquals(0, initialState.pastActivities.size)

            // User completes reminder early
            viewModel.toggleActivityCompletion(801L, true)
            testScheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(0, updatedState.todayActivities.size)
            assertEquals(1, updatedState.pastActivities.size)
            assertEquals(801L, updatedState.pastActivities.first().id)
            assertEquals(true, updatedState.pastActivities.first().isCompleted)
        }
    }

    @Test
    fun `test advancing ticker moves elapsed event from todayActivities to pastActivities`() = runTest(testDispatcher) {
        val now = getTodayMiddayMillis()
        testTickerFlow.value = now

        val timedEvent = ActivityItem(
            id = 901L,
            title = "Quick Sync",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = now - 600_000L,
            endTimeMillis = now + 600_000L
        )

        activityRepository.insertActivity(timedEvent)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val activeState = awaitItem()
            assertEquals(1, activeState.todayActivities.size)
            assertEquals(0, activeState.pastActivities.size)

            // Advance time past the event's end time
            testTickerFlow.value = now + 1200_000L
            testScheduler.advanceUntilIdle()

            val pastState = awaitItem()
            assertEquals(0, pastState.todayActivities.size)
            assertEquals(1, pastState.pastActivities.size)
            assertEquals(901L, pastState.pastActivities.first().id)
        }
    }
}
