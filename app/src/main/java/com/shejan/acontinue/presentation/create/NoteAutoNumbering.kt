package com.shejan.acontinue.presentation.create

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Handles automated list numbering and intelligent editing behaviors for note text:
 * 1. Automatically inserts sequential numbers glued with a Non-Breaking Space (e.g., "1.\u00A0" -> "2.\u00A0") when Enter is pressed.
 * 2. Renumbers subsequent consecutive list items when an item is inserted mid-list.
 * 3. Gracefully terminates the list when Enter is pressed on an empty numbered item (clearing the prefix).
 * 4. Cleans up empty prefixes in a single stroke when Backspace is pressed immediately after the prefix.
 * 5. Converts manually typed spaces after numbered dots (e.g. "1. ") into non-breaking spaces to prevent line breaking between number and text.
 */
object NoteAutoNumbering {

    const val NBSP: Char = '\u00A0'

    private val NUMBERED_PREFIX_REGEX = Regex("""^([\s\u00A0]*)(\d+)\.[\s\u00A0]*(.*)$""")
    private val EMPTY_NUMBERED_PREFIX_REGEX = Regex("""^([\s\u00A0]*)(\d+)\.$""")
    private val TYPED_DOT_SPACE_REGEX = Regex("""^([\s\u00A0]*)(\d+)\. $""")

