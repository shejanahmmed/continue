package com.shejan.acontinue.presentation.create

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

enum class HeadingType {
    BODY,
    H1,
    H2
}

enum class InlineStyle {
    BOLD,
    ITALIC,
    UNDERLINE,
    STRIKETHROUGH
}

data class FormatSpan(
    val start: Int,
    val end: Int,
    val styles: Set<InlineStyle>
)

data class NoteFormatState(
    val spans: List<FormatSpan> = emptyList(),
    val lineHeadings: Map<Int, HeadingType> = emptyMap(),
    val activeTypingStyles: Set<InlineStyle> = emptySet()
)

object NoteTextFormatter {

    fun getLineIndex(text: String, cursorIndex: Int): Int {
        if (text.isEmpty() || cursorIndex <= 0) return 0
        val safeIndex = cursorIndex.coerceAtMost(text.length)
        return text.substring(0, safeIndex).count { it == '\n' }
    }

    fun getHeadingForLine(state: NoteFormatState, lineIndex: Int): HeadingType {
        return state.lineHeadings[lineIndex] ?: HeadingType.BODY
    }

    fun setHeadingForLine(
        text: String,
        cursorIndex: Int,
        heading: HeadingType,
        state: NoteFormatState
    ): NoteFormatState {
        val lineIndex = getLineIndex(text, cursorIndex)
        val current = getHeadingForLine(state, lineIndex)
        val newHeadings = state.lineHeadings.toMutableMap()
        if (current == heading && heading != HeadingType.BODY) {
            newHeadings.remove(lineIndex)
        } else if (heading == HeadingType.BODY) {
            newHeadings.remove(lineIndex)
        } else {
            newHeadings[lineIndex] = heading
        }
        return state.copy(lineHeadings = newHeadings)
    }

    fun isStyleActiveAt(
        style: InlineStyle,
        selection: TextRange,
        state: NoteFormatState
    ): Boolean {
        if (selection.collapsed) {
            return style in state.activeTypingStyles
        }
        val min = selection.min
        val max = selection.max
        val matchingSpans = state.spans.filter { span ->
            span.start < max && span.end > min && style in span.styles
        }
        if (matchingSpans.isEmpty()) return false
        var coveredLength = 0
        for (i in min until max) {
            if (matchingSpans.any { it.start <= i && it.end > i }) {
                coveredLength++
            }
        }
        return coveredLength == (max - min)
    }

    fun toggleStyle(
        selection: TextRange,
        style: InlineStyle,
        state: NoteFormatState
    ): NoteFormatState {
        if (selection.collapsed) {
            val newActive = if (style in state.activeTypingStyles) {
                state.activeTypingStyles - style
            } else {
                state.activeTypingStyles + style
            }
            return state.copy(activeTypingStyles = newActive)
        }

        val start = selection.min
        val end = selection.max
        val isCurrentlyActive = isStyleActiveAt(style, selection, state)

        val newSpans = mutableListOf<FormatSpan>()

        for (span in state.spans) {
            if (span.end <= start || span.start >= end) {
                newSpans.add(span)
            } else {
                if (span.start < start) {
                    newSpans.add(span.copy(end = start))
                }
                val overlapStart = maxOf(span.start, start)
                val overlapEnd = minOf(span.end, end)
                val updatedStyles = if (isCurrentlyActive) {
                    span.styles - style
                } else {
                    span.styles + style
                }
                if (updatedStyles.isNotEmpty()) {
                    newSpans.add(FormatSpan(overlapStart, overlapEnd, updatedStyles))
                }
                if (span.end > end) {
                    newSpans.add(span.copy(start = end))
                }
            }
        }

        if (!isCurrentlyActive) {
            var curr = start
            val sorted = newSpans.filter { it.end > start && it.start < end }.sortedBy { it.start }
            for (s in sorted) {
                if (s.start > curr) {
                    newSpans.add(FormatSpan(curr, s.start, setOf(style)))
                }
                curr = maxOf(curr, s.end)
            }
            if (curr < end) {
                newSpans.add(FormatSpan(curr, end, setOf(style)))
            }
        }

        return state.copy(spans = mergeSpans(newSpans))
    }

