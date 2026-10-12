package com.shejan.acontinue.data.repository

import com.shejan.acontinue.data.local.dao.TaskDao
import com.shejan.acontinue.data.mapper.toDomain
import com.shejan.acontinue.data.mapper.toEntity
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getAllTasks(): Flow<List<ActivityTask>> {
        return taskDao.getAllTasks().map { list -> list.map { it.toDomain() } }
    }

    override fun getTasksForActivity(activityId: Long): Flow<List<ActivityTask>> {
        return taskDao.getTasksForActivity(activityId).map { list -> list.map { it.toDomain() } }
    }

    override fun getPendingTasks(): Flow<List<ActivityTask>> {
        return taskDao.getPendingTasks().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertTask(task: ActivityTask): Long {
        return taskDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: ActivityTask) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun toggleTaskCompletion(taskId: Long, isCompleted: Boolean) {
        taskDao.updateCompletionStatus(taskId, isCompleted)
    }

    override suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
    }
}
