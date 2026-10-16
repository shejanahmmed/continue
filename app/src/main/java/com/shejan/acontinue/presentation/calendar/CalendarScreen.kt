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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SentimentSatisfiedAlt
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
                            onClick = { viewModel.previousMonth() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronLeft,
                                contentDescription = "Previous Month",
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
                            onClick = { viewModel.nextMonth() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = "Next Month",
                                tint = UtilitarianTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Right Actions: Search & Persistent Today Badge
                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { /* Search */ },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = UtilitarianTextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Today Jump Badge [16]
                        Surface(
                            shape = RoundedCornerShape(7.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.5.dp, UtilitarianTextPrimary),
                            modifier = Modifier
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
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Day of Week Header Row
            item {
                DayOfWeekHeaderRow()
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Full-Month 6x7 Grid
            item {
                UtilitarianMonthGrid(
                    days = uiState.monthDays,
                    onDayClick = { day -> viewModel.selectDate(day.dateMillis) }
                )
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
                    val countBadge = if (uiState.selectedDayTotalCount > 0) {
                        "(${uiState.selectedDayCompletedCount}/${uiState.selectedDayTotalCount})"
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
                items(uiState.selectedDayActivities, key = { it.id }) { activity ->
                    AgendaActivityRow(
                        activity = activity,
                        onClick = { onActivityClick(activity.id) },
                        onToggleCompletion = { done -> viewModel.toggleActivityCompletion(activity.id, done) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                        onClick = { onDayClick(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 2.dp)
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
private fun AgendaActivityRow(
    activity: ActivityItem,
    onClick: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val shortTimeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
    val startTimeReadout = shortTimeFormat.format(Date(activity.startTimeMillis))
    val timeSpan = "${timeFormat.format(Date(activity.startTimeMillis))} – ${timeFormat.format(Date(activity.endTimeMillis))}"

    MonochromeCard(
        onClick = onClick,
        borderColor = if (activity.isCompleted) UtilitarianBorderStroke else Color(0xFF2C2C2C)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Start Time column
            Text(
                text = startTimeReadout,
                color = UtilitarianTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.width(44.dp)
            )

            val isReminder = activity.entryType == com.shejan.acontinue.domain.model.EntryType.REMINDER
            val accentColor = if (isReminder) Color(activity.reminderCategoryColorHex) else Color(activity.category.colorHex)

            // Category Vertical Accent Stroke
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(30.dp)
                    .background(
                        color = accentColor,
                        shape = RoundedCornerShape(1.5.dp)
                    )
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Activity Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    color = if (activity.isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (activity.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(modifier = Modifier.height(1.dp))
                val subtitle = if (isReminder) {
                    "${activity.reminderCategory} • $timeSpan"
                } else if (activity.location.isNotBlank()) {
                    "$timeSpan  ${activity.location}"
                } else {
                    timeSpan
                }
                Text(
                    text = subtitle,
                    color = UtilitarianTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

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
