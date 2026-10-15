package com.shejan.acontinue.presentation.create

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeLinkRepository
import com.shejan.acontinue.FakeNoteRepository
import com.shejan.acontinue.FakeScheduler
import com.shejan.acontinue.FakeSettingsRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.ReminderCategory
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
    private lateinit var linkRepository: FakeLinkRepository
    private lateinit var viewModel: CreateEntryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        noteRepository = FakeNoteRepository()
        scheduler = FakeScheduler()
        settingsRepository = FakeSettingsRepository()
        linkRepository = FakeLinkRepository()
        viewModel = CreateEntryViewModel(
            activityRepository,
            noteRepository,
            scheduler,
            settingsRepository,
            linkRepository
        )
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

    @Test
    fun `test adding, removing and saving attachments`() = runTest(testDispatcher) {
        val att1 = com.shejan.acontinue.core.util.ProcessedAttachment(
            id = "att-1",
            fileName = "diagram.png",
            filePath = "/fake/diagram.png",
            mimeType = "image/png",
            sizeBytes = 204800L
        )
        val att2 = com.shejan.acontinue.core.util.ProcessedAttachment(
            id = "att-2",
            fileName = "syllabus.pdf",
            filePath = "/fake/syllabus.pdf",
            mimeType = "application/pdf",
            sizeBytes = 1048576L
        )

        viewModel.addAttachment(att1)
        viewModel.addAttachment(att2)
        assertEquals(2, viewModel.state.value.attachments.size)

        // Remove one attachment
        viewModel.removeAttachment("att-1")
        assertEquals(1, viewModel.state.value.attachments.size)
        assertEquals("att-2", viewModel.state.value.attachments.first().id)

        // Save entry and verify attachment is persisted in linkRepository
        viewModel.updateTitle("Class Prep")
        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())
            val savedAttachments = linkRepository.attachmentsFlow.value
            assertEquals(1, savedAttachments.size)
            assertEquals("syllabus.pdf", savedAttachments.first().fileName)
            assertEquals("/fake/syllabus.pdf", savedAttachments.first().uriString)
        }
    }

    @Test
    fun `test reminder mode with early alert and reminder list`() = runTest(testDispatcher) {
        viewModel.updateEntryType(EntryType.REMINDER)
        viewModel.updateTitle("Pick up dry cleaning")
        viewModel.updateEarlyAlertMinutes(15)
        viewModel.updateReminderList("Personal Reminders")

        assertEquals(15, viewModel.state.value.earlyAlertMinutes)
        assertEquals("Personal Reminders", viewModel.state.value.reminderList)

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            val activities = activityRepository.activitiesFlow.value
            assertEquals(1, activities.size)
            val saved = activities.first()
            assertEquals("Pick up dry cleaning", saved.title)
            assertEquals(EntryType.REMINDER, saved.entryType)
            assertEquals(15, saved.reminderMinutesBefore)
            assertEquals(listOf(15), saved.reminderMinutesList)
            assertEquals("", saved.location)
            assertEquals("", saved.description)
        }
    }

    @Test
    fun `test reminder mode with minutely and hourly recurrence`() = runTest(testDispatcher) {
        viewModel.updateEntryType(EntryType.REMINDER)
        viewModel.updateTitle("Take medicine")
        viewModel.updateRecurrence(RecurrenceMode.HOURLY, 2)

        assertEquals(RecurrenceMode.HOURLY, viewModel.state.value.recurrenceMode)
        assertEquals(2, viewModel.state.value.recurrenceInterval)

        viewModel.updateRecurrence(RecurrenceMode.MINUTELY, 30)
        assertEquals(RecurrenceMode.MINUTELY, viewModel.state.value.recurrenceMode)
        assertEquals(30, viewModel.state.value.recurrenceInterval)

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            val activities = activityRepository.activitiesFlow.value
            assertEquals(1, activities.size)
            val saved = activities.first()
            assertEquals("Take medicine", saved.title)
            assertEquals(EntryType.REMINDER, saved.entryType)
            assertEquals(RecurrenceMode.MINUTELY, saved.recurrenceMode)
            assertEquals(30, saved.recurrenceInterval)
        }
    }

    @Test
    fun `test reminder intensity update and persistence`() = runTest(testDispatcher) {
        viewModel.updateEntryType(EntryType.REMINDER)
        viewModel.updateTitle("Water plants")

        // Initial priority should be MEDIUM
        assertEquals(ActivityPriority.MEDIUM, viewModel.state.value.priority)
        assertEquals("Medium", viewModel.state.value.priority.reminderTitle)

        // Update to HIGH (Strong)
        viewModel.updatePriority(ActivityPriority.HIGH)
        assertEquals(ActivityPriority.HIGH, viewModel.state.value.priority)
        assertEquals("Strong", viewModel.state.value.priority.reminderTitle)
        assertEquals("Full screen alert and long sound", viewModel.state.value.priority.reminderDescription)

        // Update to LOW (Light)
        viewModel.updatePriority(ActivityPriority.LOW)
        assertEquals(ActivityPriority.LOW, viewModel.state.value.priority)
        assertEquals("Light", viewModel.state.value.priority.reminderTitle)
        assertEquals("Notification and short sound", viewModel.state.value.priority.reminderDescription)

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            val activities = activityRepository.activitiesFlow.value
            assertEquals(1, activities.size)
            val saved = activities.first()
            assertEquals("Water plants", saved.title)
            assertEquals(ActivityPriority.LOW, saved.priority)
        }
    }

    @Test
    fun `test reminder category selection, addition, and persistence`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        // Default category should be "My reminders"
        assertEquals(ReminderCategory.DEFAULT.name, viewModel.state.value.selectedReminderCategory.name)
        assertEquals(ReminderCategory.DEFAULT.iconName, viewModel.state.value.selectedReminderCategory.iconName)

        // Add custom category "Work"
        viewModel.addReminderCategory("Work", 0xFF4FC3F7, "business")
        testScheduler.advanceUntilIdle()

        assertEquals("Work", viewModel.state.value.selectedReminderCategory.name)
        assertEquals(0xFF4FC3F7, viewModel.state.value.selectedReminderCategory.colorHex)
        assertEquals("business", viewModel.state.value.selectedReminderCategory.iconName)
        assertEquals(2, viewModel.state.value.savedReminderCategories.size)

        // Create a reminder with the selected category
        viewModel.updateEntryType(EntryType.REMINDER)
        viewModel.updateTitle("Team Standup")

        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()

            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            val activities = activityRepository.activitiesFlow.value
            assertEquals(1, activities.size)
            val saved = activities.first()
            assertEquals("Team Standup", saved.title)
            assertEquals(EntryType.REMINDER, saved.entryType)
            assertEquals("Work", saved.reminderCategory)
            assertEquals(0xFF4FC3F7, saved.reminderCategoryColorHex)
            assertEquals("business", saved.reminderCategoryIcon)
        }
    }

    @Test
    fun `test reminder category bidirectional sync and updates across reminders`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        // 1. Create a reminder with custom category "Groceries"
        viewModel.addReminderCategory("Groceries", 0xFFAED581, "shopping_cart")
        testScheduler.advanceUntilIdle()
        viewModel.updateEntryType(EntryType.REMINDER)
        viewModel.updateTitle("Buy Milk")
        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()
            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())
        }

        // 2. Instantiate a second ViewModel (simulating user opening the screen to create another reminder)
        val secondViewModel = CreateEntryViewModel(
            activityRepository,
            noteRepository,
            scheduler,
            settingsRepository,
            linkRepository
        )
        testScheduler.advanceUntilIdle()

        // Verify "Groceries" is synced and available in the categories list for other reminders
        val syncedCategories = secondViewModel.state.value.savedReminderCategories
        assertTrue(syncedCategories.any { it.name == "Groceries" })
        assertEquals(1, secondViewModel.state.value.categoryReminderCounts["Groceries"])

        // 3. Update category details (new color/icon) and verify existing reminders in repository are updated
        secondViewModel.addReminderCategory("Groceries", 0xFFFFA726, "local_grocery_store")
        testScheduler.advanceUntilIdle()

        val updatedActivity = activityRepository.activitiesFlow.value.first()
        assertEquals(0xFFFFA726, updatedActivity.reminderCategoryColorHex)
        assertEquals("local_grocery_store", updatedActivity.reminderCategoryIcon)
    }

    @Test
    fun `test updateReminderCategory renames category across reminders in database and updates settings`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.addReminderCategory("Work", 0xFF4FC3F7, "badge")
        testScheduler.advanceUntilIdle()

        val workCategory = viewModel.state.value.savedReminderCategories.first { it.name == "Work" }

        viewModel.updateEntryType(EntryType.REMINDER)
        viewModel.updateTitle("Standup Meeting")
        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()
            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())
        }

        viewModel.updateReminderCategory(
            oldCategory = workCategory,
            newName = "Office",
            newColorHex = 0xFFBA68C8,
            newIconName = "apartment"
        )
        testScheduler.advanceUntilIdle()

        val updatedActivity = activityRepository.activitiesFlow.value.first()
        assertEquals("Office", updatedActivity.reminderCategory)
        assertEquals(0xFFBA68C8, updatedActivity.reminderCategoryColorHex)
        assertEquals("apartment", updatedActivity.reminderCategoryIcon)

        assertEquals("Office", viewModel.state.value.selectedReminderCategory.name)
        assertEquals(0xFFBA68C8, viewModel.state.value.selectedReminderCategory.colorHex)
        assertEquals("apartment", viewModel.state.value.selectedReminderCategory.iconName)
    }

    @Test
    fun `test deleteReminderCategory resets affected reminders to default category and removes from settings`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.addReminderCategory("Gym", 0xFFFA7268, "run")
        testScheduler.advanceUntilIdle()

        val gymCategory = viewModel.state.value.savedReminderCategories.first { it.name == "Gym" }

        viewModel.updateEntryType(EntryType.REMINDER)
        viewModel.updateTitle("Leg Day")
        viewModel.events.test {
            viewModel.saveEntry()
            testScheduler.advanceUntilIdle()
            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())
        }

        viewModel.deleteReminderCategory(gymCategory)
        testScheduler.advanceUntilIdle()

        val resetActivity = activityRepository.activitiesFlow.value.first()
        assertEquals(ReminderCategory.DEFAULT.name, resetActivity.reminderCategory)
        assertEquals(ReminderCategory.DEFAULT.colorHex, resetActivity.reminderCategoryColorHex)
        assertEquals(ReminderCategory.DEFAULT.iconName, resetActivity.reminderCategoryIcon)

        assertEquals(ReminderCategory.DEFAULT, viewModel.state.value.selectedReminderCategory)
    }

    @Test
    fun `test default reminder category cannot be edited or deleted`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        val defaultCategory = ReminderCategory.DEFAULT

        viewModel.updateReminderCategory(
            oldCategory = defaultCategory,
            newName = "Hacked Reminders",
            newColorHex = 0xFF000000,
            newIconName = "close"
        )
        testScheduler.advanceUntilIdle()

        assertEquals(ReminderCategory.DEFAULT.name, viewModel.state.value.selectedReminderCategory.name)

        viewModel.deleteReminderCategory(defaultCategory)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.savedReminderCategories.any { it.isDefault })
    }

    @Test
    fun `test saveNote creates ActivityItem with EntryType NOTE and records in noteRepository`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.saveNote(title = "Meeting Notes", content = "Discuss Q4 roadmap and deliverables")
            testScheduler.advanceUntilIdle()
            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())
        }

        val activities = activityRepository.activitiesFlow.value
        assertEquals(1, activities.size)
        val savedActivity = activities.first()
        assertEquals("Meeting Notes", savedActivity.title)
        assertEquals("Discuss Q4 roadmap and deliverables", savedActivity.description)
        assertEquals(EntryType.NOTE, savedActivity.entryType)

        val notes = noteRepository.notesFlow.value
        assertEquals(1, notes.size)
        val savedNote = notes.first()
        assertEquals(savedActivity.id, savedNote.activityId)
        assertEquals("Meeting Notes", savedNote.title)
        assertEquals("Discuss Q4 roadmap and deliverables", savedNote.content)
    }

    @Test
    fun `test saveNote with blank title uses first line of content`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.saveNote(title = "", content = "Important idea\nSecond line details")
            testScheduler.advanceUntilIdle()
            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())
        }

        val activities = activityRepository.activitiesFlow.value
        assertEquals(1, activities.size)
        assertEquals("Important idea", activities.first().title)
    }

    @Test
    fun `test saveNote with blank title and content is safely ignored`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.saveNote(title = "   ", content = "   ")
        testScheduler.advanceUntilIdle()

        assertTrue(activityRepository.activitiesFlow.value.isEmpty())
        assertTrue(noteRepository.notesFlow.value.isEmpty())
    }

    @Test
    fun `test updateEntryType to NOTE updates state`() = runTest(testDispatcher) {
        viewModel.updateEntryType(EntryType.NOTE)
        assertEquals(EntryType.NOTE, viewModel.state.value.entryType)
    }

    @Test
    fun `test saveNote with custom colorHex persists color in ActivityItem`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.saveNote(title = "Crimson Note", content = "Important crimson note", colorHex = 0xFF5C1324L)
            testScheduler.advanceUntilIdle()
            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())
        }

        val activities = activityRepository.activitiesFlow.value
        assertEquals(1, activities.size)
        val saved = activities.first()
        assertEquals(0xFF5C1324L, saved.reminderCategoryColorHex)
    }
}

