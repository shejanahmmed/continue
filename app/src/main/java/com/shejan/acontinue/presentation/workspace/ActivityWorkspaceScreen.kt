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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import com.shejan.acontinue.core.util.AttachmentHelper
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.shejan.acontinue.ui.theme.UtilitarianTextSecondary
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ActivityWorkspaceScreen(
    viewModel: ActivityWorkspaceViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

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

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
    val timeSpan = if (activity.isAllDay) {
        "${dateFormat.format(Date(activity.startTimeMillis))} • All day"
    } else {
        "${dateFormat.format(Date(activity.startTimeMillis))} • ${timeFormat.format(Date(activity.startTimeMillis))} – ${timeFormat.format(Date(activity.endTimeMillis))}"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UtilitarianPureBlack)
            .padding(horizontal = 16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = UtilitarianTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                UtilitarianBadge(
                    text = "WORKSPACE",
                    backgroundColor = UtilitarianBorderStroke
                )
            }

            IconButton(onClick = { viewModel.deleteActivity() }) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    tint = UtilitarianTextDisabled
                )
            }
        }

        // Header Title & Metadata
        Text(
            text = activity.title,
            color = UtilitarianTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        val accentColor = Color(activity.category.colorHex)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeSpan,
                color = UtilitarianTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }

        if (activity.location.isNotEmpty()) {
            val context = LocalContext.current
            val isLink = LocationHelper.isMapLocationLink(activity.location)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { LocationHelper.openLocationInMapApp(context, activity.location) }
                    .padding(vertical = 2.dp)
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Location",
                    tint = if (isLink) Color(0xFF3897F0) else UtilitarianTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = activity.location,
                    color = if (isLink) Color(0xFF3897F0) else UtilitarianTextSecondary,
                    fontSize = 12.sp
                )
                if (isLink) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = "Open in Map",
                        tint = Color(0xFF3897F0),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Workspace Tabs
        WorkspaceTabRow(
            selectedTab = uiState.selectedTab,
            onSelectTab = { viewModel.selectTab(it) },
            notesCount = aggregate.notes.size,
            tasksCount = aggregate.tasks.size,
            linksCount = aggregate.links.size
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
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
                border = BorderStroke(1.dp, if (isSelected) UtilitarianTextPrimary else UtilitarianBorderStroke)
            ) {
                Text(
                    text = label,
                    color = if (isSelected) UtilitarianTextPrimary else UtilitarianTextDisabled,
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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            MonochromeCard {
                Column {
                    Text(
                        text = "OBJECTIVE & DETAILS",
                        color = UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (activity.description.isNotBlank()) activity.description else "No description specified for this workspace.",
                        color = UtilitarianTextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            MonochromeCard {
                Column {
                    Text(
                        text = "WORKSPACE SUMMARY",
                        color = UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "• Notes recorded: ${aggregate.notes.size}", color = UtilitarianTextPrimary, fontSize = 13.sp)
                    Text(text = "• Checklist tasks: ${aggregate.tasks.size} (${aggregate.tasks.count { it.isCompleted }} done)", color = UtilitarianTextPrimary, fontSize = 13.sp)
                    Text(text = "• Resource links: ${aggregate.links.size}", color = UtilitarianTextPrimary, fontSize = 13.sp)
                    Text(text = "• Attachments: ${aggregate.attachments.size}", color = UtilitarianTextPrimary, fontSize = 13.sp)
                    Text(text = "• Reminder: ${if (activity.reminderMinutesBefore >= 0) "${activity.reminderMinutesBefore}m prior" else "None"}", color = UtilitarianTextPrimary, fontSize = 13.sp)
                    Text(text = "• Priority: ${activity.priority.name}", color = UtilitarianTextPrimary, fontSize = 13.sp)
                }
            }
        }

        if (aggregate.attachments.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                MonochromeCard {
                    Column {
                        Text(
                            text = "ATTACHMENTS (${aggregate.attachments.size})",
                            color = UtilitarianTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        val context = LocalContext.current
                        aggregate.attachments.forEach { att ->
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
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
                                        tint = UtilitarianTextPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = att.fileName,
                                            color = UtilitarianTextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = AttachmentHelper.formatFileSize(att.sizeBytes),
                                            color = UtilitarianTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                        contentDescription = "Open",
                                        tint = Color(0xFF3897F0),
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

@Composable
private fun NotesTabContent(
    notes: List<ActivityNote>,
    onAddNote: (String, String) -> Unit,
    onDeleteNote: (Long) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Quick Add Note Box
        item {
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
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (notes.isEmpty()) {
            item {
                Text(
                    text = "No notes added yet. Use the box above to capture thoughts during this activity.",
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(notes, key = { it.id }) { note ->
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
                        IconButton(onClick = { onDeleteNote(note.id) }) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = UtilitarianTextDisabled, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (tasks.isEmpty()) {
            item {
                Text(
                    text = "No checklist items. Add follow-up assignments or items to remember.",
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
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

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
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
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (links.isEmpty()) {
            item {
                Text(
                    text = "No resource links attached. Save URLs discussed in this session.",
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            items(links, key = { it.id }) { link ->
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
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
