package com.shejan.acontinue.presentation.create

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteTextFormatterTest {

    @Test
    fun testToggleBoldOnSelection() {
        val text = "Hello world"
        var state = NoteFormatState()

        // Select "world" (range 6..11)
        val selection = TextRange(6, 11)
        assertFalse(NoteTextFormatter.isStyleActiveAt(InlineStyle.BOLD, selection, state))

        state = NoteTextFormatter.toggleStyle(selection, InlineStyle.BOLD, state)
        assertTrue(NoteTextFormatter.isStyleActiveAt(InlineStyle.BOLD, selection, state))
        assertEquals(1, state.spans.size)
        assertEquals(6, state.spans[0].start)
        assertEquals(11, state.spans[0].end)
        assertTrue(InlineStyle.BOLD in state.spans[0].styles)

        // Toggle OFF
        state = NoteTextFormatter.toggleStyle(selection, InlineStyle.BOLD, state)
        assertFalse(NoteTextFormatter.isStyleActiveAt(InlineStyle.BOLD, selection, state))
        assertTrue(state.spans.isEmpty())
    }

    @Test
    fun testSetHeadingForLine() {
        val text = "First line\nSecond line"
        var state = NoteFormatState()

        // Cursor in second line (index 15)
        state = NoteTextFormatter.setHeadingForLine(text, 15, HeadingType.H1, state)
        assertEquals(HeadingType.H1, NoteTextFormatter.getHeadingForLine(state, 1))
        assertEquals(HeadingType.BODY, NoteTextFormatter.getHeadingForLine(state, 0))

        // Change to H2
        state = NoteTextFormatter.setHeadingForLine(text, 15, HeadingType.H2, state)
        assertEquals(HeadingType.H2, NoteTextFormatter.getHeadingForLine(state, 1))

        // Revert to Aa (BODY)
        state = NoteTextFormatter.setHeadingForLine(text, 15, HeadingType.BODY, state)
        assertEquals(HeadingType.BODY, NoteTextFormatter.getHeadingForLine(state, 1))
    }

    @Test
    fun testMarkdownSerializationAndParsingRoundtrip() {
        val text = "Title line\nBold text and normal"
        var state = NoteFormatState()

        // Make line 0 H1
        state = NoteTextFormatter.setHeadingForLine(text, 0, HeadingType.H1, state)
        // Make "Bold" (range 11..15) bold
        state = NoteTextFormatter.toggleStyle(TextRange(11, 15), InlineStyle.BOLD, state)

        val markdown = NoteTextFormatter.serializeToMarkdown(text, state)
        assertEquals("# Title line\n**Bold** text and normal", markdown)

        // Parse back
        val (parsedText, parsedState) = NoteTextFormatter.parseMarkdownToState(markdown)
        assertEquals("Title line\nBold text and normal", parsedText)
        assertEquals(HeadingType.H1, parsedState.lineHeadings[0])
        assertTrue(parsedState.spans.any { it.start == 11 && it.end == 15 && InlineStyle.BOLD in it.styles })
    }

    @Test
    fun testMultipleInlineStylesCombination() {
        val text = "Sample"
        var state = NoteFormatState()
        val selection = TextRange(0, 6)

        state = NoteTextFormatter.toggleStyle(selection, InlineStyle.BOLD, state)
        state = NoteTextFormatter.toggleStyle(selection, InlineStyle.ITALIC, state)
        state = NoteTextFormatter.toggleStyle(selection, InlineStyle.UNDERLINE, state)
        state = NoteTextFormatter.toggleStyle(selection, InlineStyle.STRIKETHROUGH, state)

        val span = state.spans.first()
        assertTrue(InlineStyle.BOLD in span.styles)
        assertTrue(InlineStyle.ITALIC in span.styles)
        assertTrue(InlineStyle.UNDERLINE in span.styles)
        assertTrue(InlineStyle.STRIKETHROUGH in span.styles)

        val annotated = NoteTextFormatter.buildAnnotatedString(text, state, Color.White)
        assertEquals(text, annotated.text)
        assertTrue(annotated.spanStyles.isNotEmpty())
    }
}
