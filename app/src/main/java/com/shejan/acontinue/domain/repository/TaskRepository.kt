package com.shejan.acontinue.domain.repository

import com.shejan.acontinue.domain.model.ActivityTask
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<ActivityTask>>
    fun getTasksForActivity(activityId: Long): Flow<List<ActivityTask>>
    fun getPendingTasks(): Flow<List<ActivityTask>>
    suspend fun insertTask(task: ActivityTask): Long
    suspend fun updateTask(task: ActivityTask)
    suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean)
    suspend fun deleteTask(taskId: Long)
}
