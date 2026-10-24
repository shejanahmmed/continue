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
    val activeTypingStyles: Set<InlineStyle>? = null
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

    fun setHeadingForSelection(
        text: String,
        selection: TextRange,
        heading: HeadingType,
        state: NoteFormatState
    ): NoteFormatState {
        val startLine = getLineIndex(text, selection.min)
        val endLine = getLineIndex(text, selection.max)
        val newHeadings = state.lineHeadings.toMutableMap()

        // Check if all selected lines already have this heading
        val allAlreadyHaveHeading = (startLine..endLine).all { line ->
            (newHeadings[line] ?: HeadingType.BODY) == heading
        }

        // Toggling matching Google Docs:
        // If all selected lines currently have this heading, or heading is BODY, revert all to BODY.
        // Otherwise, apply the heading to all selected lines.
        val targetHeading = if (allAlreadyHaveHeading || heading == HeadingType.BODY) {
            HeadingType.BODY
        } else {
            heading
        }

        for (line in startLine..endLine) {
            if (targetHeading == HeadingType.BODY) {
                newHeadings.remove(line)
            } else {
                newHeadings[line] = targetHeading
            }
        }

        return state.copy(lineHeadings = newHeadings)
    }

    fun setHeadingForLine(
        text: String,
        cursorIndex: Int,
        heading: HeadingType,
        state: NoteFormatState
    ): NoteFormatState {
        return setHeadingForSelection(text, TextRange(cursorIndex, cursorIndex), heading, state)
    }

    fun isStyleActiveAt(
        style: InlineStyle,
        selection: TextRange,
        state: NoteFormatState
    ): Boolean {
        if (selection.collapsed) {
            state.activeTypingStyles?.let {
                return style in it
            }
            val pos = selection.min
            val probe = if (pos > 0) pos - 1 else 0
            return state.spans.any { it.start <= probe && it.end > probe && style in it.styles }
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
            val isCurrentlyActive = isStyleActiveAt(style, selection, state)
            val baseStyles = state.activeTypingStyles ?: run {
                val pos = selection.min
                val probe = if (pos > 0) pos - 1 else 0
                state.spans.firstOrNull { it.start <= probe && it.end > probe }?.styles ?: emptySet()
            }
            val newActive = if (isCurrentlyActive) {
                baseStyles - style
            } else {
                baseStyles + style
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
        if (oldText == newText) return state

        val diff = newText.length - oldText.length

        var editStart = 0
        while (editStart < oldText.length && editStart < newText.length && oldText[editStart] == newText[editStart]) {
            editStart++
        }

        var oldEnd = oldText.length
        var newEnd = newText.length
        while (oldEnd > editStart && newEnd > editStart && oldText[oldEnd - 1] == newText[newEnd - 1]) {
            oldEnd--
            newEnd--
        }

        val deletedText = oldText.substring(editStart, oldEnd)
        val insertedText = newText.substring(editStart, newEnd)

        val newlinesDeleted = deletedText.count { it == '\n' }
        val newlinesInserted = insertedText.count { it == '\n' }
        val lineWhereEditStarted = oldText.substring(0, editStart).count { it == '\n' }

        // --- 1. Line Headings Shifting (Google Docs Behavior) ---
        val newHeadings = mutableMapOf<Int, HeadingType>()

        if (newlinesInserted == 0 && newlinesDeleted == 0) {
            val totalLines = newText.count { it == '\n' } + 1
            state.lineHeadings.forEach { (lineIdx, heading) ->
                if (lineIdx < totalLines) {
                    newHeadings[lineIdx] = heading
                }
            }
        } else {
            // Lines strictly before the edit line are completely untouched
            state.lineHeadings.forEach { (lineIdx, heading) ->
                if (lineIdx < lineWhereEditStarted) {
                    newHeadings[lineIdx] = heading
                }
            }

            val lineOffsetInOld = oldText.lastIndexOf('\n', (editStart - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
            val nextNewlineInOld = oldText.indexOf('\n', editStart).let { if (it == -1) oldText.length else it }
            val restOfLineInOld = oldText.substring(editStart, nextNewlineInOld)
            val isEnterAtStartOfLine = (editStart == lineOffsetInOld)
            val isEnterAtEndOfLine = restOfLineInOld.isBlank()

            val headingAtEditLine = state.lineHeadings[lineWhereEditStarted] ?: HeadingType.BODY

            if (newlinesDeleted > 0 && newlinesInserted == 0) {
                // Lines were joined (e.g. Backspace)
                val lineWhereEditEnded = lineWhereEditStarted + newlinesDeleted
                val mergedHeading = if (headingAtEditLine != HeadingType.BODY) {
                    headingAtEditLine
                } else {
                    state.lineHeadings[lineWhereEditEnded] ?: HeadingType.BODY
                }
                if (mergedHeading != HeadingType.BODY) {
                    newHeadings[lineWhereEditStarted] = mergedHeading
                }
            } else if (newlinesInserted > 0) {
                if (isEnterAtStartOfLine) {
                    // Enter pressed at the start of the line: empty line added above.
                    // The original line content moves down by newlinesInserted and preserves its heading.
                    if (headingAtEditLine != HeadingType.BODY) {
                        newHeadings[lineWhereEditStarted + newlinesInserted] = headingAtEditLine
                    }
                } else if (isEnterAtEndOfLine) {
                    // Enter pressed at the end of a heading line (Google Docs standard):
                    // The heading line preserves its style, and newly created lines default to BODY.
                    if (headingAtEditLine != HeadingType.BODY) {
                        newHeadings[lineWhereEditStarted] = headingAtEditLine
                    }
                } else {
                    // Enter pressed in the middle of a heading line: split heading across lines
                    if (headingAtEditLine != HeadingType.BODY) {
                        newHeadings[lineWhereEditStarted] = headingAtEditLine
                        newHeadings[lineWhereEditStarted + newlinesInserted] = headingAtEditLine
                    }
                }
            }

            // Shift subsequent lines
            val lineWhereEditEnded = lineWhereEditStarted + newlinesDeleted
            state.lineHeadings.forEach { (lineIdx, heading) ->
                if (lineIdx > lineWhereEditEnded) {
                    val shiftedLine = lineIdx + newlinesInserted - newlinesDeleted
                    if (shiftedLine >= 0) {
                        newHeadings[shiftedLine] = heading
                    }
                }
            }
        }

        // --- 2. Format Spans Shifting ---
        val newSpans = mutableListOf<FormatSpan>()

        val stylesToApply = state.activeTypingStyles ?: if (diff > 0 && oldText.isNotEmpty()) {
            val probe = (editStart - 1).coerceAtLeast(0)
            state.spans.firstOrNull { it.start <= probe && it.end > probe }?.styles ?: emptySet()
        } else {
            emptySet()
        }

        if (diff > 0) {
            val insertPoint = editStart
            val insertLen = diff

            for (span in state.spans) {
                when {
                    span.end <= insertPoint -> {
                        newSpans.add(span)
                    }
                    span.start >= insertPoint -> {
                        newSpans.add(span.copy(start = span.start + insertLen, end = span.end + insertLen))
                    }
                    else -> {
                        val matchingStyles = span.styles.filter { it in stylesToApply }.toSet()
                        val nonMatchingStyles = span.styles.filter { it !in stylesToApply }.toSet()

                        if (nonMatchingStyles.isEmpty()) {
                            newSpans.add(span.copy(end = span.end + insertLen))
                        } else {
                            newSpans.add(span.copy(end = insertPoint))
                            newSpans.add(span.copy(start = insertPoint + insertLen, end = span.end + insertLen))
                            if (matchingStyles.isNotEmpty()) {
                                newSpans.add(FormatSpan(insertPoint, insertPoint + insertLen, matchingStyles))
                            }
                        }
                    }
                }
            }

            if (stylesToApply.isNotEmpty()) {
                newSpans.add(FormatSpan(insertPoint, insertPoint + insertLen, stylesToApply))
            }
        } else if (diff < 0) {
            val deleteStart = editStart
            val deleteEnd = oldEnd
            val deleteCount = deleteEnd - deleteStart

            for (span in state.spans) {
                when {
                    span.end <= deleteStart -> {
                        newSpans.add(span)
                    }
                    span.start >= deleteEnd -> {
                        newSpans.add(span.copy(start = span.start - deleteCount, end = span.end - deleteCount))
                    }
                    span.start >= deleteStart && span.end <= deleteEnd -> {
                        // Fully deleted
                    }
                    span.start < deleteStart && span.end > deleteEnd -> {
                        newSpans.add(span.copy(end = span.end - deleteCount))
                    }
                    span.start < deleteStart && span.end <= deleteEnd -> {
                        newSpans.add(span.copy(end = deleteStart))
                    }
                    span.start >= deleteStart && span.end > deleteEnd -> {
                        newSpans.add(span.copy(start = deleteStart, end = span.end - deleteCount))
                    }
                }
            }
        } else {
            newSpans.addAll(state.spans)
        }

        return state.copy(
            spans = mergeSpans(newSpans),
            lineHeadings = newHeadings
        )
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
        activeMatchRange: IntRange? = null,
        baseOffset: Int = 0,
        fullNoteText: String = text
    ): AnnotatedString {
        return buildAnnotatedString {
            append(text)

            if (text.isNotEmpty()) {
                val baseLine = if (baseOffset > 0 && fullNoteText.isNotEmpty()) {
                    fullNoteText.substring(0, baseOffset.coerceAtMost(fullNoteText.length)).count { it == '\n' }
                } else {
                    0
                }

                val lines = text.split('\n')
                var lineStart = 0
                lines.forEachIndexed { lineIdx, lineText ->
                    val lineEnd = lineStart + lineText.length
                    val globalLineIdx = baseLine + lineIdx
                    val heading = state.lineHeadings[globalLineIdx] ?: HeadingType.BODY
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
                                    fontSize = 20.sp,
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
                val s = (span.start - baseOffset).coerceIn(0, text.length)
                val e = (span.end - baseOffset).coerceIn(0, text.length)
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
                    val globalMatchStart = baseOffset + match.first
                    val isCurrentActive = activeMatchRange != null &&
                        activeMatchRange.first == globalMatchStart &&
                        activeMatchRange.last == (baseOffset + match.last)

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
    private val activeMatchRange: IntRange? = null,
    private val baseOffset: Int = 0,
    private val fullNoteText: String = ""
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val full = if (fullNoteText.isNotEmpty()) fullNoteText else text.text
        val annotated = NoteTextFormatter.buildAnnotatedString(
            text = text.text,
            state = formatState,
            baseColor = baseColor,
            searchQuery = searchQuery,
            activeMatchRange = activeMatchRange,
            baseOffset = baseOffset,
            fullNoteText = full
        )
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}
