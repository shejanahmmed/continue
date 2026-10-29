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
import com.shejan.acontinue.domain.model.MonthlyRepeatType
import com.shejan.acontinue.domain.model.RecurrenceEndType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.YearlyRepeatType

fun ActivityEntity.toDomain(): ActivityItem {
    val parsedList = if (reminderMinutesCsv.isNotBlank()) {
        reminderMinutesCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
    } else if (reminderMinutesBefore >= 0) {
        listOf(reminderMinutesBefore)
    } else {
        emptyList()
    }
    val parsedDaysOfWeek = if (recurrenceDaysOfWeekCsv.isNotBlank()) {
        recurrenceDaysOfWeekCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
    } else {
        emptyList()
    }
    val parsedMonthlyDays = if (monthlySelectedDaysCsv.isNotBlank()) {
        monthlySelectedDaysCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
    } else {
        emptyList()
    }
    val parsedYearlyMonths = if (yearlySelectedMonthsCsv.isNotBlank()) {
        yearlySelectedMonthsCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
    } else {
        emptyList()
    }

    val parsedEntryType = when (entryType.trim().uppercase()) {
        "REMINDER" -> EntryType.REMINDER
        "NOTE", "NOTES" -> EntryType.NOTE
        else -> EntryType.ACTIVITY
    }

    return ActivityItem(
        id = id,
        title = title,
        description = description,
        entryType = parsedEntryType,
        category = runCatching { ActivityCategory.valueOf(category) }.getOrDefault(ActivityCategory.OTHER),
        isAllDay = isAllDay,
        startTimeMillis = startTimeMillis,
        endTimeMillis = if (parsedEntryType == EntryType.REMINDER) startTimeMillis else endTimeMillis,
        location = location,
        priority = runCatching { ActivityPriority.valueOf(priority) }.getOrDefault(ActivityPriority.MEDIUM),
        reminderMinutesBefore = reminderMinutesBefore,
        reminderMinutesList = parsedList,
        recurrenceMode = runCatching { RecurrenceMode.valueOf(recurrenceMode) }.getOrDefault(RecurrenceMode.NONE),
        recurrenceInterval = recurrenceInterval,
        recurrenceDaysOfWeek = parsedDaysOfWeek,
        monthlyRepeatType = runCatching { MonthlyRepeatType.valueOf(monthlyRepeatType) }.getOrDefault(MonthlyRepeatType.SAME_DAY_OF_MONTH),
        monthlySelectedDays = parsedMonthlyDays,
        yearlyRepeatType = runCatching { YearlyRepeatType.valueOf(yearlyRepeatType) }.getOrDefault(YearlyRepeatType.SAME_DATE),
        yearlySelectedMonths = parsedYearlyMonths,
        recurrenceEndType = runCatching { RecurrenceEndType.valueOf(recurrenceEndType) }.getOrDefault(RecurrenceEndType.FOREVER),
        recurrenceEndCount = recurrenceEndCount,
        recurrenceEndDateMillis = recurrenceEndDateMillis,
        isCompleted = if (parsedEntryType == EntryType.REMINDER) isCompleted else false,
        isSkipped = if (parsedEntryType == EntryType.REMINDER) isSkipped else false,
        reminderCategory = reminderCategory,
        reminderCategoryColorHex = reminderCategoryColorHex,
        reminderCategoryIcon = reminderCategoryIcon,
        createdAtMillis = createdAtMillis
    )
}

fun ActivityItem.toEntity(): ActivityEntity {
    val csv = reminderMinutesList.joinToString(",")
    return ActivityEntity(
        id = id,
        title = title,
        description = description,
        entryType = entryType.name,
        category = category.name,
        isAllDay = isAllDay,
        startTimeMillis = startTimeMillis,
        endTimeMillis = if (entryType == EntryType.REMINDER) startTimeMillis else endTimeMillis,
        location = location,
        priority = priority.name,
        reminderMinutesBefore = reminderMinutesList.firstOrNull() ?: reminderMinutesBefore,
        reminderMinutesCsv = csv,
        recurrenceMode = recurrenceMode.name,
        recurrenceInterval = recurrenceInterval,
        recurrenceDaysOfWeekCsv = recurrenceDaysOfWeek.joinToString(","),
        monthlyRepeatType = monthlyRepeatType.name,
        monthlySelectedDaysCsv = monthlySelectedDays.joinToString(","),
        yearlyRepeatType = yearlyRepeatType.name,
        yearlySelectedMonthsCsv = yearlySelectedMonths.joinToString(","),
        recurrenceEndType = recurrenceEndType.name,
        recurrenceEndCount = recurrenceEndCount,
        recurrenceEndDateMillis = recurrenceEndDateMillis,
        isCompleted = if (entryType == EntryType.REMINDER) isCompleted else false,
        isSkipped = if (entryType == EntryType.REMINDER) isSkipped else false,
        reminderCategory = reminderCategory,
        reminderCategoryColorHex = reminderCategoryColorHex,
        reminderCategoryIcon = reminderCategoryIcon,
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
    val parsedLabels = if (labels.isNotBlank()) {
        labels.split(",").map { it.trim() }.filter { it.isNotBlank() }
    } else {
        emptyList()
    }
    return ActivityNote(
        id = id,
        activityId = activityId,
        title = title,
        content = content,
        colorHex = if (colorHex != 0L && colorHex != 0xFF121212L) colorHex else 0xFF000000L,
        labels = parsedLabels,
        updatedAtMillis = updatedAtMillis
    )
}

fun ActivityNote.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        activityId = activityId,
        title = title,
        content = content,
        colorHex = colorHex,
        labels = labels.map { it.trim() }.filter { it.isNotBlank() }.joinToString(","),
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
