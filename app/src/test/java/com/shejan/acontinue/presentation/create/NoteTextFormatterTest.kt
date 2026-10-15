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

    @Test
    fun testFindMatchesCaseInsensitive() {
        val text = "Device info\nDevice name: Shejan Processor"
        val matches = NoteTextFormatter.findMatches(text, "devic")

        assertEquals(2, matches.size)
        assertEquals(0..4, matches[0])
        assertEquals(12..16, matches[1])
    }

    @Test
    fun testFindMatchesEmptyOrBlank() {
        val text = "Some note content"
        assertTrue(NoteTextFormatter.findMatches(text, "").isEmpty())
        assertTrue(NoteTextFormatter.findMatches(text, "   ").isEmpty())
        assertTrue(NoteTextFormatter.findMatches("", "word").isEmpty())
    }

    @Test
    fun testSearchHighlightsDistinguishesActiveMatch() {
        val text = "Device one and Device two"
        val matches = NoteTextFormatter.findMatches(text, "Device")
        assertEquals(2, matches.size)

        // Active match is matches[0] (0..5)
        val annotated = NoteTextFormatter.buildAnnotatedString(
            text = text,
            state = NoteFormatState(),
            baseColor = Color.White,
            searchQuery = "Device",
            activeMatchRange = matches[0]
        )

        // Verify span styles were applied for both matches
        val styles = annotated.spanStyles
        assertTrue("Expected at least 2 styled spans for 2 matches", styles.size >= 2)

        val activeSpan = styles.firstOrNull { it.start == 0 && it.end == 6 }
        val secondarySpan = styles.firstOrNull { it.start == 15 && it.end == 21 }

        assertTrue(activeSpan != null)
        assertTrue(secondarySpan != null)

        // Active match should be soft sunny light yellow (0xFFFFEE58) and dark text
        assertEquals(Color(0xFFFFEE58), activeSpan!!.item.background)
        assertEquals(Color(0xFF1E1E1E), activeSpan.item.color)

        // Secondary match should be subtle light yellow (0xFFFFEE58 with alpha)
        assertEquals(Color(0xFFFFEE58).copy(alpha = 0.28f), secondarySpan!!.item.background)
        assertEquals(Color.White, secondarySpan.item.color)
    }

    @Test
    fun testMatchNavigationCyclingLogic() {
        val totalMatches = 3
        var activeIndex = 0

        // Down arrow (Next)
        activeIndex = (activeIndex + 1) % totalMatches
        assertEquals(1, activeIndex)

        activeIndex = (activeIndex + 1) % totalMatches
        assertEquals(2, activeIndex)

        // Wraps around to 0
        activeIndex = (activeIndex + 1) % totalMatches
        assertEquals(0, activeIndex)

        // Up arrow (Previous) wraps around to 2
        activeIndex = if (activeIndex - 1 < 0) totalMatches - 1 else activeIndex - 1
        assertEquals(2, activeIndex)
    }
}
