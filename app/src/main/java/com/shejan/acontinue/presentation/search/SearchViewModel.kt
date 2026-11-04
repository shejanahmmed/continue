package com.shejan.acontinue.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.core.util.LinkHelper
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import com.shejan.acontinue.domain.repository.SettingsRepository
import com.shejan.acontinue.presentation.diary.DiaryNoteItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

private val AUDIO_TAG_REGEX = Regex("""!\[audio\]\(([^)]+)\)""")
private val PHOTO_TAG_REGEX = Regex("""!\[photo\]\(([^)]+)\)""")
private val LIST_LINE_REGEX = Regex("""^\s*(\[[\sxX]\]|[-*•]|\d+[\.\)])\s+""")

enum class SearchNoteType {
    LISTS,
    URLS,
    AUDIO,
    PHOTOS
}

data class SearchUiState(
    val searchQuery: String = "",
    val selectedTypes: Set<SearchNoteType> = emptySet(),
    val results: List<DiaryNoteItem> = emptyList(),
    val availableLabels: List<String> = emptyList(),
    val isLoading: Boolean = false
)

class SearchViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTypes = MutableStateFlow<Set<SearchNoteType>>(emptySet())
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        val savedLabelsFlow = settingsRepository?.getNoteLabels() ?: flowOf(emptyList())

        combine(
            _searchQuery,
            _selectedTypes,
            activityRepository.getAllActivities(),
            noteRepository.searchNotes(""),
            savedLabelsFlow
        ) { query, selectedTypes, activities, notes, savedLabels ->
            val allNoteItems = buildSearchNoteItems(activities, notes)
            val labelsFromNotes = allNoteItems.flatMap { it.labels }
            val combinedLabels = (savedLabels + labelsFromNotes)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .sortedWith(String.CASE_INSENSITIVE_ORDER)

            val isFilterOrQueryActive = query.isNotBlank() || selectedTypes.isNotEmpty()

            if (!isFilterOrQueryActive) {
                SearchUiState(
                    searchQuery = query,
                    selectedTypes = selectedTypes,
                    results = emptyList(),
                    availableLabels = combinedLabels,
                    isLoading = false
                )
            } else {
                val filtered = allNoteItems.filter { note ->
                    val matchesQuery = if (query.isBlank()) {
                        true
                    } else {
                        note.title.contains(query, ignoreCase = true) ||
                        note.previewText.contains(query, ignoreCase = true) ||
                        note.labels.any { it.contains(query, ignoreCase = true) }
                    }

                    val matchesTypes = if (selectedTypes.isEmpty()) {
                        true
                    } else {
                        selectedTypes.all { type ->
                            when (type) {
                                SearchNoteType.LISTS -> note.isChecklist || hasListSyntax(note.content)
                                SearchNoteType.URLS -> LinkHelper.extractUrls(note.title).isNotEmpty() ||
                                    LinkHelper.extractUrls(note.content).isNotEmpty()
                                SearchNoteType.AUDIO -> note.audioCount > 0
                                SearchNoteType.PHOTOS -> note.photoCount > 0
                            }
                        }
                    }

                    matchesQuery && matchesTypes
                }
                SearchUiState(
                    searchQuery = query,
                    selectedTypes = selectedTypes,
                    results = filtered,
                    availableLabels = combinedLabels,
                    isLoading = false
                )
            }
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleTypeFilter(type: SearchNoteType) {
        _selectedTypes.update { current ->
            if (current.contains(type)) current - type else current + type
        }
    }

    fun clearTypeFilters() {
        _selectedTypes.value = emptySet()
    }

    private fun hasListSyntax(text: String): Boolean {
        return text.lines().any { line ->
            val trimmed = line.trimStart()
            trimmed.startsWith("[ ] ") ||
            trimmed.startsWith("[x] ") ||
            trimmed.startsWith("[X] ") ||
            trimmed.startsWith("- ") ||
            trimmed.startsWith("* ") ||
            trimmed.startsWith("• ") ||
            LIST_LINE_REGEX.containsMatchIn(line)
        }
    }

    private fun buildSearchNoteItems(
        activities: List<ActivityItem>,
        notes: List<ActivityNote>
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
                    isPinned = false
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
                        isPinned = false
                    )
                )
            }
        }

        return result
    }
}
