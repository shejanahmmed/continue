package com.shejan.acontinue.data.repository

import com.shejan.acontinue.data.local.dao.LinkDao
import com.shejan.acontinue.data.local.dao.NoteDao
import com.shejan.acontinue.data.mapper.toDomain
import com.shejan.acontinue.data.mapper.toEntity
import com.shejan.acontinue.domain.model.ActivityAttachment
import com.shejan.acontinue.domain.model.ActivityLink
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.repository.LinkRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepositoryImpl(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getNotesForActivity(activityId: Long): Flow<List<ActivityNote>> {
        return noteDao.getNotesForActivity(activityId).map { list -> list.map { it.toDomain() } }
    }

    override fun searchNotes(query: String): Flow<List<ActivityNote>> {
        return noteDao.searchNotes(query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertNote(note: ActivityNote): Long {
        return noteDao.insertNote(note.toEntity())
    }

    override suspend fun updateNote(note: ActivityNote) {
        noteDao.updateNote(note.toEntity())
    }

    override suspend fun deleteNote(noteId: Long) {
        noteDao.deleteNote(noteId)
    }
}

class LinkRepositoryImpl(
    private val linkDao: LinkDao
) : LinkRepository {

    override fun getLinksForActivity(activityId: Long): Flow<List<ActivityLink>> {
        return linkDao.getLinksForActivity(activityId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertLink(link: ActivityLink): Long {
        return linkDao.insertLink(link.toEntity())
    }

    override suspend fun deleteLink(linkId: Long) {
        linkDao.deleteLink(linkId)
    }

    override fun getAttachmentsForActivity(activityId: Long): Flow<List<ActivityAttachment>> {
        return linkDao.getAttachmentsForActivity(activityId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertAttachment(attachment: ActivityAttachment): Long {
        return linkDao.insertAttachment(attachment.toEntity())
    }

    override suspend fun deleteAttachment(attachmentId: Long) {
        linkDao.deleteAttachment(attachmentId)
    }
}
