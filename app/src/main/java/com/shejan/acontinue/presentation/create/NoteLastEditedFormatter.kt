package com.shejan.acontinue.presentation.create

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formats timestamps for note modification headers.
 */
object NoteLastEditedFormatter {

    /**
     * Formats the given timestamp into a user-facing string:
     * - Same day (today): "Last edited 1:46 AM (Today)"
     * - 1 day ago: "Last edited 1:46 AM (Yesterday)"
     * - Earlier this year: "Last edited 1:46 AM (Oct 14)"
     * - Different year: "Last edited 1:46 AM (Oct 14, 2025)"
     */
    fun format(timestamp: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val targetCal = Calendar.getInstance().apply { timeInMillis = timestamp }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date(timestamp))

        val isSameYear = nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)
        val isSameDay = isSameYear && nowCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)

        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val isYesterday = yesterdayCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
                yesterdayCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)

        return when {
            isSameDay -> "Last edited $formattedTime (Today)"
            isYesterday -> "Last edited $formattedTime (Yesterday)"
            isSameYear -> {
                val monthDayFormat = SimpleDateFormat("MMM d", Locale.getDefault())
                "Last edited $formattedTime (${monthDayFormat.format(Date(timestamp))})"
            }
            else -> {
                val fullDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                "Last edited $formattedTime (${fullDateFormat.format(Date(timestamp))})"
            }
        }
    }
}
