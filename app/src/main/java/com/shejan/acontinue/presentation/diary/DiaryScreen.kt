package com.shejan.acontinue.presentation.diary

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.shejan.acontinue.core.util.ImageUtils
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.LocalToastState
import com.shejan.acontinue.presentation.create.isDefaultNoteColor
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
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
    onCreateNote: (() -> Unit)? = null,
    onNavigateToSearch: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val noteCount = uiState.notes.size
    val badgeText = if (noteCount == 1) "1 NOTE" else "$noteCount NOTES"
    val haptic = LocalHapticFeedback.current
    val toastState = LocalToastState.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val staggeredGridState = rememberLazyStaggeredGridState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var animatingDismissKeys by remember { mutableStateOf<Set<String>>(emptySet()) }
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
        Dialog(
            onDismissRequest = { showDeleteDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF161616),
                border = BorderStroke(1.dp, UtilitarianBorderStroke)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "DELETE $count ${if (count == 1) "NOTE" else "NOTES"}?",
                        color = UtilitarianTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This action cannot be undone.",
                        color = UtilitarianTextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel Button Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF202020))
                                .border(1.dp, Color(0xFF383838), RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showDeleteDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CANCEL",
                                color = UtilitarianTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Delete Button Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFF453A).copy(alpha = 0.16f))
                                .border(1.dp, Color(0xFFFF453A), RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val keysToDelete = uiState.selectedKeys.toSet()
                                    showDeleteDialog = false
                                    animatingDismissKeys = keysToDelete
                                    coroutineScope.launch {
                                        delay(230.milliseconds)
                                        viewModel.deleteSelectedNotes()
                                        animatingDismissKeys = emptySet()
                                        toastState.show(if (count == 1) "1 note moved to bin" else "$count notes moved to bin")
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DELETE",
                                color = Color(0xFFFF453A),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    val renderTopHeader: @Composable (Modifier) -> Unit = { headerModifier ->
        DotMatrixHeader(
            title = if (uiState.isSelectionMode) "${uiState.selectedKeys.size} SELECTED" else "DIARY",
            badgeText = if (uiState.isSelectionMode) null else badgeText,
            modifier = headerModifier,
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
                                        if (isFilterOpen) isFilterOpen = false
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
                                contentDescription = if (isFilterOpen) "Close filter options" else "Filter and sort cards",
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
                                    val count = uiState.selectedKeys.size
                                    val willUnpin = uiState.areAllSelectedPinned
                                    viewModel.togglePinSelectedNotes()
                                    toastState.show(
                                        if (willUnpin) {
                                            if (count == 1) "1 note unpinned" else "$count notes unpinned"
                                        } else {
                                            if (count == 1) "1 note pinned" else "$count notes pinned"
                                        }
                                    )
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
                                    val archiveCount = uiState.selectedKeys.size
                                    val keysToArchive = uiState.selectedKeys.toSet()
                                    animatingDismissKeys = keysToArchive
                                    coroutineScope.launch {
                                        delay(230.milliseconds)
                                        viewModel.archiveSelectedNotes()
                                        animatingDismissKeys = emptySet()
                                        toastState.show(if (archiveCount == 1) "1 note archived" else "$archiveCount notes archived")
                                    }
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
                                tint = UtilitarianTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 100.dp)
            ) {
                renderTopHeader(Modifier)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    DiaryEmptyState(
                        onCreateNote = { onCreateNote?.invoke() ?: onNoteClick(0L) }
                    )
                }
            }
        } else {
            val pinnedNotes = remember(uiState.notes) { uiState.notes.filter { it.isPinned } }
            val otherNotes = remember(uiState.notes) { uiState.notes.filter { !it.isPinned } }
            val hasPinned = pinnedNotes.isNotEmpty()

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
                    renderTopHeader(Modifier)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (hasPinned) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Text(
                            text = "PINNED",
                            color = UtilitarianTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 4.dp)
                        )
                    }

                    items(pinnedNotes, key = { "diary_note_${it.activityId}_${it.noteId}" }) { note ->
                        val isSelected = note.itemKey in uiState.selectedKeys
                        val isDismissing = note.itemKey in animatingDismissKeys
                        DiaryNoteCard(
                            note = note,
                            isSelected = isSelected,
                            isSelectionMode = uiState.isSelectionMode,
                            isDismissing = isDismissing,
                            isGridMode = uiState.isGridMode,
                            modifier = Modifier.animateItem(
                                fadeInSpec = null,
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                fadeOutSpec = null
                            ),
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

                    if (otherNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Text(
                                text = "OTHERS",
                                color = UtilitarianTextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 14.dp, bottom = 4.dp)
                            )
                        }

                        items(otherNotes, key = { "diary_note_${it.activityId}_${it.noteId}" }) { note ->
                            val isSelected = note.itemKey in uiState.selectedKeys
                            val isDismissing = note.itemKey in animatingDismissKeys
                            DiaryNoteCard(
                                note = note,
                                isSelected = isSelected,
                                isSelectionMode = uiState.isSelectionMode,
                                isDismissing = isDismissing,
                                isGridMode = uiState.isGridMode,
                                modifier = Modifier.animateItem(
                                    fadeInSpec = null,
                                    placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    fadeOutSpec = null
                                ),
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
                } else {
                    items(uiState.notes, key = { "diary_note_${it.activityId}_${it.noteId}" }) { note ->
                        val isSelected = note.itemKey in uiState.selectedKeys
                        val isDismissing = note.itemKey in animatingDismissKeys
                        DiaryNoteCard(
                            note = note,
                            isSelected = isSelected,
                            isSelectionMode = uiState.isSelectionMode,
                            isDismissing = isDismissing,
                            isGridMode = uiState.isGridMode,
                            modifier = Modifier.animateItem(
                                fadeInSpec = null,
                                placementSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                fadeOutSpec = null
                            ),
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

        // Layer 3A: Top Header (rendered above scrim so page title "DIARY", badge & buttons are un-dimmed)
        AnimatedVisibility(
            visible = isFilterOpen,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(120)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            renderTopHeader(Modifier)
        }

        // Layer 3B: Floating Filter Speed Dial Pills (staggered cascade entrance from right edge)
        AnimatedVisibility(
            visible = isFilterOpen,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(120)) + slideOutHorizontally(
                animationSpec = tween(140, easing = FastOutSlowInEasing)
            ) { fullWidth -> fullWidth },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 72.dp, end = 16.dp)
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

private val PHOTO_TAG_REGEX = Regex("""!\[photo\]\(([^)]+)\)""")

@Composable
private fun CardPhotoThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            ImageUtils.loadSampledBitmapFromUri(context, uri, reqWidth = 600, reqHeight = 600)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF161616))
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = "Note image preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Photo,
                    contentDescription = null,
                    tint = UtilitarianTextDisabled,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun NoteAudioRows(
    audioCount: Int,
    hasCustomColor: Boolean,
    modifier: Modifier = Modifier
) {
    if (audioCount <= 0) return
    val count = minOf(audioCount, 4)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..count) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Audio recording",
                    tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recording $i",
                    color = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (audioCount > 4) {
            val remaining = audioCount - 4
            Text(
                text = "+ $remaining more",
                color = if (hasCustomColor) Color.White.copy(alpha = 0.6f) else UtilitarianTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
private fun DiaryNoteCard(
    note: DiaryNoteItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    modifier: Modifier = Modifier,
    isDismissing: Boolean = false,
    isGridMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val windowInfo = LocalWindowInfo.current
    val colors = LocalUtilitarianColors.current
    val screenWidthPx = windowInfo.containerSize.width.toFloat()

    var isRightColumn by remember { mutableStateOf(false) }
    val exitAnim = remember { Animatable(0f) }

    LaunchedEffect(isDismissing) {
        if (isDismissing) {
            exitAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
        }
    }

    val parsedChecklistItems = remember(note.content, note.previewText, note.isChecklist) {
        val raw = if (note.previewText.isNotBlank()) note.previewText else note.content
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isNotEmpty() && lines.all { it.startsWith("[ ] ") || it.startsWith("[x] ", ignoreCase = true) || it.startsWith("[X] ") }) {
            lines.map { line ->
                val isChecked = line.startsWith("[x] ", ignoreCase = true) || line.startsWith("[X] ")
                val text = if (line.length >= 4) line.substring(4).trim() else ""
                isChecked to text
            }
        } else {
            null
        }
    }
    val hasChecklist = !parsedChecklistItems.isNullOrEmpty()
    val hasTitle = note.title.isNotBlank() && !note.title.equals("Untitled Note", ignoreCase = true)
    val hasContent = note.previewText.isNotBlank() && !hasChecklist
    val hasLabels = note.labels.isNotEmpty()
    val hasAudio = note.audioCount > 0
    val hasPhoto = note.photoCount > 0
    val isMediaOnly = (hasAudio || hasPhoto) && !hasContent && !hasChecklist

    val firstPhotoUri = remember(note.content, hasPhoto) {
        if (hasPhoto) {
            PHOTO_TAG_REGEX.find(note.content)?.groupValues?.getOrNull(1)?.let { uriStr ->
                runCatching { Uri.parse(uriStr) }.getOrNull()
            }
        } else {
            null
        }
    }

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

    val hasCustomColor = !isDefaultNoteColor(note.colorHex)
    val baseBorderColor = if (hasCustomColor) {
        Color.White.copy(alpha = 0.15f)
    } else {
        UtilitarianBorderStroke
    }

    val cardShape = RoundedCornerShape(12.dp)
    val cardBorderColor = if (isSelected) Color.White else baseBorderColor
    val cardBorderWidth = if (isSelected) 2.dp else 1.dp
    val cardBackgroundColor = if (hasCustomColor) {
        Color(note.colorHex)
    } else {
        if (isSelected) Color(0xFF1E1E1E) else UtilitarianCardBackground
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                if (isGridMode) {
                    isRightColumn = coordinates.boundsInRoot().center.x >= (screenWidthPx / 2f)
                }
            }
            .graphicsLayer {
                if (exitAnim.value > 0f) {
                    val travelDistance = if (isGridMode) (screenWidthPx / 2f) * 1.5f else screenWidthPx * 1.1f
                    val direction = if (isGridMode && isRightColumn) 1f else -1f
                    this.translationX = exitAnim.value * travelDistance * direction
                    this.alpha = (1f - exitAnim.value).coerceIn(0f, 1f)
                }
            }
            .clip(cardShape)
            .combinedClickable(
                enabled = !isDismissing,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = cardShape,
        color = cardBackgroundColor,
        border = BorderStroke(cardBorderWidth, cardBorderColor)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isMediaOnly) {
                    if (hasTitle) {
                        // Title row with optional pin and selection indicators
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
                                            imageVector = Icons.Filled.PushPin,
                                            contentDescription = "Pinned note",
                                            tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextSecondary,
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
                                                    if (isSelected) Color.White else if (hasCustomColor) Color.White.copy(alpha = 0.4f) else UtilitarianBorderStroke,
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
                        Spacer(modifier = Modifier.height(8.dp))
                        if (hasPhoto && firstPhotoUri != null) {
                            CardPhotoThumbnail(uri = firstPhotoUri)
                            if (hasAudio) {
                                Spacer(modifier = Modifier.height(6.dp))
                                NoteAudioRows(
                                    audioCount = note.audioCount,
                                    hasCustomColor = hasCustomColor
                                )
                            }
                        } else if (hasAudio) {
                            NoteAudioRows(
                                audioCount = note.audioCount,
                                hasCustomColor = hasCustomColor
                            )
                        }
                    } else {
                        // Untitled Media Note
                        if (hasPhoto && firstPhotoUri != null) {
                            if (note.isPinned || isSelectionMode) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (note.isPinned) {
                                        Icon(
                                            imageVector = Icons.Filled.PushPin,
                                            contentDescription = "Pinned note",
                                            tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextSecondary,
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
                                                    if (isSelected) Color.White else if (hasCustomColor) Color.White.copy(alpha = 0.4f) else UtilitarianBorderStroke,
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
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            CardPhotoThumbnail(uri = firstPhotoUri)
                            if (hasAudio) {
                                Spacer(modifier = Modifier.height(6.dp))
                                NoteAudioRows(
                                    audioCount = note.audioCount,
                                    hasCustomColor = hasCustomColor
                                )
                            }
                        } else if (hasAudio) {
                            if (note.audioCount == 1) {
                                // Single compact row for single audio untitled note
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Mic,
                                            contentDescription = "Audio recording",
                                            tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Recording 1",
                                            color = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    if (note.isPinned || isSelectionMode) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            if (note.isPinned) {
                                                Icon(
                                                    imageVector = Icons.Filled.PushPin,
                                                    contentDescription = "Pinned note",
                                                    tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextSecondary,
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
                                                            if (isSelected) Color.White else if (hasCustomColor) Color.White.copy(alpha = 0.4f) else UtilitarianBorderStroke,
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
                            } else {
                                // Multi-recording untitled note
                                if (note.isPinned || isSelectionMode) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (note.isPinned) {
                                            Icon(
                                                imageVector = Icons.Filled.PushPin,
                                                contentDescription = "Pinned note",
                                                tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextSecondary,
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
                                                        if (isSelected) Color.White else if (hasCustomColor) Color.White.copy(alpha = 0.4f) else UtilitarianBorderStroke,
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
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                NoteAudioRows(
                                    audioCount = note.audioCount,
                                    hasCustomColor = hasCustomColor
                                )
                            }
                        }
                    }
                } else {
                    // Header indicators for untitled notes
                    if (!hasTitle && (note.isPinned || isSelectionMode)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (note.isPinned) {
                                Icon(
                                    imageVector = Icons.Filled.PushPin,
                                    contentDescription = "Pinned note",
                                    tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextSecondary,
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
                                            if (isSelected) Color.White else if (hasCustomColor) Color.White.copy(alpha = 0.4f) else UtilitarianBorderStroke,
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
                        if (hasContent || hasChecklist || hasLabels) {
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
                                            imageVector = Icons.Filled.PushPin,
                                            contentDescription = "Pinned note",
                                            tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextSecondary,
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
                                                    if (isSelected) Color.White else if (hasCustomColor) Color.White.copy(alpha = 0.4f) else UtilitarianBorderStroke,
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
                    if (hasTitle && (hasContent || hasChecklist)) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Part 2: Note Content or Checklist Preview
                    if (parsedChecklistItems != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val displayItems = parsedChecklistItems.take(8)
                            displayItems.forEach { (isChecked, itemText) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                        contentDescription = if (isChecked) "Completed" else "To do",
                                        tint = if (isChecked) {
                                            Color(0xFF3897F0)
                                        } else {
                                            if (hasCustomColor) Color.White.copy(alpha = 0.7f) else colors.textPrimaryMuted
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = itemText,
                                        color = if (isChecked) {
                                            if (hasCustomColor) Color.White.copy(alpha = 0.5f) else UtilitarianTextDisabled
                                        } else {
                                            UtilitarianTextPrimary
                                        },
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            if (parsedChecklistItems.size > 8) {
                                val remaining = parsedChecklistItems.size - 8
                                Text(
                                    text = "+ $remaining more",
                                    color = if (hasCustomColor) Color.White.copy(alpha = 0.6f) else UtilitarianTextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                                )
                            }
                        }
                    } else if (hasContent) {
                        Text(
                            text = displayContent,
                            color = UtilitarianTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            maxLines = 10,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Attached media indicators for notes with text/checklist
                    val hasAttachedMedia = hasAudio || hasPhoto
                    if (hasAttachedMedia) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (hasAudio) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Mic,
                                        contentDescription = "Audio attached",
                                        tint = if (hasCustomColor) Color.White.copy(alpha = 0.7f) else UtilitarianTextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${note.audioCount}",
                                        color = if (hasCustomColor) Color.White.copy(alpha = 0.7f) else UtilitarianTextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            if (hasPhoto) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.Photo,
                                        contentDescription = "Photo attached",
                                        tint = if (hasCustomColor) Color.White.copy(alpha = 0.7f) else UtilitarianTextSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${note.photoCount}",
                                        color = if (hasCustomColor) Color.White.copy(alpha = 0.7f) else UtilitarianTextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Spacer between Content (or Title) and Part 3 Labels
                if (hasLabels && (hasTitle || hasContent || hasChecklist || isMediaOnly)) {
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
                                        color = if (hasCustomColor) Color.White.copy(alpha = 0.2f) else UtilitarianBorderStroke,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(
                                        if (hasCustomColor) Color.Black.copy(alpha = 0.25f) else Color(0xFF161616),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (hasCustomColor) Color.White.copy(alpha = 0.9f) else UtilitarianTextSecondary,
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
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val travelDistancePx = with(density) { 260.dp.toPx() }

    // 4 items: 3 pills + 1 cross button
    val animations = remember { List(4) { Animatable(1f) } }

    LaunchedEffect(Unit) {
        animations.forEachIndexed { index, animatable ->
            launch {
                delay((index * 40).milliseconds) // Staggered by 40ms (~20% progress of the preceding spring)
                animatable.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        dampingRatio = Spring.DampingRatioLowBouncy
                    )
                )
            }
        }
    }

    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DiarySortOption.entries.forEachIndexed { index, option ->
            val icon = when (option) {
                DiarySortOption.DEFAULT -> Icons.AutoMirrored.Outlined.Sort
                DiarySortOption.DATE_CREATED -> Icons.Outlined.CalendarToday
                DiarySortOption.DATE_MODIFIED -> Icons.Outlined.AccessTime
            }
            val animValue = animations[index].value
            Box(
                modifier = Modifier.graphicsLayer {
                    translationX = animValue * travelDistancePx
                    alpha = (1f - animValue).coerceIn(0f, 1f)
                }
            ) {
                FilterOptionPill(
                    icon = icon,
                    option = option,
                    isSelected = currentOption == option,
                    onClick = { onSelectOption(option) }
                )
            }
        }

        // Dedicated Cross close button horizontally centered just below the last pill
        val crossAnimValue = animations[3].value
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .graphicsLayer {
                    translationX = crossAnimValue * travelDistancePx
                    alpha = (1f - crossAnimValue).coerceIn(0f, 1f)
                }
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFF202020))
                .border(
                    1.dp,
                    UtilitarianBorderStroke.copy(alpha = 0.5f),
                    CircleShape
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCloseClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close filter options",
                tint = UtilitarianTextPrimary,
                modifier = Modifier.size(20.dp)
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

@Composable
private fun DiaryEmptyState(
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val colors = LocalUtilitarianColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No notes recorded yet.",
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Capture your thoughts, ideas, and voice notes.",
            color = colors.textSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            border = if (!colors.isDark) BorderStroke(1.dp, Color(0xFFD4D4D4)) else null,
            modifier = Modifier
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCreateNote()
                }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "CREATE NOTE",
                    color = Color.Black,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