    fun onTextChange(
        oldText: String,
        newText: String,
        selection: TextRange,
        state: NoteFormatState
    ): NoteFormatState {
        val diff = newText.length - oldText.length
        if (diff == 0) return state

        val cursor = selection.min
        val newSpans = mutableListOf<FormatSpan>()

        if (diff > 0) {
            val insertPoint = (cursor - diff).coerceAtLeast(0)
            for (span in state.spans) {
                when {
                    span.end <= insertPoint -> newSpans.add(span)
                    span.start >= insertPoint -> newSpans.add(span.copy(start = span.start + diff, end = span.end + diff))
                    else -> {
                        newSpans.add(span.copy(end = span.end + diff))
                    }
                }
            }
            if (state.activeTypingStyles.isNotEmpty()) {
                newSpans.add(FormatSpan(insertPoint, insertPoint + diff, state.activeTypingStyles))
            }
        } else {
            val deletePoint = cursor
            val deleteCount = -diff
            for (span in state.spans) {
                when {
                    span.end <= deletePoint -> newSpans.add(span)
                    span.start >= deletePoint + deleteCount -> newSpans.add(span.copy(start = span.start - deleteCount, end = span.end - deleteCount))
                    else -> {
                        val newStart = minOf(span.start, deletePoint)
                        val newEnd = (span.end - deleteCount).coerceAtLeast(newStart)
                        if (newEnd > newStart) {
                            newSpans.add(span.copy(start = newStart, end = newEnd))
                        }
                    }
                }
            }
        }

        val lines = newText.split('\n')
        val updatedLineHeadings = state.lineHeadings.filterKeys { it < lines.size }

        return state.copy(spans = mergeSpans(newSpans), lineHeadings = updatedLineHeadings)
    }

    private fun mergeSpans(spans: List<FormatSpan>): List<FormatSpan> {
        val valid = spans.filter { it.start < it.end && it.styles.isNotEmpty() }.sortedBy { it.start }
        if (valid.size <= 1) return valid

        val result = mutableListOf<FormatSpan>()
        var current = valid[0]

        for (i in 1 until valid.size) {
            val next = valid[i]
            if (current.end >= next.start && current.styles == next.styles) {
                current = current.copy(end = maxOf(current.end, next.end))
            } else {
                result.add(current)
                current = next
            }
        }
        result.add(current)
        return result
    }

    fun findMatches(text: String, query: String): List<IntRange> {
        if (text.isEmpty() || query.isBlank()) return emptyList()
        val results = mutableListOf<IntRange>()
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = 0
        while (startIndex < text.length) {
            val foundIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (foundIndex < 0) break
            results.add(foundIndex until (foundIndex + lowerQuery.length))
            startIndex = foundIndex + lowerQuery.length
        }
        return results
    }

