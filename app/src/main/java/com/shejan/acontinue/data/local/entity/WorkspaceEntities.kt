package com.shejan.acontinue.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["activityId"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val activityId: Long?,
    val title: String,
    val isCompleted: Boolean,
    val dueDateMillis: Long?,
    val priority: String,
    val createdAtMillis: Long
)

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["activityId"])]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val activityId: Long,
    val title: String,
    val content: String,
    val colorHex: Long = 0L,
    val labels: String = "",
    val updatedAtMillis: Long
)

@Entity(
    tableName = "links",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["activityId"])]
)
data class LinkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val activityId: Long,
    val url: String,
    val title: String,
    val createdAtMillis: Long
)

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["activityId"])]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val activityId: Long,
    val fileName: String,
    val uriString: String,
    val mimeType: String,
    val sizeBytes: Long,
    val createdAtMillis: Long
)