    /**
     * Intercepts and transforms [newTfv] if a list-related edit (Enter / Backspace / Space on a numbered prefix) occurred.
     * Returns the modified [TextFieldValue] with updated text and cursor position, or [newTfv] unchanged.
     */
    fun handleAutoNumbering(oldTfv: TextFieldValue, newTfv: TextFieldValue): TextFieldValue {
        val oldText = oldTfv.text
        val newText = newTfv.text

        if (oldText == newText) return newTfv

        val cursor = newTfv.selection.start

        // --- Scenario 1: Newline insertion (Enter key) ---
        val isSingleNewlineInserted = cursor > 0 &&
            newText[cursor - 1] == '\n' &&
            (newText.length == oldText.length + 1 || newText.length == oldText.length - oldTfv.selection.length + 1)

        if (isSingleNewlineInserted) {
            val newlinePos = cursor - 1

            // Find the start of the line preceding this newline
            val lineStart = newText.lastIndexOf('\n', (newlinePos - 1).coerceAtLeast(0)).let {
                if (it == -1 || newlinePos == 0) 0 else it + 1
            }
            val lineBeforeNewline = newText.substring(lineStart, newlinePos)

            val match = NUMBERED_PREFIX_REGEX.matchEntire(lineBeforeNewline)
            if (match != null) {
                val indent = match.groupValues[1]
                val numStr = match.groupValues[2]
                val content = match.groupValues[3]

                val isContentEmpty = content.all { it.isWhitespace() || it == NBSP }

                if (isContentEmpty) {
                    // Empty list item: User pressed Enter on an empty prefix like "2. " or "2.\u00A0"
                    // Cleanly terminate the list by removing the empty prefix and leaving a blank line.
                    val textBeforeLine = newText.substring(0, lineStart)
                    val textAfterNewline = newText.substring(newlinePos + 1)
                    val cleanText = textBeforeLine + indent + "\n" + textAfterNewline
                    val newCursor = (textBeforeLine + indent + "\n").length
                    return TextFieldValue(text = cleanText, selection = TextRange(newCursor))
                } else {
                    // Active list item: User pressed Enter on "1.\u00A0Homework"
                    val currentNum = numStr.toIntOrNull() ?: 1
                    val nextNum = currentNum + 1
                    val nextPrefix = "$indent$nextNum.$NBSP"

                    val textAfter = newText.substring(newlinePos + 1)

                    val finalText: String
                    if (textAfter.startsWith("\n")) {
                        // User pressed Enter at end of line, with an existing line following
                        val subsequent = textAfter.substring(1)
                        val renumberedSubsequent = renumberSubsequentLines(subsequent, nextNum, indent)
                        finalText = newText.substring(0, newlinePos + 1) + nextPrefix + "\n" + renumberedSubsequent
                    } else if (textAfter.isNotEmpty()) {
                        // User pressed Enter in the middle of text (or before next line text)
                        val nextNewlineIdx = textAfter.indexOf('\n')
                        if (nextNewlineIdx != -1) {
                            val currentLineRemainder = textAfter.substring(0, nextNewlineIdx)
                            val subsequent = textAfter.substring(nextNewlineIdx + 1)
                            val renumberedSubsequent = renumberSubsequentLines(subsequent, nextNum, indent)
                            finalText = newText.substring(0, newlinePos + 1) + nextPrefix + currentLineRemainder + "\n" + renumberedSubsequent
                        } else {
                            finalText = newText.substring(0, newlinePos + 1) + nextPrefix + textAfter
                        }
                    } else {
                        // User pressed Enter at the end of the entire text
                        finalText = newText.substring(0, newlinePos + 1) + nextPrefix
                    }

                    val finalCursor = newlinePos + 1 + nextPrefix.length
                    return TextFieldValue(text = finalText, selection = TextRange(finalCursor))
                }
            }
        }

        // --- Scenario 2: Backspace on an empty numbered prefix ---
        // Example: Line was "2.\u00A0" (cursor at 3). User hits backspace, deleting NBSP or space -> "2."
        if (oldText.length == newText.length + 1 && cursor < oldText.length) {
            val deletedChar = oldText[cursor]
            if (deletedChar == ' ' || deletedChar == NBSP) {
                val lineStart = newText.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let {
                    if (it == -1 || cursor == 0) 0 else it + 1
                }
                val lineText = newText.substring(lineStart, cursor)
                val backspaceMatch = EMPTY_NUMBERED_PREFIX_REGEX.matchEntire(lineText)
                if (backspaceMatch != null) {
                    val indent = backspaceMatch.groupValues[1]
                    val prefixBeforeLine = newText.substring(0, lineStart)
                    val textAfterPrefix = newText.substring(cursor)
                    val finalText = prefixBeforeLine + indent + textAfterPrefix
                    val finalCursor = prefixBeforeLine.length + indent.length
                    return TextFieldValue(text = finalText, selection = TextRange(finalCursor))
                }
            }
        }

        // --- Scenario 3: Manually typed space after a number dot (e.g. "1. ") -> convert to NBSP ---
        if (newText.length == oldText.length + 1 && cursor > 0 && newText[cursor - 1] == ' ') {
            val lineStart = newText.lastIndexOf('\n', (cursor - 2).coerceAtLeast(0)).let {
                if (it == -1 || cursor <= 1) 0 else it + 1
            }
            val lineText = newText.substring(lineStart, cursor)
            val typedMatch = TYPED_DOT_SPACE_REGEX.matchEntire(lineText)
            if (typedMatch != null) {
                val indent = typedMatch.groupValues[1]
                val numStr = typedMatch.groupValues[2]
                val textBeforeLine = newText.substring(0, lineStart)
                val textAfterSpace = newText.substring(cursor)
                val fixedText = textBeforeLine + indent + numStr + "." + NBSP + textAfterSpace
                return TextFieldValue(text = fixedText, selection = TextRange(cursor))
            }
        }

        return newTfv
    }

    /**
     * Renumbers subsequent contiguous list items if an item was inserted in the middle of a numbered list.
     * Stops renumbering at the first non-numbered line or blank line.
     */
    private fun renumberSubsequentLines(text: String, startingExpectedNum: Int, targetIndent: String): String {
        if (text.isEmpty()) return text

        val lines = text.split('\n')
        var currentExpected = startingExpectedNum
        val modifiedLines = lines.toMutableList()
        var modified = false

        for (i in modifiedLines.indices) {
            val line = modifiedLines[i]
            val match = NUMBERED_PREFIX_REGEX.matchEntire(line)
            if (match != null) {
                val indent = match.groupValues[1]
                val num = match.groupValues[2].toIntOrNull()
                val content = match.groupValues[3]

                if (indent == targetIndent && num != null && num == currentExpected) {
                    val newNum = num + 1
                    modifiedLines[i] = "$indent$newNum.$NBSP$content"
                    currentExpected = num + 1
                    modified = true
                } else {
                    break
                }
            } else {
                break
            }
        }

        return if (modified) modifiedLines.joinToString("\n") else text
    }
}
