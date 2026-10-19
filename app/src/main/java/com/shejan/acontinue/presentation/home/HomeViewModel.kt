package com.shejan.acontinue.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.TaskRepository
import com.shejan.acontinue.domain.usecase.GetTodayTimelineUseCase
import com.shejan.acontinue.domain.usecase.TodayTimeline
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class HomeUiState(
    val timeline: TodayTimeline = TodayTimeline(emptyList(), null, null),
    val pendingTasks: List<ActivityTask> = emptyList(),
    val todayEventCount: Int = 0,
    val todayCompletedEventCount: Int = 0,
    val todayReminderCount: Int = 0,
    val todayCompletedReminderCount: Int = 0,
    val todayPendingReminderCount: Int = 0,
    val todayMissedReminderCount: Int = 0,
    val todayTotalCount: Int = 0,
    val todayCompletedCount: Int = 0,
    val currentEvent: ActivityItem? = null,
    val nextEvent: ActivityItem? = null,
    val nextReminder: ActivityItem? = null,
    val isLoading: Boolean = false
) {
    val eventProgress: Float
        get() = if (todayEventCount > 0) todayCompletedEventCount.toFloat() / todayEventCount.toFloat() else 0f

    val reminderProgress: Float
        get() = if (todayReminderCount > 0) todayCompletedReminderCount.toFloat() / todayReminderCount.toFloat() else 0f

    val completionProgress: Float
        get() = if (todayTotalCount > 0) todayCompletedCount.toFloat() / todayTotalCount.toFloat() else 0f
}

class HomeViewModel(
    private val getTodayTimelineUseCase: GetTodayTimelineUseCase,
    private val taskRepository: TaskRepository,
    private val activityRepository: ActivityRepository,
    private val tickerFlow: Flow<Long> = defaultForegroundTicker()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        combine(
            getTodayTimelineUseCase(),
            taskRepository.getPendingTasks(),
            tickerFlow
        ) { timeline, pendingTasks, now ->
            val events = timeline.activities.filter { it.entryType == EntryType.ACTIVITY }
            val eventCount = events.size

            fun getEffectiveEnd(event: ActivityItem): Long = if (event.isAllDay) {
                java.util.Calendar.getInstance().apply {
                    timeInMillis = if (event.endTimeMillis > event.startTimeMillis) event.endTimeMillis else event.startTimeMillis
                    set(java.util.Calendar.HOUR_OF_DAY, 23)
                    set(java.util.Calendar.MINUTE, 59)
                    set(java.util.Calendar.SECOND, 59)
                    set(java.util.Calendar.MILLISECOND, 999)
                }.timeInMillis
            } else if (event.endTimeMillis > event.startTimeMillis) {
                event.endTimeMillis
            } else {
                event.startTimeMillis
            }

            // Events conclude automatically once their scheduled timetable end time has elapsed
            val elapsedEvents = events.count { event ->
                getEffectiveEnd(event) <= now
            }

            // Active events whose timetable window contains now
            val activeEvents = events.filter { event ->
                val end = getEffectiveEnd(event)
                now in event.startTimeMillis..end
            }
            // Prioritize scheduled timed events over all-day events if both are active
            val currentEvt = activeEvents.firstOrNull { !it.isAllDay } ?: activeEvents.firstOrNull()

            // Upcoming event is strictly one starting in the future
            val nextEvt = events.filter { it.startTimeMillis > now }
                .minByOrNull { it.startTimeMillis }

            val reminders = timeline.activities.filter { it.entryType == EntryType.REMINDER }
            val reminderCount = reminders.size
            val completedReminders = reminders.count { it.isCompleted }
            val pendingReminders = (reminderCount - completedReminders).coerceAtLeast(0)
            val missedReminders = reminders.count { !it.isCompleted && it.startTimeMillis < now }

            // Upcoming reminder is the earliest pending reminder
            val nextRem = reminders.filter { !it.isCompleted }
                .minByOrNull { it.startTimeMillis }

            val updatedTimeline = timeline.copy(
                currentEvent = currentEvt,
                nextEvent = nextEvt,
                nextReminder = nextRem
            )

            HomeUiState(
                timeline = updatedTimeline,
                pendingTasks = pendingTasks,
                todayEventCount = eventCount,
                todayCompletedEventCount = elapsedEvents,
                todayReminderCount = reminderCount,
                todayCompletedReminderCount = completedReminders,
                todayPendingReminderCount = pendingReminders,
                todayMissedReminderCount = missedReminders,
                todayTotalCount = reminderCount,
                todayCompletedCount = completedReminders,
                currentEvent = currentEvt,
                nextEvent = nextEvt,
                nextReminder = nextRem,
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

    fun deleteActivity(activityId: Long) {
        viewModelScope.launch {
            activityRepository.deleteActivity(activityId)
        }
    }

    companion object {
        fun defaultForegroundTicker(intervalMillis: Long = 30_000L): Flow<Long> = flow {
            while (true) {
                emit(System.currentTimeMillis())
                delay(intervalMillis)
            }
        }
    }
}
