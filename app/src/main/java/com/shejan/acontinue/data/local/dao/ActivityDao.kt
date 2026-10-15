package com.shejan.acontinue.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shejan.acontinue.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("""
        SELECT * FROM activities 
        WHERE startTimeMillis <= :endMillis AND endTimeMillis >= :startMillis 
        ORDER BY startTimeMillis ASC
    """)
    fun getActivitiesForDateRange(startMillis: Long, endMillis: Long): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    fun getActivityById(id: Long): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities ORDER BY startTimeMillis DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Update
    suspend fun updateActivity(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteActivity(id: Long)

    @Query("UPDATE activities SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    @Query("""
        SELECT * FROM activities 
        WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' 
        ORDER BY startTimeMillis DESC
    """)
    fun searchActivities(query: String): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE startTimeMillis > :now AND isCompleted = 0")
    suspend fun getUpcomingIncompleteActivities(now: Long): List<ActivityEntity>

    @Query("UPDATE activities SET reminderCategoryColorHex = :colorHex, reminderCategoryIcon = :icon WHERE reminderCategory = :categoryName")
    suspend fun updateReminderCategoryDetails(categoryName: String, colorHex: Long, icon: String)

    @Query("UPDATE activities SET reminderCategory = :newCategory, reminderCategoryColorHex = :newColorHex, reminderCategoryIcon = :newIcon WHERE id = :activityId")
    suspend fun updateActivityReminderCategory(activityId: Long, newCategory: String, newColorHex: Long, newIcon: String)

    @Query("UPDATE activities SET reminderCategory = :newName, reminderCategoryColorHex = :newColorHex, reminderCategoryIcon = :newIcon WHERE reminderCategory = :oldName")
    suspend fun renameReminderCategoryAcrossActivities(oldName: String, newName: String, newColorHex: Long, newIcon: String)

    @Query("UPDATE activities SET reminderCategory = :fallbackName, reminderCategoryColorHex = :fallbackColorHex, reminderCategoryIcon = :fallbackIcon WHERE reminderCategory = :deletedName")
    suspend fun resetActivitiesToDefaultCategory(deletedName: String, fallbackName: String, fallbackColorHex: Long, fallbackIcon: String)
}
