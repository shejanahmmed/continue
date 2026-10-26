package com.shejan.acontinue.presentation.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
    val isChecklist: Boolean = false
)

data class DiaryUiState(
    val searchQuery: String = "",
    val notes: List<DiaryNoteItem> = emptyList(),
    val isLoading: Boolean = false,
    val sortOption: DiarySortOption = DiarySortOption.DEFAULT
)

class DiaryViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOption = MutableStateFlow(DiarySortOption.DEFAULT)
    private val _uiState = MutableStateFlow(DiaryUiState(isLoading = true))
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    init {
        combine(
            _searchQuery,
            _sortOption,
            activityRepository.getAllActivities(),
            noteRepository.searchNotes("")
        ) { query, sortOption, activities, notes ->
            val allNoteItems = buildDiaryNoteItems(activities, notes)
            val filtered = if (query.isBlank()) {
                allNoteItems
            } else {
                allNoteItems.filter { note ->
                    note.title.contains(query, ignoreCase = true) ||
                    note.previewText.contains(query, ignoreCase = true) ||
                    note.labels.any { it.contains(query, ignoreCase = true) }
                }
            }
            val sorted = when (sortOption) {
                DiarySortOption.DEFAULT -> filtered.sortedByDescending { it.createdAtMillis }
                DiarySortOption.DATE_CREATED -> filtered.sortedBy { it.createdAtMillis }
                DiarySortOption.DATE_MODIFIED -> filtered.sortedByDescending { it.updatedAtMillis }
            }
            DiaryUiState(
                searchQuery = query,
                notes = sorted,
                isLoading = false,
                sortOption = sortOption
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

    private fun buildDiaryNoteItems(
        activities: List<ActivityItem>,
        notes: List<ActivityNote>
    ): List<DiaryNoteItem> {
        val noteActivities = activities.filter { it.entryType == EntryType.NOTE }
        val notesByActivityId = notes.groupBy { it.activityId }
        val result = mutableListOf<DiaryNoteItem>()
        val seenActivityIds = mutableSetOf<Long>()

        for (act in noteActivities) {
            seenActivityIds.add(act.id)
            val matchingNote = notesByActivityId[act.id]?.firstOrNull()
            val rawTitle = matchingNote?.title?.takeIf { it.isNotBlank() } ?: act.title
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

            val color = if (act.reminderCategoryColorHex != 0L && act.reminderCategoryColorHex != 0xFF121212L) {
                act.reminderCategoryColorHex
            } else {
                0xFF000000L
            }

            val labels = if (act.reminderCategory.isBlank() || act.reminderCategory.equals("Notes", ignoreCase = true)) {
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
                    isChecklist = isChecklist
                )
            )
        }

        for (note in notes) {
            if (!seenActivityIds.contains(note.activityId)) {
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
                        colorHex = 0xFF000000L,
                        labels = emptyList(),
                        createdAtMillis = note.updatedAtMillis,
                        updatedAtMillis = note.updatedAtMillis,
                        audioCount = audioCount,
                        photoCount = photoCount,
                        isChecklist = isChecklist
                    )
                )
            }
        }

        return result
    }
}
