package com.shejan.acontinue.presentation.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.shejan.acontinue.presentation.create.getCategoryIcon
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.core.designsystem.components.UtilitarianBadge
import com.shejan.acontinue.core.designsystem.components.UtilitarianDailyMetricCard
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.ActivityTask
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.presentation.calendar.UtilitarianEventDetailSheet
import com.shejan.acontinue.ui.theme.UtilitarianAccentRed
import com.shejan.acontinue.ui.theme.UtilitarianBorderActive
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianCardBackground
import com.shejan.acontinue.ui.theme.UtilitarianPureBlack
import com.shejan.acontinue.ui.theme.UtilitarianTextDisabled
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary
import com.shejan.acontinue.ui.theme.UtilitarianTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onActivityClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToCalendar: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedReminderForDetail by remember { mutableStateOf<ActivityItem?>(null) }
    var liveNow by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val dateHeader = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())

    val focalEvent = uiState.currentEvent ?: uiState.nextEvent
    val isEventNow = uiState.currentEvent != null
    val focalReminder = uiState.nextReminder

    LaunchedEffect(isEventNow, focalEvent?.id) {
        while (true) {
            liveNow = System.currentTimeMillis()
            delay(if (isEventNow) 1000L else 30_000L)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Top Header
        item {
            DotMatrixHeader(
                title = "HOME",
                leadingIcon = Icons.Outlined.Home,
                badgeText = dateHeader.uppercase(),
                badgeAtEnd = true
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Main Hero Metric Card: Compact dual-gauge overview
            UtilitarianDailyMetricCard(
                eventCount = uiState.todayEventCount,
                completedEventCount = uiState.todayCompletedEventCount,
                eventProgress = uiState.eventProgress,
                reminderCount = uiState.todayReminderCount,
                completedReminderCount = uiState.todayCompletedReminderCount,
                pendingReminderCount = uiState.todayPendingReminderCount,
                missedReminderCount = uiState.todayMissedReminderCount,
                reminderProgress = uiState.reminderProgress,
                onEventsClick = onNavigateToCalendar,
                onRemindersClick = onNavigateToCalendar
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Card 2: Current / Upcoming Event Focus
        if (focalEvent != null) {
            item {
                val headerText = if (isEventNow) "NOW" else "UPCOMING EVENT"
                val headerColor = if (isEventNow) UtilitarianAccentRed else UtilitarianTextSecondary
                val cardBorderColor = if (isEventNow) UtilitarianAccentRed else UtilitarianBorderActive

                Text(
                    text = headerText,
                    color = headerColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                MonochromeCard(
                    borderColor = cardBorderColor,
                    onClick = {
                        onActivityClick(focalEvent.id)
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(focalEvent.category.colorHex), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = focalEvent.title,
                                color = UtilitarianTextPrimary,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val timeSpan = if (focalEvent.isAllDay) {
                                "All day"
                            } else {
                                "${timeFormat.format(Date(focalEvent.startTimeMillis))} – ${timeFormat.format(Date(focalEvent.endTimeMillis))}"
                            }

                            Box(
                                modifier = Modifier.size(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = UtilitarianTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = timeSpan,
                                color = UtilitarianTextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (focalEvent.location.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = UtilitarianTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = focalEvent.location,
                                    color = UtilitarianTextSecondary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val timerText = if (isEventNow) {
                                val elapsed = formatElapsedStopwatch(focalEvent.startTimeMillis, liveNow)
                                val remaining = formatTimeRemaining(getEffectiveEnd(focalEvent), liveNow)
                                "$elapsed ($remaining)"
                            } else {
                                formatTimeRemaining(focalEvent.startTimeMillis, liveNow)
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isEventNow) UtilitarianAccentRed.copy(alpha = 0.12f) else Color(0xFF1E1E1E),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isEventNow) UtilitarianAccentRed.copy(alpha = 0.5f) else UtilitarianBorderStroke,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isEventNow) Icons.Outlined.Timer else Icons.Outlined.HourglassEmpty,
                                        contentDescription = null,
                                        tint = if (isEventNow) UtilitarianAccentRed else UtilitarianTextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = timerText,
                                        color = if (isEventNow) UtilitarianAccentRed else UtilitarianTextSecondary,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "OPEN WORKSPACE",
                                    color = if (isEventNow) UtilitarianAccentRed else UtilitarianTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = if (isEventNow) UtilitarianAccentRed else UtilitarianTextPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (focalReminder != null) 16.dp else 24.dp))
            }
        }

        // Card 3: Upcoming Reminder Focus
        if (focalReminder != null) {
            item {
                Text(
                    text = "UPCOMING REMINDER",
                    color = UtilitarianTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                MonochromeCard(
                    borderColor = UtilitarianBorderActive,
                    onClick = {
                        selectedReminderForDetail = focalReminder
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Row 1: Title with icon on left
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = UtilitarianTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = focalReminder.title,
                                color = UtilitarianTextPrimary,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Row 2: Time with icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val alertTime = timeFormat.format(Date(focalReminder.startTimeMillis))

                            Box(
                                modifier = Modifier.size(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = UtilitarianTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = alertTime,
                                color = UtilitarianTextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Row 3: Priority badge + Category badge in category color box
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isStrong = focalReminder.priority == ActivityPriority.HIGH
                            val priorityColor = if (isStrong) UtilitarianAccentRed else UtilitarianTextSecondary
                            val priorityBg = if (isStrong) UtilitarianAccentRed.copy(alpha = 0.15f) else Color(0xFF222226)
                            val priorityBorder = if (isStrong) UtilitarianAccentRed.copy(alpha = 0.5f) else Color(0xFF333338)

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = priorityBg,
                                border = BorderStroke(1.dp, priorityBorder)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Flag,
                                        contentDescription = null,
                                        tint = priorityColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = focalReminder.priority.reminderTitle.uppercase(Locale.getDefault()),
                                        color = priorityColor,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            val catBadgeText = focalReminder.reminderCategory.uppercase(Locale.getDefault())
                            val catBadgeColor = Color(focalReminder.reminderCategoryColorHex)
                            val catContentColor = if (catBadgeColor.luminance() > 0.55f) Color(0xFF141416) else Color.White

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = catBadgeColor,
                                border = BorderStroke(1.dp, catBadgeColor)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp)
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(focalReminder.reminderCategoryIcon),
                                        contentDescription = null,
                                        tint = catContentColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = catBadgeText,
                                        color = catContentColor,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Section 2: Timeline
        item {
            Text(
                text = "TODAY'S SCHEDULE",
                color = UtilitarianTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (uiState.timeline.activities.isEmpty()) {
            item {
                MonochromeCard {
                    Text(
                        text = "No activities scheduled for today. Tap '+' below to plan an activity or class.",
                        color = UtilitarianTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            items(uiState.timeline.activities, key = { it.id }) { activity ->
                val isReminder = activity.entryType == EntryType.REMINDER
                TimelineActivityItem(
                    activity = activity,
                    onActivityClick = {
                        if (isReminder) {
                            selectedReminderForDetail = activity
                        } else {
                            onActivityClick(activity.id)
                        }
                    },
                    onToggleCompletion = { isCompleted ->
                        viewModel.toggleActivityCompletion(activity.id, isCompleted)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Section 3: Pending Responsibilities
        if (uiState.pendingTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "PENDING ACTION ITEMS (${uiState.pendingTasks.size})",
                    color = UtilitarianTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(uiState.pendingTasks, key = { it.id }) { task ->
                PendingTaskRow(
                    task = task,
                    onToggle = { isChecked -> viewModel.toggleTask(task.id, isChecked) },
                    onClick = { task.activityId?.let(onActivityClick) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }

    if (selectedReminderForDetail != null) {
        UtilitarianEventDetailSheet(
            activity = selectedReminderForDetail!!,
            onDismiss = { selectedReminderForDetail = null },
            onDelete = { id ->
                viewModel.deleteActivity(id)
                selectedReminderForDetail = null
            },
            onToggleCompletion = { id, done ->
                viewModel.toggleActivityCompletion(id, done)
                selectedReminderForDetail = selectedReminderForDetail?.copy(isCompleted = done)
            }
        )
    }
}

@Composable
private fun TimelineActivityItem(
    activity: ActivityItem,
    onActivityClick: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit
) {
    val isReminder = activity.entryType == com.shejan.acontinue.domain.model.EntryType.REMINDER
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val startStr = timeFormat.format(Date(activity.startTimeMillis))
    val endStr = timeFormat.format(Date(activity.endTimeMillis))

    MonochromeCard(
        onClick = onActivityClick,
        borderColor = if (isReminder && activity.isCompleted) UtilitarianBorderStroke else UtilitarianBorderActive
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.width(76.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (activity.isAllDay) {
                    Text(
                        text = "All day",
                        color = if (isReminder && activity.isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (isReminder) {
                    Text(
                        text = startStr,
                        color = if (activity.isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = startStr,
                        color = UtilitarianTextPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = endStr,
                        color = UtilitarianTextDisabled,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }

            val catColor = if (isReminder) Color(activity.reminderCategoryColorHex) else Color(activity.category.colorHex)

            Spacer(modifier = Modifier.width(6.dp))

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(28.dp)
                    .background(catColor, RoundedCornerShape(1.5.dp))
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    color = if (isReminder && activity.isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (isReminder && activity.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (isReminder) {
                    Text(
                        text = activity.reminderCategory,
                        color = catColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (activity.location.isNotEmpty()) {
                    Text(
                        text = activity.location,
                        color = UtilitarianTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (isReminder) {
                Checkbox(
                    checked = activity.isCompleted,
                    onCheckedChange = onToggleCompletion,
                    colors = CheckboxDefaults.colors(
                        checkedColor = UtilitarianAccentRed,
                        uncheckedColor = UtilitarianBorderStroke,
                        checkmarkColor = UtilitarianTextPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun PendingTaskRow(
    task: ActivityTask,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = UtilitarianCardBackground,
        border = BorderStroke(1.dp, UtilitarianBorderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onToggle,
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
                fontSize = 13.sp,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f)
            )
            if (task.activityId != null) {
                UtilitarianBadge(
                    text = "WORKSPACE",
                    backgroundColor = UtilitarianBorderStroke
                )
            }
        }
    }
}

private fun getEffectiveEnd(event: ActivityItem): Long = if (event.isAllDay) {
    java.util.Calendar.getInstance().apply {
        timeInMillis = if (event.endTimeMillis > event.startTimeMillis) event.endTimeMillis else event.startTimeMillis
        set(java.util.Calendar.HOUR_OF_DAY, 23)
        set(java.util.Calendar.MINUTE, 59)
        set(java.util.Calendar.SECOND, 59)
        set(java.util.Calendar.MILLISECOND, 999)
    }.timeInMillis
} else if (event.endTimeMillis > event.startTimeMillis) {
    event.endTimeMillis
} else {
    event.startTimeMillis
}

private fun formatTimeRemaining(targetMillis: Long, now: Long): String {
    val diffMillis = targetMillis - now
    if (diffMillis <= 0L) return "<1m left"
    val totalSeconds = diffMillis / 1000L
    val totalMinutes = totalSeconds / 60L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L

    return when {
        hours > 0L && minutes > 0L -> "${hours}h ${minutes}m left"
        hours > 0L -> "${hours}h left"
        minutes > 0L -> "${minutes}m left"
        else -> "<1m left"
    }
}

private fun formatElapsedStopwatch(startMillis: Long, now: Long): String {
    val elapsedSeconds = ((now - startMillis) / 1000L).coerceAtLeast(0L)
    val hours = elapsedSeconds / 3600L
    val minutes = (elapsedSeconds % 3600L) / 60L
    val seconds = elapsedSeconds % 60L

    return if (hours > 0L) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d elapsed", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d elapsed", minutes, seconds)
    }
}

