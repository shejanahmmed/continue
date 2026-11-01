package com.shejan.acontinue.presentation.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onNoteClick: (activityId: Long, noteId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalUtilitarianColors.current
    val haptic = LocalHapticFeedback.current

    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
        ) {
            // Header
            item {
                DotMatrixHeader(
                    title = "ARCHIVE",
                    badgeText = if (uiState.notes.isNotEmpty()) "${uiState.notes.size} NOTES" else null,
                    actions = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp))
                                .background(Color(0xFF161616))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateBack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search Bar
            if (uiState.notes.isNotEmpty() || uiState.searchQuery.isNotBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp))
                            .background(Color(0xFF161616))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = colors.textPrimaryMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            Box(modifier = Modifier.weight(1f)) {
                                if (uiState.searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search archived notes...",
                                        color = colors.textPrimaryMuted,
                                        fontSize = 13.sp
                                    )
                                }
                                BasicTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChange(it) },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = colors.textPrimary,
                                        fontSize = 13.sp
                                    ),
                                    cursorBrush = SolidColor(colors.textPrimary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            if (uiState.searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear search",
                                    tint = colors.textPrimaryMuted,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { viewModel.onSearchQueryChange("") }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // Empty State
            if (uiState.notes.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(12.dp))
                                    .background(Color(0xFF161616)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Archive,
                                    contentDescription = null,
                                    tint = colors.textPrimaryMuted,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "NO ARCHIVED NOTES",
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Notes you archive will appear here instead of your diary",
                                color = colors.textPrimaryMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(uiState.notes, key = { it.uniqueKey }) { note ->
                    MonochromeCard(
                        modifier = Modifier.padding(bottom = 10.dp),
                        onClick = { onNoteClick(note.activityId, note.noteId) }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Top Row: Date & Labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (note.archivedAtMillis > 0L) {
                                    Text(
                                        text = dateFormat.format(Date(note.archivedAtMillis)),
                                        color = colors.textPrimaryMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }

                                if (note.labels.isNotEmpty()) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        note.labels.take(2).forEach { label ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF202020))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = colors.textSecondary,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Title
                            Text(
                                text = if (note.title.isNotBlank()) note.title else "(Untitled Note)",
                                color = if (note.title.isNotBlank()) colors.textPrimary else colors.textPrimaryMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Preview snippet
                            if (note.previewText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.previewText,
                                    color = colors.textSecondary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons: Unarchive & Move to Bin
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Unarchive Button
                                Box(
                                    modifier = Modifier
                                        .height(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(6.dp))
                                        .background(Color(0xFF202020))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.unarchiveNote(note)
                                        }
                                        .padding(horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Unarchive,
                                            contentDescription = "Unarchive",
                                            tint = colors.textPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "UNARCHIVE",
                                            color = colors.textPrimary,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Move to Bin Button
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(6.dp))
                                        .background(Color(0xFF202020))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.deleteNote(note)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Move to Bin",
                                        tint = colors.textPrimaryMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

