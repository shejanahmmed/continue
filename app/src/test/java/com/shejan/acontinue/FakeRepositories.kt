package com.shejan.acontinue

import com.shejan.acontinue.domain.model.ActivityAttachment
import com.shejan.acontinue.domain.model.ActivityItem
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
            list.filter { it.startTimeMillis <= endMillis && it.endTimeMillis >= startMillis }
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
            if (it.id == id) it.copy(isCompleted = isCompleted) else it
        }
    }

    override fun searchActivities(query: String): Flow<List<ActivityItem>> {
        return activitiesFlow.map { list ->
            list.filter { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
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

