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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ActivityEntity::class,
        TaskEntity::class,
        NoteEntity::class,
        LinkEntity::class,
        AttachmentEntity::class
    ],
    version = 5,
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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN isSkipped INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): ContinueDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ContinueDatabase::class.java,
                    "continue_app.db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
