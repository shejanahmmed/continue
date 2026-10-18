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
}
