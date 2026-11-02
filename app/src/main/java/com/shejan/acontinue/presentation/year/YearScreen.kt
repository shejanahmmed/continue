package com.shejan.acontinue.presentation.year

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.ui.theme.UtilitarianPureBlack

@Composable
fun YearScreen(
    viewModel: YearViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToSearch: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UtilitarianPureBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
        ) {
            // Top Bar: Hamburger Menu, Dynamic Year, Search Action, and Today Day Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hamburger icon (navigates back to Menu)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateBack()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Dynamic Year Title (e.g. "2026")
                Text(
                    text = "${uiState.year}",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // Search Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToSearch?.invoke()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Today Date Badge (e.g. [ 2 ])
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.5.dp, Color.White, RoundedCornerShape(6.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${uiState.todayDayOfMonth}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 12-Month Matrix: 4 rows of 3 columns
            val monthRows = uiState.months.chunked(3)
            monthRows.forEachIndexed { rowIndex, rowMonths ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowMonths.forEach { monthData ->
                        MonthCalendarCard(
                            monthData = monthData,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (rowIndex < monthRows.size - 1) {
                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MonthCalendarCard(
    monthData: YearMonthData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Month Title (Current month in a white filled badge; other months centered in plain white)
        Box(
            modifier = Modifier.height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (monthData.isCurrentMonth) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = monthData.monthName,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            } else {
                Text(
                    text = monthData.monthName,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Weekday Header Row (Saturday is Col 0 [Blue], Sunday is Col 1 [Red], weekdays [White])
        val weekdayHeaders = listOf(
            "S" to Color(0xFF388AE6), // Saturday Blue
            "S" to Color(0xFFE54D42), // Sunday Red
            "M" to Color(0xFFDDDDDD),
            "T" to Color(0xFFDDDDDD),
            "W" to Color(0xFFDDDDDD),
            "T" to Color(0xFFDDDDDD),
            "F" to Color(0xFFDDDDDD)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
        ) {
            weekdayHeaders.forEach { (label, color) ->
                Text(
                    text = label,
                    color = color,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Day Cell Matrix
        val allSlots: List<YearDayItem?> = List(monthData.leadingEmptySlots) { null } + monthData.days
        val weekRows = allSlots.chunked(7)

        weekRows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.5.dp)
            ) {
                for (col in 0 until 7) {
                    val dayItem = week.getOrNull(col)
                    if (dayItem != null) {
                        YearDayCell(
                            day = dayItem,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(1.5.dp))
        }
    }
}

@Composable
private fun YearDayCell(
    day: YearDayItem,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        day.isToday -> Color.White
        day.categoryColorHex != null -> Color(day.categoryColorHex).copy(alpha = 0.5f)
        else -> Color(day.fallbackColorHex)
    }

    val textColor = if (day.isToday) Color.Black else Color.White

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(3.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${day.dayOfMonth}",
            color = textColor,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
            fontSize = 7.5.sp,
            textAlign = TextAlign.Center
        )
    }
}

