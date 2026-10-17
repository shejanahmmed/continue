package com.shejan.acontinue.core.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class EventNotificationFormatterTest {

    private val testLocale = Locale.US

    @Test
    fun `formatEventDateTime returns empty string when startTimeMillis is non-positive`() {
        val result = EventNotificationFormatter.formatEventDateTime(
            startTimeMillis = 0L,
            endTimeMillis = 1000L,
            isAllDay = false,
            locale = testLocale
        )
        assertEquals("", result)
    }

    @Test
    fun `formatEventDateTime formats same day event with start and end times`() {
        val calStart = Calendar.getInstance(testLocale).apply {
            set(2026, Calendar.OCTOBER, 17, 10, 0, 0)
        }
        val calEnd = Calendar.getInstance(testLocale).apply {
            set(2026, Calendar.OCTOBER, 17, 11, 30, 0)
        }

        val result = EventNotificationFormatter.formatEventDateTime(
            startTimeMillis = calStart.timeInMillis,
            endTimeMillis = calEnd.timeInMillis,
            isAllDay = false,
            locale = testLocale
        )

        assertEquals("Sat, 17 Oct • 10:00 AM – 11:30 AM", result)
    }

    @Test
    fun `formatEventDateTime formats same day all-day event correctly`() {
        val calStart = Calendar.getInstance(testLocale).apply {
            set(2026, Calendar.OCTOBER, 17, 0, 0, 0)
        }
        val calEnd = Calendar.getInstance(testLocale).apply {
            set(2026, Calendar.OCTOBER, 17, 23, 59, 59)
        }

        val result = EventNotificationFormatter.formatEventDateTime(
            startTimeMillis = calStart.timeInMillis,
            endTimeMillis = calEnd.timeInMillis,
            isAllDay = true,
            locale = testLocale
        )

        assertEquals("Sat, 17 Oct • All day", result)
    }

    @Test
    fun `formatEventDateTime formats multi-day event correctly`() {
        val calStart = Calendar.getInstance(testLocale).apply {
            set(2026, Calendar.OCTOBER, 17, 10, 0, 0)
        }
        val calEnd = Calendar.getInstance(testLocale).apply {
            set(2026, Calendar.OCTOBER, 18, 12, 0, 0)
        }

        val result = EventNotificationFormatter.formatEventDateTime(
            startTimeMillis = calStart.timeInMillis,
            endTimeMillis = calEnd.timeInMillis,
            isAllDay = false,
            locale = testLocale
        )

        assertEquals("17 Oct, 10:00 AM – 18 Oct, 12:00 PM", result)
    }

    @Test
    fun `formatEventDateTime formats event when endTime is missing or before start`() {
        val calStart = Calendar.getInstance(testLocale).apply {
            set(2026, Calendar.OCTOBER, 17, 10, 0, 0)
        }

        val result = EventNotificationFormatter.formatEventDateTime(
            startTimeMillis = calStart.timeInMillis,
            endTimeMillis = 0L,
            isAllDay = false,
            locale = testLocale
        )

        assertEquals("Sat, 17 Oct • 10:00 AM", result)
    }

    @Test
    fun `formatSingleLineLocation collapses multiline input and whitespace`() {
        val input = "   Room 402 \n Second Floor \r\n Main Building   "
        val result = EventNotificationFormatter.formatSingleLineLocation(input)
        assertEquals("Room 402 Second Floor Main Building", result)
    }

    @Test
    fun `formatSingleLineLocation truncates excessively long URLs or strings`() {
        val longUrl = "https://meet.google.com/abc-defg-hij?authuser=0&hs=122&pli=1&extra_parameter_that_makes_this_string_very_long_indeed"
        val result = EventNotificationFormatter.formatSingleLineLocation(longUrl)
        assertTrue(result.length <= 70)
        assertTrue(result.endsWith("..."))
    }
}
