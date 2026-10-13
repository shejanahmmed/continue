package com.shejan.acontinue.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.core.notification.ActivityScheduler
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.ReminderOffset
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

data class CreateEntryState(
    val entryType: EntryType = EntryType.ACTIVITY,
    val title: String = "",
    val emoji: String = "",
    val recentEmojis: List<String> = emptyList(),
    val category: ActivityCategory = ActivityCategory.PERSONAL,
    val isAllDay: Boolean = false,
    val startMillis: Long = System.currentTimeMillis() + 3600000L,
    val endMillis: Long = System.currentTimeMillis() + 7200000L,
    val location: String = "",
    val reminderOffset: ReminderOffset = ReminderOffset.FIFTEEN_MINUTES,
    val recurrenceMode: RecurrenceMode = RecurrenceMode.NONE,
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
                _state.update { it.copy(recentEmojis = userSettings.recentEmojis) }
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
    fun updateLocation(location: String) = _state.update { it.copy(location = location) }
    fun updateReminderOffset(offset: ReminderOffset) = _state.update { it.copy(reminderOffset = offset) }
    fun updateRecurrence(mode: RecurrenceMode) = _state.update { it.copy(recurrenceMode = mode) }
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
                reminderMinutesBefore = current.reminderOffset.minutesBefore,
                recurrenceMode = current.recurrenceMode,
                isCompleted = false
            )

            val newId = activityRepository.insertActivity(activity)
            val savedActivity = activity.copy(id = newId)

            // If initial notes were provided, also record them as the first workspace note
            if (current.notes.isNotBlank()) {
                noteRepository.insertNote(
                    ActivityNote(
                        activityId = newId,
                        title = "Initial Notes",
                        content = current.notes.trim()
                    )
                )
            }

            // Schedule exact notification
            scheduler.schedule(savedActivity)

            _state.update { it.copy(isSaving = false) }
            _events.emit(CreateEntryEvent.SavedSuccessfully)
        }
    }
}
