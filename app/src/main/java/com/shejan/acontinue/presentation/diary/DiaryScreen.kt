package com.shejan.acontinue.presentation.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.core.designsystem.components.UtilitarianBadge
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianCardBackground
import com.shejan.acontinue.ui.theme.UtilitarianTextDisabled
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary
import com.shejan.acontinue.ui.theme.UtilitarianTextSecondary

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    onNoteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onActivityClick: ((Long) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val noteCount = uiState.notes.size
    val badgeText = if (noteCount == 1) "1 NOTE" else "$noteCount NOTES"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        item {
            DotMatrixHeader(
                title = "DIARY",
                leadingIcon = Icons.AutoMirrored.Outlined.MenuBook,
                badgeText = badgeText
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Search Bar
        item {
            TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = {
                    Text(
                        text = "Search notes...",
                        color = UtilitarianTextDisabled,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search notes",
                        tint = UtilitarianTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = UtilitarianCardBackground,
                    unfocusedContainerColor = UtilitarianCardBackground,
                    focusedTextColor = UtilitarianTextPrimary,
                    unfocusedTextColor = UtilitarianTextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Notes List or Empty State
        if (uiState.notes.isEmpty()) {
            item {
                MonochromeCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "No matching notes found." else "No notes recorded yet.",
                            color = UtilitarianTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "Try searching with different keywords." else "Capture your thoughts, ideas, and voice notes.",
                            color = UtilitarianTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(uiState.notes, key = { "diary_note_${it.activityId}_${it.noteId}" }) { note ->
                DiaryNoteCard(
                    note = note,
                    onClick = { onNoteClick(if (note.activityId > 0L) note.activityId else note.noteId) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiaryNoteCard(
    note: DiaryNoteItem,
    onClick: () -> Unit
) {
    val hasTitle = note.title.isNotBlank() && !note.title.equals("Untitled Note", ignoreCase = true)
    val hasContent = note.previewText.isNotBlank()
    val hasLabels = note.labels.isNotEmpty()

    val displayContent = remember(note.previewText) {
        val lines = note.previewText.lines()
        if (lines.size > 10) {
            val first9 = lines.take(9)
            val tenth = lines[9].trimEnd()
            val tenthWithDots = if (tenth.endsWith("...")) tenth else "$tenth..."
            (first9 + tenthWithDots).joinToString("\n")
        } else {
            note.previewText
        }
    }

    val hasCustomColor = note.colorHex != 0L && note.colorHex != 0xFF000000L && note.colorHex != 0xFF121212L
    val cardBorderColor = if (hasCustomColor) {
        Color(note.colorHex).copy(alpha = 0.6f)
    } else {
        UtilitarianBorderStroke
    }

    MonochromeCard(
        onClick = onClick,
        borderColor = cardBorderColor
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Part 1: Full Title
            if (hasTitle) {
                Text(
                    text = note.title,
                    color = UtilitarianTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Spacer between Part 1 and Part 2
            if (hasTitle && hasContent) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Part 2: Note Content (First 10 lines max with ellipsis on 10th line)
            if (hasContent) {
                Text(
                    text = displayContent,
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Spacer between Content (or Title) and Part 3 Labels
            if (hasLabels && (hasTitle || hasContent)) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Part 3: Labels in small boxes
            if (hasLabels) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    note.labels.forEach { label ->
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = UtilitarianBorderStroke,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(Color(0xFF161616), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "#$label",
                                color = UtilitarianTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
