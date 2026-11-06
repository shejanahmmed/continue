package com.shejan.acontinue.presentation.workspace

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import com.shejan.acontinue.core.util.AttachmentHelper
import com.shejan.acontinue.core.util.EventFormatters
import com.shejan.acontinue.domain.model.RecurrenceMode
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.HighContrastButton
import com.shejan.acontinue.core.designsystem.components.LocalToastState
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.core.designsystem.components.UtilitarianBadge
import com.shejan.acontinue.core.util.ClickableLinkText
import com.shejan.acontinue.core.util.LocationHelper
import com.shejan.acontinue.domain.model.ActivityLink
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.model.ActivityWorkspaceAggregate
import com.shejan.acontinue.presentation.create.NoteTextFormatter
import com.shejan.acontinue.presentation.create.isDefaultNoteColor
import com.shejan.acontinue.ui.theme.UtilitarianAccentRed
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianCardBackground
import com.shejan.acontinue.ui.theme.UtilitarianPureBlack
import com.shejan.acontinue.ui.theme.UtilitarianTextDisabled
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimaryMuted
import com.shejan.acontinue.ui.theme.UtilitarianTextSecondary
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityWorkspaceScreen(
    viewModel: ActivityWorkspaceViewModel,
    onNavigateBack: () -> Unit,
    onEditActivity: (Long) -> Unit = {},
    onCreateWorkspaceNote: (Long) -> Unit = {},
    onOpenWorkspaceNote: (Long, Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val toastState = LocalToastState.current

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is WorkspaceEvent.ActivityDeleted -> {
                    toastState.show("Activity deleted")
                    onNavigateBack()
                }
            }
        }
    }

    if (uiState.isLoading || uiState.aggregate == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(UtilitarianPureBlack),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = UtilitarianAccentRed)
        }
        return
    }

    val aggregate = uiState.aggregate!!
    val activity = aggregate.activity

    val haptic = LocalHapticFeedback.current

    BackHandler(enabled = showMenu) {
        showMenu = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UtilitarianPureBlack)
    ) {
        // Layer 1: Main Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Top Bar
            item {
                WorkspaceTopBar(
                    isMenuOpen = showMenu,
                    onNavigateBack = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    },
                    onToggleMenu = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = !showMenu
                    }
                )
            }

            // Header Title Card
            item {
                MonochromeCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = activity.title,
                            color = UtilitarianTextPrimaryMuted,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Workspace Tabs
            item {
                WorkspaceTabRow(
                    selectedTab = uiState.selectedTab,
                    onSelectTab = { viewModel.selectTab(it) },
                    notesCount = aggregate.notes.size,
                    tasksCount = aggregate.tasks.size,
                    linksCount = aggregate.links.size
                )
            }

            // Tab Content
            item {
                when (uiState.selectedTab) {
                    WorkspaceTab.OVERVIEW -> OverviewTabContent(
                        aggregate = aggregate,
                        onAddAttachment = { fileName, uriString, mimeType, sizeBytes ->
                            viewModel.addAttachment(fileName, uriString, mimeType, sizeBytes)
                        },
                        onDeleteAttachment = { attachmentId, filePath ->
                            viewModel.deleteAttachment(attachmentId, filePath)
                            toastState.show("Attachment deleted")
                        }
                    )
                    WorkspaceTab.NOTES -> NotesTabContent(
                        notes = aggregate.notes,
                        onCreateNoteClick = { onCreateWorkspaceNote(activity.id) },
                        onNoteClick = { noteId -> onOpenWorkspaceNote(activity.id, noteId) },
                        onDeleteNote = {
                            viewModel.deleteNote(it)
                            toastState.show("Note moved to bin")
                        }
                    )
                    WorkspaceTab.TASKS -> TasksTabContent(
                        tasks = aggregate.tasks,
                        onAddTask = { viewModel.addTask(it) },
                        onUpdateTask = { id, title -> viewModel.updateTask(id, title) },
                        onToggleTask = { id, done -> viewModel.toggleTask(id, done) },
                        onDeleteTask = {
                            viewModel.deleteTask(it)
                            toastState.show("Task deleted")
                        }
                    )
                    WorkspaceTab.LINKS -> LinksTabContent(
                        links = aggregate.links,
                        onAddLink = { url, title -> viewModel.addLink(url, title) },
                        onUpdateLink = { id, url, title -> viewModel.updateLink(id, url, title) },
                        onDeleteLink = {
                            viewModel.deleteLink(it)
                            toastState.show("Link deleted")
                        }
                    )
                }
            }
        }

        // Layer 2: Dim Scrim
        AnimatedVisibility(
            visible = showMenu,
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
                        showMenu = false
                    }
            )
        }

        // Layer 3A: Top Bar Header above Scrim (remains crisp & un-dimmed)
        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(120)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            WorkspaceTopBar(
                isMenuOpen = showMenu,
                onNavigateBack = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMenu = false
                    onNavigateBack()
                },
                onToggleMenu = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMenu = false
                }
            )
        }

        // Layer 3B: Floating Speed Dial Pills
        AnimatedVisibility(
            visible = showMenu,
            enter = fadeIn(tween(100)),
            exit = fadeOut(tween(120)) + slideOutHorizontally(
                animationSpec = tween(140, easing = FastOutSlowInEasing)
            ) { fullWidth -> fullWidth },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 58.dp, end = 16.dp)
        ) {
            WorkspaceOptionsSpeedDial(
                onEditClick = {
                    showMenu = false
                    onEditActivity(activity.id)
                },
                onDeleteClick = {
                    showMenu = false
                    showDeleteConfirmDialog = true
                },
                onCloseClick = {
                    showMenu = false
                }
            )
        }
    }

    if (showDeleteConfirmDialog) {
        Dialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showDeleteConfirmDialog = false },
                contentAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = 0.35f)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E2023),
                    border = BorderStroke(1.dp, Color(0xFF3F454C)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Intercept touch to prevent dismissing on dialog body click */ }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Delete event?",
                            color = UtilitarianTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Are you sure you want to delete this event? This action cannot be undone.",
                            color = UtilitarianTextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cancel Button Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2C3136))
                                    .border(1.dp, Color(0xFF3F454C), RoundedCornerShape(12.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showDeleteConfirmDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = UtilitarianTextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Delete Button Box
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFD71921))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showDeleteConfirmDialog = false
                                        viewModel.deleteActivity()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Delete",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
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
private fun WorkspaceTopBar(
    isMenuOpen: Boolean,
    onNavigateBack: () -> Unit,
    onToggleMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = UtilitarianTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "WORKSPACE",
            color = UtilitarianTextPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isMenuOpen) Color(0xFF2C2C2C) else Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onToggleMenu
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = if (isMenuOpen) "Close options" else "Options",
                tint = if (isMenuOpen) Color.White else UtilitarianTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WorkspaceOptionsSpeedDial(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val travelDistancePx = with(density) { 260.dp.toPx() }

    val animations = remember { List(3) { Animatable(1f) } }

    LaunchedEffect(Unit) {
        animations.forEachIndexed { index, animatable ->
            launch {
                delay((index * 40).milliseconds)
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
        // Pill 1: Edit
        val editAnimValue = animations[0].value
        Box(
            modifier = Modifier.graphicsLayer {
                translationX = editAnimValue * travelDistancePx
                alpha = (1f - editAnimValue).coerceIn(0f, 1f)
            }
        ) {
            WorkspaceActionPill(
                icon = Icons.Outlined.Edit,
                label = "Edit",
                isDestructive = false,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEditClick()
                }
            )
        }

        // Pill 2: Delete
        val deleteAnimValue = animations[1].value
        Box(
            modifier = Modifier.graphicsLayer {
                translationX = deleteAnimValue * travelDistancePx
                alpha = (1f - deleteAnimValue).coerceIn(0f, 1f)
            }
        ) {
            WorkspaceActionPill(
                icon = Icons.Outlined.Delete,
                label = "Delete",
                isDestructive = true,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDeleteClick()
                }
            )
        }

        // Dedicated Cross close button horizontally centered just below the last pill
        val crossAnimValue = animations[2].value
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
                contentDescription = "Close options",
                tint = UtilitarianTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun WorkspaceActionPill(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean,
    onClick: () -> Unit
) {
    val pillShape = RoundedCornerShape(32.dp)
    val backgroundColor = if (isDestructive) UtilitarianAccentRed else Color(0xFF202020)
    val borderColor = if (isDestructive) UtilitarianAccentRed else UtilitarianBorderStroke
    val contentColor = if (isDestructive) Color.White else UtilitarianTextPrimary

    Surface(
        modifier = Modifier
            .width(136.dp)
            .clip(pillShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = pillShape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = contentColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WorkspaceTabRow(
    selectedTab: WorkspaceTab,
    onSelectTab: (WorkspaceTab) -> Unit,
    notesCount: Int,
    tasksCount: Int,
    linksCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WorkspaceTab.entries.forEach { tab ->
            val label = when (tab) {
                WorkspaceTab.OVERVIEW -> tab.title
                WorkspaceTab.NOTES -> "${tab.title} ($notesCount)"
                WorkspaceTab.TASKS -> "${tab.title} ($tasksCount)"
                WorkspaceTab.LINKS -> "${tab.title} ($linksCount)"
            }
            val isSelected = selectedTab == tab

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelectTab(tab) },
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) UtilitarianCardBackground else UtilitarianPureBlack,
                border = BorderStroke(1.dp, if (isSelected) UtilitarianTextPrimaryMuted else UtilitarianBorderStroke)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) UtilitarianTextPrimaryMuted else UtilitarianTextSecondary,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OverviewTabContent(
    aggregate: ActivityWorkspaceAggregate,
    onAddAttachment: (fileName: String, uriString: String, mimeType: String, sizeBytes: Long) -> Unit,
    onDeleteAttachment: (attachmentId: Long, filePath: String?) -> Unit
) {
    val activity = aggregate.activity
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                uris.forEach { uri ->
                    AttachmentHelper.processAndCopyUri(context, uri)
                        .onSuccess { proc ->
                            onAddAttachment(proc.fileName, proc.filePath, proc.mimeType, proc.sizeBytes)
                        }
                        .onFailure { error ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    error.message ?: "Failed to process attachment",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card 1: DATE, TIME & LOCATION
        MonochromeCard {
                Column {
                    Text(
                        text = "DATE, TIME & LOCATION",
                        color = UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = UtilitarianTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = EventFormatters.formatDate(activity.startTimeMillis),
                            color = UtilitarianTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    // Time span
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = UtilitarianTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = EventFormatters.formatTimeSpan(
                                activity.startTimeMillis,
                                activity.endTimeMillis,
                                activity.isAllDay
                            ),
                            color = UtilitarianTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Location
                    val context = LocalContext.current
                    val effectiveLocation = activity.location.trim()
                    val hasLocation = effectiveLocation.isNotBlank() && !effectiveLocation.equals("location", ignoreCase = true)
                    if (hasLocation) {
                        val isLink = LocationHelper.isMapLocationLink(effectiveLocation)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    LocationHelper.openLocationInMapApp(context, effectiveLocation)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location",
                                tint = if (isLink) Color(0xFF3897F0) else UtilitarianTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = effectiveLocation,
                                color = if (isLink) Color(0xFF3897F0) else UtilitarianTextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = "Open Map",
                                tint = Color(0xFF3897F0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = "Location",
                                tint = UtilitarianTextDisabled,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Not set",
                                color = UtilitarianTextDisabled,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

        // Card 2: EVENT SETTINGS
        MonochromeCard {
                Column {
                    Text(
                        text = "EVENT SETTINGS",
                        color = UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Alert / Reminders
                    val alertText = EventFormatters.formatAlerts(activity) ?: "Not set"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = null,
                                tint = UtilitarianTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Alert",
                                color = UtilitarianTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = alertText,
                            color = if (alertText == "Not set") UtilitarianTextDisabled else UtilitarianTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Repeat / Recurrence
                    val repeatText = if (activity.recurrenceMode != RecurrenceMode.NONE) {
                        EventFormatters.formatRecurrenceRule(activity)
                    } else {
                        "Not set"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = null,
                                tint = UtilitarianTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Repeat",
                                color = UtilitarianTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = repeatText,
                            color = if (repeatText == "Not set") UtilitarianTextDisabled else UtilitarianTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Priority
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Flag,
                                contentDescription = null,
                                tint = UtilitarianTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Priority",
                                color = UtilitarianTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = activity.priority.name,
                            color = UtilitarianTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

        // Card 3: EVENT NOTE
        MonochromeCard {
                Column {
                    Text(
                        text = "EVENT NOTE",
                        color = UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (activity.description.isNotBlank()) {
                        ClickableLinkText(
                            text = activity.description,
                            color = UtilitarianTextSecondary,
                            linkColor = Color(0xFF3897F0),
                            style = TextStyle(fontSize = 14.sp),
                            lineHeight = 20.sp
                        )
                    } else {
                        Text(
                            text = "Not set",
                            color = UtilitarianTextDisabled,
                            fontSize = 13.sp
                        )
                    }
                }
            }

        // Card 4: ATTACHMENTS
        MonochromeCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (aggregate.attachments.isNotEmpty()) {
                                "ATTACHMENTS (${aggregate.attachments.size})"
                            } else {
                                "ATTACHMENTS"
                            },
                            color = UtilitarianTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    filePickerLauncher.launch(AttachmentHelper.SUPPORTED_MIME_TYPES)
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add attachment",
                                tint = UtilitarianTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ATTACH",
                                color = UtilitarianTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (aggregate.attachments.isNotEmpty()) {
                        aggregate.attachments.forEachIndexed { index, att ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            Surface(
                                onClick = {
                                    AttachmentHelper.openAttachmentInDefaultApp(
                                        context = context,
                                        filePath = att.uriString,
                                        mimeType = att.mimeType
                                    ).onFailure { error ->
                                        Toast.makeText(
                                            context,
                                            error.message ?: "Cannot open file",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E1E20),
                                border = BorderStroke(1.dp, Color(0xFF2C2C2E))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AttachFile,
                                        contentDescription = null,
                                        tint = UtilitarianTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = att.fileName,
                                            color = UtilitarianTextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = AttachmentHelper.formatFileSize(att.sizeBytes),
                                            color = UtilitarianTextDisabled,
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteAttachment(att.id, att.uriString) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Remove attachment",
                                            tint = UtilitarianTextDisabled,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Surface(
                            onClick = { filePickerLauncher.launch(AttachmentHelper.SUPPORTED_MIME_TYPES) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF161616),
                            border = BorderStroke(1.dp, UtilitarianBorderStroke)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AttachFile,
                                    contentDescription = null,
                                    tint = UtilitarianTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "+ Attach File or Photo",
                                    color = UtilitarianTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
    }
}

private val WORKSPACE_AUDIO_TAG_REGEX = Regex("""!\[audio\]\(([^)]+)\)""")
private val WORKSPACE_PHOTO_TAG_REGEX = Regex("""!\[photo\]\(([^)]+)\)""")

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotesTabContent(
    notes: List<ActivityNote>,
    onCreateNoteClick: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Create Event Note Action Card
        MonochromeCard(
            onClick = onCreateNoteClick
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "EVENT WORKSPACE NOTES",
                        color = UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Take rich notes, checklists, or voice recordings during this event.",
                        color = UtilitarianTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                HighContrastButton(
                    text = "+ Create Note",
                    onClick = onCreateNoteClick
                )
            }
        }

        if (notes.isEmpty()) {
            MonochromeCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No event notes recorded yet.",
                        color = UtilitarianTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ Create Note' above to capture notes while this event is running.",
                        color = UtilitarianTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            notes.forEach { note ->
                WorkspaceNoteCard(
                    note = note,
                    onClick = { onNoteClick(note.id) },
                    onDelete = { onDeleteNote(note.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkspaceNoteCard(
    note: ActivityNote,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showNoteMenu by remember { mutableStateOf(false) }

    val hasTitle = note.title.isNotBlank() && !note.title.equals("Untitled Note", ignoreCase = true)
    val audioCount = remember(note.content) { WORKSPACE_AUDIO_TAG_REGEX.findAll(note.content).count() }
    val photoCount = remember(note.content) { WORKSPACE_PHOTO_TAG_REGEX.findAll(note.content).count() }

    val cleanPreview = remember(note.content) {
        val withoutMedia = note.content
            .replace(WORKSPACE_AUDIO_TAG_REGEX, "")
            .replace(WORKSPACE_PHOTO_TAG_REGEX, "")
            .trim()
        val lines = withoutMedia.lines().filter { it.isNotBlank() }
        val isChecklist = lines.isNotEmpty() && lines.all {
            it.startsWith("[ ] ") || it.startsWith("[x] ") || it.startsWith("[X] ")
        }
        val plain = if (isChecklist) {
            withoutMedia
        } else {
            NoteTextFormatter.parseMarkdownToState(withoutMedia).first
        }
        val plainLines = plain.lines()
        if (plainLines.size > 10) {
            val first9 = plainLines.take(9)
            val tenth = plainLines[9].trimEnd()
            val tenthWithDots = if (tenth.endsWith("...")) tenth else "$tenth..."
            (first9 + tenthWithDots).joinToString("\n")
        } else {
            plain
        }
    }

    val parsedChecklistItems = remember(note.content, cleanPreview) {
        val lines = cleanPreview.lines().map { it.trim() }.filter { it.isNotBlank() }
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
    val hasContent = cleanPreview.isNotBlank() && !hasChecklist
    val hasBadges = audioCount > 0 || photoCount > 0 || note.labels.isNotEmpty()
    val hasCustomColor = !isDefaultNoteColor(note.colorHex)
    val borderColor = if (hasCustomColor) {
        Color.White.copy(alpha = 0.15f)
    } else {
        UtilitarianBorderStroke
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (hasCustomColor) Color(note.colorHex) else UtilitarianCardBackground,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (hasTitle) {
                        Text(
                            text = note.title,
                            color = UtilitarianTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    } else if (parsedChecklistItems != null) {
                        Column(
                            modifier = Modifier.weight(1f),
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
                                        tint = if (isChecked) Color(0xFF3897F0) else if (hasCustomColor) Color.White.copy(alpha = 0.7f) else UtilitarianTextSecondary,
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
                            text = cleanPreview,
                            color = UtilitarianTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            maxLines = 10,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    Box(modifier = Modifier.padding(start = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showNoteMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Note Options",
                                tint = UtilitarianTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showNoteMenu,
                            onDismissRequest = { showNoteMenu = false },
                            shape = RoundedCornerShape(12.dp),
                            containerColor = Color(0xFF1E2023),
                            border = BorderStroke(1.dp, Color(0xFF33383F)),
                            modifier = Modifier
                                .width(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .clickable {
                                        showNoteMenu = false
                                        onClick()
                                    }
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = null,
                                    tint = UtilitarianTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Edit",
                                    color = UtilitarianTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            HorizontalDivider(
                                color = Color(0xFF2C3136),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .clickable {
                                        showNoteMenu = false
                                        onDelete()
                                    }
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFD71921),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delete",
                                    color = Color(0xFFD71921),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (hasTitle && (hasContent || hasChecklist)) {
                    Spacer(modifier = Modifier.height(8.dp))
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
                                        tint = if (isChecked) Color(0xFF3897F0) else if (hasCustomColor) Color.White.copy(alpha = 0.7f) else UtilitarianTextSecondary,
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
                            text = cleanPreview,
                            color = UtilitarianTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            maxLines = 10,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (hasBadges) {
                    if (hasTitle || hasContent) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (audioCount > 0) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(4.dp))
                                    .background(Color(0xFF161616), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (audioCount == 1) "1 AUDIO" else "$audioCount AUDIOS",
                                    color = UtilitarianTextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        if (photoCount > 0) {
                            Box(
                                modifier = Modifier
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(4.dp))
                                    .background(Color(0xFF161616), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (photoCount == 1) "1 PHOTO" else "$photoCount PHOTOS",
                                    color = UtilitarianTextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        note.labels.forEach { label ->
                            Box(
                                modifier = Modifier
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(4.dp))
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
private fun TasksTabContent(
    tasks: List<ActivityTask>,
    onAddTask: (String) -> Unit,
    onUpdateTask: (Long, String) -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    var taskInput by remember { mutableStateOf("") }
    var taskToEdit by remember { mutableStateOf<ActivityTask?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = taskInput,
                onValueChange = { taskInput = it },
                placeholder = { Text("New checklist or follow-up task...", color = UtilitarianTextDisabled, fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = UtilitarianCardBackground,
                    unfocusedContainerColor = UtilitarianCardBackground,
                    focusedTextColor = UtilitarianTextPrimary,
                    unfocusedTextColor = UtilitarianTextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            HighContrastButton(
                text = "+ Add",
                onClick = {
                    if (taskInput.isNotBlank()) {
                        onAddTask(taskInput)
                        taskInput = ""
                    }
                }
            )
        }

        if (tasks.isEmpty()) {
            Text(
                text = "No checklist items. Add follow-up assignments or items to remember.",
                color = UtilitarianTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            tasks.forEach { task ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = UtilitarianCardBackground,
                    border = BorderStroke(1.dp, UtilitarianBorderStroke)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { done -> onToggleTask(task.id, done) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = UtilitarianAccentRed,
                                uncheckedColor = UtilitarianBorderStroke,
                                checkmarkColor = UtilitarianTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.title,
                            color = if (task.isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                            fontSize = 14.sp,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { taskToEdit = task }) {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit", tint = UtilitarianTextDisabled, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onDeleteTask(task.id) }) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = UtilitarianTextDisabled, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (taskToEdit != null) {
        val currentTask = taskToEdit!!
        var editTitle by remember(currentTask) { mutableStateOf(currentTask.title) }

        Dialog(onDismissRequest = { taskToEdit = null }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = UtilitarianCardBackground,
                border = BorderStroke(1.dp, UtilitarianBorderStroke),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "EDIT TASK",
                        color = UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    TextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = UtilitarianCardBackground,
                            unfocusedContainerColor = UtilitarianCardBackground,
                            focusedTextColor = UtilitarianTextPrimary,
                            unfocusedTextColor = UtilitarianTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { taskToEdit = null }) {
                            Text("Cancel", color = UtilitarianTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        HighContrastButton(
                            text = "Save",
                            onClick = {
                                if (editTitle.isNotBlank()) {
                                    onUpdateTask(currentTask.id, editTitle)
                                    taskToEdit = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinksTabContent(
    links: List<ActivityLink>,
    onAddLink: (String, String) -> Unit,
    onUpdateLink: (Long, String, String) -> Unit,
    onDeleteLink: (Long) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
    var linkToEdit by remember { mutableStateOf<ActivityLink?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MonochromeCard {
            Column {
                Text(
                    text = "SAVE REFERENCE LINK / URL",
                    color = UtilitarianTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                TextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    placeholder = { Text("Link title (e.g. Lab Spec, Slide Deck)", color = UtilitarianTextDisabled, fontSize = 13.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = UtilitarianTextPrimary,
                        unfocusedTextColor = UtilitarianTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = { Text("https://...", color = UtilitarianTextDisabled, fontSize = 13.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = UtilitarianTextPrimary,
                        unfocusedTextColor = UtilitarianTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                HighContrastButton(
                    text = "+ Save Link",
                    onClick = {
                        if (urlInput.isNotBlank()) {
                            onAddLink(urlInput, titleInput)
                            urlInput = ""
                            titleInput = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        if (links.isEmpty()) {
            Text(
                text = "No resource links attached. Save URLs discussed in this session.",
                color = UtilitarianTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            links.forEach { link ->
                MonochromeCard(
                    onClick = {
                        val parsed = if (!link.url.startsWith("http://") && !link.url.startsWith("https://")) "https://${link.url}" else link.url
                        runCatching {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(parsed))
                            context.startActivity(browserIntent)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Outlined.Link, contentDescription = null, tint = UtilitarianTextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = link.title, color = UtilitarianTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = link.url, color = UtilitarianTextSecondary, fontSize = 11.sp)
                        }
                        Icon(imageVector = Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, tint = UtilitarianTextDisabled, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = { linkToEdit = link }) {
                            Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit", tint = UtilitarianTextDisabled, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onDeleteLink(link.id) }) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = UtilitarianTextDisabled, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (linkToEdit != null) {
        val currentLink = linkToEdit!!
        var editTitle by remember(currentLink) { mutableStateOf(currentLink.title) }
        var editUrl by remember(currentLink) { mutableStateOf(currentLink.url) }

        Dialog(onDismissRequest = { linkToEdit = null }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = UtilitarianCardBackground,
                border = BorderStroke(1.dp, UtilitarianBorderStroke),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "EDIT LINK",
                        color = UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    TextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        placeholder = { Text("Link title", color = UtilitarianTextDisabled, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = UtilitarianCardBackground,
                            unfocusedContainerColor = UtilitarianCardBackground,
                            focusedTextColor = UtilitarianTextPrimary,
                            unfocusedTextColor = UtilitarianTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = editUrl,
                        onValueChange = { editUrl = it },
                        placeholder = { Text("https://...", color = UtilitarianTextDisabled, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = UtilitarianCardBackground,
                            unfocusedContainerColor = UtilitarianCardBackground,
                            focusedTextColor = UtilitarianTextPrimary,
                            unfocusedTextColor = UtilitarianTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { linkToEdit = null }) {
                            Text("Cancel", color = UtilitarianTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        HighContrastButton(
                            text = "Save",
                            onClick = {
                                if (editUrl.isNotBlank()) {
                                    onUpdateLink(currentLink.id, editUrl, editTitle)
                                    linkToEdit = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
