package com.shejan.acontinue.presentation.diary

sealed interface NoteCardPreviewElement {
    data class TextSegment(val text: String) : NoteCardPreviewElement
    object PhotoLine : NoteCardPreviewElement
}

data class NoteCardPreviewResult(
    val elements: List<NoteCardPreviewElement>,
    val hiddenPhotoCount: Int
)

object NoteCardPreviewParser {
    val PHOTO_TAG_REGEX = Regex("""!\[photo\]\(([^)]+)\)""")
    val AUDIO_TAG_REGEX = Regex("""!\[audio\]\(([^)]+)\)""")
    val HEADING_TAG_REGEX = Regex("""(?m)^#{1,6}\s+""")

    fun parse(
        content: String,
        photoCount: Int,
        maxLines: Int = 10
    ): NoteCardPreviewResult {
        if (content.isBlank()) {
            return NoteCardPreviewResult(emptyList(), 0)
        }

        val contentWithoutAudio = content.replace(AUDIO_TAG_REGEX, "")
        val cleanContent = contentWithoutAudio.replace(HEADING_TAG_REGEX, "")

        // If note has no photos, handle as pure text up to maxLines
        if (photoCount <= 0 || !PHOTO_TAG_REGEX.containsMatchIn(cleanContent)) {
            val lines = cleanContent.trim().lines()
            if (lines.isEmpty()) return NoteCardPreviewResult(emptyList(), 0)
            val displayLines = if (lines.size > maxLines) {
                val firstPart = lines.take(maxLines - 1)
                val last = lines[maxLines - 1].trimEnd()
                val lastWithDots = if (last.endsWith("...")) last else "$last..."
                (firstPart + lastWithDots).joinToString("\n")
            } else {
                lines.joinToString("\n")
            }
            return NoteCardPreviewResult(
                elements = if (displayLines.isNotBlank()) listOf(NoteCardPreviewElement.TextSegment(displayLines)) else emptyList(),
                hiddenPhotoCount = 0
            )
        }

        // Interleave TextSegments and PhotoLines
        val rawSegments = mutableListOf<NoteCardPreviewElement>()
        var currentIndex = 0
        val matches = PHOTO_TAG_REGEX.findAll(cleanContent).toList()

        for (match in matches) {
            if (match.range.first > currentIndex) {
                val textChunk = cleanContent.substring(currentIndex, match.range.first)
                val cleanedText = textChunk.trim('\r', '\n')
                if (cleanedText.isNotEmpty()) {
                    rawSegments.add(NoteCardPreviewElement.TextSegment(cleanedText))
                }
            }
            rawSegments.add(NoteCardPreviewElement.PhotoLine)
            var nextIndex = match.range.last + 1
            if (nextIndex < cleanContent.length && cleanContent[nextIndex] == '\r') {
                nextIndex++
            }
            if (nextIndex < cleanContent.length && cleanContent[nextIndex] == '\n') {
                nextIndex++
            }
            currentIndex = nextIndex
        }

        if (currentIndex < cleanContent.length) {
            val textChunk = cleanContent.substring(currentIndex).trim('\r', '\n')
            if (textChunk.isNotEmpty()) {
                rawSegments.add(NoteCardPreviewElement.TextSegment(textChunk))
            }
        }

        // Allocate elements against maxLines line budget
        val visibleElements = mutableListOf<NoteCardPreviewElement>()
        var linesRemaining = maxLines
        var photosShown = 0

        for (element in rawSegments) {
            if (linesRemaining <= 0) break
            when (element) {
                is NoteCardPreviewElement.PhotoLine -> {
                    visibleElements.add(element)
                    photosShown++
                    linesRemaining -= 1
                }
                is NoteCardPreviewElement.TextSegment -> {
                    val segLines = element.text.lines()
                    if (segLines.size <= linesRemaining) {
                        visibleElements.add(element)
                        linesRemaining -= segLines.size
                    } else {
                        val taken = segLines.take(linesRemaining - 1)
                        val last = segLines[linesRemaining - 1].trimEnd()
                        val lastWithDots = if (last.endsWith("...")) last else "$last..."
                        val truncated = (taken + lastWithDots).joinToString("\n")
                        visibleElements.add(NoteCardPreviewElement.TextSegment(truncated))
                        linesRemaining = 0
                    }
                }
            }
        }

        val totalPhotosInContent = matches.size
        val hiddenPhotos = (totalPhotosInContent - photosShown).coerceAtLeast(0)

        return NoteCardPreviewResult(
            elements = visibleElements,
            hiddenPhotoCount = hiddenPhotos
        )
    }
}
