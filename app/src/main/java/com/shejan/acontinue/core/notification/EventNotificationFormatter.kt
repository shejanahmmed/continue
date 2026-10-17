package com.shejan.acontinue.core.notification

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object EventNotificationFormatter {

    fun formatEventDateTime(
        startTimeMillis: Long,
        endTimeMillis: Long,
        isAllDay: Boolean,
        locale: Locale = Locale.getDefault()
    ): String {
        if (startTimeMillis <= 0L) return ""

        val startDate = Date(startTimeMillis)
        val dateFormat = SimpleDateFormat("EEE, d MMM", locale)
        val timeFormat = SimpleDateFormat("h:mm a", locale)

        if (isAllDay) {
            val startCal = Calendar.getInstance().apply { timeInMillis = startTimeMillis }
            val endCal = Calendar.getInstance().apply { timeInMillis = endTimeMillis }

            val isSameDay = startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                    startCal.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR)

            return if (isSameDay || endTimeMillis <= startTimeMillis) {
                "${dateFormat.format(startDate)} • All day"
            } else {
                val endDate = Date(endTimeMillis)
                "${dateFormat.format(startDate)} – ${dateFormat.format(endDate)} • All day"
            }
        }

        val formattedStartTime = timeFormat.format(startDate)

        if (endTimeMillis <= startTimeMillis) {
            return "${dateFormat.format(startDate)} • $formattedStartTime"
        }

        val startCal = Calendar.getInstance().apply { timeInMillis = startTimeMillis }
        val endCal = Calendar.getInstance().apply { timeInMillis = endTimeMillis }

        val isSameDay = startCal.get(Calendar.YEAR) == endCal.get(Calendar.YEAR) &&
                startCal.get(Calendar.DAY_OF_YEAR) == endCal.get(Calendar.DAY_OF_YEAR)

        val endDate = Date(endTimeMillis)
        return if (isSameDay) {
            val formattedEndTime = timeFormat.format(endDate)
            "${dateFormat.format(startDate)} • $formattedStartTime – $formattedEndTime"
        } else {
            val fullDateTimeFormat = SimpleDateFormat("d MMM, h:mm a", locale)
            "${fullDateTimeFormat.format(startDate)} – ${fullDateTimeFormat.format(endDate)}"
        }
    }

    fun formatSingleLineLocation(location: String): String {
        val singleLine = location.replace("\r", " ").replace("\n", " ").trim()
        if (singleLine.isBlank()) return ""
        val sanitized = singleLine.replace(Regex("\\s+"), " ")
        return if (sanitized.length > 70) {
            sanitized.take(67) + "..."
        } else {
            sanitized
        }
    }
}
