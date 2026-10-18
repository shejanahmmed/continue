package com.shejan.acontinue.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.core.designsystem.components.UtilitarianBadge
import com.shejan.acontinue.core.designsystem.components.UtilitarianDailyMetricCard
import com.shejan.acontinue.domain.model.ActivityItem
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

    val dateHeader = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())

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
                reminderProgress = uiState.reminderProgress,
                onEventsClick = onNavigateToCalendar,
                onRemindersClick = onNavigateToCalendar
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 1: Next Up or Current Focus
        val focalActivity = uiState.timeline.currentActivity ?: uiState.timeline.nextActivity
        if (focalActivity != null) {
            item {
                val isReminder = focalActivity.entryType == com.shejan.acontinue.domain.model.EntryType.REMINDER
                val headerText = if (uiState.timeline.currentActivity != null) {
                    if (isReminder) "REMINDER" else "CURRENT EVENT"
                } else {
                    if (isReminder) "UPCOMING REMINDER" else "UP NEXT"
                }
                Text(
                    text = headerText,
                    color = UtilitarianTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                MonochromeCard(
                    borderColor = if (uiState.timeline.currentActivity != null) UtilitarianAccentRed else UtilitarianBorderActive,
                    onClick = {
                        if (isReminder) {
                            selectedReminderForDetail = focalActivity
                        } else {
                            onActivityClick(focalActivity.id)
                        }
                    }
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isReminder) {
                                val catBadgeText = focalActivity.reminderCategory.uppercase()
                                val catBadgeColor = Color(focalActivity.reminderCategoryColorHex)
                                UtilitarianBadge(
                                    text = catBadgeText,
                                    backgroundColor = catBadgeColor.copy(alpha = 0.2f),
                                    textColor = UtilitarianTextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                            val timeSpan = if (focalActivity.isAllDay) {
                                "All day"
                            } else if (isReminder) {
                                timeFormat.format(Date(focalActivity.startTimeMillis))
                            } else {
                                "${timeFormat.format(Date(focalActivity.startTimeMillis))} – ${timeFormat.format(Date(focalActivity.endTimeMillis))}"
                            }
                            Text(
                                text = timeSpan,
                                color = UtilitarianTextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = focalActivity.title,
                            color = UtilitarianTextPrimary,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        if (focalActivity.location.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = UtilitarianTextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = focalActivity.location,
                                    color = UtilitarianTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isReminder) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "OPEN WORKSPACE",
                                    color = if (uiState.timeline.currentActivity != null) UtilitarianAccentRed else UtilitarianTextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = null,
                                    tint = if (uiState.timeline.currentActivity != null) UtilitarianAccentRed else UtilitarianTextPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "VIEW REMINDER",
                                    color = UtilitarianTextSecondary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
