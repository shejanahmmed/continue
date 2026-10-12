package com.shejan.acontinue.data.mapper

import com.shejan.acontinue.data.local.entity.ActivityEntity
import com.shejan.acontinue.data.local.entity.AttachmentEntity
import com.shejan.acontinue.data.local.entity.LinkEntity
import com.shejan.acontinue.data.local.entity.NoteEntity
import com.shejan.acontinue.data.local.entity.TaskEntity
import com.shejan.acontinue.domain.model.ActivityAttachment
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityLink
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode

fun ActivityEntity.toDomain(): ActivityItem {
    return ActivityItem(
        id = id,
        title = title,
        description = description,
        entryType = runCatching { EntryType.valueOf(entryType) }.getOrDefault(EntryType.ACTIVITY),
        category = runCatching { ActivityCategory.valueOf(category) }.getOrDefault(ActivityCategory.OTHER),
        isAllDay = isAllDay,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        location = location,
        priority = runCatching { ActivityPriority.valueOf(priority) }.getOrDefault(ActivityPriority.MEDIUM),
        reminderMinutesBefore = reminderMinutesBefore,
        recurrenceMode = runCatching { RecurrenceMode.valueOf(recurrenceMode) }.getOrDefault(RecurrenceMode.NONE),
        isCompleted = isCompleted,
        createdAtMillis = createdAtMillis
    )
}

fun ActivityItem.toEntity(): ActivityEntity {
    return ActivityEntity(
        id = id,
        title = title,
        description = description,
        entryType = entryType.name,
        category = category.name,
        isAllDay = isAllDay,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        location = location,
        priority = priority.name,
        reminderMinutesBefore = reminderMinutesBefore,
        recurrenceMode = recurrenceMode.name,
        isCompleted = isCompleted,
        createdAtMillis = createdAtMillis
    )
}

fun TaskEntity.toDomain(): ActivityTask {
    return ActivityTask(
        id = id,
        activityId = activityId,
        title = title,
        isCompleted = isCompleted,
        dueDateMillis = dueDateMillis,
        priority = runCatching { ActivityPriority.valueOf(priority) }.getOrDefault(ActivityPriority.MEDIUM),
        createdAtMillis = createdAtMillis
    )
}

fun ActivityTask.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        activityId = activityId,
        title = title,
        isCompleted = isCompleted,
        dueDateMillis = dueDateMillis,
        priority = priority.name,
        createdAtMillis = createdAtMillis
    )
}

fun NoteEntity.toDomain(): ActivityNote {
    return ActivityNote(
        id = id,
        activityId = activityId,
        title = title,
        content = content,
        updatedAtMillis = updatedAtMillis
    )
}

fun ActivityNote.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        activityId = activityId,
        title = title,
        content = content,
        updatedAtMillis = updatedAtMillis
    )
}

fun LinkEntity.toDomain(): ActivityLink {
    return ActivityLink(
        id = id,
        activityId = activityId,
        url = url,
        title = title,
        createdAtMillis = createdAtMillis
    )
}

fun ActivityLink.toEntity(): LinkEntity {
    return LinkEntity(
        id = id,
        activityId = activityId,
        url = url,
        title = title,
        createdAtMillis = createdAtMillis
    )
}

fun AttachmentEntity.toDomain(): ActivityAttachment {
    return ActivityAttachment(
        id = id,
        activityId = activityId,
        fileName = fileName,
        uriString = uriString,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        createdAtMillis = createdAtMillis
    )
}

fun ActivityAttachment.toEntity(): AttachmentEntity {
    return AttachmentEntity(
        id = id,
        activityId = activityId,
        fileName = fileName,
        uriString = uriString,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        createdAtMillis = createdAtMillis
    )
}
