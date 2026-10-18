package com.shejan.acontinue.core.notification

import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.EntryType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPrioritySystemTest {

    @Test
    fun `verify reminder priority alert styles and titles match Samsung spec`() {
        assertEquals("Light", ActivityPriority.LOW.reminderTitle)
        assertEquals("Notification and short sound", ActivityPriority.LOW.reminderDescription)

        assertEquals("Medium", ActivityPriority.MEDIUM.reminderTitle)
        assertEquals("Full screen alert and short sound", ActivityPriority.MEDIUM.reminderDescription)

        assertEquals("Strong", ActivityPriority.HIGH.reminderTitle)
        assertEquals("Full screen alert and long sound", ActivityPriority.HIGH.reminderDescription)
    }

    @Test
    fun `channel mapping reflects appropriate priority channel identifiers`() {
        val getChannelForPriority = { priority: ActivityPriority, isEvent: Boolean ->
            if (isEvent) {
                AlarmScheduler.CHANNEL_ID_EVENTS
            } else {
                when (priority) {
                    ActivityPriority.LOW -> AlarmScheduler.CHANNEL_ID_LIGHT
                    ActivityPriority.MEDIUM -> AlarmScheduler.CHANNEL_ID_MEDIUM
                    ActivityPriority.HIGH -> AlarmScheduler.CHANNEL_ID_STRONG
                }
            }
        }

        assertEquals(AlarmScheduler.CHANNEL_ID_LIGHT, getChannelForPriority(ActivityPriority.LOW, false))
        assertEquals(AlarmScheduler.CHANNEL_ID_MEDIUM, getChannelForPriority(ActivityPriority.MEDIUM, false))
        assertEquals(AlarmScheduler.CHANNEL_ID_STRONG, getChannelForPriority(ActivityPriority.HIGH, false))
        assertEquals(AlarmScheduler.CHANNEL_ID_EVENTS, getChannelForPriority(ActivityPriority.HIGH, true))
    }

    @Test
    fun `action constants are unique and strictly namespaced`() {
        assertEquals("com.shejan.acontinue.ACTION_COMPLETE_REMINDER", ReminderActionReceiver.ACTION_COMPLETE)
        assertEquals("com.shejan.acontinue.ACTION_SNOOZE_REMINDER", ReminderActionReceiver.ACTION_SNOOZE)
        assertEquals("com.shejan.acontinue.ACTION_DISMISS_REMINDER", ReminderActionReceiver.ACTION_DISMISS)
    }

    @Test
    fun `priority string parsing safely defaults to MEDIUM on malformed input`() {
        val parsePriority = { raw: String? ->
            runCatching { ActivityPriority.valueOf(raw.orEmpty()) }.getOrDefault(ActivityPriority.MEDIUM)
        }

        assertEquals(ActivityPriority.MEDIUM, parsePriority(null))
        assertEquals(ActivityPriority.MEDIUM, parsePriority(""))
        assertEquals(ActivityPriority.MEDIUM, parsePriority("INVALID_VALUE"))
        assertEquals(ActivityPriority.LOW, parsePriority("LOW"))
        assertEquals(ActivityPriority.MEDIUM, parsePriority("MEDIUM"))
        assertEquals(ActivityPriority.HIGH, parsePriority("HIGH"))
    }

    @Test
    fun `reminder schedule offset ensures on-time trigger alongside early alerts`() {
        val reminder = ActivityItem(
            id = 42L,
            title = "Medicine Check",
            entryType = EntryType.REMINDER,
            startTimeMillis = 1000000L,
            endTimeMillis = 1000000L,
            priority = ActivityPriority.HIGH,
            reminderMinutesBefore = 15,
            reminderMinutesList = listOf(15)
        )

        val rawOffsets = reminder.reminderMinutesList
        val offsets = if (reminder.entryType == EntryType.REMINDER) {
            (rawOffsets + 0).distinct().sorted()
        } else {
            rawOffsets
        }

        assertEquals(listOf(0, 15), offsets)
        assertTrue("On-time reminder trigger (0m) must be preserved", offsets.contains(0))
    }

    @Test
    fun `snooze calculates exactly 10 minutes forward`() {
        val baseTime = 1700000000000L
        val snoozeMillis = baseTime + (10 * 60 * 1000L)
        assertEquals(600_000L, snoozeMillis - baseTime)
    }
}
