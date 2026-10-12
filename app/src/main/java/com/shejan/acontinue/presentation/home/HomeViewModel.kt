package com.shejan.acontinue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.TaskRepository
import com.shejan.acontinue.domain.usecase.GetTodayTimelineUseCase
import com.shejan.acontinue.domain.usecase.TodayTimeline
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class HomeUiState(
    val timeline: TodayTimeline = TodayTimeline(emptyList(), null, null),
    val pendingTasks: List<ActivityTask> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val getTodayTimelineUseCase: GetTodayTimelineUseCase,
    private val taskRepository: TaskRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        combine(
            getTodayTimelineUseCase(),
            taskRepository.getPendingTasks()
        ) { timeline, pendingTasks ->
            HomeUiState(
                timeline = timeline,
                pendingTasks = pendingTasks,
                isLoading = false
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun toggleTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(taskId, isCompleted)
        }
    }

    fun toggleActivityCompletion(activityId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            activityRepository.toggleActivityCompletion(activityId, isCompleted)
        }
    }
}
