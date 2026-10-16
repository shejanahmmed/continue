package com.shejan.acontinue.presentation.calendar

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
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
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        viewModel = CalendarViewModel(activityRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test calendar queries activities on selected date`() = runTest(testDispatcher) {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 10, 0, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = startTime + 3600000L

        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Club Meeting",
                category = ActivityCategory.MEETING,
                startTimeMillis = startTime,
                endTimeMillis = endTime
            )
        )

        viewModel.selectDate(startTime)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.activities.size)
            assertEquals("Club Meeting", state.activities.first().title)
            assertEquals(1, state.selectedDayActivities.size)
        }
    }

    @Test
    fun `test calendar synchronizes both events and reminders with proper dot colors`() = runTest(testDispatcher) {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 14, 0, 0)
        }
        val baseTime = cal.timeInMillis

        // Insert Event
        activityRepository.insertActivity(
            ActivityItem(
                id = 10L,
                title = "Engineering Standup",
                entryType = EntryType.ACTIVITY,
                category = ActivityCategory.PROJECT,
                startTimeMillis = baseTime,
                endTimeMillis = baseTime + 3600000L
            )
        )

        // Insert Reminder
        val reminderColor = 0xFF9D7BFC
        activityRepository.insertActivity(
            ActivityItem(
                id = 11L,
                title = "Submit Report",
                entryType = EntryType.REMINDER,
                reminderCategory = "Work",
                reminderCategoryColorHex = reminderColor,
                startTimeMillis = baseTime + 1800000L,
                endTimeMillis = baseTime + 3600000L
            )
        )

        viewModel.selectDate(baseTime)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.selectedDayActivities.size)
            val dayCell = state.monthDays.firstOrNull { it.isCurrentMonth && it.dayOfMonth == 16 }
            assertTrue(dayCell != null)
            assertEquals(2, dayCell?.activities?.size)
            assertEquals(listOf(ActivityCategory.PROJECT.colorHex, reminderColor), dayCell?.dotColorsHex)
            assertFalse(dayCell!!.hasMoreDots)
        }
    }

    @Test
    fun `test daily recurring activity appears across multiple days`() = runTest(testDispatcher) {
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 9, 0, 0)
        }
        val startTime = startCal.timeInMillis

        activityRepository.insertActivity(
            ActivityItem(
                id = 20L,
                title = "Daily Workout",
                entryType = EntryType.ACTIVITY,
                category = ActivityCategory.PERSONAL,
                startTimeMillis = startTime,
                endTimeMillis = startTime + 3600000L,
                recurrenceMode = RecurrenceMode.DAILY,
                recurrenceInterval = 1
            )
        )

        // Select Oct 15 (should have the workout occurrence)
        val targetCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 9, 0, 0)
        }
        viewModel.selectDate(targetCal.timeInMillis)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val dayCell = state.monthDays.firstOrNull { it.isCurrentMonth && it.dayOfMonth == 15 }
            assertTrue(dayCell != null)
            assertTrue(dayCell!!.activities.any { it.title == "Daily Workout" })
            assertTrue(state.selectedDayActivities.any { it.title == "Daily Workout" })
        }
    }

    @Test
    fun `test dot overflow when day has more than 8 activities`() = runTest(testDispatcher) {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 8, 0, 0)
        }
        val startTime = cal.timeInMillis

        // Insert 9 items
        for (i in 1..9) {
            activityRepository.insertActivity(
                ActivityItem(
                    id = 100L + i,
                    title = "Task $i",
                    entryType = EntryType.REMINDER,
                    startTimeMillis = startTime + i * 60000L,
                    endTimeMillis = startTime + i * 60000L + 30000L
                )
            )
        }

        viewModel.selectDate(startTime)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val dayCell = state.monthDays.firstOrNull { it.isCurrentMonth && it.dayOfMonth == 16 }
            assertTrue(dayCell != null)
            assertEquals(9, dayCell?.activities?.size)
            assertTrue(dayCell!!.hasMoreDots)
        }
    }

    @Test
    fun `test calendar generates 42 grid cells and populates day activities`() = runTest(testDispatcher) {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 12, 13, 0, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = startTime + 5400000L

        activityRepository.insertActivity(
            ActivityItem(
                id = 2L,
                title = "SE331 [Online]",
                category = ActivityCategory.CLASS,
                startTimeMillis = startTime,
                endTimeMillis = endTime,
                location = "Home"
            )
        )

        viewModel.selectDate(startTime)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(42, state.monthDays.size)
            val dayCell = state.monthDays.firstOrNull { it.isCurrentMonth && it.dayOfMonth == 12 }
            assertTrue(dayCell != null)
            assertEquals(1, dayCell?.activities?.size)
            assertEquals("SE331 [Online]", dayCell?.activities?.first()?.title)
        }
    }

    @Test
    fun `test nextMonth and previousMonth advance and rewind calendar`() = runTest(testDispatcher) {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 1, 10, 0, 0)
        }
        viewModel.selectDate(cal.timeInMillis)
        testScheduler.advanceUntilIdle()

        viewModel.nextMonth()
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val newMonthCal = Calendar.getInstance().apply { timeInMillis = state.currentMonthMillis }
            assertEquals(Calendar.NOVEMBER, newMonthCal.get(Calendar.MONTH))
        }

        viewModel.previousMonth()
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val revertedMonthCal = Calendar.getInstance().apply { timeInMillis = state.currentMonthMillis }
            assertEquals(Calendar.OCTOBER, revertedMonthCal.get(Calendar.MONTH))
        }
    }

    @Test
    fun `test jumpToToday resets selection to current day`() = runTest(testDispatcher) {
        val pastCal = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 1, 10, 0, 0)
        }
        viewModel.selectDate(pastCal.timeInMillis)
        testScheduler.advanceUntilIdle()

        viewModel.jumpToToday()
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val todayCal = Calendar.getInstance()
            val stateCal = Calendar.getInstance().apply { timeInMillis = state.selectedDateMillis }
            assertEquals(todayCal.get(Calendar.YEAR), stateCal.get(Calendar.YEAR))
            assertEquals(todayCal.get(Calendar.DAY_OF_YEAR), stateCal.get(Calendar.DAY_OF_YEAR))
        }
    }
}
