package com.shejan.acontinue.presentation.year

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Calendar

data class YearDayItem(
    val dayOfMonth: Int,
    val dateMillis: Long,
    val isToday: Boolean,
    val categoryColorHex: Long? = null,
    val hasActivity: Boolean = false,
    val fallbackColorHex: Long = 0xFF242424L
)

data class YearMonthData(
    val monthIndex: Int,
    val monthName: String,
    val isCurrentMonth: Boolean,
    val leadingEmptySlots: Int,
    val days: List<YearDayItem>
)

data class YearUiState(
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val todayDayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val currentMonthIndex: Int = Calendar.getInstance().get(Calendar.MONTH),
    val months: List<YearMonthData> = emptyList(),
    val searchQuery: String = ""
)

class YearViewModel(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(buildYearState(Calendar.getInstance().get(Calendar.YEAR), emptyList()))
    val uiState: StateFlow<YearUiState> = _uiState.asStateFlow()

    private var cachedActivities: List<ActivityItem> = emptyList()

    init {
        activityRepository.getAllActivities()
            .onEach { activities ->
                cachedActivities = activities.filter { !it.isDeleted }
                _uiState.value = buildYearState(_uiState.value.year, cachedActivities)
            }
            .launchIn(viewModelScope)
    }

    fun setYear(newYear: Int) {
        if (_uiState.value.year != newYear) {
            _uiState.value = buildYearState(newYear, cachedActivities)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private fun buildYearState(year: Int, activities: List<ActivityItem>): YearUiState {
        val nowCal = Calendar.getInstance()
        val currentYear = nowCal.get(Calendar.YEAR)
        val currentMonth = nowCal.get(Calendar.MONTH)
        val todayDay = nowCal.get(Calendar.DAY_OF_MONTH)

        // Group activities by Triple(year, month, day)
        val activityMap = mutableMapOf<Triple<Int, Int, Int>, ActivityItem>()
        for (activity in activities) {
            val actCal = Calendar.getInstance().apply { timeInMillis = activity.startTimeMillis }
            val key = Triple(
                actCal.get(Calendar.YEAR),
                actCal.get(Calendar.MONTH),
                actCal.get(Calendar.DAY_OF_MONTH)
            )
            if (!activityMap.containsKey(key)) {
                activityMap[key] = activity
            }
        }

        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"
        )

        val months = (0..11).map { monthIndex ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, monthIndex)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // In our calendar week: Saturday is Col 0, Sunday is Col 1, ... Friday is Col 6
            // Calendar.DAY_OF_WEEK: SATURDAY=7, SUNDAY=1, MONDAY=2, ... FRIDAY=6
            // (dayOfWeek % 7) gives: SATURDAY(7)%7 = 0, SUNDAY(1)%7 = 1, ... FRIDAY(6)%7 = 6
            val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val leadingEmptySlots = firstDayOfWeek % 7
            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            val days = (1..daysInMonth).map { day ->
                cal.set(Calendar.DAY_OF_MONTH, day)
                val dateMillis = cal.timeInMillis
                val isToday = (year == currentYear && monthIndex == currentMonth && day == todayDay)
                val matchingActivity = activityMap[Triple(year, monthIndex, day)]

                val categoryColor = matchingActivity?.let {
                    if (it.entryType == EntryType.REMINDER) it.reminderCategoryColorHex else it.category.colorHex
                }

                YearDayItem(
                    dayOfMonth = day,
                    dateMillis = dateMillis,
                    isToday = isToday,
                    categoryColorHex = categoryColor,
                    hasActivity = matchingActivity != null,
                    fallbackColorHex = getDeterministicFallbackColor(year, monthIndex, day)
                )
            }

            YearMonthData(
                monthIndex = monthIndex,
                monthName = monthNames[monthIndex],
                isCurrentMonth = (year == currentYear && monthIndex == currentMonth),
                leadingEmptySlots = leadingEmptySlots,
                days = days
            )
        }

        return YearUiState(
            year = year,
            todayDayOfMonth = todayDay,
            currentMonthIndex = currentMonth,
            months = months
        )
    }

    companion object {
        // Deterministic utilitarian palette mapping matching the visual identity of the reference design
        fun getDeterministicFallbackColor(year: Int, monthIndex: Int, day: Int): Long {
            val palette = listOf(
                0xFF133E26L, // Dark Green
                0xFF1B3B5AL, // Dark Blue
                0xFF4A1A1AL, // Dark Red
                0xFF0E444BL, // Dark Teal
                0xFF4D380EL, // Dark Amber
                0xFF242424L, // Neutral Dark Grey
                0xFF242424L,
                0xFF242424L
            )
            val seed = (year * 37 + monthIndex * 31 + day * 17)
            val index = kotlin.math.abs(seed) % palette.size
            return palette[index]
        }
    }
}

class YearViewModelFactory(
    private val activityRepository: ActivityRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(YearViewModel::class.java)) {
            return YearViewModel(activityRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
