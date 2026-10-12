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
    val recurrenceMode: String,
    val isCompleted: Boolean,
    val createdAtMillis: Long
)
