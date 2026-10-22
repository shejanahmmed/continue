package com.shejan.acontinue.presentation.workspace

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Subtitles
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.HighContrastButton
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.core.designsystem.components.UtilitarianBadge
import com.shejan.acontinue.core.util.ClickableLinkText
import com.shejan.acontinue.core.util.LocationHelper
import com.shejan.acontinue.domain.model.ActivityLink
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.model.ActivityWorkspaceAggregate
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is WorkspaceEvent.ActivityDeleted -> onNavigateBack()
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UtilitarianPureBlack)
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
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
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Outlined.Dashboard,
                    contentDescription = null,
                    tint = UtilitarianTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
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

                Box {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Options",
                            tint = UtilitarianTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        offset = DpOffset(x = (-100).dp, y = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = Color(0xFF1E2023),
                        border = BorderStroke(1.dp, Color(0xFF33383F)),
                        modifier = Modifier
                            .width(140.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showMenu = false
                                    onEditActivity(activity.id)
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
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showMenu = false
                                    showDeleteConfirmDialog = true
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
        }

        // Header Title Card with Subtitles Icon and First-Line Alignment
        item {
            MonochromeCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Subtitles,
                        contentDescription = null,
                        tint = UtilitarianTextPrimaryMuted,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = activity.title,
                        color = UtilitarianTextPrimaryMuted,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f)
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
                WorkspaceTab.OVERVIEW -> OverviewTabContent(aggregate = aggregate)
                WorkspaceTab.NOTES -> NotesTabContent(
                    notes = aggregate.notes,
                    onAddNote = { title, content -> viewModel.addNote(title, content) },
                    onDeleteNote = { viewModel.deleteNote(it) }
                )
                WorkspaceTab.TASKS -> TasksTabContent(
                    tasks = aggregate.tasks,
                    onAddTask = { viewModel.addTask(it) },
                    onToggleTask = { id, done -> viewModel.toggleTask(id, done) },
                    onDeleteTask = { viewModel.deleteTask(it) }
                )
                WorkspaceTab.LINKS -> LinksTabContent(
                    links = aggregate.links,
                    onAddLink = { url, title -> viewModel.addLink(url, title) },
                    onDeleteLink = { viewModel.deleteLink(it) }
                )
            }
        }
    }

    if (showDeleteConfirmDialog) {
        Dialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF202020),
                border = BorderStroke(1.dp, Color(0xFF333333)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Trash Icon in Red Circle Badge
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFD71921).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = Color(0xFFD71921),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Delete event?",
                        color = UtilitarianTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Are you sure you want to delete this event? This action cannot be undone.",
                        color = UtilitarianTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

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
private fun OverviewTabContent(aggregate: ActivityWorkspaceAggregate) {
    val activity = aggregate.activity

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
                    Spacer(modifier = Modifier.height(8.dp))
                    if (aggregate.attachments.isNotEmpty()) {
                        val context = LocalContext.current
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
                                        android.widget.Toast.makeText(
                                            context,
                                            error.message ?: "Cannot open file",
                                            android.widget.Toast.LENGTH_SHORT
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
                                            maxLines = 1
                                        )
                                        Text(
                                            text = AttachmentHelper.formatFileSize(att.sizeBytes),
                                            color = UtilitarianTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No attachments",
                            color = UtilitarianTextDisabled,
                            fontSize = 13.sp
                        )
                    }
                }
            }
    }
}

@Composable
private fun NotesTabContent(
    notes: List<ActivityNote>,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Add Note Box
        MonochromeCard {
            Column {
                Text(
                    text = "ADD LECTURE / WORKSPACE NOTE",
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
                    placeholder = { Text("Note title (optional)", color = UtilitarianTextDisabled, fontSize = 13.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = UtilitarianTextPrimary,
                        unfocusedTextColor = UtilitarianTextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = contentInput,
                    onValueChange = { contentInput = it },
                    placeholder = { Text("Write notes, observations, or key takeaways...", color = UtilitarianTextDisabled, fontSize = 13.sp) },
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
                    text = "+ Save Note",
                    onClick = {
                        if (contentInput.isNotBlank() || titleInput.isNotBlank()) {
                            onAddNote(titleInput, contentInput)
                            titleInput = ""
                            contentInput = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        if (notes.isEmpty()) {
            Text(
                text = "No notes added yet. Use the box above to capture thoughts during this activity.",
                color = UtilitarianTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            notes.forEach { note ->
                MonochromeCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = note.title, color = UtilitarianTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            ClickableLinkText(
                                text = note.content,
                                color = UtilitarianTextSecondary,
                                linkColor = Color(0xFF3897F0),
                                style = TextStyle(fontSize = 13.sp),
                                lineHeight = 18.sp
                            )
                        }
                        IconButton(onClick = { /* Reserved for 3-dot menu options */ }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Note Options",
                                tint = UtilitarianTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
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
    onToggleTask: (Long, Boolean) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    var taskInput by remember { mutableStateOf("") }

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
                        IconButton(onClick = { onDeleteTask(task.id) }) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = UtilitarianTextDisabled, modifier = Modifier.size(16.dp))
                        }
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
    onDeleteLink: (Long) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }
    var titleInput by remember { mutableStateOf("") }
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
                        IconButton(onClick = { onDeleteLink(link.id) }) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = UtilitarianTextDisabled, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
