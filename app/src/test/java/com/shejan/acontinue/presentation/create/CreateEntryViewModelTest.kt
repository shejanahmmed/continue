package com.shejan.acontinue.presentation.create

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeNoteRepository
import com.shejan.acontinue.FakeScheduler
import com.shejan.acontinue.FakeSettingsRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.EntryType
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

@OptIn(ExperimentalCoroutinesApi::class)
class CreateEntryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var scheduler: FakeScheduler
    private lateinit var settingsRepository: FakeSettingsRepository
    private lateinit var viewModel: CreateEntryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        noteRepository = FakeNoteRepository()
        scheduler = FakeScheduler()
        settingsRepository = FakeSettingsRepository()
        viewModel = CreateEntryViewModel(activityRepository, noteRepository, scheduler, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test empty title emits error event`() = runTest(testDispatcher) {
        viewModel.updateTitle("")

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is CreateEntryEvent.ShowError)
        }
    }

    @Test
    fun `test saving valid activity schedules reminder and creates initial note`() = runTest(testDispatcher) {
        viewModel.updateTitle("CS 101 Lecture")
        viewModel.updateCategory(ActivityCategory.CLASS)
        viewModel.updateNotes("Bring textbook Chapter 4")

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertEquals(CreateEntryEvent.SavedSuccessfully, event)

            val activities = activityRepository.activitiesFlow.value
            assertEquals(1, activities.size)
            assertEquals("CS 101 Lecture", activities.first().title)
            assertEquals(ActivityCategory.CLASS, activities.first().category)

            val notes = noteRepository.notesFlow.value
            assertEquals(1, notes.size)
            assertEquals("Bring textbook Chapter 4", notes.first().content)

            assertEquals(1, scheduler.scheduledActivities.size)
            assertEquals("CS 101 Lecture", scheduler.scheduledActivities.first().title)
        }
    }

    @Test
    fun `test saving reminder entry type`() = runTest(testDispatcher) {
        viewModel.updateEntryType(EntryType.REMINDER)
        viewModel.updateTitle("Submit Assignment")

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            val saved = activityRepository.activitiesFlow.value.first()
            assertEquals(EntryType.REMINDER, saved.entryType)
            assertEquals("Submit Assignment", saved.title)
        }
    }

    @Test
    fun `test appending emoji updates title and state`() = runTest(testDispatcher) {
        assertTrue(viewModel.state.value.recentEmojis.isEmpty())

        viewModel.appendEmojiToTitle("🎂")
        testScheduler.advanceUntilIdle()

        assertEquals("🎂", viewModel.state.value.title)
        assertEquals("🎂", viewModel.state.value.emoji)
        assertEquals(listOf("🎂"), viewModel.state.value.recentEmojis)
        assertEquals(listOf("🎂"), settingsRepository.settingsFlow.value.recentEmojis)

        viewModel.updateTitle("Birthday Party")
        viewModel.appendEmojiToTitle("🎉")
        testScheduler.advanceUntilIdle()

        assertEquals("🎉 Birthday Party", viewModel.state.value.title)
        assertEquals("🎉", viewModel.state.value.emoji)
        assertEquals(listOf("🎉", "🎂"), viewModel.state.value.recentEmojis)
        assertEquals(listOf("🎉", "🎂"), settingsRepository.settingsFlow.value.recentEmojis)
    }

    @Test
    fun `test updateStartDate preserves time and adjusts endMillis if necessary`() = runTest(testDispatcher) {
        val initialStart = viewModel.state.value.startMillis
        val initialEnd = viewModel.state.value.endMillis

        val startCal = java.util.Calendar.getInstance().apply { timeInMillis = initialStart }
        val hour = startCal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = startCal.get(java.util.Calendar.MINUTE)

        viewModel.updateStartDate(2026, java.util.Calendar.OCTOBER, 13)

        val updatedStart = viewModel.state.value.startMillis
        val updatedStartCal = java.util.Calendar.getInstance().apply { timeInMillis = updatedStart }

        assertEquals(2026, updatedStartCal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.OCTOBER, updatedStartCal.get(java.util.Calendar.MONTH))
        assertEquals(13, updatedStartCal.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(hour, updatedStartCal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(minute, updatedStartCal.get(java.util.Calendar.MINUTE))
        assertTrue(viewModel.state.value.endMillis >= updatedStart)
    }

    @Test
    fun `test updateEndDate preserves time and maintains interval integrity`() = runTest(testDispatcher) {
        val initialEnd = viewModel.state.value.endMillis
        val endCal = java.util.Calendar.getInstance().apply { timeInMillis = initialEnd }
        val hour = endCal.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = endCal.get(java.util.Calendar.MINUTE)

        viewModel.updateEndDate(2027, java.util.Calendar.NOVEMBER, 20)

        val updatedEnd = viewModel.state.value.endMillis
        val updatedEndCal = java.util.Calendar.getInstance().apply { timeInMillis = updatedEnd }

        assertEquals(2027, updatedEndCal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.NOVEMBER, updatedEndCal.get(java.util.Calendar.MONTH))
        assertEquals(20, updatedEndCal.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(hour, updatedEndCal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(minute, updatedEndCal.get(java.util.Calendar.MINUTE))
        assertTrue(updatedEnd >= viewModel.state.value.startMillis)
    }

    @Test
    fun `test default start and end times initialize to clean top of hour`() = runTest(testDispatcher) {
        val startMillis = viewModel.state.value.startMillis
        val endMillis = viewModel.state.value.endMillis

        val startCal = java.util.Calendar.getInstance().apply { timeInMillis = startMillis }
        assertEquals(0, startCal.get(java.util.Calendar.MINUTE))
        assertEquals(0, startCal.get(java.util.Calendar.SECOND))
        assertEquals(0, startCal.get(java.util.Calendar.MILLISECOND))

        assertEquals(startMillis + 3600000L, endMillis)
    }

    @Test
    fun `test updateStartTime sets correct hour minute ampm and adjusts endMillis if necessary`() = runTest(testDispatcher) {
        // Set start time to 8:30 PM (20:30)
        viewModel.updateStartTime(hour12 = 8, minute = 30, isPm = true)

        val startCal = java.util.Calendar.getInstance().apply { timeInMillis = viewModel.state.value.startMillis }
        assertEquals(20, startCal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(30, startCal.get(java.util.Calendar.MINUTE))
        assertEquals(0, startCal.get(java.util.Calendar.SECOND))

        // Verify endMillis is maintained greater than or equal to startMillis
        assertTrue(viewModel.state.value.endMillis >= viewModel.state.value.startMillis)
    }

    @Test
    fun `test updateEndTime sets correct hour minute ampm and maintains interval integrity`() = runTest(testDispatcher) {
        // Set end time to 10:45 PM (22:45)
        viewModel.updateEndTime(hour12 = 10, minute = 45, isPm = true)

        val endCal = java.util.Calendar.getInstance().apply { timeInMillis = viewModel.state.value.endMillis }
        assertEquals(22, endCal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(45, endCal.get(java.util.Calendar.MINUTE))
        assertEquals(0, endCal.get(java.util.Calendar.SECOND))

        assertTrue(viewModel.state.value.endMillis >= viewModel.state.value.startMillis)
    }

    @Test
    fun `test multi alert selection and custom alert addition`() = runTest(testDispatcher) {
        // Initial state has alertsEnabled = false and selectedAlertMinutes = emptySet()
        assertFalse(viewModel.state.value.alertsEnabled)
        assertTrue(viewModel.state.value.selectedAlertMinutes.isEmpty())

        // Enable alerts -> defaults to 10 mins before
        viewModel.toggleAlertsEnabled(true)
        assertTrue(viewModel.state.value.alertsEnabled)
        assertEquals(setOf(10), viewModel.state.value.selectedAlertMinutes)

        // Toggle '1 hour before' (60 min)
        viewModel.toggleAlertMinute(60)
        assertEquals(setOf(10, 60), viewModel.state.value.selectedAlertMinutes)

        // Add custom alert '9 hours'
        viewModel.addCustomAlert(9, "hours")
        testScheduler.advanceUntilIdle()

        // 9 hours = 540 min
        assertEquals(setOf(10, 60, 540), viewModel.state.value.selectedAlertMinutes)
        assertEquals(1, viewModel.state.value.savedCustomAlerts.size)
        assertEquals(9, viewModel.state.value.savedCustomAlerts.first().value)
        assertEquals("hours", viewModel.state.value.savedCustomAlerts.first().unit)

        // Delete custom alert
        val customId = viewModel.state.value.savedCustomAlerts.first().id
        viewModel.deleteCustomAlert(customId)
        testScheduler.advanceUntilIdle()

        assertEquals(setOf(10, 60), viewModel.state.value.selectedAlertMinutes)
        assertTrue(viewModel.state.value.savedCustomAlerts.isEmpty())
    }

    @Test
    fun `test disabling alert switch clears scheduled offsets`() = runTest(testDispatcher) {
        viewModel.updateTitle("Team Sync")
        viewModel.toggleAlertsEnabled(false)

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            val saved = activityRepository.activitiesFlow.value.first()
            assertTrue(saved.reminderMinutesList.isEmpty())
            assertEquals(-1, saved.reminderMinutesBefore)
        }
    }

    @Test
    fun `test recurrence defaults and state mutations`() = runTest(testDispatcher) {
        val defaultState = viewModel.state.value
        assertEquals(com.shejan.acontinue.domain.model.RecurrenceMode.NONE, defaultState.recurrenceMode)
        assertEquals(1, defaultState.recurrenceInterval)
        assertEquals(com.shejan.acontinue.domain.model.RecurrenceEndType.FOREVER, defaultState.recurrenceEndType)
        assertEquals(10, defaultState.recurrenceEndCount)

        // Update to Weekly with interval 2
        viewModel.updateRecurrence(com.shejan.acontinue.domain.model.RecurrenceMode.WEEKLY, 2)
        assertEquals(com.shejan.acontinue.domain.model.RecurrenceMode.WEEKLY, viewModel.state.value.recurrenceMode)
        assertEquals(2, viewModel.state.value.recurrenceInterval)
        assertFalse(viewModel.state.value.recurrenceDaysOfWeek.isEmpty())

        // Toggle weekday
        viewModel.toggleRecurrenceDay(java.util.Calendar.MONDAY)
        assertTrue(viewModel.state.value.recurrenceDaysOfWeek.contains(java.util.Calendar.MONDAY))

        // Update Monthly mode and toggle specific day
        viewModel.updateMonthlyRepeatType(com.shejan.acontinue.domain.model.MonthlyRepeatType.SPECIFIC_DATES)
        assertEquals(com.shejan.acontinue.domain.model.MonthlyRepeatType.SPECIFIC_DATES, viewModel.state.value.monthlyRepeatType)
        viewModel.toggleMonthlySelectedDay(28)
        assertTrue(viewModel.state.value.monthlySelectedDays.contains(28))

        // Update Yearly mode and toggle month
        viewModel.updateYearlyRepeatType(com.shejan.acontinue.domain.model.YearlyRepeatType.SPECIFIC_MONTHS)
        assertEquals(com.shejan.acontinue.domain.model.YearlyRepeatType.SPECIFIC_MONTHS, viewModel.state.value.yearlyRepeatType)
        viewModel.toggleYearlySelectedMonth(java.util.Calendar.DECEMBER)
        assertTrue(viewModel.state.value.yearlySelectedMonths.contains(java.util.Calendar.DECEMBER))

        // Update duration type and count
        viewModel.updateRecurrenceEndType(com.shejan.acontinue.domain.model.RecurrenceEndType.COUNT)
        viewModel.updateRecurrenceEndCount(5)
        assertEquals(com.shejan.acontinue.domain.model.RecurrenceEndType.COUNT, viewModel.state.value.recurrenceEndType)
        assertEquals(5, viewModel.state.value.recurrenceEndCount)
    }

    @Test
    fun `test saving entry preserves recurrence metadata`() = runTest(testDispatcher) {
        viewModel.updateTitle("Project Sprint")
        viewModel.updateRecurrence(com.shejan.acontinue.domain.model.RecurrenceMode.WEEKLY, 2)
        viewModel.toggleRecurrenceDay(java.util.Calendar.WEDNESDAY)
        viewModel.updateRecurrenceEndType(com.shejan.acontinue.domain.model.RecurrenceEndType.COUNT)
        viewModel.updateRecurrenceEndCount(8)

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            val saved = activityRepository.activitiesFlow.value.first()
            assertEquals("Project Sprint", saved.title)
            assertEquals(com.shejan.acontinue.domain.model.RecurrenceMode.WEEKLY, saved.recurrenceMode)
            assertEquals(2, saved.recurrenceInterval)
            assertTrue(saved.recurrenceDaysOfWeek.contains(java.util.Calendar.WEDNESDAY))
            assertEquals(com.shejan.acontinue.domain.model.RecurrenceEndType.COUNT, saved.recurrenceEndType)
            assertEquals(8, saved.recurrenceEndCount)
        }
    }
}

