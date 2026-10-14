package com.shejan.acontinue.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val description: String,
    val entryType: String,
    val category: String,
    val isAllDay: Boolean,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val location: String,
    val priority: String,
    val reminderMinutesBefore: Int,
    val reminderMinutesCsv: String = "",
    val recurrenceMode: String,
    val recurrenceInterval: Int = 1,
    val recurrenceDaysOfWeekCsv: String = "",
    val monthlyRepeatType: String = "SAME_DAY_OF_MONTH",
    val monthlySelectedDaysCsv: String = "",
    val yearlyRepeatType: String = "SAME_DATE",
    val yearlySelectedMonthsCsv: String = "",
    val recurrenceEndType: String = "FOREVER",
    val recurrenceEndCount: Int = 10,
    val recurrenceEndDateMillis: Long = 0L,
    val isCompleted: Boolean,
    val reminderCategory: String = "My reminders",
    val reminderCategoryColorHex: Long = 0xFF9D7BFC,
    val reminderCategoryIcon: String = "bell",
    val createdAtMillis: Long
)
