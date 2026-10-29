package com.shejan.acontinue

import com.shejan.acontinue.domain.model.ActivityAttachment
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.ActivityLink
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.LinkRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import com.shejan.acontinue.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeActivityRepository : ActivityRepository {
    val activitiesFlow = MutableStateFlow<List<ActivityItem>>(emptyList())
    private var nextId = 1L

    override fun getActivitiesForDateRange(startMillis: Long, endMillis: Long): Flow<List<ActivityItem>> {
        return activitiesFlow.map { list ->
            list.flatMap { item ->
                com.shejan.acontinue.domain.util.RecurrenceEvaluator.expandOccurrencesForRange(item, startMillis, endMillis)
            }.sortedBy { it.startTimeMillis }
        }
    }

    override fun getActivityById(id: Long): Flow<ActivityItem?> {
        return activitiesFlow.map { list -> list.firstOrNull { it.id == id } }
    }

    override fun getAllActivities(): Flow<List<ActivityItem>> = activitiesFlow

    override suspend fun insertActivity(activity: ActivityItem): Long {
        val id = if (activity.id > 0) activity.id else nextId++
        val saved = activity.copy(id = id)
        activitiesFlow.value = activitiesFlow.value + saved
        return id
    }

    override suspend fun updateActivity(activity: ActivityItem) {
        activitiesFlow.value = activitiesFlow.value.map { if (it.id == activity.id) activity else it }
    }

    override suspend fun deleteActivity(id: Long) {
        activitiesFlow.value = activitiesFlow.value.filterNot { it.id == id }
    }

    override suspend fun toggleActivityCompletion(id: Long, isCompleted: Boolean) {
        activitiesFlow.value = activitiesFlow.value.map {
            if (it.id == id && it.entryType == EntryType.REMINDER) {
                it.copy(isCompleted = isCompleted, isSkipped = if (isCompleted) false else it.isSkipped)
            } else it
        }
    }

    override suspend fun toggleActivitySkipped(id: Long, isSkipped: Boolean) {
        activitiesFlow.value = activitiesFlow.value.map {
            if (it.id == id && it.entryType == EntryType.REMINDER) it.copy(isSkipped = isSkipped) else it
        }
    }

    override fun searchActivities(query: String): Flow<List<ActivityItem>> {
        return activitiesFlow.map { list ->
            list.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun updateReminderCategoryDetails(categoryName: String, colorHex: Long, icon: String) {
        activitiesFlow.value = activitiesFlow.value.map {
            if (it.reminderCategory == categoryName) it.copy(reminderCategoryColorHex = colorHex, reminderCategoryIcon = icon) else it
        }
    }

    override suspend fun updateActivityReminderCategory(activityId: Long, newCategory: String, newColorHex: Long, newIcon: String) {
        activitiesFlow.value = activitiesFlow.value.map {
            if (it.id == activityId) it.copy(reminderCategory = newCategory, reminderCategoryColorHex = newColorHex, reminderCategoryIcon = newIcon) else it
        }
    }

    override suspend fun renameReminderCategoryAcrossActivities(oldName: String, newName: String, newColorHex: Long, newIcon: String) {
        activitiesFlow.value = activitiesFlow.value.map {
            if (it.reminderCategory == oldName) it.copy(reminderCategory = newName, reminderCategoryColorHex = newColorHex, reminderCategoryIcon = newIcon) else it
        }
    }

    override suspend fun resetActivitiesToDefaultCategory(deletedName: String, fallbackName: String, fallbackColorHex: Long, fallbackIcon: String) {
        activitiesFlow.value = activitiesFlow.value.map {
            if (it.reminderCategory == deletedName) it.copy(reminderCategory = fallbackName, reminderCategoryColorHex = fallbackColorHex, reminderCategoryIcon = fallbackIcon) else it
        }
    }
}

class FakeTaskRepository : TaskRepository {
    val tasksFlow = MutableStateFlow<List<ActivityTask>>(emptyList())
    private var nextId = 1L

    override fun getAllTasks(): Flow<List<ActivityTask>> = tasksFlow

    override fun getTasksForActivity(activityId: Long): Flow<List<ActivityTask>> {
        return tasksFlow.map { list -> list.filter { it.activityId == activityId } }
    }

    override fun getPendingTasks(): Flow<List<ActivityTask>> {
        return tasksFlow.map { list -> list.filterNot { it.isCompleted } }
    }

    override suspend fun insertTask(task: ActivityTask): Long {
        val id = if (task.id > 0) task.id else nextId++
        val saved = task.copy(id = id)
        tasksFlow.value = tasksFlow.value + saved
        return id
    }

    override suspend fun updateTask(task: ActivityTask) {
        tasksFlow.value = tasksFlow.value.map { if (it.id == task.id) task else it }
    }

    override suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        tasksFlow.value = tasksFlow.value.map {
            if (it.id == taskId) it.copy(isCompleted = isCompleted) else it
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        tasksFlow.value = tasksFlow.value.filterNot { it.id == taskId }
    }
}

class FakeNoteRepository : NoteRepository {
    val notesFlow = MutableStateFlow<List<ActivityNote>>(emptyList())
    private var nextId = 1L

    override fun getNotesForActivity(activityId: Long): Flow<List<ActivityNote>> {
        return notesFlow.map { list -> list.filter { it.activityId == activityId } }
    }

    override fun getNoteById(noteId: Long): Flow<ActivityNote?> {
        return notesFlow.map { list -> list.firstOrNull { it.id == noteId } }
    }

    override fun searchNotes(query: String): Flow<List<ActivityNote>> {
        return notesFlow.map { list ->
            list.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
        }
    }

    override suspend fun insertNote(note: ActivityNote): Long {
        val id = if (note.id > 0) note.id else nextId++
        val saved = note.copy(id = id)
        notesFlow.value = notesFlow.value + saved
        return id
    }

    override suspend fun updateNote(note: ActivityNote) {
        notesFlow.value = notesFlow.value.map { if (it.id == note.id) note else it }
    }

    override suspend fun deleteNote(noteId: Long) {
        notesFlow.value = notesFlow.value.filterNot { it.id == noteId }
    }
}

class FakeLinkRepository : LinkRepository {
    val linksFlow = MutableStateFlow<List<ActivityLink>>(emptyList())
    val attachmentsFlow = MutableStateFlow<List<ActivityAttachment>>(emptyList())
    private var nextLinkId = 1L
    private var nextAttachmentId = 1L

    override fun getLinksForActivity(activityId: Long): Flow<List<ActivityLink>> {
        return linksFlow.map { list -> list.filter { it.activityId == activityId } }
    }

    override suspend fun insertLink(link: ActivityLink): Long {
        val id = if (link.id > 0) link.id else nextLinkId++
        val saved = link.copy(id = id)
        linksFlow.value = linksFlow.value + saved
        return id
    }

    override suspend fun deleteLink(linkId: Long) {
        linksFlow.value = linksFlow.value.filterNot { it.id == linkId }
    }

    override fun getAttachmentsForActivity(activityId: Long): Flow<List<ActivityAttachment>> {
        return attachmentsFlow.map { list -> list.filter { it.activityId == activityId } }
    }

    override suspend fun insertAttachment(attachment: ActivityAttachment): Long {
        val id = if (attachment.id > 0) attachment.id else nextAttachmentId++
        val saved = attachment.copy(id = id)
        attachmentsFlow.value = attachmentsFlow.value + saved
        return id
    }

    override suspend fun deleteAttachment(attachmentId: Long) {
        attachmentsFlow.value = attachmentsFlow.value.filterNot { it.id == attachmentId }
    }
}

class FakeScheduler : com.shejan.acontinue.core.notification.ActivityScheduler {
    val scheduledActivities = mutableListOf<ActivityItem>()
    val cancelledIds = mutableListOf<Long>()

    override fun schedule(activity: ActivityItem) {
        scheduledActivities.add(activity)
    }

    override fun cancel(activityId: Long) {
        cancelledIds.add(activityId)
    }
}

class FakeSettingsRepository : com.shejan.acontinue.domain.repository.SettingsRepository {
    val settingsFlow = MutableStateFlow(com.shejan.acontinue.domain.model.UserSettings())

    override fun getUserSettings(): Flow<com.shejan.acontinue.domain.model.UserSettings> = settingsFlow

    override fun getCurrentSettings(): com.shejan.acontinue.domain.model.UserSettings = settingsFlow.value

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(isOnboardingCompleted = completed)
    }

    override suspend fun setThemeMode(mode: com.shejan.acontinue.domain.model.ThemeMode) {
        settingsFlow.value = settingsFlow.value.copy(themeMode = mode)
    }

    override suspend fun setAccentPalette(palette: com.shejan.acontinue.domain.model.AccentPalette) {
        settingsFlow.value = settingsFlow.value.copy(accentPalette = palette)
    }

    override suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(hapticFeedbackEnabled = enabled)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(notificationsEnabled = enabled)
    }

    override suspend fun addRecentEmoji(emoji: String) {
        val updated = (listOf(emoji) + settingsFlow.value.recentEmojis).distinct().take(30)
        settingsFlow.value = settingsFlow.value.copy(recentEmojis = updated)
    }

    override suspend fun addCustomAlert(value: Int, unit: String): com.shejan.acontinue.domain.model.CustomAlert {
        val alert = com.shejan.acontinue.domain.model.CustomAlert(value = value, unit = unit)
        val updated = settingsFlow.value.customAlerts + alert
        settingsFlow.value = settingsFlow.value.copy(customAlerts = updated)
        return alert
    }

    override suspend fun updateCustomAlert(id: String, value: Int, unit: String) {
        val updated = settingsFlow.value.customAlerts.map {
            if (it.id == id) it.copy(value = value, unit = unit) else it
        }
        settingsFlow.value = settingsFlow.value.copy(customAlerts = updated)
    }

    override suspend fun deleteCustomAlert(id: String) {
        val updated = settingsFlow.value.customAlerts.filterNot { it.id == id }
        settingsFlow.value = settingsFlow.value.copy(customAlerts = updated)
    }

    override suspend fun addReminderCategory(
        name: String,
        colorHex: Long,
        iconName: String
    ): com.shejan.acontinue.domain.model.ReminderCategory {
        val trimmed = name.trim()
        val current = settingsFlow.value.reminderCategories
        val existing = current.find { it.name.equals(trimmed, ignoreCase = true) }
        if (existing != null) {
            val updated = current.map { item ->
                if (item.id == existing.id) {
                    item.copy(colorHex = colorHex, iconName = iconName)
                } else item
            }
            settingsFlow.value = settingsFlow.value.copy(reminderCategories = updated)
            return existing.copy(colorHex = colorHex, iconName = iconName)
        }

        val category = com.shejan.acontinue.domain.model.ReminderCategory(
            name = trimmed,
            colorHex = colorHex,
            iconName = iconName,
            isDefault = false
        )
        val updated = current + category
        settingsFlow.value = settingsFlow.value.copy(reminderCategories = updated)
        return category
    }

    override suspend fun updateReminderCategory(
        id: String,
        newName: String,
        newColorHex: Long,
        newIconName: String
    ): com.shejan.acontinue.domain.model.ReminderCategory {
        val trimmed = newName.trim()
        val current = settingsFlow.value.reminderCategories
        var updatedItem: com.shejan.acontinue.domain.model.ReminderCategory? = null
        val updated = current.map { item ->
            if (item.id == id && !item.isDefault) {
                val modified = item.copy(name = trimmed, colorHex = newColorHex, iconName = newIconName)
                updatedItem = modified
                modified
            } else item
        }
        val result = updatedItem ?: com.shejan.acontinue.domain.model.ReminderCategory(id = id, name = trimmed, colorHex = newColorHex, iconName = newIconName)
        settingsFlow.value = settingsFlow.value.copy(reminderCategories = updated)
        return result
    }

    override suspend fun deleteReminderCategory(id: String) {
        val current = settingsFlow.value.reminderCategories
        val updated = current.filterNot { it.id == id && !it.isDefault }
        settingsFlow.value = settingsFlow.value.copy(reminderCategories = updated)
    }

    override fun getNoteLabels(): Flow<List<String>> {
        return settingsFlow.map { it.noteLabels }
    }

    override suspend fun addNoteLabel(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val current = settingsFlow.value.noteLabels
        if (current.any { it.equals(trimmed, ignoreCase = true) }) return
        val updated = current + trimmed
        settingsFlow.value = settingsFlow.value.copy(noteLabels = updated)
    }

    override suspend fun updateNoteLabel(oldName: String, newName: String) {
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank()) return
        val current = settingsFlow.value.noteLabels
        val updated = current.map { if (it.equals(oldName, ignoreCase = true)) trimmedNew else it }.distinct()
        settingsFlow.value = settingsFlow.value.copy(noteLabels = updated)
    }

    override suspend fun deleteNoteLabel(name: String) {
        val updated = settingsFlow.value.noteLabels.filterNot { it.equals(name, ignoreCase = true) }
        settingsFlow.value = settingsFlow.value.copy(noteLabels = updated)
    }

    val pinnedNotesFlow = MutableStateFlow<Set<String>>(emptySet())

    override fun getPinnedNoteIds(): Flow<Set<String>> = pinnedNotesFlow

    override suspend fun setNotesPinned(ids: Set<String>, pinned: Boolean) {
        val current = pinnedNotesFlow.value.toMutableSet()
        if (pinned) {
            current.addAll(ids)
        } else {
            current.removeAll(ids)
        }
        pinnedNotesFlow.value = current
    }

    override suspend fun toggleNotesPinned(ids: Set<String>) {
        val current = pinnedNotesFlow.value
        val allPinned = ids.isNotEmpty() && ids.all { it in current }
        setNotesPinned(ids, !allPinned)
    }
}

