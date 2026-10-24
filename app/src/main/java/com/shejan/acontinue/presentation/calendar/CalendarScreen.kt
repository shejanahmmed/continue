package com.shejan.acontinue.presentation.calendar

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
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.layout.navigationBarsPadding
import com.shejan.acontinue.domain.model.ActivityWorkspaceAggregate
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
import java.util.Calendar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.ui.theme.UtilitarianAccentRed
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
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onActivityClick: (Long) -> Unit,
    onCreateClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val monthShortFormat = SimpleDateFormat("MMM", Locale.getDefault())
    val monthShortTitle = monthShortFormat.format(Date(uiState.currentMonthMillis)).uppercase()

    val dayNumberFormat = SimpleDateFormat("dd", Locale.getDefault())
    val dayNumberStr = dayNumberFormat.format(Date(uiState.selectedDateMillis))

    val dayWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dayWeekStr = dayWeekFormat.format(Date(uiState.selectedDateMillis)).uppercase()

    val pillDateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    val pillDateStr = pillDateFormat.format(Date(uiState.selectedDateMillis))

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // Top Bar: Centered Month Title & Right Actions
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Center Month with Navigation
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (uiState.isWeekMode) viewModel.previousWeek() else viewModel.previousMonth()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronLeft,
                                contentDescription = if (uiState.isWeekMode) "Previous Week" else "Previous Month",
                                tint = UtilitarianTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = monthShortTitle,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp,
                            color = UtilitarianTextPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )

                        IconButton(
                            onClick = {
                                if (uiState.isWeekMode) viewModel.nextWeek() else viewModel.nextMonth()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = if (uiState.isWeekMode) "Next Week" else "Next Month",
                                tint = UtilitarianTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Right Action: Persistent Today Badge
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.5.dp, UtilitarianTextPrimary),
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(28.dp)
                            .clickable { viewModel.jumpToToday() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.todayDateNumber.toString(),
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = UtilitarianTextPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Day of Week Header Row
            item {
                DayOfWeekHeaderRow()
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Collapsible Month/Week Grid with Drag Gestures
            item {
                var dragX by remember { mutableFloatStateOf(0f) }
                var dragY by remember { mutableFloatStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(uiState.isWeekMode) {
                            detectDragGestures(
                                onDragStart = {
                                    dragX = 0f
                                    dragY = 0f
                                },
                                onDragEnd = {
                                    val absX = kotlin.math.abs(dragX)
                                    val absY = kotlin.math.abs(dragY)
                                    if (absY > absX && absY > 25.dp.toPx()) {
                                        if (dragY < 0) {
                                            viewModel.setWeekMode(true)
                                        } else {
                                            viewModel.setWeekMode(false)
                                        }
                                    } else if (absX > absY && absX > 30.dp.toPx()) {
                                        if (dragX < 0) {
                                            if (uiState.isWeekMode) viewModel.nextWeek() else viewModel.nextMonth()
                                        } else {
                                            if (uiState.isWeekMode) viewModel.previousWeek() else viewModel.previousMonth()
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    dragX += dragAmount.x
                                    dragY += dragAmount.y
                                    if (kotlin.math.abs(dragX) > 15.dp.toPx() || kotlin.math.abs(dragY) > 15.dp.toPx()) {
                                        change.consume()
                                    }
                                }
                            )
                        }
                ) {
                    AnimatedContent(
                        targetState = uiState.isWeekMode,
                        transitionSpec = {
                            if (targetState) {
                                (slideInVertically(animationSpec = tween(250)) { -it / 2 } + fadeIn(animationSpec = tween(200))) togetherWith
                                        (slideOutVertically(animationSpec = tween(250)) { -it / 2 } + fadeOut(animationSpec = tween(150)))
                            } else {
                                (slideInVertically(animationSpec = tween(250)) { -it / 2 } + fadeIn(animationSpec = tween(200))) togetherWith
                                        (slideOutVertically(animationSpec = tween(250)) { -it / 2 } + fadeOut(animationSpec = tween(150)))
                            }
                        },
                        label = "CalendarCollapsibleTransition"
                    ) { inWeekMode ->
                        if (inWeekMode) {
                            UtilitarianWeekRow(
                                days = uiState.currentWeekDays,
                                todayPulseTrigger = uiState.todayPulseTrigger,
                                onDayClick = { day -> viewModel.selectDate(day.dateMillis) }
                            )
                        } else {
                            UtilitarianMonthGrid(
                                days = uiState.monthDays,
                                todayPulseTrigger = uiState.todayPulseTrigger,
                                onDayClick = { day ->
                                    viewModel.selectDate(day.dateMillis)
                                    if (day.activities.isNotEmpty()) {
                                        viewModel.setWeekMode(true)
                                    }
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Selected Day Agenda Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dayNumberStr,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = UtilitarianTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dayWeekStr,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = UtilitarianTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val countBadge = if (uiState.selectedDayReminderCount > 0) {
                        "(${uiState.selectedDayCompletedReminderCount}/${uiState.selectedDayReminderCount})"
                    } else if (uiState.selectedDayTotalCount > 0) {
                        "(${uiState.selectedDayTotalCount})"
                    } else {
                        "(0)"
                    }
                    Text(
                        text = countBadge,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = UtilitarianTextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Selected Day Agenda Items or Screenshot-Matched Empty State
            if (uiState.selectedDayActivities.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 44.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SentimentSatisfiedAlt,
                            contentDescription = null,
                            tint = Color(0xFF6E7681),
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tap to add a sticker to this day.",
                            color = UtilitarianTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            } else {
                // 1. All-Day Cards (at the top)
                if (uiState.allDayActivities.isNotEmpty()) {
                    items(uiState.allDayActivities, key = { "allday_${it.id}" }) { activity ->
                        val isExpanded = uiState.expandedActivityId == activity.id
                        val workspace = if (isExpanded) uiState.expandedWorkspace else null
                        AllDayActivityCard(
                            activity = activity,
                            isExpanded = isExpanded,
                            workspace = workspace,
                            onToggleExpansion = { viewModel.toggleCardExpansion(activity.id) },
                            onToggleCompletion = { done -> viewModel.toggleActivityCompletion(activity.id, done) },
                            onDelete = { id -> viewModel.deleteActivity(id) },
                            onOpenWorkspace = { id -> onActivityClick(id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // 2. Timed Activities & Reminders Cards
                if (uiState.timedActivities.isNotEmpty()) {
                    items(uiState.timedActivities, key = { "timed_${it.id}" }) { activity ->
                        val isExpanded = uiState.expandedActivityId == activity.id
                        val workspace = if (isExpanded) uiState.expandedWorkspace else null
                        TimedAgendaActivityCard(
                            activity = activity,
                            isExpanded = isExpanded,
                            workspace = workspace,
                            onToggleExpansion = { viewModel.toggleCardExpansion(activity.id) },
                            onToggleCompletion = { done -> viewModel.toggleActivityCompletion(activity.id, done) },
                            onDelete = { id -> viewModel.deleteActivity(id) },
                            onOpenWorkspace = { id -> onActivityClick(id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Floating Quick Action Pill Button
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .clickable { onCreateClick(uiState.selectedDateMillis) },
            shape = CircleShape,
            color = Color(0xFF222222),
            border = BorderStroke(1.dp, UtilitarianBorderStroke)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Add on $pillDateStr",
                    color = UtilitarianTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add activity on date",
                    tint = UtilitarianTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Samsung Calendar style Event / Reminder Detail Bottom Sheet
        if (uiState.selectedActivityForDetail != null) {
            UtilitarianEventDetailSheet(
                activity = uiState.selectedActivityForDetail!!,
                onDismiss = { viewModel.dismissActivityDetail() },
                onDelete = { id -> viewModel.deleteActivity(id) },
                onToggleCompletion = { id, done -> viewModel.toggleActivityCompletion(id, done) },
                onToggleSkip = { id -> viewModel.skipReminder(id) },
                onOpenWorkspace = { id ->
                    viewModel.dismissActivityDetail()
                    onActivityClick(id)
                }
            )
        }
    }
}



@Composable
private fun DayOfWeekHeaderRow() {
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        dayLabels.forEachIndexed { index, label ->
            val headerColor = when (index) {
                0 -> Color(0xFF5C6BC0) // Sunday distinct accent
                6 -> Color(0xFF7E8CE0) // Saturday subtle accent
                else -> UtilitarianTextSecondary
            }
            Text(
                text = label,
                color = headerColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun UtilitarianMonthGrid(
    days: List<CalendarDay>,
    todayPulseTrigger: Long = 0L,
    onDayClick: (CalendarDay) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val rows = days.chunked(7)
        rows.forEach { weekDays ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weekDays.forEach { day ->
                    MonthDayCell(
                        day = day,
                        todayPulseTrigger = todayPulseTrigger,
                        onClick = { onDayClick(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun UtilitarianWeekRow(
    days: List<CalendarDay>,
    todayPulseTrigger: Long = 0L,
    onDayClick: (CalendarDay) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        days.forEach { day ->
            MonthDayCell(
                day = day,
                todayPulseTrigger = todayPulseTrigger,
                onClick = { onDayClick(day) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthDayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    todayPulseTrigger: Long = 0L
) {
    val cellBorderColor = when {
        day.isSelected -> Color(0xFF6E7681)
        day.isToday -> UtilitarianBorderStroke
        else -> Color(0xFF222222)
    }

    val cellBackgroundColor = when {
        day.isSelected -> Color(0xFF21262D)
        else -> Color(0xFF14171A)
    }

    Surface(
        modifier = modifier
            .height(54.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = cellBackgroundColor,
        border = BorderStroke(1.dp, cellBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 3.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Date Number: Current date ALWAYS displayed in a small white rounded box
            if (day.isToday) {
                val animProgress = remember { Animatable(0f) }

                LaunchedEffect(todayPulseTrigger) {
                    if (todayPulseTrigger > 0L) {
                        animProgress.snapTo(0f)
                        animProgress.animateTo(
                            targetValue = 0f,
                            animationSpec = keyframes {
                                durationMillis = 1000
                                0f at 0 using FastOutSlowInEasing
                                1f at 350 using FastOutSlowInEasing
                                0f at 1000 using FastOutSlowInEasing
                            }
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    modifier = Modifier
                        .padding(bottom = 2.dp)
                        .graphicsLayer {
                            val p = animProgress.value
                            transformOrigin = TransformOrigin(0.5f, 0f)
                            scaleX = 1f + 0.16f * p
                            scaleY = 1f + 0.22f * p
                        }
                ) {
                    Text(
                        text = day.dayOfMonth.toString(),
                        color = Color.Black,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                    )
                }
            } else {
                Text(
                    text = day.dayOfMonth.toString(),
                    color = if (day.isCurrentMonth) UtilitarianTextPrimary else UtilitarianTextDisabled,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = if (day.isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Multi-Color Dot Matrix Indicator
            DayDotMatrix(
                colorsHex = day.dotColorsHex,
                hasOverflow = day.hasMoreDots,
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

@Composable
private fun DayDotMatrix(
    colorsHex: List<Long>,
    hasOverflow: Boolean,
    modifier: Modifier = Modifier
) {
    if (colorsHex.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Line 1: up to 4 dots
        val line1Colors = colorsHex.take(4)
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            line1Colors.forEach { hex ->
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(color = Color(hex), shape = CircleShape)
                )
            }
        }

        // Line 2: up to 4 items (or 3 dots + 1 arrow if hasOverflow)
        if (colorsHex.size > 4 || hasOverflow) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasOverflow) {
                    val line2Colors = colorsHex.drop(4).take(3)
                    line2Colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(color = Color(hex), shape = CircleShape)
                        )
                    }
                    // 4th item: arrow indicating more
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = "More",
                        tint = UtilitarianTextSecondary,
                        modifier = Modifier.size(5.dp)
                    )
                } else {
                    val line2Colors = colorsHex.drop(4).take(4)
                    line2Colors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(color = Color(hex), shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AllDayActivityCard(
    activity: ActivityItem,
    isExpanded: Boolean,
    workspace: ActivityWorkspaceAggregate?,
    onToggleExpansion: () -> Unit,
    onToggleCompletion: ((Boolean) -> Unit)? = null,
    onDelete: (Long) -> Unit,
    onOpenWorkspace: (Long) -> Unit
) {
    val isReminder = activity.entryType == EntryType.REMINDER
    val accentColor = if (isReminder) Color(activity.reminderCategoryColorHex) else Color(activity.category.colorHex)
    val isCompleted = isReminder && activity.isCompleted
    val displayAccentColor = if (isCompleted) accentColor.copy(alpha = 0.4f) else accentColor
    val subTextColor = if (isCompleted) UtilitarianTextDisabled else UtilitarianTextSecondary

    MonochromeCard(
        onClick = onToggleExpansion,
        borderColor = if (isReminder && activity.isCompleted) UtilitarianBorderStroke else Color(0xFF2C2C2C),
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = displayAccentColor,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.title,
                        color = if (isReminder && activity.isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (isReminder && activity.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "All day",
                        color = subTextColor,
                        fontSize = 11.sp
                    )
                }

                if (isReminder) {
                    Checkbox(
                        checked = activity.isCompleted,
                        onCheckedChange = { done -> onToggleCompletion?.invoke(done) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = UtilitarianAccentRed,
                            uncheckedColor = UtilitarianBorderStroke,
                            checkmarkColor = UtilitarianTextPrimary
                        )
                    )
                }
            }

            if (isExpanded) {
                ExpandedCardContent(
                    activity = activity,
                    workspace = workspace,
                    onDelete = { onDelete(activity.id) },
                    onToggleCompletion = { done -> onToggleCompletion?.invoke(done) },
                    onOpenWorkspace = { onOpenWorkspace(activity.id) },
                    onCollapse = onToggleExpansion
                )
            }
        }
    }
}

@Composable
private fun TimedAgendaActivityCard(
    activity: ActivityItem,
    isExpanded: Boolean,
    workspace: ActivityWorkspaceAggregate?,
    onToggleExpansion: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    onOpenWorkspace: (Long) -> Unit
) {
    val isReminder = activity.entryType == EntryType.REMINDER
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val shortTimeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
    val startTimeReadout = if (activity.isAllDay) "ALL" else shortTimeFormat.format(Date(activity.startTimeMillis))
    val timeSpan = if (activity.isAllDay) {
        "All day"
    } else if (isReminder) {
        timeFormat.format(Date(activity.startTimeMillis))
    } else {
        "${timeFormat.format(Date(activity.startTimeMillis))} – ${timeFormat.format(Date(activity.endTimeMillis))}"
    }
    val accentColor = if (isReminder) Color(activity.reminderCategoryColorHex) else Color(activity.category.colorHex)
    val isCompleted = isReminder && activity.isCompleted
    val displayAccentColor = if (isCompleted) accentColor.copy(alpha = 0.4f) else accentColor
    val subTextColor = if (isCompleted) UtilitarianTextDisabled else UtilitarianTextSecondary

    MonochromeCard(
        onClick = onToggleExpansion,
        borderColor = if (isReminder && activity.isCompleted) UtilitarianBorderStroke else Color(0xFF2C2C2C),
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Start Time column (e.g., 10:00, 2:30)
                Text(
                    text = startTimeReadout,
                    color = if (isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.width(44.dp)
                )

                // Category Vertical Accent Stroke
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(30.dp)
                        .background(
                            color = displayAccentColor,
                            shape = RoundedCornerShape(1.5.dp)
                        )
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Activity Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.title,
                        color = if (isReminder && activity.isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (isReminder && activity.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    val hasLoc = activity.location.isNotBlank() && !activity.location.trim().equals("location", ignoreCase = true)
                    val subtitle = if (isReminder && hasLoc) {
                        "${activity.reminderCategory} • $timeSpan  ${activity.location}"
                    } else if (isReminder) {
                        "${activity.reminderCategory} • $timeSpan"
                    } else if (hasLoc) {
                        "$timeSpan  ${activity.location}"
                    } else {
                        timeSpan
                    }
                    Text(
                        text = subtitle,
                        color = subTextColor,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isReminder) {
                    Checkbox(
                        checked = activity.isCompleted,
                        onCheckedChange = { done -> onToggleCompletion(done) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = UtilitarianAccentRed,
                            uncheckedColor = UtilitarianBorderStroke,
                            checkmarkColor = UtilitarianTextPrimary
                        )
                    )
                }
            }

            if (isExpanded) {
                ExpandedCardContent(
                    activity = activity,
                    workspace = workspace,
                    onDelete = { onDelete(activity.id) },
                    onToggleCompletion = { done -> onToggleCompletion(done) },
                    onOpenWorkspace = { onOpenWorkspace(activity.id) },
                    onCollapse = onToggleExpansion
                )
            }
        }
    }
}

@Composable
private fun ExpandedCardContent(
    activity: ActivityItem,
    workspace: ActivityWorkspaceAggregate?,
    onDelete: () -> Unit,
    onToggleCompletion: ((Boolean) -> Unit)? = null,
    onOpenWorkspace: () -> Unit,
    onCollapse: () -> Unit
) {
    val fullDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val isReminder = activity.entryType == EntryType.REMINDER
    val dateStr = fullDateFormat.format(Date(activity.startTimeMillis))
    val timeStr = if (activity.isAllDay) {
        "All day"
    } else if (isReminder) {
        timeFormat.format(Date(activity.startTimeMillis))
    } else {
        "${timeFormat.format(Date(activity.startTimeMillis))} – ${timeFormat.format(Date(activity.endTimeMillis))}"
    }
    val repeatStr = if (activity.recurrenceMode != RecurrenceMode.NONE) formatRecurrenceRule(activity) else "None"
    val hasLocation = activity.location.isNotBlank() && !activity.location.trim().equals("location", ignoreCase = true)
    val locationStr = if (hasLocation) activity.location else "None"
    val noteStr = activity.description.ifBlank {
        workspace?.notes?.firstOrNull()?.content ?: ""
    }.ifBlank { "None" }
    val alertsStr = formatAlerts(activity) ?: "None"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        HorizontalDivider(color = Color(0xFF262626), thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // 1. When / Schedule
        ExpandedFieldRow(
            label = "WHEN",
            icon = Icons.Outlined.AccessTime,
            value = "$dateStr • $timeStr"
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 2. Repeat
        ExpandedFieldRow(
            label = "REPEAT",
            icon = Icons.Outlined.Repeat,
            value = repeatStr
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3. Location
        ExpandedFieldRow(
            label = "LOCATION",
            icon = Icons.Outlined.LocationOn,
            value = locationStr
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 4. Notes
        ExpandedFieldRow(
            label = "NOTE",
            icon = Icons.AutoMirrored.Outlined.Notes,
            value = noteStr
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 5. Attachments (Events only)
        if (!isReminder) {
            val attachments = workspace?.attachments ?: emptyList()
            if (attachments.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AttachFile,
                        contentDescription = null,
                        tint = UtilitarianTextSecondary,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ATTACHMENTS (${attachments.size})",
                            color = UtilitarianTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        attachments.forEach { att ->
                            Text(
                                text = "• ${att.fileName}",
                                color = UtilitarianTextPrimary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                ExpandedFieldRow(
                    label = "ATTACHMENTS",
                    icon = Icons.Outlined.AttachFile,
                    value = "None"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 6. Checklist / Tasks (Events only)
        if (!isReminder) {
            val tasks = workspace?.tasks ?: emptyList()
            if (tasks.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = UtilitarianTextSecondary,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val completedTasks = tasks.count { it.isCompleted }
                        Text(
                            text = "TASKS ($completedTasks/${tasks.size})",
                            color = UtilitarianTextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        tasks.forEach { task ->
                            Text(
                                text = if (task.isCompleted) "[✓] ${task.title}" else "[ ] ${task.title}",
                                color = if (task.isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                }
            } else {
                ExpandedFieldRow(
                    label = "TASKS",
                    icon = Icons.Outlined.Check,
                    value = "None"
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 7. Alerts
        ExpandedFieldRow(
            label = "ALERT",
            icon = Icons.Outlined.Notifications,
            value = alertsStr
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 8. Category (Reminders only) & Priority
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isReminder) Arrangement.SpaceBetween else Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isReminder) {
                Text(
                    text = "CATEGORY: ${activity.reminderCategory.uppercase(Locale.getDefault())}",
                    color = UtilitarianTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                text = "PRIORITY: ${activity.priority.name}",
                color = UtilitarianTextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = Color(0xFF262626), thickness = 0.8.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Action buttons row: DELETE, OPEN
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onDelete,
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E1414),
                border = BorderStroke(1.dp, Color(0xFF4A1A1A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = UtilitarianAccentRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DELETE",
                        color = UtilitarianAccentRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (isReminder) {
                Surface(
                    onClick = { onToggleCompletion?.invoke(!activity.isCompleted) },
                    shape = RoundedCornerShape(6.dp),
                    color = if (activity.isCompleted) Color(0xFF1A2E1A) else Color(0xFF222222),
                    border = BorderStroke(1.dp, if (activity.isCompleted) Color(0xFF2E5E2E) else UtilitarianBorderStroke)
                ) {
                    Text(
                        text = if (activity.isCompleted) "COMPLETED" else "MARK DONE",
                        color = if (activity.isCompleted) Color(0xFF4CAF50) else UtilitarianTextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            } else {
                Surface(
                    onClick = onOpenWorkspace,
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OPEN",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "Open Workspace",
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Centered collapse arrow
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = onCollapse,
                shape = CircleShape,
                color = Color(0xFF1C1C1C),
                border = BorderStroke(1.dp, UtilitarianBorderStroke),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowUp,
                        contentDescription = "Shrink card",
                        tint = UtilitarianTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpandedFieldRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UtilitarianTextSecondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = UtilitarianTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = value,
                color = if (value == "None") UtilitarianTextDisabled else UtilitarianTextPrimary,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UtilitarianEventDetailSheet(
    activity: ActivityItem,
    onDismiss: () -> Unit,
    onDelete: (Long) -> Unit,
    onToggleCompletion: (Long, Boolean) -> Unit,
    onToggleSkip: (Long) -> Unit = {},
    onOpenWorkspace: (Long) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isReminder = activity.entryType == EntryType.REMINDER
    val catColor = if (isReminder) Color(activity.reminderCategoryColorHex) else Color(activity.category.colorHex)
    val typeTitle = if (isReminder) "Reminder" else "Event"
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF202020),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .navigationBarsPadding()
        ) {
            // Drag Handle Bar on top center
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(4.dp)
                    .background(Color(0xFF444444), RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Title Row (Prominent headline, no strikethrough, solid white)
            UtilitarianDetailLine(
                icon = Icons.Outlined.Subtitles,
                label = "Title",
                value = activity.title,
                fontSize = 17.sp,
                iconSize = 20.dp,
                labelColor = UtilitarianTextPrimary,
                valueColor = UtilitarianTextPrimary,
                iconTint = UtilitarianTextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // 2. Time Row
            val fullDateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val dateStr = fullDateFormat.format(Date(activity.startTimeMillis))
            val timeStr = if (activity.isAllDay) {
                "All day, $dateStr"
            } else if (isReminder) {
                "${timeFormat.format(Date(activity.startTimeMillis))}, $dateStr"
            } else {
                "${timeFormat.format(Date(activity.startTimeMillis))} – ${timeFormat.format(Date(activity.endTimeMillis))}, $dateStr"
            }
            UtilitarianDetailLine(
                icon = Icons.Outlined.AccessTime,
                label = "Time",
                value = timeStr
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Type & Priority Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = UtilitarianTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Type : ",
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = typeTitle,
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = "  |  ",
                    color = Color(0xFF555555),
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    tint = UtilitarianTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Priority : ",
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    text = activity.priority.displayName,
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Early Alert Status Row
            val alertStr = formatAlerts(activity) ?: "Don't alert"
            UtilitarianDetailLine(
                icon = Icons.Outlined.Notifications,
                label = "Alert",
                value = alertStr
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Repeat Status Row
            val repeatStr = formatRecurrenceRule(activity)
            UtilitarianDetailLine(
                icon = Icons.Outlined.Repeat,
                label = "Repeat",
                value = repeatStr
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Category Row (Color dot moved to the right side of the category name)
            val catName = if (isReminder) activity.reminderCategory else activity.category.displayName
            UtilitarianDetailLine(
                icon = Icons.Outlined.Folder,
                label = "Category",
                value = catName,
                badgeDotColor = catColor,
                badgeDotOnRight = true
            )

            // Optional: Location if present
            val hasDetailLocation = activity.location.isNotBlank() && !activity.location.trim().equals("location", ignoreCase = true)
            if (hasDetailLocation) {
                Spacer(modifier = Modifier.height(14.dp))
                UtilitarianDetailLine(
                    icon = Icons.Outlined.LocationOn,
                    label = "Location",
                    value = activity.location
                )
            }

            // Optional: Notes if present
            if (activity.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                UtilitarianDetailLine(
                    icon = Icons.AutoMirrored.Outlined.Notes,
                    label = "Notes",
                    value = activity.description
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFF333333), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Solid Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DELETE (Solid red square box with white trash icon only)
                Surface(
                    onClick = {
                        showDeleteConfirmDialog = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFD71921),
                    shadowElevation = 0.dp,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isReminder) {
                        // SKIP (Solid button with white text)
                        if (!activity.isCompleted) {
                            Surface(
                                onClick = {
                                    onToggleSkip(activity.id)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (activity.isSkipped) Color(0xFF555555) else Color(0xFF383838),
                                shadowElevation = 0.dp
                            ) {
                                Text(
                                    text = if (activity.isSkipped) "SKIPPED" else "SKIP",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }

                        // MARK DONE / COMPLETED (Solid green button with white text)
                        Surface(
                            onClick = {
                                onToggleCompletion(activity.id, !activity.isCompleted)
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (activity.isCompleted) Color(0xFF1B5E20) else Color(0xFF2E7D32),
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = if (activity.isCompleted) "COMPLETED" else "MARK DONE",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    if (!isReminder) {
                        Surface(
                            onClick = { onOpenWorkspace(activity.id) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "OPEN",
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                    contentDescription = "Open Workspace",
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

    if (showDeleteConfirmDialog) {
        val entryTypeName = if (isReminder) "reminder" else "event"
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
                        text = "Delete $entryTypeName?",
                        color = UtilitarianTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Are you sure you want to delete this $entryTypeName? This action cannot be undone.",
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
                                .clickable {
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
                                .clickable {
                                    showDeleteConfirmDialog = false
                                    onDelete(activity.id)
                                    onDismiss()
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
private fun UtilitarianDetailLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isStrikethrough: Boolean = false,
    labelColor: Color = UtilitarianTextSecondary,
    valueColor: Color = UtilitarianTextSecondary,
    iconTint: Color = UtilitarianTextSecondary,
    badgeDotColor: Color? = null,
    badgeDotOnRight: Boolean = true,
    fontSize: TextUnit = 13.sp,
    iconSize: Dp = 18.dp,
    fontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .size(iconSize)
                .padding(top = if (fontSize > 14.sp) 2.dp else 1.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$label : ",
            color = labelColor,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
        if (badgeDotColor != null && !badgeDotOnRight) {
            Box(
                modifier = Modifier
                    .padding(top = if (fontSize > 14.sp) 6.dp else 4.dp)
                    .size(8.dp)
                    .background(badgeDotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = value,
            color = valueColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            textDecoration = if (isStrikethrough) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (badgeDotColor != null && badgeDotOnRight) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .padding(top = if (fontSize > 14.sp) 6.dp else 4.dp)
                    .size(8.dp)
                    .background(badgeDotColor, CircleShape)
            )
        }
    }
}

private fun formatRecurrenceRule(activity: ActivityItem): String =
    com.shejan.acontinue.core.util.EventFormatters.formatRecurrenceRule(activity)

private fun formatAlerts(activity: ActivityItem): String? =
    com.shejan.acontinue.core.util.EventFormatters.formatAlerts(activity)
