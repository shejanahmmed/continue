package com.shejan.acontinue.presentation.search

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.core.util.LinkHelper
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import com.shejan.acontinue.domain.repository.SettingsRepository
import com.shejan.acontinue.presentation.create.DEFAULT_NOTE_COLOR_HEX
import com.shejan.acontinue.presentation.create.isDefaultNoteColor
import com.shejan.acontinue.presentation.diary.DiaryNoteItem
import com.shejan.acontinue.presentation.diary.NoteCardPreviewParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

private val LIST_LINE_REGEX = Regex("""^\s*(\[[\sxX]\]|[-*•]|\d+[\.\)])\s+""")

enum class SearchNoteType {
    LISTS,
    URLS,
    AUDIO,
    PHOTOS
}

@Immutable
data class SearchUiState(
    val searchQuery: String = "",
    val selectedTypes: Set<SearchNoteType> = emptySet(),
    val selectedLabels: Set<String> = emptySet(),
    val selectedColors: Set<Long> = emptySet(),
    val results: List<DiaryNoteItem> = emptyList(),
    val availableLabels: List<String> = emptyList(),
    val isLoading: Boolean = false
)

private data class SearchFilterParams(
    val query: String,
    val selectedTypes: Set<SearchNoteType>,
    val selectedLabels: Set<String>,
    val selectedColors: Set<Long>
)

private data class BaseSearchData(
    val noteItems: List<DiaryNoteItem> = emptyList(),
    val availableLabels: List<String> = emptyList()
)

class SearchViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository? = null
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTypes = MutableStateFlow<Set<SearchNoteType>>(emptySet())
    private val _selectedLabels = MutableStateFlow<Set<String>>(emptySet())
    private val _selectedColors = MutableStateFlow<Set<Long>>(emptySet())
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        val savedLabelsFlow = settingsRepository?.getNoteLabels() ?: flowOf(emptyList())

        // Stage 1: Base search data cache flow (runs ONLY when database emits)
        val baseDataFlow = combine(
            activityRepository.getAllActivities(),
            noteRepository.searchNotes(""),
            savedLabelsFlow
        ) { activities, notes, savedLabels ->
            val allNoteItems = buildSearchNoteItems(activities, notes)
            val labelsFromNotes = allNoteItems.flatMap { it.labels }
            val combinedLabels = (savedLabels + labelsFromNotes)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .sortedWith(String.CASE_INSENSITIVE_ORDER)
            BaseSearchData(allNoteItems, combinedLabels)
        }

        // Stage 2: Filter parameters flow (query, type, label, and color selections)
        val filterParamsFlow = combine(
            _searchQuery,
            _selectedTypes,
            _selectedLabels,
            _selectedColors
        ) { query, selectedTypes, selectedLabels, selectedColors ->
            SearchFilterParams(query, selectedTypes, selectedLabels, selectedColors)
        }

        // Fast filtering pipeline: executes against pre-cached in-memory items on keystrokes
        combine(
            filterParamsFlow,
            baseDataFlow
        ) { params, baseData ->
            val query = params.query
            val selectedTypes = params.selectedTypes
            val selectedLabels = params.selectedLabels
            val selectedColors = params.selectedColors

            val isFilterOrQueryActive = query.isNotBlank() ||
                selectedTypes.isNotEmpty() ||
                selectedLabels.isNotEmpty() ||
                selectedColors.isNotEmpty()

            if (!isFilterOrQueryActive) {
                SearchUiState(
                    searchQuery = query,
                    selectedTypes = selectedTypes,
                    selectedLabels = selectedLabels,
                    selectedColors = selectedColors,
                    results = emptyList(),
                    availableLabels = baseData.availableLabels,
                    isLoading = false
                )
            } else {
                val trimmedQuery = query.trim()
                val hasQuery = trimmedQuery.isNotBlank()

                val filtered = baseData.noteItems.filter { note ->
                    // 1. Color filter: instant Long set comparison
                    if (selectedColors.isNotEmpty()) {
                        val normalized = normalizeColor(note.colorHex)
                        if (!selectedColors.contains(normalized)) return@filter false
                    }

                    // 2. Type filter: fast flag checks and guarded URL verification
                    if (selectedTypes.isNotEmpty()) {
                        val matchesAllTypes = selectedTypes.all { type ->
                            when (type) {
                                SearchNoteType.LISTS -> note.isChecklist || hasListSyntax(note.content)
                                SearchNoteType.URLS -> hasUrls(note.title, note.content)
                                SearchNoteType.AUDIO -> note.audioCount > 0
                                SearchNoteType.PHOTOS -> note.photoCount > 0
                            }
                        }
                        if (!matchesAllTypes) return@filter false
                    }

                    // 3. Label filter: note label matching
                    if (selectedLabels.isNotEmpty()) {
                        val matchesAllLabels = selectedLabels.all { selected ->
                            note.labels.any { it.equals(selected, ignoreCase = true) }
                        }
                        if (!matchesAllLabels) return@filter false
                    }

                    // 4. Query filter: only evaluated for notes passing all other active filters
                    if (hasQuery) {
                        val matchesQuery = note.title.contains(trimmedQuery, ignoreCase = true) ||
                            note.previewText.contains(trimmedQuery, ignoreCase = true) ||
                            note.content.contains(trimmedQuery, ignoreCase = true) ||
                            note.labels.any { it.contains(trimmedQuery, ignoreCase = true) }
                        if (!matchesQuery) return@filter false
                    }

                    true
                }

                SearchUiState(
                    searchQuery = query,
                    selectedTypes = selectedTypes,
                    selectedLabels = selectedLabels,
                    selectedColors = selectedColors,
                    results = filtered,
                    availableLabels = baseData.availableLabels,
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

    fun toggleLabelFilter(label: String) {
        _selectedLabels.update { current ->
            if (current.contains(label)) current - label else current + label
        }
    }

    fun clearLabelFilters() {
        _selectedLabels.value = emptySet()
    }

    fun toggleColorFilter(colorHex: Long) {
        val normalized = normalizeColor(colorHex)
        _selectedColors.update { current ->
            if (current.contains(normalized)) current - normalized else current + normalized
        }
    }

    fun clearColorFilters() {
        _selectedColors.value = emptySet()
    }

    private fun normalizeColor(colorHex: Long): Long {
        return if (isDefaultNoteColor(colorHex)) DEFAULT_NOTE_COLOR_HEX else colorHex
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

    private fun hasUrls(title: String, content: String): Boolean {
        val hasUrlSubstring = title.contains("http://", ignoreCase = true) ||
            title.contains("https://", ignoreCase = true) ||
            title.contains("www.", ignoreCase = true) ||
            content.contains("http://", ignoreCase = true) ||
            content.contains("https://", ignoreCase = true) ||
            content.contains("www.", ignoreCase = true)

        if (!hasUrlSubstring) return false

        return LinkHelper.extractUrls(title).isNotEmpty() || LinkHelper.extractUrls(content).isNotEmpty()
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

            val audioCount = NoteCardPreviewParser.AUDIO_TAG_REGEX.findAll(rawContent).count()
            val photoCount = NoteCardPreviewParser.PHOTO_TAG_REGEX.findAll(rawContent).count()

            val cleanPreview = rawContent
                .replace(NoteCardPreviewParser.AUDIO_TAG_REGEX, "")
                .replace(NoteCardPreviewParser.PHOTO_TAG_REGEX, "")
                .trim()

            val lines = cleanPreview.lines().map { it.trim() }.filter { it.isNotBlank() }
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
                val audioCount = NoteCardPreviewParser.AUDIO_TAG_REGEX.findAll(note.content).count()
                val photoCount = NoteCardPreviewParser.PHOTO_TAG_REGEX.findAll(note.content).count()
                val cleanPreview = note.content
                    .replace(NoteCardPreviewParser.AUDIO_TAG_REGEX, "")
                    .replace(NoteCardPreviewParser.PHOTO_TAG_REGEX, "")
                    .trim()
                val lines = cleanPreview.lines().map { it.trim() }.filter { it.isNotBlank() }
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
