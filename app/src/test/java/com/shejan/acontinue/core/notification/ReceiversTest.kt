package com.shejan.acontinue.core.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class ReceiversTest {

    @Test
    fun `verify extra activity id constant is consistent for event deep link`() {
        assertEquals("extra_activity_id", AlarmScheduler.EXTRA_ACTIVITY_ID)
    }

    @Test
    fun `verify receiver action constants are strictly namespaced`() {
        assertEquals("com.shejan.acontinue.ACTION_COMPLETE_REMINDER", ReminderActionReceiver.ACTION_COMPLETE)
        assertEquals("com.shejan.acontinue.ACTION_SNOOZE_REMINDER", ReminderActionReceiver.ACTION_SNOOZE)
        assertEquals("com.shejan.acontinue.ACTION_DISMISS_REMINDER", ReminderActionReceiver.ACTION_DISMISS)
    }
}
