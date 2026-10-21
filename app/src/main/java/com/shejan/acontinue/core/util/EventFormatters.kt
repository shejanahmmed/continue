package com.shejan.acontinue.core.util

import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.RecurrenceMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object EventFormatters {

    fun formatRecurrenceRule(activity: ActivityItem): String {
        return when (activity.recurrenceMode) {
            RecurrenceMode.NONE -> "Does not repeat"
            RecurrenceMode.MINUTELY -> "Repeats every minute"
            RecurrenceMode.HOURLY -> if (activity.recurrenceInterval == 1) "Repeats hourly" else "Repeats every ${activity.recurrenceInterval} hours"
            RecurrenceMode.DAILY -> if (activity.recurrenceInterval == 1) "Repeats daily" else "Repeats every ${activity.recurrenceInterval} days"
            RecurrenceMode.WEEKDAYS -> "Repeats on weekdays"
            RecurrenceMode.WEEKLY -> {
                if (activity.recurrenceDaysOfWeek.isEmpty()) {
                    if (activity.recurrenceInterval == 1) "Repeats weekly" else "Repeats every ${activity.recurrenceInterval} weeks"
                } else {
                    val dayNames = activity.recurrenceDaysOfWeek.mapNotNull { day ->
                        when (day) {
                            Calendar.SUNDAY -> "Sun"
                            Calendar.MONDAY -> "Mon"
                            Calendar.TUESDAY -> "Tue"
                            Calendar.WEDNESDAY -> "Wed"
                            Calendar.THURSDAY -> "Thu"
                            Calendar.FRIDAY -> "Fri"
                            Calendar.SATURDAY -> "Sat"
                            else -> null
                        }
                    }.joinToString(", ")
                    "Repeats weekly on $dayNames"
                }
            }
            RecurrenceMode.MONTHLY -> "Repeats monthly"
            RecurrenceMode.YEARLY -> "Repeats yearly"
        }
    }

    fun formatAlerts(activity: ActivityItem): String? {
        return if (activity.reminderMinutesList.isNotEmpty()) {
            activity.reminderMinutesList.joinToString(", ") { mins ->
                when (mins) {
                    0 -> "At start time"
                    10 -> "10 mins before"
                    15 -> "15 mins before"
                    30 -> "30 mins before"
                    60 -> "1 hour before"
                    1440 -> "1 day before"
                    else -> "$mins mins before"
                }
            }
        } else if (activity.reminderMinutesBefore >= 0) {
            when (activity.reminderMinutesBefore) {
                0 -> "At start time"
                60 -> "1 hour before"
                1440 -> "1 day before"
                else -> "${activity.reminderMinutesBefore} mins before"
            }
        } else {
            null
        }
    }

    fun formatDate(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        return dateFormat.format(Date(timeMillis))
    }

    fun formatTimeSpan(startMillis: Long, endMillis: Long, isAllDay: Boolean): String {
        if (isAllDay) return "All day"
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        return "${timeFormat.format(Date(startMillis))} – ${timeFormat.format(Date(endMillis))}"
    }
}
