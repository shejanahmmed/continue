package com.shejan.acontinue.presentation.create

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class NoteLastEditedFormatterTest {

    @Test
    fun testSameDayFormattingContainsTime() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 1, 46)
        }.timeInMillis

        val formatted = NoteLastEditedFormatter.format(timestamp = now, nowMillis = now)
        assertTrue("Formatted text should start with 'Last edited'", formatted.startsWith("Last edited"))
        assertTrue("Formatted text should contain time", formatted.contains("1:46") || formatted.contains("01:46"))
    }

    @Test
    fun testYesterdayFormatting() {
        val today = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 12, 0)
        }.timeInMillis

        val yesterday = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 15, 14, 30)
        }.timeInMillis

        val formatted = NoteLastEditedFormatter.format(timestamp = yesterday, nowMillis = today)
        assertTrue("Formatted text should mention yesterday", formatted.startsWith("Last edited yesterday"))
    }

    @Test
    fun testPreviousMonthFormatting() {
        val now = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 12, 0)
        }.timeInMillis

        val pastDate = Calendar.getInstance().apply {
            set(2026, Calendar.AUGUST, 5, 9, 15)
        }.timeInMillis

        val formatted = NoteLastEditedFormatter.format(timestamp = pastDate, nowMillis = now)
        assertTrue("Formatted text should contain month and day", formatted.contains("Aug") && formatted.contains("5"))
    }
}
