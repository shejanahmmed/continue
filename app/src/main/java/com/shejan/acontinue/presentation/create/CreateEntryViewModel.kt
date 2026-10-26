package com.shejan.acontinue.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.core.notification.ActivityScheduler
import com.shejan.acontinue.core.util.AttachmentHelper
import com.shejan.acontinue.core.util.ProcessedAttachment
import com.shejan.acontinue.domain.model.ActivityAttachment
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.CustomAlert
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.MonthlyRepeatType
import com.shejan.acontinue.domain.model.RecurrenceEndType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.ReminderCategory
import com.shejan.acontinue.domain.model.ReminderOffset
import com.shejan.acontinue.domain.model.YearlyRepeatType
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.LinkRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import com.shejan.acontinue.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val attachments: List<ProcessedAttachment> = emptyList(),
    val priority: ActivityPriority = ActivityPriority.MEDIUM,
    val earlyAlertMinutes: Int = -1,
    val reminderList: String = "My reminders",
    val selectedReminderCategory: ReminderCategory = ReminderCategory.DEFAULT,
    val savedReminderCategories: List<ReminderCategory> = listOf(ReminderCategory.DEFAULT),
    val categoryReminderCounts: Map<String, Int> = emptyMap(),
    val reminderCategory: String = "Notes",
    val reminderCategoryColorHex: Long = 0xFF000000L,
    val savedNoteLabels: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val editingActivityId: Long? = null
)

sealed interface CreateEntryEvent {
    data object SavedSuccessfully : CreateEntryEvent
    data class ShowError(val message: String) : CreateEntryEvent
}

class CreateEntryViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository,
    private val scheduler: ActivityScheduler,
    private val settingsRepository: SettingsRepository? = null,
    private val linkRepository: LinkRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEntryState())
    val state: StateFlow<CreateEntryState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateEntryEvent>()
    val events: SharedFlow<CreateEntryEvent> = _events.asSharedFlow()

    init {
        val settingsFlow = settingsRepository?.getUserSettings() ?: flowOf(com.shejan.acontinue.domain.model.UserSettings())
        combine(
            settingsFlow,
            activityRepository.getAllActivities()
        ) { userSettings, activities ->
            val reminderActivities = activities.filter { it.entryType == EntryType.REMINDER }
            val counts = reminderActivities
                .groupingBy { it.reminderCategory }
                .eachCount()

            val fromReminders = reminderActivities
                .map {
                    ReminderCategory(
                        name = it.reminderCategory,
                        colorHex = it.reminderCategoryColorHex,
                        iconName = it.reminderCategoryIcon
                    )
                }
                .distinctBy { it.name }

            val merged = (userSettings.reminderCategories + fromReminders)
                .distinctBy { it.name }

            val finalCategories = if (merged.none { it.name.equals(ReminderCategory.DEFAULT.name, ignoreCase = true) }) {
                listOf(ReminderCategory.DEFAULT) + merged
            } else {
                merged
            }

            Triple(userSettings, finalCategories, counts)
        }.onEach { (userSettings, categories, counts) ->
            _state.update { current ->
                val currentSelected = current.selectedReminderCategory
                val updatedSelected = categories.find { it.name.equals(currentSelected.name, ignoreCase = true) } ?: currentSelected
                current.copy(
                    recentEmojis = userSettings.recentEmojis,
                    savedCustomAlerts = userSettings.customAlerts,
                    savedReminderCategories = categories,
                    categoryReminderCounts = counts,
                    selectedReminderCategory = updatedSelected,
                    savedNoteLabels = userSettings.noteLabels
                )
            }
        }.launchIn(viewModelScope)
    }

    fun resetForm() {
        _state.update { current ->
            CreateEntryState(
                recentEmojis = current.recentEmojis,
                savedCustomAlerts = current.savedCustomAlerts,
                savedReminderCategories = current.savedReminderCategories,
                categoryReminderCounts = current.categoryReminderCounts,
                savedNoteLabels = current.savedNoteLabels,
                entryType = EntryType.NOTE
            )
        }
    }

    fun updateEntryType(type: EntryType) = _state.update { it.copy(entryType = type) }
    fun updateTitle(title: String) = _state.update { it.copy(title = title) }
    fun updateEmoji(emoji: String) = _state.update { it.copy(emoji = emoji) }
    fun selectEmoji(emoji: String) {
        if (emoji.isBlank()) return
        _state.update { current ->
            val newEmoji = if (current.emoji == emoji) "" else emoji
            val updatedRecents = (listOf(emoji) + current.recentEmojis).distinct().take(30)
            current.copy(emoji = newEmoji, recentEmojis = updatedRecents)
        }
        viewModelScope.launch {
            settingsRepository?.addRecentEmoji(emoji)
        }
    }
    fun appendEmojiToTitle(emoji: String) = selectEmoji(emoji)
    fun updateCategory(category: ActivityCategory) = _state.update { it.copy(category = category) }
    fun updateAllDay(isAllDay: Boolean) = _state.update { it.copy(isAllDay = isAllDay) }
    fun updateStartMillis(millis: Long) = _state.update { it.copy(startMillis = millis) }
    fun updateEndMillis(millis: Long) = _state.update { it.copy(endMillis = millis) }

    fun loadActivityForEditing(activityId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(editingActivityId = null) }
            var activity = activityRepository.getActivityById(activityId).firstOrNull()
            val existingNotes = noteRepository.getNotesForActivity(activityId).firstOrNull() ?: emptyList()
            var matchingNote = existingNotes.firstOrNull()
            var effectiveActivityId = activityId

            if (activity == null && matchingNote == null) {
                // If neither activity nor note exists under activityId, check if activityId was passed as a noteId
                val allNotes = noteRepository.searchNotes("").firstOrNull() ?: emptyList()
                val foundNote = allNotes.find { it.id == activityId }
                if (foundNote != null) {
                    matchingNote = foundNote
                    if (foundNote.activityId > 0L) {
                        effectiveActivityId = foundNote.activityId
                        activity = activityRepository.getActivityById(foundNote.activityId).firstOrNull()
                    }
                } else {
                    return@launch
                }
            }

            val effectiveTitle = matchingNote?.title?.takeIf { it.isNotBlank() && it != "Untitled Note" }
                ?: activity?.title?.takeIf { it.isNotBlank() }
                ?: matchingNote?.title
                ?: "Untitled Note"

            val effectiveContent = matchingNote?.content?.takeIf { it.isNotBlank() }
                ?: activity?.description
                ?: ""

            // Self-heal activity description in Room database if it was previously saved empty
            if (activity != null && activity.description.isBlank() && effectiveContent.isNotBlank()) {
                val updatedActivity = activity.copy(description = effectiveContent)
                activityRepository.updateActivity(updatedActivity)
                activity = updatedActivity
            } else if (activity == null && matchingNote != null) {
                // Auto-create ActivityItem for orphaned or standalone notes
                val now = matchingNote.updatedAtMillis
                val newActivity = ActivityItem(
                    id = if (effectiveActivityId > 0L) effectiveActivityId else 0L,
                    title = effectiveTitle,
                    description = effectiveContent,
                    entryType = EntryType.NOTE,
                    category = ActivityCategory.OTHER,
                    isAllDay = false,
                    startTimeMillis = 0L,
                    endTimeMillis = 0L,
                    location = "",
                    priority = ActivityPriority.MEDIUM,
                    reminderMinutesBefore = -1,
                    reminderMinutesList = emptyList(),
                    recurrenceMode = RecurrenceMode.NONE,
                    isCompleted = false,
                    reminderCategory = "Notes",
                    reminderCategoryColorHex = 0xFF000000L,
                    createdAtMillis = now
                )
                val createdId = activityRepository.insertActivity(newActivity)
                effectiveActivityId = createdId
                activity = newActivity.copy(id = createdId)
                if (matchingNote.activityId != createdId) {
                    noteRepository.updateNote(matchingNote.copy(activityId = createdId))
                }
            }

            val targetId = activity?.id ?: effectiveActivityId
            val attachments = linkRepository?.getAttachmentsForActivity(targetId)?.firstOrNull() ?: emptyList()
            val processed = attachments.map { att ->
                ProcessedAttachment(
                    id = att.id.toString(),
                    fileName = att.fileName,
                    filePath = att.uriString,
                    mimeType = att.mimeType,
                    sizeBytes = att.sizeBytes
                )
            }

            _state.update { current ->
                current.copy(
                    editingActivityId = targetId,
                    entryType = activity?.entryType ?: EntryType.NOTE,
                    title = effectiveTitle,
                    notes = effectiveContent,
                    category = activity?.category ?: ActivityCategory.OTHER,
                    isAllDay = activity?.isAllDay ?: false,
                    startMillis = activity?.startTimeMillis ?: defaultStartMillis(),
                    endMillis = activity?.endTimeMillis ?: defaultEndMillis(defaultStartMillis()),
                    location = if (activity?.location?.trim()?.equals("location", ignoreCase = true) == true) "" else (activity?.location ?: ""),
                    priority = activity?.priority ?: ActivityPriority.MEDIUM,
                    reminderCategory = activity?.reminderCategory?.takeIf { it.isNotBlank() } ?: "Notes",
                    reminderCategoryColorHex = activity?.reminderCategoryColorHex ?: 0xFF000000L,
                    alertsEnabled = activity?.reminderMinutesList?.isNotEmpty() == true || (activity?.reminderMinutesBefore ?: -1) >= 0,
                    selectedAlertMinutes = activity?.reminderMinutesList?.toSet() ?: emptySet(),
                    earlyAlertMinutes = activity?.reminderMinutesBefore ?: -1,
                    recurrenceMode = activity?.recurrenceMode ?: RecurrenceMode.NONE,
                    recurrenceInterval = activity?.recurrenceInterval ?: 1,
                    recurrenceDaysOfWeek = activity?.recurrenceDaysOfWeek?.toSet() ?: emptySet(),
                    monthlyRepeatType = activity?.monthlyRepeatType ?: MonthlyRepeatType.SAME_DAY_OF_MONTH,
                    monthlySelectedDays = activity?.monthlySelectedDays?.toSet() ?: emptySet(),
                    yearlyRepeatType = activity?.yearlyRepeatType ?: YearlyRepeatType.SAME_DATE,
                    yearlySelectedMonths = activity?.yearlySelectedMonths?.toSet() ?: emptySet(),
                    recurrenceEndType = activity?.recurrenceEndType ?: RecurrenceEndType.FOREVER,
                    recurrenceEndCount = activity?.recurrenceEndCount ?: 10,
                    recurrenceEndDateMillis = if ((activity?.recurrenceEndDateMillis ?: 0L) > 0L) activity!!.recurrenceEndDateMillis else defaultRecurrenceEndDateMillis(activity?.startTimeMillis ?: defaultStartMillis()),
                    attachments = processed
                )
            }
        }
    }

    fun setInitialDate(dateMillis: Long) {
        val targetCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        updateStartDate(
            year = targetCal.get(Calendar.YEAR),
            month = targetCal.get(Calendar.MONTH),
            dayOfMonth = targetCal.get(Calendar.DAY_OF_MONTH)
        )
    }

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

    fun selectReminderCategory(category: ReminderCategory) {
        _state.update {
            it.copy(
                selectedReminderCategory = category,
                reminderList = category.name
            )
        }
    }

    fun addReminderCategory(name: String, colorHex: Long, iconName: String) {
        viewModelScope.launch {
            val created = settingsRepository?.addReminderCategory(name, colorHex, iconName)
                ?: ReminderCategory(name = name, colorHex = colorHex, iconName = iconName)
            activityRepository.updateReminderCategoryDetails(name, colorHex, iconName)
            selectReminderCategory(created)
        }
    }

    fun updateReminderCategory(oldCategory: ReminderCategory, newName: String, newColorHex: Long, newIconName: String) {
        if (oldCategory.isDefault) return
        viewModelScope.launch {
            val updated = settingsRepository?.updateReminderCategory(
                id = oldCategory.id,
                newName = newName,
                newColorHex = newColorHex,
                newIconName = newIconName
            ) ?: oldCategory.copy(name = newName, colorHex = newColorHex, iconName = newIconName)

            activityRepository.renameReminderCategoryAcrossActivities(
                oldName = oldCategory.name,
                newName = newName,
                newColorHex = newColorHex,
                newIcon = newIconName
            )

            _state.update { current ->
                if (current.selectedReminderCategory.id == oldCategory.id || current.selectedReminderCategory.name == oldCategory.name) {
                    current.copy(
                        selectedReminderCategory = updated,
                        reminderList = updated.name
                    )
                } else {
                    current
                }
            }
        }
    }

    fun deleteReminderCategory(category: ReminderCategory) {
        if (category.isDefault) return
        viewModelScope.launch {
            settingsRepository?.deleteReminderCategory(category.id)
            activityRepository.resetActivitiesToDefaultCategory(
                deletedName = category.name,
                fallbackName = ReminderCategory.DEFAULT.name,
                fallbackColorHex = ReminderCategory.DEFAULT.colorHex,
                fallbackIcon = ReminderCategory.DEFAULT.iconName
            )
            _state.update { current ->
                if (current.selectedReminderCategory.id == category.id || current.selectedReminderCategory.name == category.name) {
                    current.copy(
                        selectedReminderCategory = ReminderCategory.DEFAULT,
                        reminderList = ReminderCategory.DEFAULT.name
                    )
                } else {
                    current
                }
            }
        }
    }
    fun updateEarlyAlertMinutes(minutes: Int) = _state.update { it.copy(earlyAlertMinutes = minutes) }
    fun updateReminderList(name: String) = _state.update { it.copy(reminderList = name) }

    fun addAttachment(attachment: ProcessedAttachment) {
        _state.update { it.copy(attachments = it.attachments + attachment) }
    }

    fun removeAttachment(attachmentId: String) {
        _state.update { current ->
            val item = current.attachments.find { it.id == attachmentId }
            item?.let { AttachmentHelper.deleteLocalFile(it.filePath) }
            current.copy(attachments = current.attachments.filter { it.id != attachmentId })
        }
    }

    fun saveEntry() {
        val current = _state.value
        if (current.title.isBlank()) {
            viewModelScope.launch { _events.emit(CreateEntryEvent.ShowError("Title cannot be empty")) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            val isReminder = current.entryType == EntryType.REMINDER
            val minutesList = if (isReminder) {
                if (current.earlyAlertMinutes >= 0) {
                    listOf(0, current.earlyAlertMinutes).distinct().sorted()
                } else {
                    listOf(0)
                }
            } else {
                if (current.alertsEnabled) current.selectedAlertMinutes.toList().sorted() else emptyList()
            }

            val primaryOffset = if (isReminder) {
                current.earlyAlertMinutes
            } else {
                if (minutesList.isNotEmpty()) minutesList.first() else -1
            }

            // For all-day reminders, default alarm trigger is normalized to 9:00 AM on the scheduled date
            val effectiveStartTime = if (isReminder && current.isAllDay) {
                Calendar.getInstance().apply {
                    timeInMillis = current.startMillis
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            } else {
                current.startMillis
            }

            val activity = ActivityItem(
                title = current.title.trim(),
                description = if (isReminder) "" else current.notes.trim(),
                entryType = current.entryType,
                category = if (isReminder) ActivityCategory.OTHER else current.category,
                isAllDay = current.isAllDay,
                startTimeMillis = effectiveStartTime,
                endTimeMillis = if (isReminder) effectiveStartTime else current.endMillis,
                location = if (isReminder || current.location.trim().equals("location", ignoreCase = true)) "" else current.location.trim(),
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
                isCompleted = false,
                reminderCategory = if (isReminder) current.selectedReminderCategory.name else "My reminders",
                reminderCategoryColorHex = if (isReminder) current.selectedReminderCategory.colorHex else 0xFF9D7BFC,
                reminderCategoryIcon = if (isReminder) current.selectedReminderCategory.iconName else "bell"
            )

            if (current.editingActivityId != null) {
                val targetId = current.editingActivityId
                val updatedActivity = activity.copy(id = targetId)
                activityRepository.updateActivity(updatedActivity)

                if (!isReminder) {
                    val existingAttachments = linkRepository?.getAttachmentsForActivity(targetId)?.firstOrNull() ?: emptyList()
                    val existingUris = existingAttachments.map { it.uriString }.toSet()
                    current.attachments.forEach { att ->
                        if (att.filePath !in existingUris) {
                            linkRepository?.insertAttachment(
                                ActivityAttachment(
                                    activityId = targetId,
                                    fileName = att.fileName,
                                    uriString = att.filePath,
                                    mimeType = att.mimeType,
                                    sizeBytes = att.sizeBytes
                                )
                            )
                        }
                    }
                }

                scheduler.schedule(updatedActivity)

                _state.update { it.copy(isSaving = false) }
                _events.emit(CreateEntryEvent.SavedSuccessfully)
                return@launch
            }

            val newId = activityRepository.insertActivity(activity)
            val savedActivity = activity.copy(id = newId)

            if (!isReminder) {
                current.attachments.forEach { att ->
                    linkRepository?.insertAttachment(
                        ActivityAttachment(
                            activityId = newId,
                            fileName = att.fileName,
                            uriString = att.filePath,
                            mimeType = att.mimeType,
                            sizeBytes = att.sizeBytes
                        )
                    )
                }
            }

            scheduler.schedule(savedActivity)

            _state.update { it.copy(isSaving = false) }
            _events.emit(CreateEntryEvent.SavedSuccessfully)
        }
    }

    fun addNoteLabel(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            settingsRepository?.addNoteLabel(trimmed)
        }
    }

    fun updateNoteLabel(oldName: String, newName: String) {
        val trimmedOld = oldName.trim()
        val trimmedNew = newName.trim()
        if (trimmedOld.isBlank() || trimmedNew.isBlank()) return
        viewModelScope.launch {
            settingsRepository?.updateNoteLabel(trimmedOld, trimmedNew)
        }
    }

    fun deleteNoteLabel(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            settingsRepository?.deleteNoteLabel(trimmed)
        }
    }

    suspend fun ensureDraftNoteId(
        title: String = "",
        content: String = "",
        colorHex: Long = 0xFF000000L,
        labels: List<String> = emptyList()
    ): Long {
        val currentEditId = _state.value.editingActivityId
        if (currentEditId != null) return currentEditId

        return withContext(NonCancellable) {
            val now = System.currentTimeMillis()
            val trimmedTitle = title.trim()
            val effectiveTitle = trimmedTitle.ifBlank { "Untitled Note" }
            val categoryString = if (labels.isNotEmpty()) labels.joinToString(",") else "Notes"

            val activity = ActivityItem(
                title = effectiveTitle,
                description = content.trim(),
                entryType = EntryType.NOTE,
                category = ActivityCategory.OTHER,
                isAllDay = false,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                location = "",
                priority = ActivityPriority.MEDIUM,
                reminderMinutesBefore = -1,
                reminderMinutesList = emptyList(),
                recurrenceMode = RecurrenceMode.NONE,
                isCompleted = false,
                reminderCategory = categoryString,
                reminderCategoryColorHex = colorHex,
                createdAtMillis = now
            )

            val newId = activityRepository.insertActivity(activity)
            noteRepository.insertNote(
                ActivityNote(
                    activityId = newId,
                    title = effectiveTitle,
                    content = content.trim(),
                    updatedAtMillis = now
                )
            )
            _state.update { it.copy(editingActivityId = newId) }
            newId
        }
    }

    fun saveNote(
        title: String,
        content: String,
        colorHex: Long = 0xFF000000L,
        labels: List<String> = emptyList()
    ) {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()
        val currentEditId = _state.value.editingActivityId

        if (trimmedTitle.isBlank() && trimmedContent.isBlank()) {
            return
        }

        viewModelScope.launch {
            // NonCancellable guarantees the Room database write commits even if the screen pops immediately
            withContext(NonCancellable) {
                _state.update { it.copy(isSaving = true) }
                val now = System.currentTimeMillis()
                val effectiveTitle = trimmedTitle.ifBlank {
                    val firstLine = trimmedContent.lineSequence().firstOrNull()?.trim() ?: ""
                    val cleanLine = firstLine
                        .removePrefix("[ ] ")
                        .removePrefix("[x] ")
                        .removePrefix("[X] ")
                        .trim()
                    if (cleanLine.isNotBlank()) cleanLine.take(40) else "Untitled Note"
                }
                val categoryString = if (labels.isNotEmpty()) labels.joinToString(",") else "Notes"

                if (currentEditId != null) {
                    val existing = activityRepository.getActivityById(currentEditId).firstOrNull()
                    val updatedActivity = (existing ?: ActivityItem(
                        id = currentEditId,
                        title = effectiveTitle,
                        description = trimmedContent,
                        entryType = EntryType.NOTE,
                        category = ActivityCategory.OTHER,
                        isAllDay = false,
                        startTimeMillis = 0L,
                        endTimeMillis = 0L,
                        location = "",
                        priority = ActivityPriority.MEDIUM,
                        reminderMinutesBefore = -1,
                        reminderMinutesList = emptyList(),
                        recurrenceMode = RecurrenceMode.NONE,
                        isCompleted = false,
                        reminderCategory = categoryString,
                        reminderCategoryColorHex = colorHex,
                        createdAtMillis = now
                    )).copy(
                        title = effectiveTitle,
                        description = trimmedContent,
                        entryType = EntryType.NOTE,
                        startTimeMillis = 0L,
                        endTimeMillis = 0L,
                        reminderCategory = categoryString,
                        reminderCategoryColorHex = colorHex
                    )
                    activityRepository.updateActivity(updatedActivity)

                    val existingNotes = noteRepository.getNotesForActivity(currentEditId).firstOrNull() ?: emptyList()
                    val targetNote = existingNotes.firstOrNull()
                    if (targetNote != null) {
                        noteRepository.updateNote(
                            targetNote.copy(
                                title = effectiveTitle,
                                content = trimmedContent,
                                updatedAtMillis = now
                            )
                        )
                    } else {
                        noteRepository.insertNote(
                            ActivityNote(
                                activityId = currentEditId,
                                title = effectiveTitle,
                                content = trimmedContent,
                                updatedAtMillis = now
                            )
                        )
                    }
                } else {
                    val activity = ActivityItem(
                        title = effectiveTitle,
                        description = trimmedContent,
                        entryType = EntryType.NOTE,
                        category = ActivityCategory.OTHER,
                        isAllDay = false,
                        startTimeMillis = 0L,
                        endTimeMillis = 0L,
                        location = "",
                        priority = ActivityPriority.MEDIUM,
                        reminderMinutesBefore = -1,
                        reminderMinutesList = emptyList(),
                        recurrenceMode = RecurrenceMode.NONE,
                        isCompleted = false,
                        reminderCategory = categoryString,
                        reminderCategoryColorHex = colorHex,
                        createdAtMillis = now
                    )

                    val newId = activityRepository.insertActivity(activity)
                    noteRepository.insertNote(
                        ActivityNote(
                            activityId = newId,
                            title = effectiveTitle,
                            content = trimmedContent,
                            updatedAtMillis = now
                        )
                    )
                    _state.update { it.copy(editingActivityId = newId) }
                }

                _state.update { it.copy(isSaving = false) }
                _events.emit(CreateEntryEvent.SavedSuccessfully)
            }
        }
    }

    fun deleteCurrentNote() {
        val currentEditId = _state.value.editingActivityId ?: return
        viewModelScope.launch {
            withContext(NonCancellable) {
                val notes = noteRepository.getNotesForActivity(currentEditId).firstOrNull() ?: emptyList()
                for (n in notes) {
                    noteRepository.deleteNote(n.id)
                }
                activityRepository.deleteActivity(currentEditId)
                _events.emit(CreateEntryEvent.SavedSuccessfully)
            }
        }
    }

    suspend fun makeCopyOfNote(
        title: String,
        content: String,
        colorHex: Long = 0xFF000000L,
        labels: List<String> = emptyList()
    ): Long {
        val trimmedTitle = title.trim()
        val trimmedContent = content.trim()
        if (trimmedTitle.isBlank() && trimmedContent.isBlank()) {
            return -1L
        }

        return withContext(NonCancellable) {
            val now = System.currentTimeMillis()
            val effectiveTitle = trimmedTitle.ifBlank {
                val firstLine = trimmedContent.lineSequence().firstOrNull()?.trim() ?: ""
                val cleanLine = firstLine
                    .removePrefix("[ ] ")
                    .removePrefix("[x] ")
                    .removePrefix("[X] ")
                    .trim()
                if (cleanLine.isNotBlank()) cleanLine.take(40) else "Untitled Note"
            }
            val categoryString = if (labels.isNotEmpty()) labels.joinToString(",") else "Notes"

            val activity = ActivityItem(
                title = effectiveTitle,
                description = trimmedContent,
                entryType = EntryType.NOTE,
                category = ActivityCategory.OTHER,
                isAllDay = false,
                startTimeMillis = 0L,
                endTimeMillis = 0L,
                location = "",
                priority = ActivityPriority.MEDIUM,
                reminderMinutesBefore = -1,
                reminderMinutesList = emptyList(),
                recurrenceMode = RecurrenceMode.NONE,
                isCompleted = false,
                reminderCategory = categoryString,
                reminderCategoryColorHex = colorHex,
                createdAtMillis = now
            )

            val newId = activityRepository.insertActivity(activity)
            noteRepository.insertNote(
                ActivityNote(
                    activityId = newId,
                    title = effectiveTitle,
                    content = trimmedContent,
                    updatedAtMillis = now
                )
            )

            val currentId = _state.value.editingActivityId
            if (currentId != null && currentId > 0L && linkRepository != null) {
                val existingAttachments = linkRepository.getAttachmentsForActivity(currentId).firstOrNull() ?: emptyList()
                existingAttachments.forEach { att ->
                    linkRepository.insertAttachment(
                        att.copy(id = 0L, activityId = newId, createdAtMillis = now)
                    )
                }
            }

            newId
        }
    }
}
