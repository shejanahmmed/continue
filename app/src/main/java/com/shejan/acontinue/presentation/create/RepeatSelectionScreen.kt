package com.shejan.acontinue.presentation.create

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.domain.model.MonthlyRepeatType
import com.shejan.acontinue.domain.model.RecurrenceEndType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.YearlyRepeatType
import java.util.Calendar

// Theme colors matching the design reference screenshots exactly
private val ScreenBackground = Color(0xFF000000)
private val CardBackground = Color(0xFF1C1C1E)
private val CardDividerColor = Color(0xFF2C2C2E)
private val AccentBlue = Color(0xFF3897F0)
private val UnselectedCircleBorder = Color(0xFF5E5E62)
private val TextMuted = Color(0xFF8E8E93)
private val SaturdayColor = Color(0xFF3897F0)
private val SundayColor = Color(0xFFEF5350)

@Composable
fun RepeatSelectionScreen(
    state: CreateEntryState,
    onSelectRecurrence: (RecurrenceMode, Int) -> Unit,
    onUpdateInterval: (Int) -> Unit,
    onToggleRecurrenceDay: (Int) -> Unit,
    onUpdateMonthlyType: (MonthlyRepeatType) -> Unit,
    onToggleMonthlyDay: (Int) -> Unit,
    onUpdateYearlyType: (YearlyRepeatType) -> Unit,
    onToggleYearlyMonth: (Int) -> Unit,
    onUpdateEndType: (RecurrenceEndType) -> Unit,
    onUpdateEndCount: (Int) -> Unit,
    onUpdateEndDate: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val cal = remember(state.startMillis) {
        Calendar.getInstance().apply { timeInMillis = state.startMillis }
    }
    val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val dayOfWeekInMonth = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH)
    val monthIndex = cal.get(Calendar.MONTH)

    val subtitleText = remember(
        state.recurrenceMode,
        state.recurrenceInterval,
        state.recurrenceDaysOfWeek,
        state.monthlyRepeatType,
        state.monthlySelectedDays,
        state.yearlyRepeatType,
        state.yearlySelectedMonths,
        state.recurrenceEndType,
        state.recurrenceEndCount,
        state.recurrenceEndDateMillis
    ) {
        buildDynamicSubtitle(
            state = state,
            dayOfMonth = dayOfMonth,
            dayOfWeek = dayOfWeek,
            dayOfWeekInMonth = dayOfWeekInMonth,
            monthIndex = monthIndex
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 48.dp)
        ) {
            // Header: < Repeat
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Repeat",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Subtitle Description (Dynamic based on selected rule)
            Text(
                text = subtitleText,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Card 1: Recurrence Frequency Container (#1C1C1E, RoundedCornerShape 28dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(CardBackground)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // 1. Don't repeat
                    RepeatRow(
                        isSelected = state.recurrenceMode == RecurrenceMode.NONE,
                        onClick = { onSelectRecurrence(RecurrenceMode.NONE, 1) },
                        content = {
                            Text(
                                text = "Don't repeat",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    )

                    HorizontalDivider(
                        color = CardDividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 60.dp, end = 20.dp)
                    )

                    // 2. Every [1] day
                    val isDaily = state.recurrenceMode == RecurrenceMode.DAILY
                    RepeatRow(
                        isSelected = isDaily,
                        onClick = { onSelectRecurrence(RecurrenceMode.DAILY, state.recurrenceInterval) },
                        content = {
                            IntervalUnitRow(
                                interval = state.recurrenceInterval,
                                unitSingular = "day",
                                isSelected = isDaily,
                                onIntervalChange = onUpdateInterval
                            )
                        }
                    )

                    HorizontalDivider(
                        color = CardDividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 60.dp, end = 20.dp)
                    )

                    // 3. Every [1] week
                    val isWeekly = state.recurrenceMode == RecurrenceMode.WEEKLY
                    Column(modifier = Modifier.fillMaxWidth()) {
                        RepeatRow(
                            isSelected = isWeekly,
                            onClick = { onSelectRecurrence(RecurrenceMode.WEEKLY, state.recurrenceInterval) },
                            content = {
                                IntervalUnitRow(
                                    interval = state.recurrenceInterval,
                                    unitSingular = "week",
                                    isSelected = isWeekly,
                                    onIntervalChange = onUpdateInterval
                                )
                            }
                        )

                        // Weekday Multi-Selector Row: S S M T W T F
                        AnimatedVisibility(
                            visible = isWeekly,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            WeeklyDaySelectorRow(
                                selectedDays = state.recurrenceDaysOfWeek,
                                onToggleDay = onToggleRecurrenceDay,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 18.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = CardDividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 60.dp, end = 20.dp)
                    )

                    // 4. Every [1] month
                    val isMonthly = state.recurrenceMode == RecurrenceMode.MONTHLY
                    Column(modifier = Modifier.fillMaxWidth()) {
                        RepeatRow(
                            isSelected = isMonthly,
                            onClick = { onSelectRecurrence(RecurrenceMode.MONTHLY, state.recurrenceInterval) },
                            content = {
                                IntervalUnitRow(
                                    interval = state.recurrenceInterval,
                                    unitSingular = "month",
                                    isSelected = isMonthly,
                                    onIntervalChange = onUpdateInterval
                                )
                            }
                        )

                        // Monthly Sub-Options (3 Capsule Pills + 31-Day Grid)
                        AnimatedVisibility(
                            visible = isMonthly,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            MonthlyOptionsSection(
                                dayOfMonth = dayOfMonth,
                                dayOfWeekName = getDayOfWeekName(dayOfWeek),
                                nthOrdinal = getOrdinal(dayOfWeekInMonth),
                                activeType = state.monthlyRepeatType,
                                selectedDays = state.monthlySelectedDays,
                                onSelectType = onUpdateMonthlyType,
                                onToggleDay = onToggleMonthlyDay,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 18.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = CardDividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 60.dp, end = 20.dp)
                    )

                    // 5. Every [1] year
                    val isYearly = state.recurrenceMode == RecurrenceMode.YEARLY
                    Column(modifier = Modifier.fillMaxWidth()) {
                        RepeatRow(
                            isSelected = isYearly,
                            onClick = { onSelectRecurrence(RecurrenceMode.YEARLY, state.recurrenceInterval) },
                            content = {
                                IntervalUnitRow(
                                    interval = state.recurrenceInterval,
                                    unitSingular = "year",
                                    isSelected = isYearly,
                                    onIntervalChange = onUpdateInterval
                                )
                            }
                        )

                        // Yearly Sub-Options (3 Capsule Pills + 12-Month Grid)
                        AnimatedVisibility(
                            visible = isYearly,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            YearlyOptionsSection(
                                dayOfMonthOrdinal = getOrdinal(dayOfMonth),
                                monthShortName = getMonthShortName(monthIndex),
                                dayOfWeekName = getDayOfWeekName(dayOfWeek),
                                nthOrdinal = getOrdinal(dayOfWeekInMonth),
                                activeType = state.yearlyRepeatType,
                                selectedMonths = state.yearlySelectedMonths,
                                onSelectType = onUpdateYearlyType,
                                onToggleMonth = onToggleYearlyMonth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * Frequency row item with custom radio button and content
 */
@Composable
private fun RepeatRow(
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RepeatRadioButton(isSelected = isSelected)
        Spacer(modifier = Modifier.width(18.dp))
        content()
    }
}

/**
 * Pixel-perfect radio button: 22dp circle, 10dp inner dot when selected
 */
@Composable
private fun RepeatRadioButton(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        Box(
            modifier = modifier
                .size(22.dp)
                .border(2.dp, AccentBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AccentBlue)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(22.dp)
                .border(2.dp, UnselectedCircleBorder, CircleShape)
        )
    }
}

/**
 * "Every  [ 1 ]  day/week/month/year" with an underlined editable interval
 */
@Composable
private fun IntervalUnitRow(
    interval: Int,
    unitSingular: String,
    isSelected: Boolean,
    onIntervalChange: (Int) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var textValue by remember(interval) { mutableStateOf(interval.toString()) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Every ",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .width(42.dp)
                .height(28.dp)
                .drawBehind {
                    val strokeWidth = 1.5.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(
                        color = if (isSelected) AccentBlue else Color(0xFF8E8E93),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = textValue,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(3)
                    textValue = filtered
                    val parsed = filtered.toIntOrNull()
                    if (parsed != null && parsed > 0) {
                        onIntervalChange(parsed)
                    }
                },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(AccentBlue),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        val unitLabel = if (interval == 1) unitSingular else "${unitSingular}s"
        Text(
            text = unitLabel,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

/**
 * Weekly multi-day selector row: S  S  M  T  W  T  F
 */
@Composable
private fun WeeklyDaySelectorRow(
    selectedDays: Set<Int>,
    onToggleDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = remember {
        listOf(
            Triple(Calendar.SATURDAY, "S", SaturdayColor),
            Triple(Calendar.SUNDAY, "S", SundayColor),
            Triple(Calendar.MONDAY, "M", Color.White),
            Triple(Calendar.TUESDAY, "T", Color.White),
            Triple(Calendar.WEDNESDAY, "W", Color.White),
            Triple(Calendar.THURSDAY, "T", Color.White),
            Triple(Calendar.FRIDAY, "F", Color.White)
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { (calDay, letter, defaultColor) ->
            val isSelected = selectedDays.contains(calDay)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(1.5.dp, AccentBlue, CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onToggleDay(calDay) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = if (isSelected) AccentBlue else defaultColor,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Monthly Sub-Options Section: 3 Capsule Pills + 31-Day Circular Grid
 */
@Composable
private fun MonthlyOptionsSection(
    dayOfMonth: Int,
    dayOfWeekName: String,
    nthOrdinal: String,
    activeType: MonthlyRepeatType,
    selectedDays: Set<Int>,
    onSelectType: (MonthlyRepeatType) -> Unit,
    onToggleDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Pill 1: Repeat on the 14th
        CapsuleOptionPill(
            label = "Repeat on the ${getOrdinal(dayOfMonth)}",
            isActive = activeType == MonthlyRepeatType.SAME_DAY_OF_MONTH,
            onClick = { onSelectType(MonthlyRepeatType.SAME_DAY_OF_MONTH) }
        )

        // Pill 2: Repeat on the 2nd Wednesday
        CapsuleOptionPill(
            label = "Repeat on the $nthOrdinal $dayOfWeekName",
            isActive = activeType == MonthlyRepeatType.DAY_OF_WEEK_OF_MONTH,
            onClick = { onSelectType(MonthlyRepeatType.DAY_OF_WEEK_OF_MONTH) }
        )

        // Pill 3: Select dates to repeat
        CapsuleOptionPill(
            label = "Select dates to repeat",
            isActive = activeType == MonthlyRepeatType.SPECIFIC_DATES,
            onClick = { onSelectType(MonthlyRepeatType.SPECIFIC_DATES) }
        )

        // 31-Day Circular Button Grid (Visible when Pill 3 is active)
        AnimatedVisibility(
            visible = activeType == MonthlyRepeatType.SPECIFIC_DATES,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val totalDays = 31
                val rows = 5
                val cols = 7

                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0 until cols) {
                            val dayNum = r * cols + c + 1
                            if (dayNum <= totalDays) {
                                val isDaySelected = selectedDays.contains(dayNum)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isDaySelected) AccentBlue else Color(0xFF242426))
                                        .border(
                                            width = if (isDaySelected) 0.dp else 1.dp,
                                            color = if (isDaySelected) Color.Transparent else Color(0xFF3A3A3C),
                                            shape = CircleShape
                                        )
                                        .clickable { onToggleDay(dayNum) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNum",
                                        color = if (isDaySelected) Color.White else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (isDaySelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Yearly Sub-Options Section: 3 Capsule Pills + 12-Month Circular Grid
 */
@Composable
private fun YearlyOptionsSection(
    dayOfMonthOrdinal: String,
    monthShortName: String,
    dayOfWeekName: String,
    nthOrdinal: String,
    activeType: YearlyRepeatType,
    selectedMonths: Set<Int>,
    onSelectType: (YearlyRepeatType) -> Unit,
    onToggleMonth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Pill 1: Repeat on 14th Oct
        CapsuleOptionPill(
            label = "Repeat on $dayOfMonthOrdinal $monthShortName",
            isActive = activeType == YearlyRepeatType.SAME_DATE,
            onClick = { onSelectType(YearlyRepeatType.SAME_DATE) }
        )

        // Pill 2: Repeat on the 2nd Wednesday of Oct
        CapsuleOptionPill(
            label = "Repeat on the $nthOrdinal $dayOfWeekName of $monthShortName",
            isActive = activeType == YearlyRepeatType.DAY_OF_WEEK_OF_MONTH,
            onClick = { onSelectType(YearlyRepeatType.DAY_OF_WEEK_OF_MONTH) }
        )

        // Pill 3: Select months to repeat on the 14th day
        CapsuleOptionPill(
            label = "Select months to repeat on the $dayOfMonthOrdinal day",
            isActive = activeType == YearlyRepeatType.SPECIFIC_MONTHS,
            onClick = { onSelectType(YearlyRepeatType.SPECIFIC_MONTHS) }
        )

        // 12-Month Circular Button Grid (Visible when Pill 3 is active)
        AnimatedVisibility(
            visible = activeType == YearlyRepeatType.SPECIFIC_MONTHS,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val months = listOf(
                    listOf(Pair(0, "JAN"), Pair(1, "FEB"), Pair(2, "MAR"), Pair(3, "APR")),
                    listOf(Pair(4, "MAY"), Pair(5, "JUN"), Pair(6, "JUL"), Pair(7, "AUG")),
                    listOf(Pair(8, "SEPT"), Pair(9, "OCT"), Pair(10, "NOV"), Pair(11, "DEC"))
                )

                months.forEach { rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        rowMonths.forEach { (mIndex, mLabel) ->
                            val isMonthSelected = selectedMonths.contains(mIndex)
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (isMonthSelected) AccentBlue else Color(0xFF242426))
                                    .border(
                                        width = if (isMonthSelected) 0.dp else 1.dp,
                                        color = if (isMonthSelected) Color.Transparent else Color(0xFF3A3A3C),
                                        shape = CircleShape
                                    )
                                    .clickable { onToggleMonth(mIndex) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mLabel,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isMonthSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Capsule pill button (height 44dp, corner radius 22dp)
 */
@Composable
private fun CapsuleOptionPill(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) AccentBlue else Color(0xFF3A3A3C),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) AccentBlue else TextMuted,
            fontSize = 15.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Dynamic Subtitle Builder that resolves grammatically accurate sentence descriptions
 */
private fun buildDynamicSubtitle(
    state: CreateEntryState,
    dayOfMonth: Int,
    dayOfWeek: Int,
    dayOfWeekInMonth: Int,
    monthIndex: Int
): String {
    return when (state.recurrenceMode) {
        RecurrenceMode.NONE -> "This event doesn't repeat."
        RecurrenceMode.DAILY -> {
            if (state.recurrenceInterval == 1) {
                "This event will repeat every day."
            } else {
                "This event will repeat every ${state.recurrenceInterval} days."
            }
        }
        RecurrenceMode.WEEKLY -> {
            val daysList = state.recurrenceDaysOfWeek.sorted()
            val dayNames = if (daysList.isEmpty()) {
                getDayOfWeekName(dayOfWeek)
            } else {
                daysList.joinToString(", ") { getDayOfWeekName(it) }
            }
            if (state.recurrenceInterval == 1) {
                "This event will repeat every $dayNames."
            } else {
                "This event will repeat every ${state.recurrenceInterval} weeks on $dayNames."
            }
        }
        RecurrenceMode.WEEKDAYS -> "This event will repeat every weekday."
        RecurrenceMode.MONTHLY -> {
            val intervalClause = if (state.recurrenceInterval == 1) "every month" else "every ${state.recurrenceInterval} months"
            when (state.monthlyRepeatType) {
                MonthlyRepeatType.SAME_DAY_OF_MONTH -> {
                    "This event will repeat on the ${getOrdinal(dayOfMonth)} day of $intervalClause."
                }
                MonthlyRepeatType.DAY_OF_WEEK_OF_MONTH -> {
                    "This event will repeat on the ${getOrdinal(dayOfWeekInMonth)} ${getDayOfWeekName(dayOfWeek)} of $intervalClause."
                }
                MonthlyRepeatType.SPECIFIC_DATES -> {
                    val days = state.monthlySelectedDays.sorted()
                    val daysStr = if (days.isEmpty()) "${getOrdinal(dayOfMonth)} day" else days.joinToString(", ") { getOrdinal(it) } + " day"
                    "This event will repeat on the $daysStr of $intervalClause."
                }
            }
        }
        RecurrenceMode.YEARLY -> {
            val intervalClause = if (state.recurrenceInterval == 1) "every year" else "every ${state.recurrenceInterval} years"
            when (state.yearlyRepeatType) {
                YearlyRepeatType.SAME_DATE -> {
                    "This event will repeat $intervalClause on ${getOrdinal(dayOfMonth)} ${getMonthShortName(monthIndex)}."
                }
                YearlyRepeatType.DAY_OF_WEEK_OF_MONTH -> {
                    "This event will repeat on the ${getOrdinal(dayOfWeekInMonth)} ${getDayOfWeekName(dayOfWeek)} of ${getMonthShortName(monthIndex)} $intervalClause."
                }
                YearlyRepeatType.SPECIFIC_MONTHS -> {
                    val months = state.yearlySelectedMonths.sorted()
                    val monthsStr = if (months.isEmpty()) getMonthShortName(monthIndex) else months.joinToString(", ") { getMonthShortName(it) }
                    "This event will repeat on the ${getOrdinal(dayOfMonth)} day of $monthsStr $intervalClause."
                }
            }
        }
    }
}

private fun getOrdinal(n: Int): String {
    if (n in 11..13) return "${n}th"
    return when (n % 10) {
        1 -> "${n}st"
        2 -> "${n}nd"
        3 -> "${n}rd"
        else -> "${n}th"
    }
}

private fun getDayOfWeekName(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> "Day"
    }
}

private fun getMonthShortName(monthIndex: Int): String {
    return when (monthIndex) {
        0 -> "Jan"
        1 -> "Feb"
        2 -> "Mar"
        3 -> "Apr"
        4 -> "May"
        5 -> "Jun"
        6 -> "Jul"
        7 -> "Aug"
        8 -> "Sept"
        9 -> "Oct"
        10 -> "Nov"
        11 -> "Dec"
        else -> "Month"
    }
}
