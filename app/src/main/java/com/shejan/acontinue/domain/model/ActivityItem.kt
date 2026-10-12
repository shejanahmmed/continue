package com.shejan.acontinue.domain.model

data class ActivityItem(
    val id: Long = 0L,
    val title: String,
    val description: String = "",
    val entryType: EntryType = EntryType.ACTIVITY,
    val category: ActivityCategory = ActivityCategory.OTHER,
    val isAllDay: Boolean = false,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val location: String = "",
    val priority: ActivityPriority = ActivityPriority.MEDIUM,
    val reminderMinutesBefore: Int = 15,
    val recurrenceMode: RecurrenceMode = RecurrenceMode.NONE,
    val isCompleted: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
