package com.shejan.acontinue.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceEndType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.model.ActivityWorkspaceAggregate
import com.shejan.acontinue.domain.usecase.GetActivityWorkspaceUseCase
import com.shejan.acontinue.domain.util.RecurrenceEvaluator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
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
) {
    val dotColorsHex: List<Long> get() = activities.map { item ->
        if (item.entryType == EntryType.REMINDER) {
            item.reminderCategoryColorHex
        } else {
            item.category.colorHex
        }
    }
    val hasMoreDots: Boolean get() = activities.size > 8
}

data class CalendarUiState(
    val currentMonthMillis: Long = System.currentTimeMillis(),
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val isWeekMode: Boolean = false,
    val monthDays: List<CalendarDay> = emptyList(),
    val selectedDayActivities: List<ActivityItem> = emptyList(),
    val selectedActivityForDetail: ActivityItem? = null,
    val expandedActivityId: Long? = null,
    val expandedWorkspace: ActivityWorkspaceAggregate? = null,
    val todayDateNumber: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val todayPulseTrigger: Long = 0L,
    val isLoading: Boolean = false
) {
    val activities: List<ActivityItem> get() = selectedDayActivities
    val selectedDayCompletedCount: Int get() = selectedDayActivities.count { it.isCompleted }
    val selectedDayTotalCount: Int get() = selectedDayActivities.size
    val selectedDayReminderCount: Int get() = selectedDayActivities.count { it.entryType == EntryType.REMINDER }
    val selectedDayEventCount: Int get() = selectedDayActivities.count { it.entryType == EntryType.ACTIVITY }
    val selectedDayCompletedReminderCount: Int get() = selectedDayActivities.count { it.entryType == EntryType.REMINDER && it.isCompleted }
    val allDayActivities: List<ActivityItem> get() = selectedDayActivities.filter { it.isAllDay }
    val timedActivities: List<ActivityItem> get() = selectedDayActivities.filter { !it.isAllDay }

    val currentWeekDays: List<CalendarDay>
        get() {
            if (monthDays.isEmpty()) return emptyList()
            val rows = monthDays.chunked(7)
            return rows.firstOrNull { week -> week.any { it.isSelected } }
                ?: rows.firstOrNull { week -> week.any { it.isToday } }
                ?: rows.firstOrNull()
                ?: emptyList()
        }
}

