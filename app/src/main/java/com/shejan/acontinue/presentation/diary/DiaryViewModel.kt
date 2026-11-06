package com.shejan.acontinue.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.DiarySortOption
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
    val notes: List<DiaryNoteItem> = emptyList(),
    val isLoading: Boolean = false,
    val sortOption: DiarySortOption = DiarySortOption.DEFAULT,
    val selectedKeys: Set<String> = emptySet(),
    val isGridMode: Boolean = false
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
    val sortOption: DiarySortOption,
    val selectedKeys: Set<String>,
    val pinnedKeys: Set<String>,
    val isGridMode: Boolean
)

class DiaryViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val _localSortOption = MutableStateFlow(DiarySortOption.DEFAULT)
    private val _selectedKeys = MutableStateFlow<Set<String>>(emptySet())
    private val _localPinnedKeys = MutableStateFlow<Set<String>>(emptySet())
    private val _localGridMode = MutableStateFlow(false)

    private val sortOptionFlow: Flow<DiarySortOption> =
        settingsRepository?.getDiarySortOption() ?: _localSortOption.asStateFlow()

    private val pinnedKeysFlow: Flow<Set<String>> =
        settingsRepository?.getPinnedNoteIds() ?: _localPinnedKeys.asStateFlow()

    private val gridModeFlow: Flow<Boolean> =
        settingsRepository?.isDiaryGridMode() ?: _localGridMode.asStateFlow()

    private val _uiState = MutableStateFlow(DiaryUiState(isLoading = true))
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        val filterFlow = combine(
            sortOptionFlow,
            _selectedKeys,
            pinnedKeysFlow,
            gridModeFlow
        ) { sortOption, selectedKeys, pinnedKeys, isGridMode ->
            FilterParams(sortOption, selectedKeys, pinnedKeys, isGridMode)
        }

        combine(
            filterFlow,
            activityRepository.getAllActivities(),
            noteRepository.searchNotes("")
        ) { params, activities, notes ->
            val allNoteItems = buildDiaryNoteItems(activities, notes, params.pinnedKeys)
            val (pinned, unpinned) = allNoteItems.partition { it.isPinned }

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
                notes = sorted,
                isLoading = false,
                sortOption = params.sortOption,
                selectedKeys = validSelectedKeys,
                isGridMode = params.isGridMode
            )
        }
        .onEach { state ->
            _uiState.value = state
        }
        .launchIn(viewModelScope)
    }

    fun onSortOptionChange(option: DiarySortOption) {
        viewModelScope.launch {
            if (settingsRepository != null) {
                settingsRepository.setDiarySortOption(option)
            } else {
                _localSortOption.value = option
            }
        }
    }

    fun toggleSelection(itemKey: String) {
        _selectedKeys.update { current ->
            if (itemKey in current) current - itemKey else current + itemKey
        }
    }

    fun clearSelection() {
        _selectedKeys.value = emptySet()
    }

    fun togglePinSelectedNotes() {
        val selected = _selectedKeys.value
        if (selected.isEmpty()) return
        val selectedNotes = _uiState.value.notes.filter { it.itemKey in selected }
        val willUnpin = _uiState.value.areAllSelectedPinned
        val targetPinnedState = !willUnpin

        val allKeysToUpdate = mutableSetOf<String>()
        for (note in selectedNotes) {
            allKeysToUpdate.add(note.itemKey)
            if (note.activityId > 0L) {
                allKeysToUpdate.add("act_${note.activityId}")
            }
            if (note.noteId > 0L) {
                allKeysToUpdate.add("note_${note.noteId}")
            }
        }
        allKeysToUpdate.addAll(selected)

        viewModelScope.launch {
            if (settingsRepository != null) {
                settingsRepository.setNotesPinned(allKeysToUpdate, targetPinnedState)
            } else {
                _localPinnedKeys.update { current ->
                    if (targetPinnedState) current + allKeysToUpdate else current - allKeysToUpdate
                }
            }
            clearSelection()
        }
    }

    fun deleteSelectedNotes(): List<DiaryNoteItem> {
        val selected = _selectedKeys.value
        if (selected.isEmpty()) return emptyList()
        val notesToDelete = _uiState.value.notes.filter { it.itemKey in selected }
        val allKeysToRemove = mutableSetOf<String>()
        for (note in notesToDelete) {
            allKeysToRemove.add(note.itemKey)
            if (note.activityId > 0L) allKeysToRemove.add("act_${note.activityId}")
            if (note.noteId > 0L) allKeysToRemove.add("note_${note.noteId}")
        }
        allKeysToRemove.addAll(selected)

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
                settingsRepository.setNotesPinned(allKeysToRemove, false)
            } else {
                _localPinnedKeys.update { it - allKeysToRemove }
            }
            clearSelection()
        }
        return notesToDelete
    }

    fun undoDeleteNotes(notes: List<DiaryNoteItem>) {
        if (notes.isEmpty()) return
        viewModelScope.launch {
            for (note in notes) {
                if (note.activityId > 0L) {
                    activityRepository.restoreActivity(note.activityId)
                }
                if (note.noteId > 0L) {
                    noteRepository.restoreNote(note.noteId)
                }
            }
        }
    }

    fun toggleLayoutMode() {
        val nextMode = !_uiState.value.isGridMode
        viewModelScope.launch {
            if (settingsRepository != null) {
                settingsRepository.setDiaryGridMode(nextMode)
            } else {
                _localGridMode.value = nextMode
            }
        }
    }

    fun archiveSelectedNotes(): List<DiaryNoteItem> {
        val selected = _selectedKeys.value
        if (selected.isEmpty()) return emptyList()
        val notesToArchive = _uiState.value.notes.filter { it.itemKey in selected }
        val allKeysToRemove = mutableSetOf<String>()
        for (note in notesToArchive) {
            allKeysToRemove.add(note.itemKey)
            if (note.activityId > 0L) allKeysToRemove.add("act_${note.activityId}")
            if (note.noteId > 0L) allKeysToRemove.add("note_${note.noteId}")
        }
        allKeysToRemove.addAll(selected)

        viewModelScope.launch {
            for (note in notesToArchive) {
                if (note.activityId > 0L) {
                    activityRepository.setActivityArchived(note.activityId, true)
                }
                if (note.noteId > 0L) {
                    noteRepository.setNoteArchived(note.noteId, true)
                }
            }
            if (settingsRepository != null) {
                settingsRepository.setNotesPinned(allKeysToRemove, false)
            } else {
                _localPinnedKeys.update { it - allKeysToRemove }
            }
            clearSelection()
        }
        return notesToArchive
    }

    fun undoArchiveNotes(notes: List<DiaryNoteItem>) {
        if (notes.isEmpty()) return
        viewModelScope.launch {
            for (note in notes) {
                if (note.activityId > 0L) {
                    activityRepository.setActivityArchived(note.activityId, false)
                }
                if (note.noteId > 0L) {
                    noteRepository.setNoteArchived(note.noteId, false)
                }
            }
        }
    }

    private fun buildDiaryNoteItems(
        activities: List<ActivityItem>,
        notes: List<ActivityNote>,
        pinnedKeys: Set<String>
    ): List<DiaryNoteItem> {
        val noteActivities = activities.filter { it.entryType == EntryType.NOTE && !it.isDeleted && !it.isArchived }
        val nonNoteActivityIds = activities.filter { it.entryType != EntryType.NOTE }.map { it.id }.toSet()
        val notesByActivityId = notes.groupBy { it.activityId }
        val result = mutableListOf<DiaryNoteItem>()
        val seenActivityIds = mutableSetOf<Long>()

        for (act in noteActivities) {
            seenActivityIds.add(act.id)
            val matchingNote = notesByActivityId[act.id]?.firstOrNull()
            if (matchingNote != null && (matchingNote.isDeleted || matchingNote.isArchived)) {
                continue
            }
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
            val isPinned = itemKey in pinnedKeys ||
                (act.id > 0L && "act_${act.id}" in pinnedKeys) ||
                (matchingNote != null && matchingNote.id > 0L && "note_${matchingNote.id}" in pinnedKeys)

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
            if (!seenActivityIds.contains(note.activityId) && !nonNoteActivityIds.contains(note.activityId) && !note.isDeleted && !note.isArchived) {
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
                val isPinned = itemKey in pinnedKeys ||
                    (note.id > 0L && "note_${note.id}" in pinnedKeys) ||
                    (note.activityId > 0L && "act_${note.activityId}" in pinnedKeys)

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
