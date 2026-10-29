package com.shejan.acontinue.data.mapper

import com.shejan.acontinue.data.local.entity.ActivityEntity
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityMappersTest {

    @Test
    fun `test legacy reminder entity with artificial duration normalizes endTime to startTime`() {
        val startTime = 1700000000000L
        val fakeEndTime = startTime + 1800000L // +30 minutes legacy bug

        val legacyEntity = ActivityEntity(
            id = 10L,
            title = "Legacy Reminder",
            description = "",
            entryType = EntryType.REMINDER.name,
            category = ActivityCategory.OTHER.name,
            isAllDay = false,
            startTimeMillis = startTime,
            endTimeMillis = fakeEndTime,
            location = "",
            priority = ActivityPriority.MEDIUM.name,
            reminderMinutesBefore = 0,
            reminderMinutesCsv = "0",
            recurrenceMode = RecurrenceMode.NONE.name,
            recurrenceInterval = 1,
            recurrenceDaysOfWeekCsv = "",
            monthlyRepeatType = "SAME_DAY_OF_MONTH",
            monthlySelectedDaysCsv = "",
            yearlyRepeatType = "SAME_DATE",
            yearlySelectedMonthsCsv = "",
            recurrenceEndType = "FOREVER",
            recurrenceEndCount = 0,
            recurrenceEndDateMillis = 0L,
            isCompleted = false,
            reminderCategory = "My reminders",
            reminderCategoryColorHex = 0xFF9D7BFC,
            reminderCategoryIcon = "bell",
            createdAtMillis = startTime
        )

        val domain = legacyEntity.toDomain()
        assertEquals(EntryType.REMINDER, domain.entryType)
        assertEquals(startTime, domain.startTimeMillis)
        assertEquals(startTime, domain.endTimeMillis) // normalized!

        val remappedEntity = domain.toEntity()
        assertEquals(startTime, remappedEntity.endTimeMillis)
    }

    @Test
    fun `test event preserves distinct startTime and endTime`() {
        val startTime = 1700000000000L
        val endTime = startTime + 3600000L // 1 hour event

        val eventEntity = ActivityEntity(
            id = 20L,
            title = "Architecture Meeting",
            description = "Sprint planning",
            entryType = EntryType.ACTIVITY.name,
            category = ActivityCategory.MEETING.name,
            isAllDay = false,
            startTimeMillis = startTime,
            endTimeMillis = endTime,
            location = "Room 3B",
            priority = ActivityPriority.HIGH.name,
            reminderMinutesBefore = 10,
            reminderMinutesCsv = "10",
            recurrenceMode = RecurrenceMode.NONE.name,
            recurrenceInterval = 1,
            recurrenceDaysOfWeekCsv = "",
            monthlyRepeatType = "SAME_DAY_OF_MONTH",
            monthlySelectedDaysCsv = "",
            yearlyRepeatType = "SAME_DATE",
            yearlySelectedMonthsCsv = "",
            recurrenceEndType = "FOREVER",
            recurrenceEndCount = 0,
            recurrenceEndDateMillis = 0L,
            isCompleted = false,
            reminderCategory = "My reminders",
            reminderCategoryColorHex = 0xFF9D7BFC,
            reminderCategoryIcon = "bell",
            createdAtMillis = startTime
        )

        val domain = eventEntity.toDomain()
        assertEquals(EntryType.ACTIVITY, domain.entryType)
        assertEquals(startTime, domain.startTimeMillis)
        assertEquals(endTime, domain.endTimeMillis) // preserved!

        val remappedEntity = domain.toEntity()
        assertEquals(endTime, remappedEntity.endTimeMillis)
    }

    @Test
    fun `test event normalizes isCompleted to false even if entity had true`() {
        val eventEntity = ActivityEntity(
            id = 21L,
            title = "Completed Event Legacy",
            description = "",
            entryType = EntryType.ACTIVITY.name,
            category = ActivityCategory.MEETING.name,
            isAllDay = false,
            startTimeMillis = 1000L,
            endTimeMillis = 2000L,
            location = "",
            priority = ActivityPriority.MEDIUM.name,
            reminderMinutesBefore = 0,
            reminderMinutesCsv = "",
            recurrenceMode = RecurrenceMode.NONE.name,
            recurrenceInterval = 1,
            recurrenceDaysOfWeekCsv = "",
            monthlyRepeatType = "SAME_DAY_OF_MONTH",
            monthlySelectedDaysCsv = "",
            yearlyRepeatType = "SAME_DATE",
            yearlySelectedMonthsCsv = "",
            recurrenceEndType = "FOREVER",
            recurrenceEndCount = 0,
            recurrenceEndDateMillis = 0L,
            isCompleted = true, // legacy erroneous value
            reminderCategory = "My reminders",
            reminderCategoryColorHex = 0xFF9D7BFC,
            reminderCategoryIcon = "bell",
            createdAtMillis = 1000L
        )

        val domain = eventEntity.toDomain()
        assertEquals(false, domain.isCompleted) // Normalized to false!

        val remapped = domain.copy(isCompleted = true).toEntity()
        assertEquals(false, remapped.isCompleted) // Event cannot be saved as completed!
    }

    @Test
    fun `test reminder preserves isCompleted true`() {
        val reminderEntity = ActivityEntity(
            id = 22L,
            title = "Completed Reminder",
            description = "",
            entryType = EntryType.REMINDER.name,
            category = ActivityCategory.OTHER.name,
            isAllDay = false,
            startTimeMillis = 1000L,
            endTimeMillis = 1000L,
            location = "",
            priority = ActivityPriority.MEDIUM.name,
            reminderMinutesBefore = 0,
            reminderMinutesCsv = "",
            recurrenceMode = RecurrenceMode.NONE.name,
            recurrenceInterval = 1,
            recurrenceDaysOfWeekCsv = "",
            monthlyRepeatType = "SAME_DAY_OF_MONTH",
            monthlySelectedDaysCsv = "",
            yearlyRepeatType = "SAME_DATE",
            yearlySelectedMonthsCsv = "",
            recurrenceEndType = "FOREVER",
            recurrenceEndCount = 0,
            recurrenceEndDateMillis = 0L,
            isCompleted = true,
            reminderCategory = "My reminders",
            reminderCategoryColorHex = 0xFF9D7BFC,
            reminderCategoryIcon = "bell",
            createdAtMillis = 1000L
        )

        val domain = reminderEntity.toDomain()
        assertEquals(true, domain.isCompleted)

        val remapped = domain.toEntity()
        assertEquals(true, remapped.isCompleted)
    }

    @Test
    fun `test entity entryType strings parse robustly to domain EntryType`() {
        val baseEntity = ActivityEntity(
            id = 50L,
            title = "Test Entry",
            description = "",
            entryType = "NOTE",
            category = ActivityCategory.OTHER.name,
            isAllDay = false,
            startTimeMillis = 1000L,
            endTimeMillis = 1000L,
            location = "",
            priority = ActivityPriority.MEDIUM.name,
            reminderMinutesBefore = 0,
            reminderMinutesCsv = "",
            recurrenceMode = RecurrenceMode.NONE.name,
            recurrenceInterval = 1,
            recurrenceDaysOfWeekCsv = "",
            monthlyRepeatType = "SAME_DAY_OF_MONTH",
            monthlySelectedDaysCsv = "",
            yearlyRepeatType = "SAME_DATE",
            yearlySelectedMonthsCsv = "",
            recurrenceEndType = "FOREVER",
            recurrenceEndCount = 0,
            recurrenceEndDateMillis = 0L,
            isCompleted = false,
            reminderCategory = "",
            reminderCategoryColorHex = 0L,
            reminderCategoryIcon = "",
            createdAtMillis = 1000L
        )

        assertEquals(EntryType.NOTE, baseEntity.copy(entryType = "NOTE").toDomain().entryType)
        assertEquals(EntryType.NOTE, baseEntity.copy(entryType = "note").toDomain().entryType)
        assertEquals(EntryType.NOTE, baseEntity.copy(entryType = "notes").toDomain().entryType)
        assertEquals(EntryType.REMINDER, baseEntity.copy(entryType = "REMINDER").toDomain().entryType)
        assertEquals(EntryType.REMINDER, baseEntity.copy(entryType = "reminder").toDomain().entryType)
        assertEquals(EntryType.ACTIVITY, baseEntity.copy(entryType = "ACTIVITY").toDomain().entryType)
        assertEquals(EntryType.ACTIVITY, baseEntity.copy(entryType = "unknown").toDomain().entryType)
    }

    @Test
    fun `test NoteEntity and ActivityNote round-trip preserves colorHex and labels`() {
        val domainNote = com.shejan.acontinue.domain.model.ActivityNote(
            id = 15L,
            activityId = 42L,
            title = "Event Running Note",
            content = "Captured during keynote",
            colorHex = 0xFF3A2040L,
            labels = listOf("Keynote", "ActionItem"),
            updatedAtMillis = 9999L
        )

        val entity = domainNote.toEntity()
        assertEquals(0xFF3A2040L, entity.colorHex)
        assertEquals("Keynote,ActionItem", entity.labels)

        val remapped = entity.toDomain()
        assertEquals(domainNote.id, remapped.id)
        assertEquals(domainNote.activityId, remapped.activityId)
        assertEquals(domainNote.title, remapped.title)
        assertEquals(domainNote.content, remapped.content)
        assertEquals(domainNote.colorHex, remapped.colorHex)
        assertEquals(domainNote.labels, remapped.labels)
        assertEquals(domainNote.updatedAtMillis, remapped.updatedAtMillis)
    }
}
