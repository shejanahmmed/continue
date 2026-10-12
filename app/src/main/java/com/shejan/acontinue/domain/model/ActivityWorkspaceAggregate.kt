package com.shejan.acontinue.domain.model

data class ActivityWorkspaceAggregate(
    val activity: ActivityItem,
    val tasks: List<ActivityTask> = emptyList(),
    val notes: List<ActivityNote> = emptyList(),
    val links: List<ActivityLink> = emptyList(),
    val attachments: List<ActivityAttachment> = emptyList()
)
