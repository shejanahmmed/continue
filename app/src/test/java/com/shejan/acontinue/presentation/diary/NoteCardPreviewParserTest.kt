package com.shejan.acontinue.presentation.diary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteCardPreviewParserTest {

    @Test
    fun parse_blankContent_returnsEmptyResult() {
        val result = NoteCardPreviewParser.parse(content = "", photoCount = 0)
        assertTrue(result.elements.isEmpty())
        assertEquals(0, result.hiddenPhotoCount)
    }

    @Test
    fun parse_pureTextWithinMaxLines_returnsSingleTextSegment() {
        val text = "Line 1\nLine 2\nLine 3"
        val result = NoteCardPreviewParser.parse(content = text, photoCount = 0, maxLines = 10)
        assertEquals(1, result.elements.size)
        val seg = result.elements[0] as NoteCardPreviewElement.TextSegment
        assertEquals(text, seg.text)
        assertEquals(0, result.hiddenPhotoCount)
    }

    @Test
    fun parse_pureTextExceedingMaxLines_truncatesWithEllipsis() {
        val lines = (1..15).map { "Line $it" }.joinToString("\n")
        val result = NoteCardPreviewParser.parse(content = lines, photoCount = 0, maxLines = 10)
        assertEquals(1, result.elements.size)
        val seg = result.elements[0] as NoteCardPreviewElement.TextSegment
        val segLines = seg.text.lines()
        assertEquals(10, segLines.size)
        assertTrue(segLines.last().endsWith("..."))
        assertEquals(0, result.hiddenPhotoCount)
    }

    @Test
    fun parse_photoInBetweenTextLines_matchesUserScenario() {
        val content = """
            qbbsjsjjsjdjdjjsjskw
            kieidieowoosososksk
            sksk

            ![photo](content://media/external/images/media/100)

            jsjjdjd
        """.trimIndent()

        val result = NoteCardPreviewParser.parse(content = content, photoCount = 1, maxLines = 10)
        assertEquals(3, result.elements.size)

        // Element 0: text before photo
        val firstSeg = result.elements[0] as NoteCardPreviewElement.TextSegment
        val firstLines = firstSeg.text.lines()
        assertEquals(3, firstLines.size)
        assertEquals("qbbsjsjjsjdjdjjsjskw", firstLines[0])
        assertEquals("kieidieowoosososksk", firstLines[1])
        assertEquals("sksk", firstLines[2])

        // Element 1: photo icon line in between
        assertTrue(result.elements[1] is NoteCardPreviewElement.PhotoLine)

        // Element 2: text after photo
        val secondSeg = result.elements[2] as NoteCardPreviewElement.TextSegment
        assertEquals("jsjjdjd", secondSeg.text.trim())

        // Photo was displayed inline, so no hidden photos at bottom
        assertEquals(0, result.hiddenPhotoCount)
    }

    @Test
    fun parse_photoOnFirstLine_placesPhotoLineFirst() {
        val content = "![photo](content://photo1)\nHello world\nSecond line"
        val result = NoteCardPreviewParser.parse(content = content, photoCount = 1, maxLines = 10)

        assertEquals(2, result.elements.size)
        assertTrue(result.elements[0] is NoteCardPreviewElement.PhotoLine)

        val textSeg = result.elements[1] as NoteCardPreviewElement.TextSegment
        assertEquals("Hello world\nSecond line", textSeg.text)
        assertEquals(0, result.hiddenPhotoCount)
    }

    @Test
    fun parse_photoBeyondMaxLines_countsAsHiddenPhoto() {
        val linesBeforePhoto = (1..12).map { "Line $it" }.joinToString("\n")
        val content = "$linesBeforePhoto\n![photo](content://photo1)\nTrailing line"

        val result = NoteCardPreviewParser.parse(content = content, photoCount = 1, maxLines = 10)

        // Since 10 text lines consume the line budget, photo is not shown inline
        assertTrue(result.elements.none { it is NoteCardPreviewElement.PhotoLine })
        assertEquals(1, result.hiddenPhotoCount)
    }

    @Test
    fun parse_audioTagsAreStrippedWithoutImpactingPhotos() {
        val content = """
            ![audio](content://audio1)
            Heading
            ![photo](content://photo1)
            Footer
        """.trimIndent()

        val result = NoteCardPreviewParser.parse(content = content, photoCount = 1, maxLines = 10)
        assertEquals(3, result.elements.size)

        val firstSeg = result.elements[0] as NoteCardPreviewElement.TextSegment
        assertEquals("Heading", firstSeg.text)

        assertTrue(result.elements[1] is NoteCardPreviewElement.PhotoLine)

        val secondSeg = result.elements[2] as NoteCardPreviewElement.TextSegment
        assertEquals("Footer", secondSeg.text)
        assertEquals(0, result.hiddenPhotoCount)
    }

    @Test
    fun parse_markdownHeadings_stripsLeadingHashMarksLeavingCleanText() {
        val content = """
            # Heading 1
            Normal body paragraph
            ## Subheading 2
            ### Deep Heading 3
        """.trimIndent()

        val result = NoteCardPreviewParser.parse(content = content, photoCount = 0)
        assertEquals(1, result.elements.size)
        val textSeg = result.elements[0] as NoteCardPreviewElement.TextSegment
        val expected = """
            Heading 1
            Normal body paragraph
            Subheading 2
            Deep Heading 3
        """.trimIndent()
        assertEquals(expected, textSeg.text)
    }

    @Test
    fun parse_inlineHashtagsWithoutSpaces_arePreserved() {
        val content = """
            #hashtag1 #hashtag2
            Note discussing #android architecture
        """.trimIndent()

        val result = NoteCardPreviewParser.parse(content = content, photoCount = 0)
        assertEquals(1, result.elements.size)
        val textSeg = result.elements[0] as NoteCardPreviewElement.TextSegment
        val expected = """
            #hashtag1 #hashtag2
            Note discussing #android architecture
        """.trimIndent()
        assertEquals(expected, textSeg.text)
    }

    @Test
    fun parse_markdownHeadingsWithPhotos_cleansHeadingsAndInterleavesPhotos() {
        val content = """
            # Meeting Notes
            ![photo](content://photo1)
            ## Action Items
        """.trimIndent()

        val result = NoteCardPreviewParser.parse(content = content, photoCount = 1)
        assertEquals(3, result.elements.size)

        val firstSeg = result.elements[0] as NoteCardPreviewElement.TextSegment
        assertEquals("Meeting Notes", firstSeg.text)

        assertTrue(result.elements[1] is NoteCardPreviewElement.PhotoLine)

        val secondSeg = result.elements[2] as NoteCardPreviewElement.TextSegment
        assertEquals("Action Items", secondSeg.text)
    }
}

