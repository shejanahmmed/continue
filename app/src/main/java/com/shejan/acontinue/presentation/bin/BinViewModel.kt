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
        val activityItems = activities.map { act ->
            val cleanDesc = act.description
                .replace(AUDIO_TAG_REGEX, "")
                .replace(PHOTO_TAG_REGEX, "")
                .trim()
            val effectiveTitle = if (act.title.equals("Untitled Note", ignoreCase = true)) "" else act.title

            TrashItem(
                id = act.id,
                isActivity = true,
                title = effectiveTitle,
                previewText = cleanDesc,
                entryType = act.entryType,
                deletedAtMillis = act.deletedAtMillis,
                colorHex = if (act.reminderCategoryColorHex != 0L && act.reminderCategoryColorHex != 0xFF121212L) act.reminderCategoryColorHex else 0xFF000000L,
                categoryName = act.reminderCategory
            )
        }

        val noteItems = notes.map { note ->
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
                categoryName = if (note.labels.isNotEmpty()) note.labels.first() else "Notes"
            )
        }

        // Deduplicate note activities that share IDs with standalone note items if any
        val combined = (activityItems + noteItems).sortedByDescending { it.deletedAtMillis }
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
            } else {
                noteRepository.restoreNote(item.id)
            }
        }
    }

    fun permanentlyDeleteItem(item: TrashItem) {
        viewModelScope.launch {
            if (item.isActivity) {
                activityRepository.permanentlyDeleteActivity(item.id)
            } else {
                noteRepository.permanentlyDeleteNote(item.id)
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

