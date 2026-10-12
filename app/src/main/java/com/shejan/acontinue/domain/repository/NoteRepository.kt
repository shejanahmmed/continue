package com.shejan.acontinue.domain.repository

import com.shejan.acontinue.domain.model.ActivityAttachment
import com.shejan.acontinue.domain.model.ActivityLink
import com.shejan.acontinue.domain.model.ActivityNote
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotesForActivity(activityId: Long): Flow<List<ActivityNote>>
    fun searchNotes(query: String): Flow<List<ActivityNote>>
    suspend fun insertNote(note: ActivityNote): Long
    suspend fun updateNote(note: ActivityNote)
    suspend fun deleteNote(noteId: Long)
}

interface LinkRepository {
    fun getLinksForActivity(activityId: Long): Flow<List<ActivityLink>>
    suspend fun insertLink(link: ActivityLink): Long
    suspend fun deleteLink(linkId: Long)

    fun getAttachmentsForActivity(activityId: Long): Flow<List<ActivityAttachment>>
    suspend fun insertAttachment(attachment: ActivityAttachment): Long
    suspend fun deleteAttachment(attachmentId: Long)
}