class CalendarViewModel(
    private val activityRepository: ActivityRepository,
    private val getActivityWorkspaceUseCase: GetActivityWorkspaceUseCase? = null
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(System.currentTimeMillis())
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val _isWeekMode = MutableStateFlow(false)
    private val _todayPulseTrigger = MutableStateFlow(0L)
    private val _selectedActivityForDetail = MutableStateFlow<ActivityItem?>(null)
    private val _expandedActivityId = MutableStateFlow<Long?>(null)
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val expandedWorkspaceFlow: Flow<ActivityWorkspaceAggregate?> = _expandedActivityId.flatMapLatest { id ->
        if (id == null || getActivityWorkspaceUseCase == null) {
            flowOf(null)
        } else {
            getActivityWorkspaceUseCase(id)
        }
    }

    private data class CalendarViewState(
        val monthMillis: Long,
        val selectedMillis: Long,
        val isWeekMode: Boolean,
        val todayPulseTrigger: Long
    )

    private data class CardFocusState(
        val detailActivity: ActivityItem?,
        val expandedId: Long?,
        val expandedWs: ActivityWorkspaceAggregate?
    )

    init {
        val calendarViewFlow = combine(
            _currentMonth,
            _selectedDate,
            _isWeekMode,
            _todayPulseTrigger
        ) { month, selected, isWeek, pulse ->
            CalendarViewState(month, selected, isWeek, pulse)
        }

        val cardFocusFlow = combine(
            _selectedActivityForDetail,
            _expandedActivityId,
            expandedWorkspaceFlow
        ) { detail, expandedId, ws ->
            CardFocusState(detail, expandedId, ws)
        }

        combine(
            calendarViewFlow,
            cardFocusFlow,
            activityRepository.getAllActivities()
        ) { viewState, focusState, allActivities ->
            buildUiState(
                monthMillis = viewState.monthMillis,
                selectedMillis = viewState.selectedMillis,
                weekMode = viewState.isWeekMode,
                todayPulseTrigger = viewState.todayPulseTrigger,
                detailActivity = focusState.detailActivity,
                expandedId = focusState.expandedId,
                expandedWs = focusState.expandedWs,
                activities = allActivities
            )
        }
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
    }

    fun toggleCardExpansion(activityId: Long) {
        _expandedActivityId.value = if (_expandedActivityId.value == activityId) null else activityId
    }

    fun collapseCard() {
        _expandedActivityId.value = null
    }

    fun showActivityDetail(activity: ActivityItem) {
        _selectedActivityForDetail.value = activity
    }

    fun dismissActivityDetail() {
        _selectedActivityForDetail.value = null
    }

    fun deleteActivity(id: Long) {
        viewModelScope.launch {
            activityRepository.deleteActivity(id)
            if (_selectedActivityForDetail.value?.id == id) {
                _selectedActivityForDetail.value = null
            }
            if (_expandedActivityId.value == id) {
                _expandedActivityId.value = null
            }
        }
    }

    fun setWeekMode(isWeek: Boolean) {
        _isWeekMode.value = isWeek
    }

    fun toggleCalendarMode() {
        _isWeekMode.value = !_isWeekMode.value
    }

    fun nextWeek() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value
            add(Calendar.DAY_OF_YEAR, 7)
        }
        selectDate(cal.timeInMillis)
    }

    fun previousWeek() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _selectedDate.value
            add(Calendar.DAY_OF_YEAR, -7)
        }
        selectDate(cal.timeInMillis)
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
        _todayPulseTrigger.value = now
    }

    fun toggleActivityCompletion(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            activityRepository.toggleActivityCompletion(id, isCompleted)
        }
    }

    fun skipReminder(id: Long) {
        viewModelScope.launch {
            activityRepository.toggleActivitySkipped(id, true)
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
        weekMode: Boolean,
        todayPulseTrigger: Long,
        detailActivity: ActivityItem?,
        expandedId: Long?,
        expandedWs: ActivityWorkspaceAggregate?,
        activities: List<ActivityItem>
    ): CalendarUiState {
        val scheduledActivities = activities.filter {
            (it.entryType == EntryType.ACTIVITY || it.entryType == EntryType.REMINDER) &&
            it.entryType != EntryType.NOTE
        }
        val bounds = calculateGridBounds(monthMillis)
        val monthCal = Calendar.getInstance().apply { timeInMillis = monthMillis }
        val targetMonth = monthCal.get(Calendar.MONTH)
        val todayMillis = System.currentTimeMillis()

        val days = (0 until 42).map { offset ->
            val dayCal = (bounds.firstGridCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            val startOfDay = getStartOfDayMillis(dayCal.timeInMillis)
            val endOfDay = getEndOfDayMillis(dayCal.timeInMillis)

            val dayActivities = scheduledActivities.flatMap { item ->
                RecurrenceEvaluator.expandOccurrencesForRange(item, startOfDay, endOfDay)
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

        val selStart = getStartOfDayMillis(selectedMillis)
        val selEnd = getEndOfDayMillis(selectedMillis)

        val selectedActivities = scheduledActivities.flatMap { item ->
            RecurrenceEvaluator.expandOccurrencesForRange(item, selStart, selEnd)
        }.sortedBy { it.startTimeMillis }

        val todayDate = Calendar.getInstance().apply { timeInMillis = todayMillis }.get(Calendar.DAY_OF_MONTH)
        val resolvedDetail = detailActivity?.let { da ->
            activities.firstOrNull { it.id == da.id }
        }
        val resolvedWorkspace = expandedWs ?: expandedId?.let { id ->
            activities.firstOrNull { it.id == id }?.let { item ->
                ActivityWorkspaceAggregate(activity = item)
            }
        }

        return CalendarUiState(
            currentMonthMillis = monthMillis,
            selectedDateMillis = selectedMillis,
            isWeekMode = weekMode,
            monthDays = days,
            selectedDayActivities = selectedActivities,
            selectedActivityForDetail = resolvedDetail,
            expandedActivityId = expandedId,
            expandedWorkspace = resolvedWorkspace,
            todayDateNumber = todayDate,
            todayPulseTrigger = todayPulseTrigger,
            isLoading = false
        )
    }

    private fun getStartOfDayMillis(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDayMillis(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }
}
