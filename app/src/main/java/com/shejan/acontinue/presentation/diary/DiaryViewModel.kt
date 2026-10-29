package com.shejan.acontinue.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import com.shejan.acontinue.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val AUDIO_TAG_REGEX = Regex("""!\[audio\]\(([^)]+)\)""")
private val PHOTO_TAG_REGEX = Regex("""!\[photo\]\(([^)]+)\)""")

enum class DiarySortOption(val displayName: String) {
    DEFAULT("Default"),
    DATE_CREATED("Date created"),
    DATE_MODIFIED("Date modified")
}

data class DiaryNoteItem(
    val activityId: Long,
    val noteId: Long = 0L,
    val title: String,
    val content: String,
    val previewText: String,
    val colorHex: Long = 0xFF000000L,
    val labels: List<String> = emptyList(),
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long,
    val audioCount: Int = 0,
    val photoCount: Int = 0,
    val isChecklist: Boolean = false,
    val isPinned: Boolean = false
) {
    val itemKey: String
        get() = if (activityId > 0L) "act_$activityId" else "note_$noteId"
}

data class DiaryUiState(
    val searchQuery: String = "",
    val notes: List<DiaryNoteItem> = emptyList(),
    val isLoading: Boolean = false,
    val sortOption: DiarySortOption = DiarySortOption.DEFAULT,
    val selectedKeys: Set<String> = emptySet()
) {
    val isSelectionMode: Boolean
        get() = selectedKeys.isNotEmpty()

    val areAllSelectedPinned: Boolean
        get() {
            if (selectedKeys.isEmpty()) return false
            val selectedNotes = notes.filter { it.itemKey in selectedKeys }
            return selectedNotes.isNotEmpty() && selectedNotes.all { it.isPinned }
        }
}

private data class FilterParams(
    val query: String,
    val sortOption: DiarySortOption,
    val selectedKeys: Set<String>,
    val pinnedKeys: Set<String>
)

class DiaryViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(DiarySortOption.DEFAULT)
    private val _selectedKeys = MutableStateFlow<Set<String>>(emptySet())
    private val _localPinnedKeys = MutableStateFlow<Set<String>>(emptySet())

    private val pinnedKeysFlow: Flow<Set<String>> =
        settingsRepository?.getPinnedNoteIds() ?: _localPinnedKeys.asStateFlow()

    private val _uiState = MutableStateFlow(DiaryUiState(isLoading = true))
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        val filterFlow = combine(
            _searchQuery,
            _sortOption,
            _selectedKeys,
            pinnedKeysFlow
        ) { query, sortOption, selectedKeys, pinnedKeys ->
            FilterParams(query, sortOption, selectedKeys, pinnedKeys)
        }

        combine(
            filterFlow,
            activityRepository.getAllActivities(),
            noteRepository.searchNotes("")
        ) { params, activities, notes ->
            val allNoteItems = buildDiaryNoteItems(activities, notes, params.pinnedKeys)
            val filtered = if (params.query.isBlank()) {
                allNoteItems
            } else {
                allNoteItems.filter { note ->
                    note.title.contains(params.query, ignoreCase = true) ||
                    note.previewText.contains(params.query, ignoreCase = true) ||
                    note.labels.any { it.contains(params.query, ignoreCase = true) }
                }
            }

            val (pinned, unpinned) = filtered.partition { it.isPinned }

            fun sortGroup(list: List<DiaryNoteItem>): List<DiaryNoteItem> {
                return when (params.sortOption) {
                    DiarySortOption.DEFAULT -> list.sortedByDescending { it.createdAtMillis }
                    DiarySortOption.DATE_CREATED -> list.sortedBy { it.createdAtMillis }
                    DiarySortOption.DATE_MODIFIED -> list.sortedByDescending { it.updatedAtMillis }
                }
            }

            val sorted = sortGroup(pinned) + sortGroup(unpinned)

            val validSelectedKeys = params.selectedKeys.filter { key ->
                allNoteItems.any { it.itemKey == key }
            }.toSet()

            DiaryUiState(
                searchQuery = params.query,
                notes = sorted,
                isLoading = false,
                sortOption = params.sortOption,
                selectedKeys = validSelectedKeys
            )
        }
        .onEach { state ->
            _uiState.value = state
        }
        .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOptionChange(option: DiarySortOption) {
        _sortOption.value = option
    }

    fun toggleSelection(itemKey: String) {
        _selectedKeys.update { current ->
            if (itemKey in current) current - itemKey else current + itemKey
        }
    }

    fun selectAll() {
        _selectedKeys.value = _uiState.value.notes.map { it.itemKey }.toSet()
    }

    fun clearSelection() {
        _selectedKeys.value = emptySet()
    }

    fun togglePinSelectedNotes() {
        val selected = _selectedKeys.value
        if (selected.isEmpty()) return
        viewModelScope.launch {
            if (settingsRepository != null) {
                settingsRepository.toggleNotesPinned(selected)
            } else {
                val current = _localPinnedKeys.value
                val allPinned = selected.all { it in current }
                _localPinnedKeys.update {
                    if (allPinned) it - selected else it + selected
                }
            }
            clearSelection()
        }
    }

    fun deleteSelectedNotes() {
        val selected = _selectedKeys.value
        if (selected.isEmpty()) return
        val notesToDelete = _uiState.value.notes.filter { it.itemKey in selected }
        viewModelScope.launch {
            for (note in notesToDelete) {
                if (note.activityId > 0L) {
                    activityRepository.deleteActivity(note.activityId)
                }
                if (note.noteId > 0L) {
                    noteRepository.deleteNote(note.noteId)
                }
            }
            if (settingsRepository != null) {
                settingsRepository.setNotesPinned(selected, false)
            } else {
                _localPinnedKeys.update { it - selected }
            }
            clearSelection()
        }
    }

    private fun buildDiaryNoteItems(
        activities: List<ActivityItem>,
        notes: List<ActivityNote>,
        pinnedKeys: Set<String>
    ): List<DiaryNoteItem> {
        val noteActivities = activities.filter { it.entryType == EntryType.NOTE }
        val nonNoteActivityIds = activities.filter { it.entryType != EntryType.NOTE }.map { it.id }.toSet()
        val notesByActivityId = notes.groupBy { it.activityId }
        val result = mutableListOf<DiaryNoteItem>()
        val seenActivityIds = mutableSetOf<Long>()

        for (act in noteActivities) {
            seenActivityIds.add(act.id)
            val matchingNote = notesByActivityId[act.id]?.firstOrNull()
            val rawTitle = matchingNote?.title ?: act.title
            val title = if (rawTitle.equals("Untitled Note", ignoreCase = true)) "" else rawTitle
            val rawContent = matchingNote?.content ?: act.description
            val updatedAt = matchingNote?.updatedAtMillis ?: act.createdAtMillis

            val audioCount = AUDIO_TAG_REGEX.findAll(rawContent).count()
            val photoCount = PHOTO_TAG_REGEX.findAll(rawContent).count()

            val cleanPreview = rawContent
                .replace(AUDIO_TAG_REGEX, "")
                .replace(PHOTO_TAG_REGEX, "")
                .trim()

            val lines = cleanPreview.lines().filter { it.isNotBlank() }
            val isChecklist = lines.isNotEmpty() && lines.all {
                it.startsWith("[ ] ") || it.startsWith("[x] ") || it.startsWith("[X] ")
            }

            val color = if (matchingNote != null && matchingNote.colorHex != 0L && matchingNote.colorHex != 0xFF121212L) {
                matchingNote.colorHex
            } else if (act.reminderCategoryColorHex != 0L && act.reminderCategoryColorHex != 0xFF121212L) {
                act.reminderCategoryColorHex
            } else {
                0xFF000000L
            }

            val labels = if (matchingNote != null && matchingNote.labels.isNotEmpty()) {
                matchingNote.labels
            } else if (act.reminderCategory.isBlank() || act.reminderCategory.equals("Notes", ignoreCase = true)) {
                emptyList()
            } else {
                act.reminderCategory.split(",").map { it.trim() }.filter { it.isNotBlank() }
            }

            val itemKey = if (act.id > 0L) "act_${act.id}" else "note_${matchingNote?.id ?: 0L}"
            val isPinned = itemKey in pinnedKeys

            result.add(
                DiaryNoteItem(
                    activityId = act.id,
                    noteId = matchingNote?.id ?: 0L,
                    title = title,
                    content = rawContent,
                    previewText = cleanPreview,
                    colorHex = color,
                    labels = labels,
                    createdAtMillis = act.createdAtMillis,
                    updatedAtMillis = updatedAt,
                    audioCount = audioCount,
                    photoCount = photoCount,
                    isChecklist = isChecklist,
                    isPinned = isPinned
                )
            )
        }

        for (note in notes) {
            if (!seenActivityIds.contains(note.activityId) && !nonNoteActivityIds.contains(note.activityId)) {
                val audioCount = AUDIO_TAG_REGEX.findAll(note.content).count()
                val photoCount = PHOTO_TAG_REGEX.findAll(note.content).count()
                val cleanPreview = note.content
                    .replace(AUDIO_TAG_REGEX, "")
                    .replace(PHOTO_TAG_REGEX, "")
                    .trim()
                val lines = cleanPreview.lines().filter { it.isNotBlank() }
                val isChecklist = lines.isNotEmpty() && lines.all {
                    it.startsWith("[ ] ") || it.startsWith("[x] ") || it.startsWith("[X] ")
                }

                val itemKey = if (note.activityId > 0L) "act_${note.activityId}" else "note_${note.id}"
                val isPinned = itemKey in pinnedKeys

                result.add(
                    DiaryNoteItem(
                        activityId = note.activityId,
                        noteId = note.id,
                        title = if (note.title.equals("Untitled Note", ignoreCase = true)) "" else note.title,
                        content = note.content,
                        previewText = cleanPreview,
                        colorHex = if (note.colorHex != 0L && note.colorHex != 0xFF121212L) note.colorHex else 0xFF000000L,
                        labels = note.labels,
                        createdAtMillis = note.updatedAtMillis,
                        updatedAtMillis = note.updatedAtMillis,
                        audioCount = audioCount,
                        photoCount = photoCount,
                        isChecklist = isChecklist,
                        isPinned = isPinned
                    )
                )
            }
        }

        return result
    }
}
