package com.shejan.acontinue.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class DiaryUiState(
    val searchQuery: String = "",
    val activities: List<ActivityItem> = emptyList(),
    val notes: List<ActivityNote> = emptyList(),
    val completedActivitiesCount: Int = 0,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        _searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    combine(
                        activityRepository.getAllActivities(),
                        noteRepository.searchNotes("")
                    ) { activities, notes ->
                        val eventActivities = activities.filter { it.entryType == EntryType.ACTIVITY }
                        Pair(eventActivities, notes)
                    }
                } else {
                    combine(
                        activityRepository.searchActivities(query),
                        noteRepository.searchNotes(query)
                    ) { activities, notes ->
                        val eventActivities = activities.filter { it.entryType == EntryType.ACTIVITY }
                        Pair(eventActivities, notes)
                    }
                }
            }
            .onEach { (activities, notes) ->
                _uiState.value = _uiState.value.copy(
                    searchQuery = _searchQuery.value,
                    activities = activities,
                    notes = notes,
                    completedActivitiesCount = activities.count { it.isCompleted },
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
