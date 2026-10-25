package com.shejan.acontinue.presentation.create

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteAutoNumberingTest {

    @Test
    fun testAutoNumberingOnEnter_SingleItem() {
        val oldText = "1. Homework"
        val oldTfv = TextFieldValue(oldText, selection = TextRange(oldText.length))
        val newText = "1. Homework\n"
        val newTfv = TextFieldValue(newText, selection = TextRange(newText.length))

        val result = NoteAutoNumbering.handleAutoNumbering(oldTfv, newTfv)
        assertEquals("1. Homework\n2.\u00A0", result.text)
        assertEquals(15, result.selection.start)
        assertEquals(15, result.selection.end)
    }

    @Test
    fun testAutoNumberingOnEnter_MultiDigit() {
        val oldText = "10. Important task"
        val oldTfv = TextFieldValue(oldText, selection = TextRange(oldText.length))
        val newText = "10. Important task\n"
        val newTfv = TextFieldValue(newText, selection = TextRange(newText.length))

        val result = NoteAutoNumbering.handleAutoNumbering(oldTfv, newTfv)
        assertEquals("10. Important task\n11.\u00A0", result.text)
        assertEquals(23, result.selection.start)
    }

    @Test
    fun testAutoNumberingOnEnter_PreservesIndent() {
        val oldText = "  1. Indented task"
        val oldTfv = TextFieldValue(oldText, selection = TextRange(oldText.length))
        val newText = "  1. Indented task\n"
        val newTfv = TextFieldValue(newText, selection = TextRange(newText.length))

        val result = NoteAutoNumbering.handleAutoNumbering(oldTfv, newTfv)
        assertEquals("  1. Indented task\n  2.\u00A0", result.text)
        assertEquals(24, result.selection.start)
    }

    @Test
    fun testAutoNumberingOnEnter_EmptyItemTerminatesList() {
        val oldText = "1. Homework\n2.\u00A0"
        val oldTfv = TextFieldValue(oldText, selection = TextRange(oldText.length))
        val newText = "1. Homework\n2.\u00A0\n"
        val newTfv = TextFieldValue(newText, selection = TextRange(newText.length))

        val result = NoteAutoNumbering.handleAutoNumbering(oldTfv, newTfv)
        assertEquals("1. Homework\n\n", result.text)
        assertEquals(13, result.selection.start)
    }

    @Test
    fun testAutoNumberingOnBackspace_EmptyPrefixDeleted() {
        val oldText = "1. Homework\n2.\u00A0"
        val oldTfv = TextFieldValue(oldText, selection = TextRange(15))
        // Backspace deleted the trailing NBSP of "2.\u00A0"
        val newText = "1. Homework\n2."
        val newTfv = TextFieldValue(newText, selection = TextRange(14))

        val result = NoteAutoNumbering.handleAutoNumbering(oldTfv, newTfv)
        assertEquals("1. Homework\n", result.text)
        assertEquals(12, result.selection.start)
    }

    @Test
    fun testAutoNumbering_MidListInsertionRenumbersSubsequent() {
        val oldText = "1.\u00A0First\n2.\u00A0Third\n3.\u00A0Fourth"
        // Cursor at end of "1.\u00A0First" (index 8)
        val oldTfv = TextFieldValue(oldText, selection = TextRange(8))
        // User presses Enter
        val newText = "1.\u00A0First\n\n2.\u00A0Third\n3.\u00A0Fourth"
        val newTfv = TextFieldValue(newText, selection = TextRange(9))

        val result = NoteAutoNumbering.handleAutoNumbering(oldTfv, newTfv)
        assertEquals("1.\u00A0First\n2.\u00A0\n3.\u00A0Third\n4.\u00A0Fourth", result.text)
        assertEquals(12, result.selection.start)
    }

    @Test
    fun testAutoNumbering_ManualSpaceAfterDot_ConvertsToNBSP() {
        val oldText = "1."
        val oldTfv = TextFieldValue(oldText, selection = TextRange(2))
        val newText = "1. "
        val newTfv = TextFieldValue(newText, selection = TextRange(3))

        val result = NoteAutoNumbering.handleAutoNumbering(oldTfv, newTfv)
        assertEquals("1.\u00A0", result.text)
        assertEquals(3, result.selection.start)
    }

    @Test
    fun testAutoNumbering_NonNumberedLine_DoesNotAddPrefix() {
        val oldText = "Just a regular note"
        val oldTfv = TextFieldValue(oldText, selection = TextRange(oldText.length))
        val newText = "Just a regular note\n"
        val newTfv = TextFieldValue(newText, selection = TextRange(newText.length))

        val result = NoteAutoNumbering.handleAutoNumbering(oldTfv, newTfv)
        assertEquals("Just a regular note\n", result.text)
        assertEquals(20, result.selection.start)
    }

    @Test
    fun testNumberedLinesHaveZeroParagraphStylesToPreventExtraLineGaps() {
        val text = "1. Homework for math class\n2. Online class\nRegular line"
        val state = NoteFormatState()

        val annotated = NoteTextFormatter.buildAnnotatedString(
            text = text,
            state = state,
            baseColor = Color.White
        )

        // ParagraphStyle introduces implicit line feeds in MultiParagraph, which creates
        // blank lines between list items. The AnnotatedString must have no ParagraphStyles.
        assertTrue("Numbered lines must not introduce ParagraphStyles that create unwanted vertical gaps", annotated.paragraphStyles.isEmpty())
    }
}
