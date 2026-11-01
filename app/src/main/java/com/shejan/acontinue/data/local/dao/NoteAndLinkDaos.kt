package com.shejan.acontinue.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shejan.acontinue.data.local.entity.AttachmentEntity
import com.shejan.acontinue.data.local.entity.LinkEntity
import com.shejan.acontinue.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND activityId = :activityId ORDER BY updatedAtMillis DESC")
    fun getNotesForActivity(activityId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    fun getNoteById(noteId: Long): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY updatedAtMillis DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAtMillis DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 1 ORDER BY archivedAtMillis DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, deletedAtMillis = :deletedAtMillis WHERE id = :noteId")
    suspend fun moveToTrash(noteId: Long, deletedAtMillis: Long)

    @Query("UPDATE notes SET isDeleted = 0, deletedAtMillis = 0 WHERE id = :noteId")
    suspend fun restoreFromTrash(noteId: Long)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun permanentlyDeleteNote(noteId: Long)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrashNotes()

    @Query("UPDATE notes SET isArchived = :isArchived, archivedAtMillis = :archivedAtMillis WHERE id = :noteId")
    suspend fun setNoteArchived(noteId: Long, isArchived: Boolean, archivedAtMillis: Long)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)
}

@Dao
interface LinkDao {
    @Query("SELECT * FROM links WHERE activityId = :activityId ORDER BY createdAtMillis DESC")
    fun getLinksForActivity(activityId: Long): Flow<List<LinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: LinkEntity): Long

    @Update
    suspend fun updateLink(link: LinkEntity)

    @Query("DELETE FROM links WHERE id = :linkId")
    suspend fun deleteLink(linkId: Long)

    @Query("SELECT * FROM attachments WHERE activityId = :activityId ORDER BY createdAtMillis DESC")
    fun getAttachmentsForActivity(activityId: Long): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity): Long

    @Query("DELETE FROM attachments WHERE id = :attachmentId")
    suspend fun deleteAttachment(attachmentId: Long)
}
