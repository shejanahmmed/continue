package com.shejan.acontinue.core.util

import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.RecurrenceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class EventFormattersTest {

    @Test
    fun `test formatRecurrenceRule for various modes`() {
        val baseItem = ActivityItem(
            title = "Test Activity",
            category = ActivityCategory.CLASS,
            startTimeMillis = 1000L,
            endTimeMillis = 2000L
        )

        assertEquals("Does not repeat", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.NONE)))
        assertEquals("Repeats every minute", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.MINUTELY)))
        assertEquals("Repeats hourly", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.HOURLY, recurrenceInterval = 1)))
        assertEquals("Repeats every 2 hours", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.HOURLY, recurrenceInterval = 2)))
        assertEquals("Repeats daily", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.DAILY, recurrenceInterval = 1)))
        assertEquals("Repeats every 3 days", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.DAILY, recurrenceInterval = 3)))
        assertEquals("Repeats on weekdays", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.WEEKDAYS)))
        assertEquals("Repeats weekly", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.WEEKLY, recurrenceInterval = 1, recurrenceDaysOfWeek = emptyList())))
        assertEquals("Repeats weekly on Mon, Wed", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.WEEKLY, recurrenceDaysOfWeek = listOf(Calendar.MONDAY, Calendar.WEDNESDAY))))
        assertEquals("Repeats monthly", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.MONTHLY)))
        assertEquals("Repeats yearly", EventFormatters.formatRecurrenceRule(baseItem.copy(recurrenceMode = RecurrenceMode.YEARLY)))
    }

    @Test
    fun `test formatAlerts for multiple and single alerts`() {
        val baseItem = ActivityItem(
            title = "Test Activity",
            category = ActivityCategory.CLASS,
            startTimeMillis = 1000L,
            endTimeMillis = 2000L
        )

        assertNull(EventFormatters.formatAlerts(baseItem.copy(reminderMinutesBefore = -1, reminderMinutesList = emptyList())))
        assertEquals("15 mins before", EventFormatters.formatAlerts(baseItem.copy(reminderMinutesBefore = 15, reminderMinutesList = emptyList())))
        assertEquals("At start time, 1 hour before", EventFormatters.formatAlerts(baseItem.copy(reminderMinutesList = listOf(0, 60))))
    }

    @Test
    fun `test formatTimeSpan for all-day and timed events`() {
        assertEquals("All day", EventFormatters.formatTimeSpan(1000L, 2000L, isAllDay = true))
    }
}
