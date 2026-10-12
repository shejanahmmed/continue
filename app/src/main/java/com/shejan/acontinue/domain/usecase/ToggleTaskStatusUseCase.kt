package com.shejan.acontinue.domain.usecase

import com.shejan.acontinue.domain.repository.TaskRepository

class ToggleTaskStatusUseCase(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, isCompleted: Boolean) {
        taskRepository.toggleTaskCompletion(taskId, isCompleted)
    }
}
