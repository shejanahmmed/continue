package com.shejan.acontinue.presentation.create

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.math.abs

/**
 * Immutable snapshot of the note editor state at a point in time.
 */
data class NoteEditorSnapshot(
    val title: String,
    val noteTextState: TextFieldValue,
    val formatState: NoteFormatState,
    val checklistItems: List<ChecklistItem>,
    val isChecklistMode: Boolean,
    val attachedImageUris: List<Uri>,
    val selectedColorHex: Long,
    val selectedLabels: Set<String> = emptySet()
)

/**
 * Manages Undo and Redo history stacks with typing debouncing, word-boundary grouping,
 * and reactive UI state indicators.
 */
class NoteUndoManager(
    private val maxHistory: Int = 50,
    initialSnapshot: NoteEditorSnapshot
) {
    private val undoStack = ArrayDeque<NoteEditorSnapshot>()
    private val redoStack = ArrayDeque<NoteEditorSnapshot>()

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    var currentSnapshot: NoteEditorSnapshot = initialSnapshot
        private set

    private var lastTypingTimeMs: Long = 0L

    init {
        undoStack.addLast(initialSnapshot)
        updateFlags()
    }

    private fun updateFlags() {
        canUndo = undoStack.size > 1
        canRedo = redoStack.isNotEmpty()
    }

    /**
     * Records an explicit action (formatting style change, checklist toggle,
     * color change, image addition/removal). Immediately commits to history.
     */
    fun recordAction(snapshot: NoteEditorSnapshot) {
        if (snapshot == currentSnapshot) return

        undoStack.addLast(snapshot)
        if (undoStack.size > maxHistory) {
            undoStack.removeFirst()
        }
        redoStack.clear()
        currentSnapshot = snapshot
        lastTypingTimeMs = System.currentTimeMillis()
        updateFlags()
    }

    /**
     * Records typing changes with debouncing and word-boundary checkpoints.
     */
    fun recordTyping(snapshot: NoteEditorSnapshot) {
        if (snapshot == currentSnapshot) return

        val now = System.currentTimeMillis()
        val textChanged = snapshot.noteTextState.text != currentSnapshot.noteTextState.text
        val titleChanged = snapshot.title != currentSnapshot.title

        val isBulkChange = abs(snapshot.noteTextState.text.length - currentSnapshot.noteTextState.text.length) > 1 ||
                abs(snapshot.title.length - currentSnapshot.title.length) > 1

        val enteredWordBoundary = if (textChanged) {
            val oldLen = currentSnapshot.noteTextState.text.length
            val newLen = snapshot.noteTextState.text.length
            if (newLen > oldLen) {
                val addedChar = snapshot.noteTextState.text.lastOrNull()
                addedChar == ' ' || addedChar == '\n'
            } else false
        } else if (titleChanged) {
            val oldLen = currentSnapshot.title.length
            val newLen = snapshot.title.length
            if (newLen > oldLen) {
                val addedChar = snapshot.title.lastOrNull()
                addedChar == ' ' || addedChar == '\n'
            } else false
        } else false

        val elapsed = now - lastTypingTimeMs
        val isFirstChange = undoStack.size == 1

        if (isFirstChange || enteredWordBoundary || isBulkChange || elapsed > 800L) {
            undoStack.addLast(snapshot)
            if (undoStack.size > maxHistory) {
                undoStack.removeFirst()
            }
            redoStack.clear()
        } else {
            // Update top snapshot in place during consecutive rapid keystrokes
            if (undoStack.isNotEmpty()) {
                undoStack.removeLast()
                undoStack.addLast(snapshot)
            }
        }

        currentSnapshot = snapshot
        lastTypingTimeMs = now
        updateFlags()
    }

    /**
     * Reverts to the previous snapshot in the undo stack.
     */
    fun undo(): NoteEditorSnapshot? {
        if (!canUndo) return null
        val current = undoStack.removeLast()
        redoStack.addLast(current)
        val previous = undoStack.last()
        currentSnapshot = previous
        updateFlags()
        return previous
    }

    /**
     * Re-applies the next undone snapshot from the redo stack.
     */
    fun redo(): NoteEditorSnapshot? {
        if (!canRedo) return null
        val next = redoStack.removeLast()
        undoStack.addLast(next)
        currentSnapshot = next
        updateFlags()
        return next
    }
}
