package com.shejan.acontinue.domain.usecase

import com.shejan.acontinue.domain.model.ActivityWorkspaceAggregate
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.LinkRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import com.shejan.acontinue.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

class GetActivityWorkspaceUseCase(
    private val activityRepository: ActivityRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
    private val linkRepository: LinkRepository
) {
    operator fun invoke(activityId: Long): Flow<ActivityWorkspaceAggregate> {
        val activityFlow = activityRepository.getActivityById(activityId).filterNotNull()
        val tasksFlow = taskRepository.getTasksForActivity(activityId)
        val notesFlow = noteRepository.getNotesForActivity(activityId)
        val linksFlow = linkRepository.getLinksForActivity(activityId)
        val attachmentsFlow = linkRepository.getAttachmentsForActivity(activityId)

        return combine(
            activityFlow,
            tasksFlow,
            notesFlow,
            linksFlow,
            attachmentsFlow
        ) { activity, tasks, notes, links, attachments ->
            val workspaceNotes = notes.filterNot { note ->
                activity.description.isNotBlank() &&
                note.title == "Notes" &&
                note.content.trim() == activity.description.trim()
            }
            ActivityWorkspaceAggregate(
                activity = activity,
                tasks = tasks,
                notes = workspaceNotes,
                links = links,
                attachments = attachments
            )
        }
    }
}
