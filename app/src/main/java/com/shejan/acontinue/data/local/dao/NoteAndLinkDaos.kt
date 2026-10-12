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
    @Query("SELECT * FROM notes WHERE activityId = :activityId ORDER BY updatedAtMillis DESC")
    fun getNotesForActivity(activityId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY updatedAtMillis DESC")
    fun searchNotes(query: String): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: Long)
}

@Dao
interface LinkDao {
    @Query("SELECT * FROM links WHERE activityId = :activityId ORDER BY createdAtMillis DESC")
    fun getLinksForActivity(activityId: Long): Flow<List<LinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: LinkEntity): Long

    @Query("DELETE FROM links WHERE id = :linkId")
    suspend fun deleteLink(linkId: Long)

    @Query("SELECT * FROM attachments WHERE activityId = :activityId ORDER BY createdAtMillis DESC")
    fun getAttachmentsForActivity(activityId: Long): Flow<List<AttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: AttachmentEntity): Long

    @Query("DELETE FROM attachments WHERE id = :attachmentId")
    suspend fun deleteAttachment(attachmentId: Long)
}
