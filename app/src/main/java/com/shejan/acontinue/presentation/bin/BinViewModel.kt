package com.shejan.acontinue.presentation.bin

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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val AUDIO_TAG_REGEX = Regex("""!\[audio\]\(([^)]+)\)""")
private val PHOTO_TAG_REGEX = Regex("""!\[photo\]\(([^)]+)\)""")

data class BinUiState(
    val items: List<TrashItem> = emptyList(),
    val isLoading: Boolean = false
)

class BinViewModel(
    private val activityRepository: ActivityRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    val uiState: StateFlow<BinUiState> = combine(
        activityRepository.getDeletedActivities(),
        noteRepository.getDeletedNotes()
    ) { activities, notes ->
        val notesByActivityId = notes.filter { it.activityId > 0L }.groupBy { it.activityId }
        val seenActivityIds = activities.map { it.id }.toSet()

        val activityItems = activities.map { act ->
            val matchingNote = notesByActivityId[act.id]?.firstOrNull()

            val rawDesc = act.description.ifBlank { matchingNote?.content.orEmpty() }
            val cleanDesc = rawDesc
                .replace(AUDIO_TAG_REGEX, "")
                .replace(PHOTO_TAG_REGEX, "")
                .trim()

            val rawTitle = act.title.ifBlank { matchingNote?.title.orEmpty() }
            val effectiveTitle = if (rawTitle.equals("Untitled Note", ignoreCase = true)) "" else rawTitle

            val effectiveColor = if (act.entryType == EntryType.NOTE) {
                if (matchingNote != null && matchingNote.colorHex != 0L && matchingNote.colorHex != 0xFF121212L) {
                    matchingNote.colorHex
                } else if (act.reminderCategoryColorHex != 0L && act.reminderCategoryColorHex != 0xFF121212L && act.reminderCategoryColorHex != 0xFF9D7BFC) {
                    act.reminderCategoryColorHex
                } else {
                    0xFF000000L
                }
            } else {
                if (act.reminderCategoryColorHex != 0L && act.reminderCategoryColorHex != 0xFF121212L) act.reminderCategoryColorHex else 0xFF000000L
            }

            val effectiveCategory = if (act.entryType == EntryType.NOTE) {
                if (matchingNote != null && matchingNote.labels.isNotEmpty()) {
                    matchingNote.labels.first()
                } else if (act.reminderCategory.isNotBlank() && !act.reminderCategory.equals("My reminders", ignoreCase = true)) {
                    act.reminderCategory
                } else {
                    "Notes"
                }
            } else {
                act.reminderCategory
            }

            TrashItem(
                id = act.id,
                isActivity = true,
                title = effectiveTitle,
                previewText = cleanDesc,
                entryType = act.entryType,
                deletedAtMillis = maxOf(act.deletedAtMillis, matchingNote?.deletedAtMillis ?: 0L),
                colorHex = effectiveColor,
                categoryName = effectiveCategory,
                linkedNoteId = matchingNote?.id ?: 0L,
                activityId = act.id
            )
        }

        val standaloneNotes = notes
            .filter { it.activityId == 0L || !seenActivityIds.contains(it.activityId) }
            .map { note ->
                val cleanContent = note.content
                    .replace(AUDIO_TAG_REGEX, "")
                    .replace(PHOTO_TAG_REGEX, "")
                    .trim()
                val effectiveTitle = if (note.title.equals("Untitled Note", ignoreCase = true)) "" else note.title

                TrashItem(
                    id = note.id,
                    isActivity = false,
                    title = effectiveTitle,
                    previewText = cleanContent,
                    entryType = EntryType.NOTE,
                    deletedAtMillis = note.deletedAtMillis,
                    colorHex = if (note.colorHex != 0L && note.colorHex != 0xFF121212L) note.colorHex else 0xFF000000L,
                    categoryName = if (note.labels.isNotEmpty()) note.labels.first() else "Notes",
                    linkedNoteId = note.id,
                    activityId = note.activityId
                )
            }

        val combined = (activityItems + standaloneNotes).sortedByDescending { it.deletedAtMillis }
        BinUiState(items = combined, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BinUiState(isLoading = true)
    )

    fun restoreItem(item: TrashItem) {
        viewModelScope.launch {
            if (item.isActivity) {
                activityRepository.restoreActivity(item.id)
                if (item.linkedNoteId > 0L) {
                    noteRepository.restoreNote(item.linkedNoteId)
                }
                val deletedNotes = noteRepository.getDeletedNotes().firstOrNull() ?: emptyList()
                deletedNotes.filter { it.activityId == item.id }.forEach {
                    noteRepository.restoreNote(it.id)
                }
            } else {
                noteRepository.restoreNote(item.id)
                if (item.activityId > 0L) {
                    activityRepository.restoreActivity(item.activityId)
                }
            }
        }
    }

    fun permanentlyDeleteItem(item: TrashItem) {
        viewModelScope.launch {
            if (item.isActivity) {
                if (item.linkedNoteId > 0L) {
                    noteRepository.permanentlyDeleteNote(item.linkedNoteId)
                }
                val deletedNotes = noteRepository.getDeletedNotes().firstOrNull() ?: emptyList()
                deletedNotes.filter { it.activityId == item.id }.forEach {
                    noteRepository.permanentlyDeleteNote(it.id)
                }
                activityRepository.permanentlyDeleteActivity(item.id)
            } else {
                noteRepository.permanentlyDeleteNote(item.id)
                if (item.activityId > 0L) {
                    activityRepository.permanentlyDeleteActivity(item.activityId)
                }
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            activityRepository.emptyTrash()
            noteRepository.emptyTrash()
        }
    }
}
