package com.shejan.acontinue.domain.model

data class ActivityLink(
    val id: Long = 0L,
    val activityId: Long,
    val url: String,
    val title: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class ActivityAttachment(
    val id: Long = 0L,
    val activityId: Long,
    val fileName: String,
    val uriString: String,
    val mimeType: String,
    val sizeBytes: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis()
)
