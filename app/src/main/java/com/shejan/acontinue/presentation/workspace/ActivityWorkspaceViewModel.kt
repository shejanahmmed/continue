package com.shejan.acontinue.presentation.workspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityLink
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.model.ActivityWorkspaceAggregate
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.ReminderCategory
import com.shejan.acontinue.domain.model.UserSettings
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.LinkRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import com.shejan.acontinue.domain.repository.SettingsRepository
import com.shejan.acontinue.domain.repository.TaskRepository
import com.shejan.acontinue.domain.usecase.GetActivityWorkspaceUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WorkspaceUiState(
    val aggregate: ActivityWorkspaceAggregate? = null,
    val selectedTab: WorkspaceTab = WorkspaceTab.NOTES,
    val isLoading: Boolean = true
)

enum class WorkspaceTab(val title: String) {
    OVERVIEW("Overview"),
    NOTES("Notes"),
    TASKS("Tasks"),
    LINKS("Links")
}

sealed interface WorkspaceEvent {
    data object ActivityDeleted : WorkspaceEvent
}

class ActivityWorkspaceViewModel(
    private val activityId: Long,
    private val getActivityWorkspaceUseCase: GetActivityWorkspaceUseCase,
    private val activityRepository: ActivityRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
    private val linkRepository: LinkRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkspaceUiState())
    val uiState: StateFlow<WorkspaceUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<WorkspaceEvent>()
    val events: SharedFlow<WorkspaceEvent> = _events.asSharedFlow()

    val availableReminderCategories: StateFlow<List<ReminderCategory>> = combine(
        settingsRepository?.getUserSettings() ?: flowOf(UserSettings()),
        activityRepository.getAllActivities()
    ) { settings, activities ->
        val reminderActivities = activities.filter { it.entryType == EntryType.REMINDER }
        val fromReminders = reminderActivities
            .map {
                ReminderCategory(
                    name = it.reminderCategory,
                    colorHex = it.reminderCategoryColorHex,
                    iconName = it.reminderCategoryIcon
                )
            }
            .distinctBy { it.name }
        val merged = (settings.reminderCategories + fromReminders).distinctBy { it.name }
        if (merged.none { it.name.equals(ReminderCategory.DEFAULT.name, ignoreCase = true) }) {
            listOf(ReminderCategory.DEFAULT) + merged
        } else {
            merged
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(ReminderCategory.DEFAULT))

    val categoryReminderCounts: StateFlow<Map<String, Int>> = activityRepository.getAllActivities()
        .map { activities ->
            activities.filter { it.entryType == EntryType.REMINDER }
                .groupingBy { it.reminderCategory }
                .eachCount()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        loadWorkspace()
    }

    private fun loadWorkspace() {
        getActivityWorkspaceUseCase(activityId)
            .onEach { agg ->
                _uiState.value = _uiState.value.copy(
                    aggregate = agg,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: WorkspaceTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun addNote(title: String, content: String) {
        if (content.isBlank() && title.isBlank()) return
        viewModelScope.launch {
            noteRepository.insertNote(
                ActivityNote(
                    activityId = activityId,
                    title = if (title.isBlank()) "Note" else title.trim(),
                    content = content.trim()
                )
            )
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
        }
    }

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.insertTask(
                ActivityTask(
                    activityId = activityId,
                    title = title.trim(),
                    isCompleted = false
                )
            )
        }
    }

    fun toggleTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(taskId, isCompleted)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }

    fun addLink(url: String, title: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            linkRepository.insertLink(
                ActivityLink(
                    activityId = activityId,
                    url = url.trim(),
                    title = if (title.isBlank()) url.trim() else title.trim()
                )
            )
        }
    }

    fun deleteLink(linkId: Long) {
        viewModelScope.launch {
            linkRepository.deleteLink(linkId)
        }
    }

    fun toggleActivityCompletion(isCompleted: Boolean) {
        viewModelScope.launch {
            activityRepository.toggleActivityCompletion(activityId, isCompleted)
        }
    }

    fun deleteActivity() {
        viewModelScope.launch {
            activityRepository.deleteActivity(activityId)
            _events.emit(WorkspaceEvent.ActivityDeleted)
        }
    }

    fun updateReminderCategory(category: ReminderCategory) {
        viewModelScope.launch {
            activityRepository.updateActivityReminderCategory(
                activityId = activityId,
                newCategory = category.name,
                newColorHex = category.colorHex,
                newIcon = category.iconName
            )
        }
    }

    fun addReminderCategory(name: String, colorHex: Long, iconName: String) {
        viewModelScope.launch {
            val created = settingsRepository?.addReminderCategory(name, colorHex, iconName)
                ?: ReminderCategory(name = name, colorHex = colorHex, iconName = iconName)
            activityRepository.updateReminderCategoryDetails(name, colorHex, iconName)
            updateReminderCategory(created)
        }
    }

    fun editReminderCategory(oldCategory: ReminderCategory, newName: String, newColorHex: Long, newIconName: String) {
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
        }
    }
}
