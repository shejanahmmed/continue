package com.shejan.acontinue.domain.util

import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.MonthlyRepeatType
import com.shejan.acontinue.domain.model.RecurrenceEndType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.YearlyRepeatType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RecurrenceEvaluatorTest {

    private fun createBaseActivity(
        startCal: Calendar,
        durationMinutes: Int = 60,
        recurrenceMode: RecurrenceMode = RecurrenceMode.NONE,
        interval: Int = 1
    ): ActivityItem {
        val startMillis = startCal.timeInMillis
        val endMillis = startMillis + (durationMinutes * 60 * 1000L)
        return ActivityItem(
            id = 1L,
            title = "Test Activity",
            entryType = EntryType.ACTIVITY,
            category = ActivityCategory.OTHER,
            startTimeMillis = startMillis,
            endTimeMillis = endMillis,
            priority = ActivityPriority.MEDIUM,
            recurrenceMode = recurrenceMode,
            recurrenceInterval = interval
        )
    }

    @Test
    fun `non-recurring activity only occurs on its base day`() {
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 10, 0, 0)
        }
        val activity = createBaseActivity(startCal, recurrenceMode = RecurrenceMode.NONE)

        val sameDay = startCal.timeInMillis
        val nextDay = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 11, 10, 0, 0)
        }.timeInMillis

        assertTrue(RecurrenceEvaluator.occursOnDay(activity, sameDay))
        assertFalse(RecurrenceEvaluator.occursOnDay(activity, nextDay))
    }

    @Test
    fun `daily recurring activity occurs every day`() {
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 10, 0, 0)
        }
        val activity = createBaseActivity(startCal, recurrenceMode = RecurrenceMode.DAILY, interval = 1)

        val day3 = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 13, 0, 0, 0)
        }.timeInMillis

        val dayBefore = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 9, 0, 0, 0)
        }.timeInMillis

        assertTrue(RecurrenceEvaluator.occursOnDay(activity, day3))
        assertFalse(RecurrenceEvaluator.occursOnDay(activity, dayBefore))
    }

    @Test
    fun `weekdays recurring activity excludes weekends`() {
        // Oct 16, 2026 is Friday
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 9, 0, 0)
        }
        val activity = createBaseActivity(startCal, recurrenceMode = RecurrenceMode.WEEKDAYS)

        val friday = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 16, 12, 0, 0) }.timeInMillis
        val saturday = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 17, 12, 0, 0) }.timeInMillis
        val sunday = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 18, 12, 0, 0) }.timeInMillis
        val monday = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 19, 12, 0, 0) }.timeInMillis

        assertTrue(RecurrenceEvaluator.occursOnDay(activity, friday))
        assertFalse(RecurrenceEvaluator.occursOnDay(activity, saturday))
        assertFalse(RecurrenceEvaluator.occursOnDay(activity, sunday))
        assertTrue(RecurrenceEvaluator.occursOnDay(activity, monday))
    }

    @Test
    fun `weekly recurring activity with custom days occurs on selected days`() {
        // Mon Oct 12, 2026
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 12, 10, 0, 0)
        }
        // Repeat on Monday (2) and Wednesday (4)
        val activity = createBaseActivity(startCal, recurrenceMode = RecurrenceMode.WEEKLY).copy(
            recurrenceDaysOfWeek = listOf(Calendar.MONDAY, Calendar.WEDNESDAY)
        )

        val monday = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 12, 0, 0, 0) }.timeInMillis
        val tuesday = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 13, 0, 0, 0) }.timeInMillis
        val wednesday = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 14, 0, 0, 0) }.timeInMillis

        assertTrue(RecurrenceEvaluator.occursOnDay(activity, monday))
        assertFalse(RecurrenceEvaluator.occursOnDay(activity, tuesday))
        assertTrue(RecurrenceEvaluator.occursOnDay(activity, wednesday))
    }

    @Test
    fun `recurrence with UNTIL_DATE ceases after end date`() {
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 10, 0, 0)
        }
        val endCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 23, 59, 59)
        }
        val activity = createBaseActivity(startCal, recurrenceMode = RecurrenceMode.DAILY).copy(
            recurrenceEndType = RecurrenceEndType.UNTIL_DATE,
            recurrenceEndDateMillis = endCal.timeInMillis
        )

        val inside = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 14, 0, 0, 0) }.timeInMillis
        val after = Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 16, 0, 0, 0) }.timeInMillis

        assertTrue(RecurrenceEvaluator.occursOnDay(activity, inside))
        assertFalse(RecurrenceEvaluator.occursOnDay(activity, after))
    }

    @Test
    fun `expandOccurrencesForRange projects accurate timestamps onto target days`() {
        // Starts at 14:00 (2:00 PM), duration 90 minutes
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 14, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val activity = createBaseActivity(startCal, durationMinutes = 90, recurrenceMode = RecurrenceMode.DAILY)

        // Range: Oct 12 00:00 to Oct 13 23:59 (2 days)
        val rangeStart = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 12, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val rangeEnd = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 13, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val occurrences = RecurrenceEvaluator.expandOccurrencesForRange(activity, rangeStart, rangeEnd)
        assertEquals(2, occurrences.size)

        val firstOcc = occurrences[0]
        val checkCal = Calendar.getInstance().apply { timeInMillis = firstOcc.startTimeMillis }
        assertEquals(12, checkCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(14, checkCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, checkCal.get(Calendar.MINUTE))
        assertEquals(90 * 60 * 1000L, firstOcc.endTimeMillis - firstOcc.startTimeMillis)
    }

    @Test
    fun `getNextOccurrenceStart returns correct upcoming timestamp`() {
        val startCal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 10, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val activity = createBaseActivity(startCal, recurrenceMode = RecurrenceMode.DAILY)

        // Query after Oct 12 at 10:00 AM -> next occurrence should be Oct 13 at 9:00 AM
        val afterTime = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 12, 10, 0, 0)
        }.timeInMillis

        val nextStart = RecurrenceEvaluator.getNextOccurrenceStart(activity, afterTime)
        assertNotNull(nextStart)

        val nextCal = Calendar.getInstance().apply { timeInMillis = nextStart!! }
        assertEquals(13, nextCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, nextCal.get(Calendar.HOUR_OF_DAY))
    }
}
