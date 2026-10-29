package com.shejan.acontinue.domain.model

data class ActivityNote(
    val id: Long = 0L,
    val activityId: Long,
    val title: String,
    val content: String,
    val colorHex: Long = 0xFF000000L,
    val labels: List<String> = emptyList(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)
