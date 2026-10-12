package com.shejan.acontinue.presentation.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.ui.theme.UtilitarianAccentRed
import com.shejan.acontinue.ui.theme.UtilitarianBorderActive
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianCardBackground
import com.shejan.acontinue.ui.theme.UtilitarianPureBlack
import com.shejan.acontinue.ui.theme.UtilitarianTextDisabled
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary
import com.shejan.acontinue.ui.theme.UtilitarianTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onActivityClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val dateTitleFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val monthTitle = dateTitleFormat.format(Date(uiState.selectedDateMillis)).uppercase()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        item {
            DotMatrixHeader(
                title = monthTitle,
                badgeText = "PLANNER"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Horizontal Day Selector Strip
        item {
            DateSelectorStrip(
                selectedDateMillis = uiState.selectedDateMillis,
                onSelectDate = { viewModel.selectDate(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Schedule on Selected Date
        val selectedDayFormat = SimpleDateFormat("EEEE, dd MMM", Locale.getDefault())
        val selectedDayStr = selectedDayFormat.format(Date(uiState.selectedDateMillis))
        item {
            Text(
                text = selectedDayStr.uppercase(),
                color = UtilitarianTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (uiState.activities.isEmpty()) {
            item {
                MonochromeCard {
                    Text(
                        text = "No activities planned for this day. Tap the center '+' button to schedule an activity or reminder.",
                        color = UtilitarianTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            items(uiState.activities, key = { it.id }) { activity ->
                CalendarActivityRow(
                    activity = activity,
                    onClick = { onActivityClick(activity.id) },
                    onToggleCompletion = { done -> viewModel.toggleActivityCompletion(activity.id, done) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DateSelectorStrip(
    selectedDateMillis: Long,
    onSelectDate: (Long) -> Unit
) {
    val days = (-3..3).map { offset ->
        Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            add(Calendar.DAY_OF_YEAR, offset)
        }.timeInMillis
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(days) { dayMillis ->
            val isSelected = isSameDay(dayMillis, selectedDateMillis)
            val dayOfWeek = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(dayMillis)).uppercase()
            val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault()).format(Date(dayMillis))

            Surface(
                modifier = Modifier
                    .width(52.dp)
                    .clickable { onSelectDate(dayMillis) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) UtilitarianCardBackground else UtilitarianPureBlack,
                border = BorderStroke(1.dp, if (isSelected) UtilitarianAccentRed else UtilitarianBorderStroke)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayOfWeek,
                        color = if (isSelected) UtilitarianTextPrimary else UtilitarianTextDisabled,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayOfMonth,
                        color = if (isSelected) UtilitarianTextPrimary else UtilitarianTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

private fun isSameDay(t1: Long, t2: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

@Composable
private fun CalendarActivityRow(
    activity: ActivityItem,
    onClick: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeSpan = "${timeFormat.format(Date(activity.startTimeMillis))} – ${timeFormat.format(Date(activity.endTimeMillis))}"

    MonochromeCard(
        onClick = onClick,
        borderColor = if (activity.isCompleted) UtilitarianBorderStroke else UtilitarianBorderActive
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeSpan,
                    color = UtilitarianTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = activity.title,
                    color = if (activity.isCompleted) UtilitarianTextDisabled else UtilitarianTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (activity.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (activity.location.isNotEmpty()) {
                    Text(
                        text = activity.location,
                        color = UtilitarianTextSecondary,
                        fontSize = 11.sp
                    )
                }
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
