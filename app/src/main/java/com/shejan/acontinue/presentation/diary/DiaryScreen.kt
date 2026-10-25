package com.shejan.acontinue.presentation.diary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    onClick = { onNoteClick(note.activityId) }
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
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
    val dateStr = remember(note.updatedAtMillis) { dateFormat.format(Date(note.updatedAtMillis)) }

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
            // Header Row: Title & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title.ifBlank { "Untitled Note" },
                    color = UtilitarianTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateStr,
                    color = UtilitarianTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }

            // Preview Text Snippet
            if (note.previewText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.previewText,
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Metadata Badges (Audio, Photo, Checklist, Labels)
            if (note.audioCount > 0 || note.photoCount > 0 || note.isChecklist || note.labels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (note.audioCount > 0) {
                        UtilitarianBadge(
                            text = if (note.audioCount == 1) "VOICE" else "${note.audioCount} VOICE",
                            backgroundColor = UtilitarianBorderStroke,
                            textColor = UtilitarianTextPrimary
                        )
                    }
                    if (note.photoCount > 0) {
                        UtilitarianBadge(
                            text = if (note.photoCount == 1) "PHOTO" else "${note.photoCount} PHOTOS",
                            backgroundColor = UtilitarianBorderStroke,
                            textColor = UtilitarianTextPrimary
                        )
                    }
                    if (note.isChecklist) {
                        UtilitarianBadge(
                            text = "CHECKLIST",
                            backgroundColor = UtilitarianBorderStroke,
                            textColor = UtilitarianTextPrimary
                        )
                    }
                    note.labels.forEach { label ->
                        UtilitarianBadge(
                            text = "#$label",
                            backgroundColor = Color(0xFF202528),
                            textColor = UtilitarianTextSecondary
                        )
                    }
                }
            }
        }
    }
}
