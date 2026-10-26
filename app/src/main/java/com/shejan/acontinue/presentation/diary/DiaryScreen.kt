package com.shejan.acontinue.presentation.diary

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
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
    val haptic = LocalHapticFeedback.current

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Intercept hardware/system back button during multi-selection mode
    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.clearSelection()
    }

    if (showDeleteDialog) {
        val count = uiState.selectedKeys.size
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF161616),
            shape = RoundedCornerShape(12.dp),
            title = {
                Text(
                    text = "DELETE $count ${if (count == 1) "NOTE" else "NOTES"}?",
                    color = UtilitarianTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "This action cannot be undone.",
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedNotes()
                        showDeleteDialog = false
                    }
                ) {
                    Text(
                        text = "DELETE",
                        color = Color(0xFFFF453A),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "CANCEL",
                        color = UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        item {
            var sortMenuExpanded by remember { mutableStateOf(false) }

            DotMatrixHeader(
                title = if (uiState.isSelectionMode) "${uiState.selectedKeys.size} SELECTED" else "DIARY",
                leadingIcon = if (uiState.isSelectionMode) null else Icons.AutoMirrored.Outlined.MenuBook,
                badgeText = if (uiState.isSelectionMode) null else badgeText,
                actions = {
                    if (!uiState.isSelectionMode) {
                        // Default Mode: Sort Button with Dropdown Menu
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161616))
                                    .clickable { sortMenuExpanded = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Sort,
                                    contentDescription = "Sort cards",
                                    tint = UtilitarianTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false },
                                modifier = Modifier
                                    .background(Color(0xFF161616))
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp))
                            ) {
                                DiarySortOption.entries.forEach { option ->
                                    val isSelected = uiState.sortOption == option
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option.displayName,
                                                color = if (isSelected) UtilitarianTextPrimary else UtilitarianTextSecondary,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                                fontSize = 14.sp
                                            )
                                        },
                                        trailingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Outlined.Check,
                                                    contentDescription = "Selected",
                                                    tint = UtilitarianTextPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            viewModel.onSortOptionChange(option)
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Multi-Selection Mode: Pin, Delete, and Cancel/Close Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pin Button
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161616))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.togglePinSelectedNotes()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.areAllSelectedPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = if (uiState.areAllSelectedPinned) "Unpin notes" else "Pin notes",
                                    tint = UtilitarianTextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete Button
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFFF453A), RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161616))
                                    .clickable { showDeleteDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete selected notes",
                                    tint = Color(0xFFFF453A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Cancel / Close Selection Button
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161616))
                                    .clickable { viewModel.clearSelection() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Cancel selection",
                                    tint = UtilitarianTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
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
                val isSelected = note.itemKey in uiState.selectedKeys
                DiaryNoteCard(
                    note = note,
                    isSelected = isSelected,
                    isSelectionMode = uiState.isSelectionMode,
                    onClick = {
                        if (uiState.isSelectionMode) {
                            viewModel.toggleSelection(note.itemKey)
                        } else {
                            onNoteClick(if (note.activityId > 0L) note.activityId else note.noteId)
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleSelection(note.itemKey)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun DiaryNoteCard(
    note: DiaryNoteItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
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
    val baseBorderColor = if (hasCustomColor) {
        Color(note.colorHex).copy(alpha = 0.6f)
    } else {
        UtilitarianBorderStroke
    }

    val cardShape = RoundedCornerShape(12.dp)
    val cardBorderColor = if (isSelected) Color.White else baseBorderColor
    val cardBorderWidth = if (isSelected) 2.dp else 1.dp
    val cardBackgroundColor = if (isSelected) Color(0xFF1E1E1E) else UtilitarianCardBackground

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = cardShape,
        color = cardBackgroundColor,
        border = BorderStroke(cardBorderWidth, cardBorderColor)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header indicators for untitled notes
                if (!hasTitle && (note.isPinned || isSelectionMode)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.isPinned) {
                            Icon(
                                imageVector = Icons.Outlined.PushPin,
                                contentDescription = "Pinned note",
                                tint = UtilitarianTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            if (isSelectionMode) Spacer(modifier = Modifier.width(6.dp))
                        }
                        if (isSelectionMode) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (isSelected) Color.White else UtilitarianBorderStroke,
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                    if (hasContent || hasLabels) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Part 1: Full Title with optional pin and selection indicators
                if (hasTitle) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = note.title,
                            color = UtilitarianTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (note.isPinned || isSelectionMode) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                if (note.isPinned) {
                                    Icon(
                                        imageVector = Icons.Outlined.PushPin,
                                        contentDescription = "Pinned note",
                                        tint = UtilitarianTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (isSelectionMode) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isSelected) Color.White else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (isSelected) Color.White else UtilitarianBorderStroke,
                                                RoundedCornerShape(4.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Outlined.Check,
                                                contentDescription = "Selected",
                                                tint = Color.Black,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
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
}
