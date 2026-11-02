package com.shejan.acontinue.presentation.diary

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimaryMuted
import com.shejan.acontinue.ui.theme.UtilitarianTextSecondary

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    onNoteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onActivityClick: ((Long) -> Unit)? = null,
    onNavigateToArchive: (() -> Unit)? = null,
    onNavigateToBin: (() -> Unit)? = null,
    onNavigateToSearch: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val noteCount = uiState.notes.size
    val badgeText = if (noteCount == 1) "1 NOTE" else "$noteCount NOTES"
    val haptic = LocalHapticFeedback.current
    val staggeredGridState = rememberLazyStaggeredGridState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isFilterOpen by remember { mutableStateOf(false) }

    // Intercept hardware/system back button during filter overlay or multi-selection mode
    BackHandler(enabled = isFilterOpen || uiState.isSelectionMode) {
        if (isFilterOpen) {
            isFilterOpen = false
        } else {
            viewModel.clearSelection()
        }
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

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(if (uiState.isGridMode) 2 else 1),
            state = staggeredGridState,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalItemSpacing = 10.dp,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                DotMatrixHeader(
                    title = if (uiState.isSelectionMode) "${uiState.selectedKeys.size} SELECTED" else "DIARY",
                    badgeText = if (uiState.isSelectionMode) null else badgeText,
                    actions = {
                        if (!uiState.isSelectionMode) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. SEARCH (Opens dedicated Search screen)
                                if (onNavigateToSearch != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(Color(0xFF202020))
                                            .border(
                                                1.dp,
                                                UtilitarianBorderStroke.copy(alpha = 0.5f),
                                                RoundedCornerShape(5.dp)
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onNavigateToSearch()
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Search,
                                            contentDescription = "Search notes",
                                            tint = UtilitarianTextPrimary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                // 2. CARD FORMAT TOGGLE (1-column vs 2-column)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF202020))
                                        .border(
                                            1.dp,
                                            UtilitarianBorderStroke.copy(alpha = 0.5f),
                                            RoundedCornerShape(5.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.toggleLayoutMode()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isGridMode) Icons.Outlined.ViewAgenda else Icons.Outlined.GridView,
                                        contentDescription = "Toggle card layout",
                                        tint = UtilitarianTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // 3. FILTER (Pill-shaped speed dial overlay)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (isFilterOpen) Color(0xFF2C2C2C) else Color(0xFF202020))
                                        .border(
                                            1.dp,
                                            if (isFilterOpen) Color.White.copy(alpha = 0.8f) else UtilitarianBorderStroke.copy(alpha = 0.5f),
                                            RoundedCornerShape(5.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isFilterOpen = !isFilterOpen
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FilterList,
                                        contentDescription = "Filter and sort cards",
                                        tint = UtilitarianTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        } else {
                            // Multi-Selection Mode: Pin, Archive, Delete, and Cancel/Close Buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Pin Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF202020))
                                        .border(
                                            1.dp,
                                            UtilitarianBorderStroke.copy(alpha = 0.5f),
                                            RoundedCornerShape(5.dp)
                                        )
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
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // 2. Archive Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF202020))
                                        .border(
                                            1.dp,
                                            UtilitarianBorderStroke.copy(alpha = 0.5f),
                                            RoundedCornerShape(5.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.archiveSelectedNotes()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Archive,
                                        contentDescription = "Archive selected notes",
                                        tint = UtilitarianTextPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // 3. Delete Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF202020))
                                        .border(
                                            1.dp,
                                            Color(0xFFFF453A).copy(alpha = 0.7f),
                                            RoundedCornerShape(5.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showDeleteDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = "Delete selected notes",
                                        tint = Color(0xFFFF453A),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                // 4. Cancel / Close Selection Button
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF202020))
                                        .border(
                                            1.dp,
                                            UtilitarianBorderStroke.copy(alpha = 0.5f),
                                            RoundedCornerShape(5.dp)
                                        )
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.clearSelection()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Cancel selection",
                                        tint = UtilitarianTextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Notes List or Empty State
            if (uiState.notes.isEmpty()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    MonochromeCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No notes recorded yet.",
                                color = UtilitarianTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Capture your thoughts, ideas, and voice notes.",
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
                }
            }
        }

        // Layer 2: Dim Scrim
        AnimatedVisibility(
            visible = isFilterOpen,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isFilterOpen = false
                    }
            )
        }

        // Layer 3A: Top Filter Button (rendered above scrim so it is NOT dimmed; tap closes overlay)
        AnimatedVisibility(
            visible = isFilterOpen,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(120)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF2C2C2C))
                    .border(
                        1.dp,
                        Color.White.copy(alpha = 0.8f),
                        RoundedCornerShape(5.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isFilterOpen = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FilterList,
                    contentDescription = "Close filter options",
                    tint = UtilitarianTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Layer 3B: Floating Filter Speed Dial Pills aligned with right edge (lowered slightly to 76.dp)
        AnimatedVisibility(
            visible = isFilterOpen,
            enter = fadeIn(tween(160)) + slideInVertically(
                animationSpec = tween(160, easing = FastOutSlowInEasing)
            ) { -it / 3 },
            exit = fadeOut(tween(120)) + slideOutVertically(
                animationSpec = tween(120, easing = FastOutSlowInEasing)
            ) { -it / 3 },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 76.dp, end = 16.dp)
        ) {
            FilterSpeedDialPills(
                currentOption = uiState.sortOption,
                onSelectOption = { option ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.onSortOptionChange(option)
                    isFilterOpen = false
                },
                onCloseClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isFilterOpen = false
                }
            )
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

@Composable
private fun FilterSpeedDialPills(
    currentOption: DiarySortOption,
    onSelectOption: (DiarySortOption) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DiarySortOption.entries.forEach { option ->
            val icon = when (option) {
                DiarySortOption.DEFAULT -> Icons.AutoMirrored.Outlined.Sort
                DiarySortOption.DATE_CREATED -> Icons.Outlined.CalendarToday
                DiarySortOption.DATE_MODIFIED -> Icons.Outlined.AccessTime
            }
            FilterOptionPill(
                icon = icon,
                option = option,
                isSelected = currentOption == option,
                onClick = { onSelectOption(option) }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Dedicated Cross close button horizontally centered just below the last pill
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF202020))
                .border(1.dp, UtilitarianBorderStroke, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onCloseClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close filter options",
                tint = UtilitarianTextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun FilterOptionPill(
    icon: ImageVector,
    option: DiarySortOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(188.dp)
            .clip(RoundedCornerShape(32.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(32.dp),
        color = if (isSelected) Color(0xFF2C2C2C) else Color(0xFF202020),
        border = BorderStroke(
            1.dp,
            if (isSelected) Color.White.copy(alpha = 0.85f) else UtilitarianBorderStroke
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) UtilitarianTextPrimary else UtilitarianTextPrimaryMuted,
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = option.displayName,
                    color = if (isSelected) UtilitarianTextPrimary else UtilitarianTextPrimaryMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    letterSpacing = 0.6.sp,
                    maxLines = 1
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Selected",
                    tint = UtilitarianTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
