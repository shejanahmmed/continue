package com.shejan.acontinue.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.repository.ActivityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.Calendar

data class CalendarDay(
    val dateMillis: Long,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val activities: List<ActivityItem> = emptyList()
)

data class CalendarUiState(
    val currentMonthMillis: Long = System.currentTimeMillis(),
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val monthDays: List<CalendarDay> = emptyList(),
    val selectedDayActivities: List<ActivityItem> = emptyList(),
    val todayDateNumber: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val isLoading: Boolean = false
) {
    val activities: List<ActivityItem> get() = selectedDayActivities
}

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(System.currentTimeMillis())
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        combine(_currentMonth, _selectedDate) { month, selected ->
            Pair(month, selected)
        }
            .flatMapLatest { (monthMillis, selectedMillis) ->
                val bounds = calculateGridBounds(monthMillis)
                activityRepository.getActivitiesForDateRange(bounds.startMillis, bounds.endMillis)
                    .map { activities ->
                        buildUiState(monthMillis, selectedMillis, activities)
                    }
            }
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
    }

    fun selectDate(millis: Long) {
        _selectedDate.value = millis
        val selCal = Calendar.getInstance().apply { timeInMillis = millis }
        val curCal = Calendar.getInstance().apply { timeInMillis = _currentMonth.value }
        if (selCal.get(Calendar.MONTH) != curCal.get(Calendar.MONTH) ||
            selCal.get(Calendar.YEAR) != curCal.get(Calendar.YEAR)
        ) {
            _currentMonth.value = millis
        }
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _currentMonth.value
            add(Calendar.MONTH, 1)
        }
        _currentMonth.value = cal.timeInMillis
        val selCal = Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }
        _selectedDate.value = selCal.timeInMillis
    }

    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _currentMonth.value
            add(Calendar.MONTH, -1)
        }
        _currentMonth.value = cal.timeInMillis
        val selCal = Calendar.getInstance().apply {
            timeInMillis = cal.timeInMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }
        _selectedDate.value = selCal.timeInMillis
    }

    fun jumpToToday() {
        val now = System.currentTimeMillis()
        _currentMonth.value = now
        _selectedDate.value = now
    }

    fun toggleActivityCompletion(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            activityRepository.toggleActivityCompletion(id, isCompleted)
        }
    }

    private data class GridBounds(val startMillis: Long, val endMillis: Long, val firstGridCal: Calendar)

    private fun calculateGridBounds(monthMillis: Long): GridBounds {
        val cal = Calendar.getInstance().apply {
            timeInMillis = monthMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val leadingDays = dayOfWeek - Calendar.SUNDAY
        val firstGridCal = (cal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, -leadingDays)
        }
        val startMillis = firstGridCal.timeInMillis
        val endCal = (firstGridCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 42)
            add(Calendar.MILLISECOND, -1)
        }
        return GridBounds(startMillis, endCal.timeInMillis, firstGridCal)
    }

    private fun buildUiState(
        monthMillis: Long,
        selectedMillis: Long,
        activities: List<ActivityItem>
    ): CalendarUiState {
        val bounds = calculateGridBounds(monthMillis)
        val monthCal = Calendar.getInstance().apply { timeInMillis = monthMillis }
        val targetMonth = monthCal.get(Calendar.MONTH)
        val todayMillis = System.currentTimeMillis()

        val days = (0 until 42).map { offset ->
            val dayCal = (bounds.firstGridCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            val startOfDay = dayCal.timeInMillis
            val endOfDay = (dayCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val dayActivities = activities.filter { item ->
                item.startTimeMillis <= endOfDay && item.endTimeMillis >= startOfDay
            }.sortedBy { it.startTimeMillis }

            CalendarDay(
                dateMillis = startOfDay,
                dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH),
                isCurrentMonth = dayCal.get(Calendar.MONTH) == targetMonth,
                isToday = isSameDay(startOfDay, todayMillis),
                isSelected = isSameDay(startOfDay, selectedMillis),
                activities = dayActivities
            )
        }

        val selDayCal = Calendar.getInstance().apply {
            timeInMillis = selectedMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val selStart = selDayCal.timeInMillis
        val selEnd = (selDayCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val selectedActivities = activities.filter { item ->
            item.startTimeMillis <= selEnd && item.endTimeMillis >= selStart
        }.sortedBy { it.startTimeMillis }

        val todayDate = Calendar.getInstance().apply { timeInMillis = todayMillis }.get(Calendar.DAY_OF_MONTH)

        return CalendarUiState(
            currentMonthMillis = monthMillis,
            selectedDateMillis = selectedMillis,
            monthDays = days,
            selectedDayActivities = selectedActivities,
            todayDateNumber = todayDate,
            isLoading = false
        )
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }
}
