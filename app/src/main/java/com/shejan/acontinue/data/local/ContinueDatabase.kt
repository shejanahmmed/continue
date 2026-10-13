package com.shejan.acontinue.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shejan.acontinue.data.local.dao.ActivityDao
import com.shejan.acontinue.data.local.dao.LinkDao
import com.shejan.acontinue.data.local.dao.NoteDao
import com.shejan.acontinue.data.local.dao.TaskDao
import com.shejan.acontinue.data.local.entity.ActivityEntity
import com.shejan.acontinue.data.local.entity.AttachmentEntity
import com.shejan.acontinue.data.local.entity.LinkEntity
import com.shejan.acontinue.data.local.entity.NoteEntity
import com.shejan.acontinue.data.local.entity.TaskEntity

@Database(
    entities = [
        ActivityEntity::class,
        TaskEntity::class,
        NoteEntity::class,
        LinkEntity::class,
        AttachmentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ContinueDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun linkDao(): LinkDao

    companion object {
        @Volatile
        private var INSTANCE: ContinueDatabase? = null

        fun getInstance(context: Context): ContinueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContinueDatabase::class.java,
                    "continue_app.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
