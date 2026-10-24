package com.shejan.acontinue.presentation.create

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteUndoManagerTest {

    private fun createEmptySnapshot(): NoteEditorSnapshot {
        return NoteEditorSnapshot(
            title = "",
            noteTextState = TextFieldValue(""),
            formatState = NoteFormatState(),
            checklistItems = emptyList(),
            isChecklistMode = false,
            attachedImageUris = emptyList(),
            selectedColorHex = 0xFF000000L
        )
    }

    @Test
    fun testInitialStateHasNoUndoOrRedo() {
        val initial = createEmptySnapshot()
        val manager = NoteUndoManager(initialSnapshot = initial)

        assertFalse("Initial state should not have undo", manager.canUndo)
        assertFalse("Initial state should not have redo", manager.canRedo)
        assertNull("Undo should return null when stack has only 1 entry", manager.undo())
        assertNull("Redo should return null when redo stack is empty", manager.redo())
    }

    @Test
    fun testRecordActionEnablesUndoAndClearsRedo() {
        val initial = createEmptySnapshot()
        val manager = NoteUndoManager(initialSnapshot = initial)

        val updated = initial.copy(selectedColorHex = 0xFF1E2838L)
        manager.recordAction(updated)

        assertTrue("After recording an action, canUndo should be true", manager.canUndo)
        assertFalse("canRedo should be false after a new action", manager.canRedo)
        assertEquals(updated, manager.currentSnapshot)

        val undone = manager.undo()
        assertNotNull(undone)
        assertEquals(initial, undone)
        assertFalse("After undoing to initial, canUndo should be false", manager.canUndo)
        assertTrue("After undoing, canRedo should be true", manager.canRedo)

        val redone = manager.redo()
        assertNotNull(redone)
        assertEquals(updated, redone)
        assertTrue("After redoing, canUndo should be true", manager.canUndo)
        assertFalse("After redoing the latest, canRedo should be false", manager.canRedo)
    }

    @Test
    fun testTypingWordBoundaryTriggersNewSnapshot() {
        val initial = createEmptySnapshot()
        val manager = NoteUndoManager(initialSnapshot = initial)

        // Type first character
        val step1 = initial.copy(noteTextState = TextFieldValue("H", TextRange(1)))
        manager.recordTyping(step1)
        assertTrue(manager.canUndo)

        // Rapid typing of word characters updates the same top snapshot
        val step2 = initial.copy(noteTextState = TextFieldValue("Hi", TextRange(2)))
        manager.recordTyping(step2)

        // Word boundary (space) commits a distinct checkpoint
        val step3 = initial.copy(noteTextState = TextFieldValue("Hi ", TextRange(3)))
        manager.recordTyping(step3)

        // Undo once should revert to the state before the space ("H" / "Hi")
        val undone = manager.undo()
        assertNotNull(undone)
        assertEquals("Hi", undone?.noteTextState?.text)

        // Undo again should revert to empty
        val undoneEmpty = manager.undo()
        assertNotNull(undoneEmpty)
        assertEquals("", undoneEmpty?.noteTextState?.text)
        assertFalse(manager.canUndo)
    }

    @Test
    fun testNewActionAfterUndoClearsRedoStack() {
        val initial = createEmptySnapshot()
        val manager = NoteUndoManager(initialSnapshot = initial)

        val action1 = initial.copy(title = "Title 1")
        manager.recordAction(action1)

        manager.undo()
        assertTrue("Redo should be available", manager.canRedo)

        // Now perform a new branch of edits
        val action2 = initial.copy(title = "Title 2")
        manager.recordAction(action2)

        assertFalse("New action must clear redo stack", manager.canRedo)
        assertNull(manager.redo())
    }

    @Test
    fun testMaxHistoryCapping() {
        val initial = createEmptySnapshot()
        val manager = NoteUndoManager(maxHistory = 5, initialSnapshot = initial)

        for (i in 1..10) {
            manager.recordAction(initial.copy(title = "Title $i"))
        }

        // We should only be able to undo up to 4 times (since maxHistory is 5 including current)
        var undoCount = 0
        while (manager.canUndo) {
            manager.undo()
            undoCount++
        }

        assertEquals(4, undoCount)
    }
}
