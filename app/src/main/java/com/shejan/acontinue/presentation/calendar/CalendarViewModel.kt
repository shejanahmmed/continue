package com.shejan.acontinue.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceEndType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    val todayDateNumber: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val isLoading: Boolean = false
) {
    val activities: List<ActivityItem> get() = selectedDayActivities
    val selectedDayCompletedCount: Int get() = selectedDayActivities.count { it.isCompleted }
    val selectedDayTotalCount: Int get() = selectedDayActivities.size

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
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(System.currentTimeMillis())
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val _isWeekMode = MutableStateFlow(false)
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        combine(
            _currentMonth,
            _selectedDate,
            _isWeekMode,
            activityRepository.getAllActivities()
        ) { monthMillis, selectedMillis, weekMode, allActivities ->
            buildUiState(monthMillis, selectedMillis, weekMode, allActivities)
        }
            .onEach { newState ->
                _uiState.value = newState
            }
            .launchIn(viewModelScope)
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
        weekMode: Boolean,
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
            val startOfDay = getStartOfDayMillis(dayCal.timeInMillis)
            val endOfDay = getEndOfDayMillis(dayCal.timeInMillis)

            val dayActivities = activities.filter { item ->
                occursOnDay(item, startOfDay, endOfDay)
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

        val selectedActivities = activities.filter { item ->
            occursOnDay(item, selStart, selEnd)
        }.sortedBy { it.startTimeMillis }

        val todayDate = Calendar.getInstance().apply { timeInMillis = todayMillis }.get(Calendar.DAY_OF_MONTH)

        return CalendarUiState(
            currentMonthMillis = monthMillis,
            selectedDateMillis = selectedMillis,
            isWeekMode = weekMode,
            monthDays = days,
            selectedDayActivities = selectedActivities,
            todayDateNumber = todayDate,
            isLoading = false
        )
    }

    private fun occursOnDay(activity: ActivityItem, startOfDay: Long, endOfDay: Long): Boolean {
        val activityStartDate = getStartOfDayMillis(activity.startTimeMillis)
        if (startOfDay < activityStartDate) return false

        if (activity.recurrenceMode == RecurrenceMode.NONE) {
            return activity.startTimeMillis <= endOfDay && activity.endTimeMillis >= startOfDay
        }

        // End condition check
        if (activity.recurrenceEndType == RecurrenceEndType.UNTIL_DATE && activity.recurrenceEndDateMillis > 0L) {
            val endLimit = getEndOfDayMillis(activity.recurrenceEndDateMillis)
            if (startOfDay > endLimit) return false
        }

        val startCal = Calendar.getInstance().apply { timeInMillis = activity.startTimeMillis }
        val targetCal = Calendar.getInstance().apply { timeInMillis = startOfDay }
        val interval = activity.recurrenceInterval.coerceAtLeast(1)

        return when (activity.recurrenceMode) {
            RecurrenceMode.NONE -> false
            RecurrenceMode.MINUTELY, RecurrenceMode.HOURLY, RecurrenceMode.DAILY -> {
                val daysDiff = ((startOfDay - activityStartDate) / (24 * 60 * 60 * 1000L)).toInt()
                if (daysDiff < 0 || daysDiff % interval != 0) return false
                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val occurrenceIndex = daysDiff / interval
                    if (occurrenceIndex >= activity.recurrenceEndCount) return false
                }
                true
            }
            RecurrenceMode.WEEKDAYS -> {
                val dow = targetCal.get(Calendar.DAY_OF_WEEK)
                if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) return false
                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val countBefore = countWeekdaysBetween(activityStartDate, startOfDay)
                    if (countBefore >= activity.recurrenceEndCount) return false
                }
                true
            }
            RecurrenceMode.WEEKLY -> {
                val dow = targetCal.get(Calendar.DAY_OF_WEEK)
                val matchesDow = if (activity.recurrenceDaysOfWeek.isNotEmpty()) {
                    dow in activity.recurrenceDaysOfWeek
                } else {
                    dow == startCal.get(Calendar.DAY_OF_WEEK)
                }
                if (!matchesDow) return false

                val startWeekCal = (startCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val targetWeekCal = (targetCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val weeksDiff = ((targetWeekCal.timeInMillis - startWeekCal.timeInMillis) / (7 * 24 * 60 * 60 * 1000L)).toInt()
                if (weeksDiff < 0 || weeksDiff % interval != 0) return false
                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val countBefore = countWeeklyOccurrences(activity, startCal, targetCal)
                    if (countBefore >= activity.recurrenceEndCount) return false
                }
                true
            }
            RecurrenceMode.MONTHLY -> {
                val targetDom = targetCal.get(Calendar.DAY_OF_MONTH)
                val matchesDom = if (activity.monthlySelectedDays.isNotEmpty()) {
                    targetDom in activity.monthlySelectedDays
                } else {
                    targetDom == startCal.get(Calendar.DAY_OF_MONTH)
                }
                if (!matchesDom) return false

                val monthsDiff = (targetCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)) * 12 +
                        (targetCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH))
                if (monthsDiff < 0 || monthsDiff % interval != 0) return false
                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val occurrenceIndex = monthsDiff / interval
                    if (occurrenceIndex >= activity.recurrenceEndCount) return false
                }
                true
            }
            RecurrenceMode.YEARLY -> {
                val targetDom = targetCal.get(Calendar.DAY_OF_MONTH)
                val targetMonth = targetCal.get(Calendar.MONTH)
                val matchesMonth = if (activity.yearlySelectedMonths.isNotEmpty()) {
                    targetMonth in activity.yearlySelectedMonths
                } else {
                    targetMonth == startCal.get(Calendar.MONTH)
                }
                if (!matchesMonth || targetDom != startCal.get(Calendar.DAY_OF_MONTH)) return false

                val yearsDiff = targetCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
                if (yearsDiff < 0 || yearsDiff % interval != 0) return false
                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val occurrenceIndex = yearsDiff / interval
                    if (occurrenceIndex >= activity.recurrenceEndCount) return false
                }
                true
            }
        }
    }

    private fun countWeekdaysBetween(startMillis: Long, targetMillis: Long): Int {
        if (targetMillis <= startMillis) return 0
        var count = 0
        val cal = Calendar.getInstance().apply { timeInMillis = startMillis }
        val targetCal = Calendar.getInstance().apply { timeInMillis = targetMillis }
        while (cal.get(Calendar.YEAR) < targetCal.get(Calendar.YEAR) ||
            (cal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) < targetCal.get(Calendar.DAY_OF_YEAR))
        ) {
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) {
                count++
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return count
    }

    private fun countWeeklyOccurrences(activity: ActivityItem, startCal: Calendar, targetCal: Calendar): Int {
        val daysList = if (activity.recurrenceDaysOfWeek.isNotEmpty()) {
            activity.recurrenceDaysOfWeek
        } else {
            listOf(startCal.get(Calendar.DAY_OF_WEEK))
        }
        var count = 0
        val cal = (startCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val targetDayMillis = getStartOfDayMillis(targetCal.timeInMillis)
        while (cal.timeInMillis < targetDayMillis) {
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            if (dow in daysList) {
                count++
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return count
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
