package com.shejan.acontinue.domain.repository

import com.shejan.acontinue.domain.model.ActivityItem
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getActivitiesForDateRange(startMillis: Long, endMillis: Long): Flow<List<ActivityItem>>
    fun getActivityById(id: Long): Flow<ActivityItem?>
    fun getAllActivities(): Flow<List<ActivityItem>>
    suspend fun insertActivity(activity: ActivityItem): Long
    suspend fun updateActivity(activity: ActivityItem)
    suspend fun deleteActivity(id: Long)
    suspend fun toggleActivityCompletion(id: Long, isCompleted: Boolean)
    fun searchActivities(query: String): Flow<List<ActivityItem>>
    suspend fun updateReminderCategoryDetails(categoryName: String, colorHex: Long, icon: String)
    suspend fun updateActivityReminderCategory(activityId: Long, newCategory: String, newColorHex: Long, newIcon: String)
}
