package com.shejan.acontinue.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.core.notification.ActivityScheduler
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.CustomAlert
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.MonthlyRepeatType
import com.shejan.acontinue.domain.model.RecurrenceEndType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.ReminderOffset
import com.shejan.acontinue.domain.model.YearlyRepeatType
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import com.shejan.acontinue.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

fun defaultStartMillis(): Long {
    val cal = Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

fun defaultEndMillis(startMillis: Long = defaultStartMillis()): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = startMillis
        add(Calendar.HOUR_OF_DAY, 1)
    }
    return cal.timeInMillis
}

fun defaultRecurrenceEndDateMillis(startMillis: Long = defaultStartMillis()): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = startMillis
        add(Calendar.DAY_OF_YEAR, 7)
    }
    return cal.timeInMillis
}

data class CreateEntryState(
    val entryType: EntryType = EntryType.ACTIVITY,
    val title: String = "",
    val emoji: String = "",
    val recentEmojis: List<String> = emptyList(),
    val category: ActivityCategory = ActivityCategory.PERSONAL,
    val isAllDay: Boolean = false,
    val startMillis: Long = defaultStartMillis(),
    val endMillis: Long = defaultEndMillis(defaultStartMillis()),
    val location: String = "",
    val reminderOffset: ReminderOffset = ReminderOffset.NONE,
    val alertsEnabled: Boolean = false,
    val selectedAlertMinutes: Set<Int> = emptySet(),
    val savedCustomAlerts: List<CustomAlert> = emptyList(),
    val recurrenceMode: RecurrenceMode = RecurrenceMode.NONE,
    val recurrenceInterval: Int = 1,
    val recurrenceDaysOfWeek: Set<Int> = emptySet(),
    val monthlyRepeatType: MonthlyRepeatType = MonthlyRepeatType.SAME_DAY_OF_MONTH,
    val monthlySelectedDays: Set<Int> = emptySet(),
    val yearlyRepeatType: YearlyRepeatType = YearlyRepeatType.SAME_DATE,
    val yearlySelectedMonths: Set<Int> = emptySet(),
    val recurrenceEndType: RecurrenceEndType = RecurrenceEndType.FOREVER,
    val recurrenceEndCount: Int = 10,
    val recurrenceEndDateMillis: Long = defaultRecurrenceEndDateMillis(defaultStartMillis()),
    val notes: String = "",
    val attachmentPath: String = "",
    val priority: ActivityPriority = ActivityPriority.MEDIUM,
    val isSaving: Boolean = false
)

sealed interface CreateEntryEvent {
    data object SavedSuccessfully : CreateEntryEvent
    data class ShowError(val message: String) : CreateEntryEvent
}

class CreateEntryViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository,
    private val scheduler: ActivityScheduler,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEntryState())
    val state: StateFlow<CreateEntryState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateEntryEvent>()
    val events: SharedFlow<CreateEntryEvent> = _events.asSharedFlow()

    init {
        settingsRepository?.getUserSettings()
            ?.onEach { userSettings ->
                _state.update { current ->
                    current.copy(
                        recentEmojis = userSettings.recentEmojis,
                        savedCustomAlerts = userSettings.customAlerts
                    )
                }
            }
            ?.launchIn(viewModelScope)
    }

    fun updateEntryType(type: EntryType) = _state.update { it.copy(entryType = type) }
    fun updateTitle(title: String) = _state.update { it.copy(title = title) }
    fun updateEmoji(emoji: String) = _state.update { it.copy(emoji = emoji) }
    fun appendEmojiToTitle(emoji: String) {
        if (emoji.isBlank()) return
        _state.update { current ->
            val updatedTitle = if (current.title.isBlank()) {
                emoji
            } else {
                "$emoji ${current.title.trim()}"
            }
            val updatedRecents = (listOf(emoji) + current.recentEmojis).distinct().take(30)
            current.copy(title = updatedTitle, emoji = emoji, recentEmojis = updatedRecents)
        }
        viewModelScope.launch {
            settingsRepository?.addRecentEmoji(emoji)
        }
    }
    fun updateCategory(category: ActivityCategory) = _state.update { it.copy(category = category) }
    fun updateAllDay(isAllDay: Boolean) = _state.update { it.copy(isAllDay = isAllDay) }
    fun updateStartMillis(millis: Long) = _state.update { it.copy(startMillis = millis) }
    fun updateEndMillis(millis: Long) = _state.update { it.copy(endMillis = millis) }

    fun updateStartDate(year: Int, month: Int, dayOfMonth: Int) {
        _state.update { current ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = current.startMillis
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            val newStart = cal.timeInMillis
            val duration = (current.endMillis - current.startMillis).coerceAtLeast(3600000L)
            val newEnd = if (current.endMillis <= newStart) newStart + duration else current.endMillis
            current.copy(startMillis = newStart, endMillis = newEnd)
        }
    }

    fun updateEndDate(year: Int, month: Int, dayOfMonth: Int) {
        _state.update { current ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = current.endMillis
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            val newEnd = cal.timeInMillis
            val newStart = if (newEnd < current.startMillis) newEnd else current.startMillis
            current.copy(startMillis = newStart, endMillis = newEnd)
        }
    }

    fun updateStartTime(hour12: Int, minute: Int, isPm: Boolean) {
        _state.update { current ->
            val hour24 = when {
                isPm && hour12 < 12 -> hour12 + 12
                !isPm && hour12 == 12 -> 0
                else -> hour12
            }
            val cal = Calendar.getInstance().apply {
                timeInMillis = current.startMillis
                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val newStart = cal.timeInMillis
            val duration = (current.endMillis - current.startMillis).coerceAtLeast(3600000L)
            val newEnd = if (current.endMillis <= newStart) newStart + duration else current.endMillis
            current.copy(startMillis = newStart, endMillis = newEnd)
        }
    }

    fun updateEndTime(hour12: Int, minute: Int, isPm: Boolean) {
        _state.update { current ->
            val hour24 = when {
                isPm && hour12 < 12 -> hour12 + 12
                !isPm && hour12 == 12 -> 0
                else -> hour12
            }
            val cal = Calendar.getInstance().apply {
                timeInMillis = current.endMillis
                set(Calendar.HOUR_OF_DAY, hour24)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val newEnd = cal.timeInMillis
            val newStart = if (newEnd < current.startMillis) newEnd else current.startMillis
            current.copy(startMillis = newStart, endMillis = newEnd)
        }
    }
    fun updateLocation(location: String) = _state.update { it.copy(location = location) }

    fun toggleAlertsEnabled(enabled: Boolean) = _state.update { current ->
        current.copy(
            alertsEnabled = enabled,
            selectedAlertMinutes = if (enabled && current.selectedAlertMinutes.isEmpty()) setOf(10) else if (!enabled) emptySet() else current.selectedAlertMinutes
        )
    }

    fun toggleAlertMinute(minutes: Int) = _state.update { current ->
        val updated = if (current.selectedAlertMinutes.contains(minutes)) {
            current.selectedAlertMinutes - minutes
        } else {
            current.selectedAlertMinutes + minutes
        }
        current.copy(selectedAlertMinutes = updated)
    }

    fun addCustomAlert(value: Int, unit: String) {
        viewModelScope.launch {
            val created = settingsRepository?.addCustomAlert(value, unit)
            val minutes = created?.minutesBefore ?: when (unit.lowercase()) {
                "minutes" -> value
                "hours" -> value * 60
                "days" -> value * 1440
                "weeks" -> value * 10080
                else -> value
            }
            _state.update { current ->
                current.copy(selectedAlertMinutes = current.selectedAlertMinutes + minutes)
            }
        }
    }

    fun updateCustomAlert(id: String, value: Int, unit: String) {
        viewModelScope.launch {
            val oldAlert = _state.value.savedCustomAlerts.firstOrNull { it.id == id }
            settingsRepository?.updateCustomAlert(id, value, unit)
            val newMinutes = when (unit.lowercase()) {
                "minutes" -> value
                "hours" -> value * 60
                "days" -> value * 1440
                "weeks" -> value * 10080
                else -> value
            }
            if (oldAlert != null) {
                _state.update { current ->
                    val wasSelected = current.selectedAlertMinutes.contains(oldAlert.minutesBefore)
                    val updatedMinutes = if (wasSelected) {
                        (current.selectedAlertMinutes - oldAlert.minutesBefore) + newMinutes
                    } else current.selectedAlertMinutes
                    current.copy(selectedAlertMinutes = updatedMinutes)
                }
            }
        }
    }

    fun deleteCustomAlert(id: String) {
        val target = _state.value.savedCustomAlerts.firstOrNull { it.id == id }
        viewModelScope.launch {
            settingsRepository?.deleteCustomAlert(id)
            if (target != null) {
                _state.update { current ->
                    current.copy(selectedAlertMinutes = current.selectedAlertMinutes - target.minutesBefore)
                }
            }
        }
    }

    fun updateReminderOffset(offset: ReminderOffset) = _state.update { it.copy(reminderOffset = offset) }

    fun updateRecurrence(mode: RecurrenceMode, interval: Int = 1) {
        val cal = Calendar.getInstance().apply { timeInMillis = _state.value.startMillis }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)

        _state.update { current ->
            current.copy(
                recurrenceMode = mode,
                recurrenceInterval = interval.coerceAtLeast(1),
                recurrenceDaysOfWeek = if (current.recurrenceDaysOfWeek.isEmpty()) setOf(dayOfWeek) else current.recurrenceDaysOfWeek,
                monthlySelectedDays = if (current.monthlySelectedDays.isEmpty()) setOf(dayOfMonth) else current.monthlySelectedDays,
                yearlySelectedMonths = if (current.yearlySelectedMonths.isEmpty()) setOf(month) else current.yearlySelectedMonths
            )
        }
    }

    fun updateRecurrenceInterval(interval: Int) {
        _state.update { it.copy(recurrenceInterval = interval.coerceAtLeast(1)) }
    }

    fun toggleRecurrenceDay(dayOfWeek: Int) {
        _state.update { current ->
            val updated = if (current.recurrenceDaysOfWeek.contains(dayOfWeek)) {
                if (current.recurrenceDaysOfWeek.size > 1) current.recurrenceDaysOfWeek - dayOfWeek else current.recurrenceDaysOfWeek
            } else {
                current.recurrenceDaysOfWeek + dayOfWeek
            }
            current.copy(recurrenceDaysOfWeek = updated)
        }
    }

    fun updateMonthlyRepeatType(type: MonthlyRepeatType) {
        _state.update { it.copy(monthlyRepeatType = type) }
    }

    fun toggleMonthlySelectedDay(day: Int) {
        _state.update { current ->
            val updated = if (current.monthlySelectedDays.contains(day)) {
                if (current.monthlySelectedDays.size > 1) current.monthlySelectedDays - day else current.monthlySelectedDays
            } else {
                current.monthlySelectedDays + day
            }
            current.copy(monthlySelectedDays = updated)
        }
    }

    fun updateYearlyRepeatType(type: YearlyRepeatType) {
        _state.update { it.copy(yearlyRepeatType = type) }
    }

    fun toggleYearlySelectedMonth(month: Int) {
        _state.update { current ->
            val updated = if (current.yearlySelectedMonths.contains(month)) {
                if (current.yearlySelectedMonths.size > 1) current.yearlySelectedMonths - month else current.yearlySelectedMonths
            } else {
                current.yearlySelectedMonths + month
            }
            current.copy(yearlySelectedMonths = updated)
        }
    }

    fun updateRecurrenceEndType(endType: RecurrenceEndType) {
        _state.update { it.copy(recurrenceEndType = endType) }
    }

    fun updateRecurrenceEndCount(count: Int) {
        _state.update { it.copy(recurrenceEndCount = count.coerceAtLeast(1)) }
    }

    fun updateRecurrenceEndDate(dateMillis: Long) {
        _state.update { it.copy(recurrenceEndDateMillis = dateMillis) }
    }

    fun updateNotes(notes: String) = _state.update { it.copy(notes = notes) }
    fun updatePriority(priority: ActivityPriority) = _state.update { it.copy(priority = priority) }

    fun saveEntry() {
        val current = _state.value
        if (current.title.isBlank()) {
            viewModelScope.launch { _events.emit(CreateEntryEvent.ShowError("Title cannot be empty")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val minutesList = if (current.alertsEnabled) {
                current.selectedAlertMinutes.toList().sorted()
            } else {
                emptyList()
            }

            val primaryOffset = if (minutesList.isNotEmpty()) minutesList.first() else -1

            val activity = ActivityItem(
                title = current.title.trim(),
                description = current.notes.trim(),
                entryType = current.entryType,
                category = current.category,
                isAllDay = current.isAllDay,
                startTimeMillis = current.startMillis,
                endTimeMillis = if (current.entryType == EntryType.REMINDER) current.startMillis + 1800000L else current.endMillis,
                location = current.location.trim(),
                priority = current.priority,
                reminderMinutesBefore = primaryOffset,
                reminderMinutesList = minutesList,
                recurrenceMode = current.recurrenceMode,
                recurrenceInterval = current.recurrenceInterval,
                recurrenceDaysOfWeek = current.recurrenceDaysOfWeek.toList().sorted(),
                monthlyRepeatType = current.monthlyRepeatType,
                monthlySelectedDays = current.monthlySelectedDays.toList().sorted(),
                yearlyRepeatType = current.yearlyRepeatType,
                yearlySelectedMonths = current.yearlySelectedMonths.toList().sorted(),
                recurrenceEndType = current.recurrenceEndType,
                recurrenceEndCount = current.recurrenceEndCount,
                recurrenceEndDateMillis = current.recurrenceEndDateMillis,
                isCompleted = false
            )

            val newId = activityRepository.insertActivity(activity)
            val savedActivity = activity.copy(id = newId)

            if (current.notes.isNotBlank()) {
                noteRepository.insertNote(
                    ActivityNote(
                        activityId = newId,
                        title = "Initial Notes",
                        content = current.notes.trim()
                    )
                )
            }

            scheduler.schedule(savedActivity)

            _state.update { it.copy(isSaving = false) }
            _events.emit(CreateEntryEvent.SavedSuccessfully)
        }
    }
}
