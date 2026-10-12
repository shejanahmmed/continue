package com.shejan.acontinue.domain.model

data class ActivityNote(
    val id: Long = 0L,
    val activityId: Long,
    val title: String,
    val content: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
