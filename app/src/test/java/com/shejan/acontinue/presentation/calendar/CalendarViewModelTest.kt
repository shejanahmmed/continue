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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

    @Test
    fun `test week mode toggle and setWeekMode`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isWeekMode)

            viewModel.setWeekMode(true)
            val weekState = awaitItem()
            assertTrue(weekState.isWeekMode)

            viewModel.toggleCalendarMode()
            val monthState = awaitItem()
            assertFalse(monthState.isWeekMode)
        }
    }

    @Test
    fun `test currentWeekDays returns exactly 7 days containing selected date`() = runTest(testDispatcher) {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 10, 0, 0)
        }
        viewModel.selectDate(cal.timeInMillis)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val weekDays = state.currentWeekDays
            assertEquals(7, weekDays.size)
            assertTrue(weekDays.any { it.dayOfMonth == 16 && it.isSelected })
        }
    }

    @Test
    fun `test nextWeek and previousWeek adjust selection by 7 days`() = runTest(testDispatcher) {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 10, 0, 0)
        }
        viewModel.selectDate(cal.timeInMillis)
        testScheduler.advanceUntilIdle()

        viewModel.nextWeek()
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val nextCal = Calendar.getInstance().apply { timeInMillis = state.selectedDateMillis }
            assertEquals(23, nextCal.get(Calendar.DAY_OF_MONTH))
        }

        viewModel.previousWeek()
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val prevCal = Calendar.getInstance().apply { timeInMillis = state.selectedDateMillis }
            assertEquals(16, prevCal.get(Calendar.DAY_OF_MONTH))
        }
    }

    @Test
    fun `test showActivityDetail and dismissActivityDetail`() = runTest(testDispatcher) {
        val item = ActivityItem(
            id = 50L,
            title = "Design Sync",
            startTimeMillis = System.currentTimeMillis(),
            endTimeMillis = System.currentTimeMillis() + 3600000L
        )
        activityRepository.insertActivity(item)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertNull(initialState.selectedActivityForDetail)

            viewModel.showActivityDetail(item)
            val detailState = awaitItem()
            assertNotNull(detailState.selectedActivityForDetail)
            assertEquals("Design Sync", detailState.selectedActivityForDetail?.title)

            viewModel.dismissActivityDetail()
            val dismissedState = awaitItem()
            assertNull(dismissedState.selectedActivityForDetail)
        }
    }

    @Test
    fun `test deleteActivity removes item and dismisses detail if active`() = runTest(testDispatcher) {
        val item = ActivityItem(
            id = 60L,
            title = "Temporary Meeting",
            startTimeMillis = System.currentTimeMillis(),
            endTimeMillis = System.currentTimeMillis() + 3600000L
        )
        activityRepository.insertActivity(item)
        testScheduler.advanceUntilIdle()

        viewModel.showActivityDetail(item)
        testScheduler.advanceUntilIdle()

        viewModel.deleteActivity(60L)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.selectedActivityForDetail)
            assertFalse(state.selectedDayActivities.any { it.id == 60L })
        }
    }

    @Test
    fun `test allDayActivities and timedActivities filter properly`() = runTest(testDispatcher) {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 20, 10, 0, 0)
        }
        val baseTime = cal.timeInMillis

        // 1. All-Day item
        activityRepository.insertActivity(
            ActivityItem(
                id = 70L,
                title = "College Holiday",
                isAllDay = true,
                startTimeMillis = baseTime,
                endTimeMillis = baseTime + 86400000L
            )
        )

        // 2. Timed item
        activityRepository.insertActivity(
            ActivityItem(
                id = 71L,
                title = "Lab Exam",
                isAllDay = false,
                startTimeMillis = baseTime,
                endTimeMillis = baseTime + 7200000L
            )
        )

        viewModel.selectDate(baseTime)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.selectedDayActivities.size)
            assertEquals(1, state.allDayActivities.size)
            assertEquals("College Holiday", state.allDayActivities.first().title)
            assertEquals(1, state.timedActivities.size)
            assertEquals("Lab Exam", state.timedActivities.first().title)
        }
    }

    @Test
    fun `test toggleCardExpansion and collapseCard`() = runTest(testDispatcher) {
        val item = ActivityItem(
            id = 80L,
            title = "Sprint Review",
            startTimeMillis = System.currentTimeMillis(),
            endTimeMillis = System.currentTimeMillis() + 3600000L
        )
        activityRepository.insertActivity(item)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val initialState = awaitItem()
            assertNull(initialState.expandedActivityId)

            viewModel.toggleCardExpansion(80L)
            val expandedState = awaitItem()
            assertEquals(80L, expandedState.expandedActivityId)

            viewModel.toggleCardExpansion(80L)
            val collapsedState = awaitItem()
            assertNull(collapsedState.expandedActivityId)

            viewModel.toggleCardExpansion(80L)
            val reExpandedState = awaitItem()
            assertEquals(80L, reExpandedState.expandedActivityId)

            viewModel.collapseCard()
            val finalCollapsedState = awaitItem()
            assertNull(finalCollapsedState.expandedActivityId)
        }
    }

    @Test
    fun `test deleting expanded activity collapses card`() = runTest(testDispatcher) {
        val item = ActivityItem(
            id = 90L,
            title = "Task to Delete",
            startTimeMillis = System.currentTimeMillis(),
            endTimeMillis = System.currentTimeMillis() + 3600000L
        )
        activityRepository.insertActivity(item)
        testScheduler.advanceUntilIdle()

        viewModel.toggleCardExpansion(90L)
        testScheduler.advanceUntilIdle()

        viewModel.deleteActivity(90L)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.expandedActivityId)
        }
    }

    @Test
    fun `test note entries are excluded from calendar day dots and selected day activities`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        val note = ActivityItem(
            id = 101L,
            title = "Personal Diary Note",
            entryType = EntryType.NOTE,
            startTimeMillis = now,
            endTimeMillis = now
        )
        val event = ActivityItem(
            id = 102L,
            title = "Design Review",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = now,
            endTimeMillis = now + 3600000L
        )

        activityRepository.insertActivity(note)
        activityRepository.insertActivity(event)
        testScheduler.advanceUntilIdle()

        viewModel.selectDate(now)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.selectedDayActivities.size)
            assertEquals("Design Review", state.selectedDayActivities.first().title)

            val matchingDay = state.monthDays.firstOrNull { it.isSelected }
            assertNotNull(matchingDay)
            assertEquals(1, matchingDay!!.activities.size)
            assertEquals("Design Review", matchingDay.activities.first().title)
        }
    }

    @Test
    fun `test jumpToToday updates todayPulseTrigger and synchronizes date`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.jumpToToday()
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.todayPulseTrigger > 0L)
            val todayDay = state.monthDays.firstOrNull { it.isToday }
            assertNotNull(todayDay)
            assertTrue(todayDay!!.isSelected)
        }
    }
}
