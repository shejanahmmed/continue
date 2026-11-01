package com.shejan.acontinue.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val AUDIO_TAG_REGEX = Regex("""!\[audio\]\(([^)]+)\)""")
private val PHOTO_TAG_REGEX = Regex("""!\[photo\]\(([^)]+)\)""")

data class ArchiveNoteItem(
    val activityId: Long,
    val noteId: Long = 0L,
    val title: String,
    val previewText: String,
    val colorHex: Long = 0xFF000000L,
    val labels: List<String> = emptyList(),
    val archivedAtMillis: Long = 0L
) {
    val uniqueKey: String
        get() = if (activityId > 0L) "act_$activityId" else "note_$noteId"
}

data class ArchiveUiState(
    val searchQuery: String = "",
    val notes: List<ArchiveNoteItem> = emptyList(),
    val isLoading: Boolean = false
)

class ArchiveViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    val uiState: StateFlow<ArchiveUiState> = combine(
        _searchQuery,
        activityRepository.getArchivedActivities(),
        noteRepository.getArchivedNotes()
    ) { query, activities, notes ->
        val activityNotes = activities
            .filter { it.entryType == EntryType.NOTE && it.isArchived && !it.isDeleted }
            .map { act ->
                val cleanDesc = act.description
                    .replace(AUDIO_TAG_REGEX, "")
                    .replace(PHOTO_TAG_REGEX, "")
                    .trim()
                val effectiveTitle = if (act.title.equals("Untitled Note", ignoreCase = true)) "" else act.title
                val labels = if (act.reminderCategory.isBlank() || act.reminderCategory.equals("Notes", ignoreCase = true)) {
                    emptyList()
                } else {
                    act.reminderCategory.split(",").map { it.trim() }.filter { it.isNotBlank() }
                }

                ArchiveNoteItem(
                    activityId = act.id,
                    noteId = 0L,
                    title = effectiveTitle,
                    previewText = cleanDesc,
                    colorHex = if (act.reminderCategoryColorHex != 0L && act.reminderCategoryColorHex != 0xFF121212L) act.reminderCategoryColorHex else 0xFF000000L,
                    labels = labels,
                    archivedAtMillis = act.archivedAtMillis
                )
            }

        val seenActivityIds = activities.map { it.id }.toSet()

        val standaloneNotes = notes
            .filter { it.isArchived && !it.isDeleted && !seenActivityIds.contains(it.activityId) }
            .map { note ->
                val cleanContent = note.content
                    .replace(AUDIO_TAG_REGEX, "")
                    .replace(PHOTO_TAG_REGEX, "")
                    .trim()
                val effectiveTitle = if (note.title.equals("Untitled Note", ignoreCase = true)) "" else note.title

                ArchiveNoteItem(
                    activityId = note.activityId,
                    noteId = note.id,
                    title = effectiveTitle,
                    previewText = cleanContent,
                    colorHex = if (note.colorHex != 0L && note.colorHex != 0xFF121212L) note.colorHex else 0xFF000000L,
                    labels = note.labels,
                    archivedAtMillis = note.archivedAtMillis
                )
            }

        val combined = (activityNotes + standaloneNotes).sortedByDescending { it.archivedAtMillis }

        val filtered = if (query.isBlank()) {
            combined
        } else {
            combined.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.previewText.contains(query, ignoreCase = true) ||
                it.labels.any { lbl -> lbl.contains(query, ignoreCase = true) }
            }
        }

        ArchiveUiState(searchQuery = query, notes = filtered, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ArchiveUiState(isLoading = true)
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun unarchiveNote(item: ArchiveNoteItem) {
        viewModelScope.launch {
            if (item.activityId > 0L) {
                activityRepository.setActivityArchived(item.activityId, false)
            }
            if (item.noteId > 0L) {
                noteRepository.setNoteArchived(item.noteId, false)
            }
        }
    }

    fun deleteNote(item: ArchiveNoteItem) {
        viewModelScope.launch {
            if (item.activityId > 0L) {
                activityRepository.deleteActivity(item.activityId)
            }
            if (item.noteId > 0L) {
                noteRepository.deleteNote(item.noteId)
            }
        }
    }
}