    fun buildAnnotatedString(
        text: String,
        state: NoteFormatState,
        baseColor: Color,
        searchQuery: String = "",
        activeMatchRange: IntRange? = null
    ): AnnotatedString {
        return buildAnnotatedString {
            append(text)

            if (text.isNotEmpty()) {
                val lines = text.split('\n')
                var lineStart = 0
                lines.forEachIndexed { lineIdx, lineText ->
                    val lineEnd = lineStart + lineText.length
                    val heading = state.lineHeadings[lineIdx] ?: HeadingType.BODY
                    when (heading) {
                        HeadingType.H1 -> {
                            addStyle(
                                SpanStyle(
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = baseColor
                                ),
                                lineStart,
                                lineEnd
                            )
                        }
                        HeadingType.H2 -> {
                            addStyle(
                                SpanStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = baseColor
                                ),
                                lineStart,
                                lineEnd
                            )
                        }
                        HeadingType.BODY -> {
                            // Standard styling
                        }
                    }
                    lineStart = lineEnd + 1
                }
            }

            for (span in state.spans) {
                val s = span.start.coerceIn(0, text.length)
                val e = span.end.coerceIn(0, text.length)
                if (s >= e) continue

                val hasBold = InlineStyle.BOLD in span.styles
                val hasItalic = InlineStyle.ITALIC in span.styles
                val hasUnderline = InlineStyle.UNDERLINE in span.styles
                val hasStrike = InlineStyle.STRIKETHROUGH in span.styles

                val decoration = when {
                    hasUnderline && hasStrike -> TextDecoration.combine(listOf(TextDecoration.Underline, TextDecoration.LineThrough))
                    hasUnderline -> TextDecoration.Underline
                    hasStrike -> TextDecoration.LineThrough
                    else -> TextDecoration.None
                }

                addStyle(
                    SpanStyle(
                        fontWeight = if (hasBold) FontWeight.Bold else null,
                        fontStyle = if (hasItalic) FontStyle.Italic else null,
                        textDecoration = decoration
                    ),
                    s,
                    e
                )
            }

            if (searchQuery.isNotBlank() && text.isNotEmpty()) {
                val matches = findMatches(text, searchQuery)
                for (match in matches) {
                    val isCurrentActive = activeMatchRange != null &&
                        activeMatchRange.first == match.first &&
                        activeMatchRange.last == match.last

                    val bgColor = if (isCurrentActive) {
                        Color(0xFFFFEE58)
                    } else {
                        Color(0xFFFFEE58).copy(alpha = 0.28f)
                    }
                    val textColor = if (isCurrentActive) Color(0xFF1E1E1E) else baseColor

                    addStyle(
                        SpanStyle(
                            background = bgColor,
                            color = textColor
                        ),
                        match.first,
                        match.last + 1
                    )
                }
            }
        }
    }

    fun serializeToMarkdown(text: String, state: NoteFormatState): String {
        if (text.isEmpty()) return ""
        val lines = text.split('\n')
        val sb = StringBuilder()

        var globalOffset = 0
        lines.forEachIndexed { lineIdx, lineText ->
            val heading = state.lineHeadings[lineIdx] ?: HeadingType.BODY
            val linePrefix = when (heading) {
                HeadingType.H1 -> "# "
                HeadingType.H2 -> "## "
                HeadingType.BODY -> ""
            }
            sb.append(linePrefix)

            val lineStart = globalOffset
            val lineEnd = lineStart + lineText.length

            val lineSpans = state.spans.filter { it.start < lineEnd && it.end > lineStart }

            if (lineSpans.isEmpty()) {
                sb.append(lineText)
            } else {
                for (charIdx in lineText.indices) {
                    val absIdx = lineStart + charIdx
                    val activeStyles = lineSpans.firstOrNull { it.start <= absIdx && it.end > absIdx }?.styles ?: emptySet()
                    val prevStyles = if (charIdx > 0) {
                        lineSpans.firstOrNull { it.start <= (absIdx - 1) && it.end > (absIdx - 1) }?.styles ?: emptySet()
                    } else emptySet()

                    if (InlineStyle.BOLD in activeStyles && InlineStyle.BOLD !in prevStyles) sb.append("**")
                    if (InlineStyle.ITALIC in activeStyles && InlineStyle.ITALIC !in prevStyles) sb.append("*")
                    if (InlineStyle.UNDERLINE in activeStyles && InlineStyle.UNDERLINE !in prevStyles) sb.append("<u>")
                    if (InlineStyle.STRIKETHROUGH in activeStyles && InlineStyle.STRIKETHROUGH !in prevStyles) sb.append("~~")

                    sb.append(lineText[charIdx])

                    val nextStyles = if (charIdx < lineText.length - 1) {
                        lineSpans.firstOrNull { it.start <= (absIdx + 1) && it.end > (absIdx + 1) }?.styles ?: emptySet()
                    } else emptySet()

                    if (InlineStyle.STRIKETHROUGH in activeStyles && InlineStyle.STRIKETHROUGH !in nextStyles) sb.append("~~")
                    if (InlineStyle.UNDERLINE in activeStyles && InlineStyle.UNDERLINE !in nextStyles) sb.append("</u>")
                    if (InlineStyle.ITALIC in activeStyles && InlineStyle.ITALIC !in nextStyles) sb.append("*")
                    if (InlineStyle.BOLD in activeStyles && InlineStyle.BOLD !in nextStyles) sb.append("**")
                }
            }

            if (lineIdx < lines.size - 1) {
                sb.append('\n')
            }
            globalOffset = lineEnd + 1
        }

        return sb.toString()
    }

    fun parseMarkdownToState(markdown: String): Pair<String, NoteFormatState> {
        if (markdown.isEmpty()) return Pair("", NoteFormatState())

        val rawLines = markdown.split('\n')
        val plainLines = mutableListOf<String>()
        val lineHeadings = mutableMapOf<Int, HeadingType>()
        val rawSpans = mutableListOf<FormatSpan>()

        var currentPlainOffset = 0

        rawLines.forEachIndexed { lineIdx, rawLine ->
            var contentLine = rawLine
            var heading = HeadingType.BODY
            when {
                contentLine.startsWith("# ") -> {
                    heading = HeadingType.H1
                    contentLine = contentLine.substring(2)
                }
                contentLine.startsWith("## ") -> {
                    heading = HeadingType.H2
                    contentLine = contentLine.substring(3)
                }
            }
            if (heading != HeadingType.BODY) {
                lineHeadings[lineIdx] = heading
            }

            val strippedBuilder = StringBuilder()
            var i = 0
            val lineStart = currentPlainOffset
            var inBold = false
            var inItalic = false
            var inUnderline = false
            var inStrike = false

            while (i < contentLine.length) {
                when {
                    contentLine.startsWith("**", i) -> {
                        inBold = !inBold
                        i += 2
                    }
                    contentLine.startsWith("~~", i) -> {
                        inStrike = !inStrike
                        i += 2
                    }
                    contentLine.startsWith("<u>", i) -> {
                        inUnderline = true
                        i += 3
                    }
                    contentLine.startsWith("</u>", i) -> {
                        inUnderline = false
                        i += 4
                    }
                    contentLine.startsWith("*", i) -> {
                        inItalic = !inItalic
                        i += 1
                    }
                    else -> {
                        val charPos = lineStart + strippedBuilder.length
                        val activeStyles = mutableSetOf<InlineStyle>()
                        if (inBold) activeStyles.add(InlineStyle.BOLD)
                        if (inItalic) activeStyles.add(InlineStyle.ITALIC)
                        if (inUnderline) activeStyles.add(InlineStyle.UNDERLINE)
                        if (inStrike) activeStyles.add(InlineStyle.STRIKETHROUGH)

                        if (activeStyles.isNotEmpty()) {
                            rawSpans.add(FormatSpan(charPos, charPos + 1, activeStyles))
                        }
                        strippedBuilder.append(contentLine[i])
                        i++
                    }
                }
            }

            plainLines.add(strippedBuilder.toString())
            currentPlainOffset += strippedBuilder.length + 1
        }

        val fullPlainText = plainLines.joinToString("\n")
        val merged = mergeSpans(rawSpans)
        return Pair(fullPlainText, NoteFormatState(spans = merged, lineHeadings = lineHeadings))
    }

    /**
     * Inserts an inline photo tag into the note text according to line rules:
     * - If the line containing the cursor is empty, the photo tag is placed directly on that line.
     * - If the line containing the cursor has any content (including whitespace or letters), the photo tag is placed on the 2nd line below the cursor.
     * - In all cases, the returned cursor offset places the cursor immediately on the next line after the photo tag.
     */
    fun insertPhotoTag(
        currentText: String,
        cursorOffset: Int,
        photoUri: String
    ): Pair<String, Int> {
        val safeOffset = cursorOffset.coerceIn(0, currentText.length)

        val lastNewlineBefore = currentText.lastIndexOf('\n', (safeOffset - 1).coerceAtLeast(0))
        val lineStartIndex = if (lastNewlineBefore == -1 || safeOffset == 0) 0 else lastNewlineBefore + 1

        val nextNewlineAfter = currentText.indexOf('\n', safeOffset)
        val lineEndIndex = if (nextNewlineAfter == -1) currentText.length else nextNewlineAfter

        val currentLineText = currentText.substring(lineStartIndex, lineEndIndex)
        val isLineEmpty = currentLineText.isEmpty()

        val tagWithNewline = "![photo]($photoUri)\n"

        return if (isLineEmpty) {
            val before = currentText.substring(0, lineStartIndex)
            val after = currentText.substring(lineEndIndex)
            val cleanAfter = if (after.startsWith("\n")) after.substring(1) else after
            val newText = before + tagWithNewline + cleanAfter
            val newCursor = (before + tagWithNewline).length
            Pair(newText, newCursor)
        } else {
            val before = currentText.substring(0, safeOffset)
            val after = currentText.substring(safeOffset)
            val cleanAfter = if (after.startsWith("\n")) after.substring(1) else after
            val tag = "\n$tagWithNewline"
            val newText = before + tag + cleanAfter
            val newCursor = (before + tag).length
            Pair(newText, newCursor)
        }
    }
}

class NoteVisualTransformation(
    private val formatState: NoteFormatState,
    private val baseColor: Color,
    private val searchQuery: String = "",
    private val activeMatchRange: IntRange? = null
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotated = NoteTextFormatter.buildAnnotatedString(
            text.text,
            formatState,
            baseColor,
            searchQuery,
            activeMatchRange
        )
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}
