package com.shejan.acontinue.presentation.create

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class NoteLastEditedFormatterTest {

    @Test
    fun testSameDayFormattingContainsTimeAndToday() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 1, 46)
        }.timeInMillis

        val formatted = NoteLastEditedFormatter.format(timestamp = now, nowMillis = now)
        assertTrue("Formatted text should start with 'Last edited'", formatted.startsWith("Last edited"))
        assertTrue("Formatted text should contain time", formatted.contains("1:46") || formatted.contains("01:46"))
        assertTrue("Formatted text should contain '(Today)' on the right side", formatted.endsWith("(Today)"))
    }

    @Test
    fun testYesterdayFormattingContainsTimeAndYesterday() {
        val today = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 12, 0)
        }.timeInMillis

        val yesterday = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 14, 30)
        }.timeInMillis

        val formatted = NoteLastEditedFormatter.format(timestamp = yesterday, nowMillis = today)
        assertTrue("Formatted text should start with 'Last edited'", formatted.startsWith("Last edited"))
        assertTrue("Formatted text should contain time", formatted.contains("2:30") || formatted.contains("14:30") || formatted.contains("02:30"))
        assertTrue("Formatted text should end with '(Yesterday)'", formatted.endsWith("(Yesterday)"))
    }

    @Test
    fun testEarlierDateFormattingContainsTimeAndDate() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 12, 0)
        }.timeInMillis

        val pastDate = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 5, 9, 15)
        }.timeInMillis

        val formatted = NoteLastEditedFormatter.format(timestamp = pastDate, nowMillis = now)
        assertTrue("Formatted text should start with 'Last edited'", formatted.startsWith("Last edited"))
        assertTrue("Formatted text should contain time", formatted.contains("9:15") || formatted.contains("09:15"))
        assertTrue("Formatted text should contain month and day in parentheses", formatted.endsWith("(Aug 5)"))
    }
}
