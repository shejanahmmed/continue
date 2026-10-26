package com.shejan.acontinue.presentation.create

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeLinkRepository
import com.shejan.acontinue.FakeNoteRepository
import com.shejan.acontinue.FakeScheduler
import com.shejan.acontinue.FakeSettingsRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
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
            assertEquals("Bring textbook Chapter 4", activities.first().description)

            // Event notes are stored on the event itself, leaving workspace notes clean
            val notes = noteRepository.notesFlow.value
            assertEquals(0, notes.size)

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
            assertEquals(saved.startTimeMillis, saved.endTimeMillis)
        }
    }

    @Test
    fun `test selecting emoji updates emoji state without modifying title`() = runTest(testDispatcher) {
        assertTrue(viewModel.state.value.recentEmojis.isEmpty())

        viewModel.selectEmoji("🎂")
        testScheduler.advanceUntilIdle()

        assertEquals("", viewModel.state.value.title)
        assertEquals("🎂", viewModel.state.value.emoji)
        assertEquals(listOf("🎂"), viewModel.state.value.recentEmojis)
        assertEquals(listOf("🎂"), settingsRepository.settingsFlow.value.recentEmojis)

        viewModel.updateTitle("Birthday Party")
        viewModel.selectEmoji("🎉")
        testScheduler.advanceUntilIdle()

        assertEquals("Birthday Party", viewModel.state.value.title)
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
            assertEquals(listOf(0, 15), saved.reminderMinutesList)
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

    @Test
    fun `test makeCopyOfNote inserts duplicate note into repositories without dismissing screen`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.makeCopyOfNote(
            title = "Project Plan",
            content = "1. Phase One\n2. Phase Two",
            colorHex = 0xFF6B4407L
        )
        testScheduler.advanceUntilIdle()

        val activities = activityRepository.activitiesFlow.value
        assertEquals(1, activities.size)
        val copyActivity = activities.first()
        assertEquals("Project Plan", copyActivity.title)
        assertEquals("1. Phase One\n2. Phase Two", copyActivity.description)
        assertEquals(EntryType.NOTE, copyActivity.entryType)
        assertEquals(0xFF6B4407L, copyActivity.reminderCategoryColorHex)

        val notes = noteRepository.notesFlow.value
        assertEquals(1, notes.size)
        val copyNote = notes.first()
        assertEquals(copyActivity.id, copyNote.activityId)
        assertEquals("Project Plan", copyNote.title)
        assertEquals("1. Phase One\n2. Phase Two", copyNote.content)

        // Verify that screen is NOT dismissed (isSaving remains false, no SavedSuccessfully event)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun `test makeCopyOfNote with blank title and content is safely ignored`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.makeCopyOfNote(title = "   ", content = "   ")
        testScheduler.advanceUntilIdle()

        assertTrue(activityRepository.activitiesFlow.value.isEmpty())
        assertTrue(noteRepository.notesFlow.value.isEmpty())
    }

    @Test
    fun `test addNoteLabel persists label and updates savedNoteLabels state`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.addNoteLabel("Work")
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Work"), viewModel.state.value.savedNoteLabels)

        // Adding duplicate or blank should be handled gracefully
        viewModel.addNoteLabel("Work")
        viewModel.addNoteLabel("   ")
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Work"), viewModel.state.value.savedNoteLabels)

        viewModel.addNoteLabel("Personal")
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Work", "Personal"), viewModel.state.value.savedNoteLabels)
    }

    @Test
    fun `test saveNote with labels saves comma-separated labels in reminderCategory`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.saveNote(
            title = "Weekly Tasks",
            content = "1. Clean room\n2. Buy groceries",
            colorHex = 0xFF121212L,
            labels = listOf("Work", "Personal")
        )
        testScheduler.advanceUntilIdle()

        val activities = activityRepository.activitiesFlow.value
        assertEquals(1, activities.size)
        val activity = activities.first()
        assertEquals("Work,Personal", activity.reminderCategory)
    }

    @Test
    fun `test makeCopyOfNote with labels preserves labels in duplicate note`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.makeCopyOfNote(
            title = "Template Note",
            content = "Checklist items",
            colorHex = 0xFF121212L,
            labels = listOf("Templates", "Routine")
        )
        testScheduler.advanceUntilIdle()

        val activities = activityRepository.activitiesFlow.value
        assertEquals(1, activities.size)
        val activity = activities.first()
        assertEquals("Templates,Routine", activity.reminderCategory)
    }

    @Test
    fun `test saveNote with default parameters uses 0xFF000000L background color`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.saveNote(
            title = "Default Color Note",
            content = "Testing background parity"
        )
        testScheduler.advanceUntilIdle()

        val activities = activityRepository.activitiesFlow.value
        assertEquals(1, activities.size)
        val activity = activities.first()
        assertEquals(0xFF000000L, activity.reminderCategoryColorHex)
    }

    @Test
    fun `test updateNoteLabel renames label and updates savedNoteLabels state`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.addNoteLabel("Work")
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Work"), viewModel.state.value.savedNoteLabels)

        viewModel.updateNoteLabel("Work", "Office")
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Office"), viewModel.state.value.savedNoteLabels)

        // Blank new name should be ignored
        viewModel.updateNoteLabel("Office", "   ")
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Office"), viewModel.state.value.savedNoteLabels)
    }

    @Test
    fun `test deleteNoteLabel removes label and updates savedNoteLabels state`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.addNoteLabel("Work")
        viewModel.addNoteLabel("Personal")
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Work", "Personal"), viewModel.state.value.savedNoteLabels)

        viewModel.deleteNoteLabel("Work")
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Personal"), viewModel.state.value.savedNoteLabels)
    }

    @Test
    fun `test save reminder schedules alarm at exact target time with offset 0 by default`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.updateEntryType(com.shejan.acontinue.domain.model.EntryType.REMINDER)
        viewModel.updateTitle("Dentist Appointment")
        viewModel.updateEarlyAlertMinutes(-1) // Default "No early alert"

        viewModel.saveEntry()
        testScheduler.advanceUntilIdle()

        assertEquals(1, scheduler.scheduledActivities.size)
        val scheduled = scheduler.scheduledActivities.first()
        assertEquals("Dentist Appointment", scheduled.title)
        assertEquals(listOf(0), scheduled.reminderMinutesList)
        assertEquals(-1, scheduled.reminderMinutesBefore)
    }

    @Test
    fun `test save reminder with early alert schedules both early offset and exact time offset 0`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.updateEntryType(com.shejan.acontinue.domain.model.EntryType.REMINDER)
        viewModel.updateTitle("Take Medication")
        viewModel.updateEarlyAlertMinutes(15) // 15 mins before

        viewModel.saveEntry()
        testScheduler.advanceUntilIdle()

        assertEquals(1, scheduler.scheduledActivities.size)
        val scheduled = scheduler.scheduledActivities.first()
        assertEquals(listOf(0, 15), scheduled.reminderMinutesList)
        assertEquals(15, scheduled.reminderMinutesBefore)
    }

    @Test
    fun `test save all day reminder normalizes start time to 9 AM on target date`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.updateEntryType(com.shejan.acontinue.domain.model.EntryType.REMINDER)
        viewModel.updateTitle("Tax Filing Deadline")
        viewModel.updateAllDay(true)
        viewModel.updateStartDate(2026, java.util.Calendar.OCTOBER, 25)

        viewModel.saveEntry()
        testScheduler.advanceUntilIdle()

        assertEquals(1, scheduler.scheduledActivities.size)
        val scheduled = scheduler.scheduledActivities.first()
        assertTrue(scheduled.isAllDay)

        val cal = java.util.Calendar.getInstance().apply { timeInMillis = scheduled.startTimeMillis }
        assertEquals(2026, cal.get(java.util.Calendar.YEAR))
        assertEquals(java.util.Calendar.OCTOBER, cal.get(java.util.Calendar.MONTH))
        assertEquals(25, cal.get(java.util.Calendar.DAY_OF_MONTH))
        assertEquals(9, cal.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(java.util.Calendar.MINUTE))
        assertEquals(0, cal.get(java.util.Calendar.SECOND))
    }

    @Test
    fun `test addReminderCategory trims input and prevents duplicate categories`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        // Add "Health" with leading/trailing spaces
        viewModel.addReminderCategory("  Health  ", 0xFF4DD0E1, "heart")
        testScheduler.advanceUntilIdle()

        assertEquals("Health", viewModel.state.value.selectedReminderCategory.name)
        val initialCategories = viewModel.state.value.savedReminderCategories
        assertTrue(initialCategories.any { it.name == "Health" })

        // Adding duplicate "health" with different case should update color/icon and not create duplicate
        viewModel.addReminderCategory("health", 0xFFFA7268, "medical")
        testScheduler.advanceUntilIdle()

        val updatedCategories = viewModel.state.value.savedReminderCategories
        val healthCount = updatedCategories.count { it.name.equals("Health", ignoreCase = true) }
        assertEquals(1, healthCount)
        assertEquals("Health", viewModel.state.value.selectedReminderCategory.name)
        assertEquals(0xFFFA7268, viewModel.state.value.selectedReminderCategory.colorHex)
    }

    @Test
    fun `test AlarmScheduler category extra constants are properly declared`() {
        assertEquals("extra_reminder_category_color", com.shejan.acontinue.core.notification.AlarmScheduler.EXTRA_REMINDER_CATEGORY_COLOR)
        assertEquals("extra_reminder_category_icon", com.shejan.acontinue.core.notification.AlarmScheduler.EXTRA_REMINDER_CATEGORY_ICON)
    }

    @Test
    fun `test saving event stores note in description and does not inject into noteRepository`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.updateEntryType(EntryType.ACTIVITY)
        viewModel.updateTitle("Physics Colloquium")
        viewModel.updateNotes("Bring question sheet and calculator")

        viewModel.saveEntry()
        testScheduler.advanceUntilIdle()

        val savedActivity = activityRepository.activitiesFlow.value.find { it.title == "Physics Colloquium" }
        org.junit.Assert.assertNotNull(savedActivity)
        assertEquals("Bring question sheet and calculator", savedActivity?.description)

        // Ensure workspace notes are empty - event notes must not pollute workspace notes
        val workspaceNotes = noteRepository.getNotesForActivity(savedActivity!!.id)
        workspaceNotes.test {
            val list = awaitItem()
            assertTrue("Workspace notes must be empty on event creation", list.isEmpty())
        }
    }

    @Test
    fun `test loadActivityForEditing populates state and saving updates existing activity`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        val original = ActivityItem(
            id = 55L,
            title = "Original Meeting",
            description = "Original agenda",
            category = ActivityCategory.PROJECT,
            location = "Room 302",
            startTimeMillis = 1000L,
            endTimeMillis = 2000L
        )
        activityRepository.insertActivity(original)
        testScheduler.advanceUntilIdle()

        // Load for editing
        viewModel.loadActivityForEditing(55L)
        testScheduler.advanceUntilIdle()

        assertEquals(55L, viewModel.state.value.editingActivityId)
        assertEquals("Original Meeting", viewModel.state.value.title)
        assertEquals("Original agenda", viewModel.state.value.notes)
        assertEquals(ActivityCategory.PROJECT, viewModel.state.value.category)
        assertEquals("Room 302", viewModel.state.value.location)

        // Modify and save
        viewModel.updateTitle("Updated Meeting")
        viewModel.updateNotes("Revised agenda")
        viewModel.saveEntry()
        testScheduler.advanceUntilIdle()

        // Verify only 1 activity exists and fields were updated
        val activities = activityRepository.activitiesFlow.value
        assertEquals(1, activities.size)
        val updated = activities.first()
        assertEquals(55L, updated.id)
        assertEquals("Updated Meeting", updated.title)
        assertEquals("Revised agenda", updated.description)
    }

    @Test
    fun `test saveNote with single character content creates activity and note with clean title`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.saveNote(title = "", content = "a")
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertEquals(CreateEntryEvent.SavedSuccessfully, event)

            val activities = activityRepository.activitiesFlow.value
            assertEquals(1, activities.size)
            val activity = activities.first()
            assertEquals("a", activity.title)
            assertEquals("a", activity.description)
            assertEquals(EntryType.NOTE, activity.entryType)
            assertEquals(0L, activity.startTimeMillis)
            assertEquals(0L, activity.endTimeMillis)

            val notes = noteRepository.notesFlow.value
            assertEquals(1, notes.size)
            val note = notes.first()
            assertEquals("a", note.title)
            assertEquals("a", note.content)
            assertEquals(activity.id, note.activityId)
        }
    }

    @Test
    fun `test saveNote with checklist content extracts clean title without checkbox prefix`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.saveNote(title = "", content = "[ ] Buy milk\n[ ] Eggs")
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertEquals(CreateEntryEvent.SavedSuccessfully, event)

            val activities = activityRepository.activitiesFlow.value
            assertEquals(1, activities.size)
            assertEquals("Buy milk", activities.first().title)
            assertEquals("[ ] Buy milk\n[ ] Eggs", activities.first().description)
        }
    }

    @Test
    fun `test saveNote when editing existing note updates record without duplicate`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        val initialActivity = ActivityItem(
            id = 88L,
            title = "Original Note Title",
            description = "Original Note Content",
            entryType = EntryType.NOTE,
            startTimeMillis = 1000L,
            endTimeMillis = 2000L,
            reminderCategory = "Work",
            reminderCategoryColorHex = 0xFF5C1324L
        )
        activityRepository.insertActivity(initialActivity)
        noteRepository.insertNote(
            ActivityNote(
                id = 101L,
                activityId = 88L,
                title = "Original Note Title",
                content = "Original Note Content",
                updatedAtMillis = 1000L
            )
        )
        testScheduler.advanceUntilIdle()

        viewModel.loadActivityForEditing(88L)
        testScheduler.advanceUntilIdle()

        assertEquals(88L, viewModel.state.value.editingActivityId)
        assertEquals(EntryType.NOTE, viewModel.state.value.entryType)
        assertEquals("Work", viewModel.state.value.reminderCategory)
        assertEquals(0xFF5C1324L, viewModel.state.value.reminderCategoryColorHex)

        viewModel.events.test {
            viewModel.saveNote(
                title = "Updated Note Title",
                content = "Updated Note Content",
                colorHex = 0xFF1C4635L,
                labels = listOf("Personal")
            )
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertEquals(CreateEntryEvent.SavedSuccessfully, event)

            // Should remain 1 activity and 1 note (no duplicate records)
            val activities = activityRepository.activitiesFlow.value
            assertEquals(1, activities.size)
            val updatedAct = activities.first()
            assertEquals(88L, updatedAct.id)
            assertEquals("Updated Note Title", updatedAct.title)
            assertEquals("Updated Note Content", updatedAct.description)
            assertEquals("Personal", updatedAct.reminderCategory)
            assertEquals(0xFF1C4635L, updatedAct.reminderCategoryColorHex)
            assertEquals(0L, updatedAct.startTimeMillis)
            assertEquals(0L, updatedAct.endTimeMillis)

            val notes = noteRepository.notesFlow.value
            assertEquals(1, notes.size)
            val updatedNote = notes.first()
            assertEquals(101L, updatedNote.id)
            assertEquals("Updated Note Title", updatedNote.title)
            assertEquals("Updated Note Content", updatedNote.content)
        }
    }

    @Test
    fun `test saveNote with blank title and content is a no-op`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.saveNote(title = "   ", content = "   ")
        testScheduler.advanceUntilIdle()

        assertTrue(activityRepository.activitiesFlow.value.isEmpty())
        assertTrue(noteRepository.notesFlow.value.isEmpty())
    }

    @Test
    fun `test deleteCurrentNote removes existing note and activity`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        val initialActivity = ActivityItem(
            id = 99L,
            title = "To Delete",
            description = "Some content",
            entryType = EntryType.NOTE,
            startTimeMillis = 1000L,
            endTimeMillis = 2000L
        )
        activityRepository.insertActivity(initialActivity)
        testScheduler.advanceUntilIdle()

        viewModel.loadActivityForEditing(99L)
        testScheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.deleteCurrentNote()
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertEquals(CreateEntryEvent.SavedSuccessfully, event)

            assertTrue(activityRepository.activitiesFlow.value.isEmpty())
        }
    }

    @Test
    fun `test saveNote called multiple times on new note updates same record without duplicate`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.events.test {
            viewModel.saveNote(title = "Draft", content = "Initial draft")
            testScheduler.advanceUntilIdle()
            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            assertEquals(1, activityRepository.activitiesFlow.value.size)
            val createdId = viewModel.state.value.editingActivityId
            org.junit.Assert.assertNotNull(createdId)

            viewModel.saveNote(title = "Draft Final", content = "Updated content")
            testScheduler.advanceUntilIdle()
            assertEquals(CreateEntryEvent.SavedSuccessfully, awaitItem())

            assertEquals(1, activityRepository.activitiesFlow.value.size)
            val updated = activityRepository.activitiesFlow.value.first()
            assertEquals("Draft Final", updated.title)
            assertEquals("Updated content", updated.description)
        }
    }

    @Test
    fun `test ensureDraftNoteId creates draft in Room and assigns editingActivityId`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        org.junit.Assert.assertNull(viewModel.state.value.editingActivityId)
        val draftId = viewModel.ensureDraftNoteId(title = "Draft Voice Note", content = "Voice note body")
        testScheduler.advanceUntilIdle()

        org.junit.Assert.assertTrue(draftId > 0L)
        assertEquals(draftId, viewModel.state.value.editingActivityId)
        assertEquals(1, activityRepository.activitiesFlow.value.size)
        assertEquals("Draft Voice Note", activityRepository.activitiesFlow.value.first().title)

        // Calling again returns the existing ID without creating a duplicate
        val secondCallId = viewModel.ensureDraftNoteId()
        assertEquals(draftId, secondCallId)
        assertEquals(1, activityRepository.activitiesFlow.value.size)
    }

    @Test
    fun `test makeCopyOfNote duplicates note and returns valid newId`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        val copyId = viewModel.makeCopyOfNote(
            title = "Original Title",
            content = "Original Content",
            colorHex = 0xFFFF0000L,
            labels = listOf("Work", "Urgent")
        )
        testScheduler.advanceUntilIdle()

        assertTrue(copyId > 0L)
        val activities = activityRepository.activitiesFlow.value
        assertEquals(1, activities.size)
        val copiedActivity = activities.first()
        assertEquals(copyId, copiedActivity.id)
        assertEquals("Original Title", copiedActivity.title)
        assertEquals("Original Content", copiedActivity.description)
        assertEquals(EntryType.NOTE, copiedActivity.entryType)
        assertEquals(0xFFFF0000L, copiedActivity.reminderCategoryColorHex)
        assertEquals("Work,Urgent", copiedActivity.reminderCategory)

        val notes = noteRepository.notesFlow.value.filter { it.activityId == copyId }
        assertEquals(1, notes.size)
        assertEquals("Original Title", notes.first().title)
        assertEquals("Original Content", notes.first().content)
    }

    @Test
    fun `test makeCopyOfNote duplicates attachments of source note`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        // Create an existing note with an attachment
        val sourceActivity = ActivityItem(
            id = 100L,
            title = "Source Note",
            description = "Source Content",
            entryType = EntryType.NOTE,
            startTimeMillis = 0L,
            endTimeMillis = 0L
        )
        activityRepository.insertActivity(sourceActivity)
        linkRepository.insertAttachment(
            com.shejan.acontinue.domain.model.ActivityAttachment(
                id = 1L,
                activityId = 100L,
                fileName = "photo.jpg",
                uriString = "file:///attachments/photo.jpg",
                mimeType = "image/jpeg"
            )
        )
        viewModel.loadActivityForEditing(100L)
        testScheduler.advanceUntilIdle()

        val copyId = viewModel.makeCopyOfNote(
            title = "Copied Note",
            content = "Copied Content"
        )
        testScheduler.advanceUntilIdle()

        assertTrue(copyId > 0L)
        val copyAttachments = linkRepository.attachmentsFlow.value.filter { it.activityId == copyId }
        assertEquals(1, copyAttachments.size)
        assertEquals(copyId, copyAttachments.first().activityId)
        assertEquals("photo.jpg", copyAttachments.first().fileName)
        assertEquals("file:///attachments/photo.jpg", copyAttachments.first().uriString)
    }

    @Test
    fun `test makeCopyOfNote returns -1 when title and content are blank`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        val copyId = viewModel.makeCopyOfNote(
            title = "   ",
            content = "   "
        )
        testScheduler.advanceUntilIdle()

        assertEquals(-1L, copyId)
        assertTrue(activityRepository.activitiesFlow.value.isEmpty())
    }

    @Test
    fun `test loadActivityForEditing loads note content from noteRepository when activity description is empty and self-heals`() = runTest(testDispatcher) {
        val activity = ActivityItem(
            id = 20L,
            title = "Existing Note Title",
            description = "",
            entryType = EntryType.NOTE,
            category = ActivityCategory.OTHER,
            isAllDay = false,
            startTimeMillis = 0L,
            endTimeMillis = 0L,
            location = "",
            priority = ActivityPriority.MEDIUM,
            reminderMinutesBefore = -1,
            reminderMinutesList = emptyList(),
            recurrenceMode = RecurrenceMode.NONE,
            isCompleted = false,
            reminderCategory = "Notes",
            reminderCategoryColorHex = 0xFF000000L,
            createdAtMillis = 1000L
        )
        activityRepository.insertActivity(activity)
        noteRepository.insertNote(
            ActivityNote(
                id = 1L,
                activityId = 20L,
                title = "Existing Note Title",
                content = "Pre-created card content from NoteEntity",
                updatedAtMillis = 1000L
            )
        )

        viewModel.loadActivityForEditing(20L)
        testScheduler.advanceUntilIdle()

        assertEquals(20L, viewModel.state.value.editingActivityId)
        assertEquals("Existing Note Title", viewModel.state.value.title)
        assertEquals("Pre-created card content from NoteEntity", viewModel.state.value.notes)

        // Verify Room self-healing: activity.description is updated so future queries are in sync
        val updatedActivity = activityRepository.activitiesFlow.value.first { it.id == 20L }
        assertEquals("Pre-created card content from NoteEntity", updatedActivity.description)
    }

    @Test
    fun `test loadActivityForEditing resolves note when passed a noteId directly`() = runTest(testDispatcher) {
        val standaloneNote = ActivityNote(
            id = 77L,
            activityId = 0L,
            title = "Standalone Quick Note",
            content = "Quick recorded voice or thought",
            updatedAtMillis = 2000L
        )
        noteRepository.insertNote(standaloneNote)

        viewModel.loadActivityForEditing(77L)
        testScheduler.advanceUntilIdle()

        val editingId = viewModel.state.value.editingActivityId
        assertTrue(editingId != null && editingId > 0L)
        assertEquals("Standalone Quick Note", viewModel.state.value.title)
        assertEquals("Quick recorded voice or thought", viewModel.state.value.notes)
    }

    @Test
    fun `test deleteCurrentNote removes both activity and associated notes`() = runTest(testDispatcher) {
        val activity = ActivityItem(
            id = 30L,
            title = "Note to delete",
            description = "Some content",
            entryType = EntryType.NOTE,
            category = ActivityCategory.OTHER,
            isAllDay = false,
            startTimeMillis = 0L,
            endTimeMillis = 0L,
            location = "",
            priority = ActivityPriority.MEDIUM,
            reminderMinutesBefore = -1,
            reminderMinutesList = emptyList(),
            recurrenceMode = RecurrenceMode.NONE,
            isCompleted = false,
            reminderCategory = "Notes",
            reminderCategoryColorHex = 0xFF000000L,
            createdAtMillis = 1000L
        )
        activityRepository.insertActivity(activity)
        noteRepository.insertNote(
            ActivityNote(
                id = 5L,
                activityId = 30L,
                title = "Note to delete",
                content = "Some content",
                updatedAtMillis = 1000L
            )
        )

        viewModel.loadActivityForEditing(30L)
        testScheduler.advanceUntilIdle()

        viewModel.deleteCurrentNote()
        testScheduler.advanceUntilIdle()

        assertTrue(activityRepository.activitiesFlow.value.none { it.id == 30L })
        assertTrue(noteRepository.notesFlow.value.none { it.activityId == 30L })
    }
}


