package com.shejan.acontinue.presentation.diary

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.core.designsystem.components.UtilitarianBadge
import com.shejan.acontinue.core.util.ClickableLinkText
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianCardBackground
import com.shejan.acontinue.ui.theme.UtilitarianTextDisabled
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary
import com.shejan.acontinue.ui.theme.UtilitarianTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    onActivityClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        item {
            DotMatrixHeader(
                title = "DIARY",
                leadingIcon = Icons.AutoMirrored.Outlined.MenuBook,
                badgeText = "${uiState.activities.size} ENTRIES"
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Search Bar
        item {
            TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = {
                    Text(
                        text = "Search notes, classes, meetings...",
                        color = UtilitarianTextDisabled,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = UtilitarianTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = UtilitarianCardBackground,
                    unfocusedContainerColor = UtilitarianCardBackground,
                    focusedTextColor = UtilitarianTextPrimary,
                    unfocusedTextColor = UtilitarianTextPrimary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }


        // Section: Activity Records
        item {
            Text(
                text = "WORKSPACES & ACTIVITIES",
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
                        text = "No records found.",
                        color = UtilitarianTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(uiState.activities, key = { it.id }) { activity ->
                DiaryActivityRow(
                    activity = activity,
                    onClick = { onActivityClick(activity.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Section: Captured Notes
        if (uiState.notes.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CAPTURED NOTES & OBSERVATIONS (${uiState.notes.size})",
                    color = UtilitarianTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(uiState.notes, key = { it.id }) { note ->
                MonochromeCard(
                    onClick = { onActivityClick(note.activityId) }
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = note.title,
                                color = UtilitarianTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            UtilitarianBadge(
                                text = "OPEN",
                                backgroundColor = UtilitarianBorderStroke
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        ClickableLinkText(
                            text = note.content,
                            color = UtilitarianTextSecondary,
                            linkColor = Color(0xFF3897F0),
                            style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}


@Composable
private fun DiaryActivityRow(
    activity: ActivityItem,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val dateStr = dateFormat.format(Date(activity.startTimeMillis))

    MonochromeCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateStr,
                    color = UtilitarianTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
                Text(
                    text = activity.title,
                    color = UtilitarianTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (activity.location.isNotEmpty()) {
                    Text(
                        text = activity.location,
                        color = UtilitarianTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            val isReminder = activity.entryType == com.shejan.acontinue.domain.model.EntryType.REMINDER
            val badgeText = if (isReminder) activity.reminderCategory.uppercase() else "EVENT"
            val badgeColor = if (isReminder) Color(activity.reminderCategoryColorHex) else Color(activity.category.colorHex)
            UtilitarianBadge(
                text = badgeText,
                backgroundColor = badgeColor.copy(alpha = 0.2f),
                textColor = UtilitarianTextPrimary
            )
        }
    }
}
