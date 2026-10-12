package com.shejan.acontinue.domain.model

data class ActivityTask(
    val id: Long = 0L,
    val activityId: Long?,
    val title: String,
    val isCompleted: Boolean = false,
    val dueDateMillis: Long? = null,
    val priority: ActivityPriority = ActivityPriority.MEDIUM,
    val createdAtMillis: Long = System.currentTimeMillis()
)
