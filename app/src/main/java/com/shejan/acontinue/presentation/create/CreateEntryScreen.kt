package com.shejan.acontinue.presentation.create

import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.ReminderOffset
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
fun CreateEntryScreen(
    viewModel: CreateEntryViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CreateEntryEvent.SavedSuccessfully -> onDismiss()
                is CreateEntryEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UtilitarianPureBlack)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 16.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Segmented Pill Toggle: Event / Activity vs Reminder
            SegmentedTopToggle(
                selectedType = state.entryType,
                onSelectType = { viewModel.updateEntryType(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Card Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = UtilitarianCardBackground,
                border = BorderStroke(1.dp, UtilitarianBorderStroke)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Title Input & Category Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = state.title,
                            onValueChange = { viewModel.updateTitle(it) },
                            placeholder = {
                                Text(
                                    text = "Title",
                                    color = UtilitarianTextDisabled,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = UtilitarianTextPrimary,
                                unfocusedTextColor = UtilitarianTextPrimary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = UtilitarianAccentRed
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // Category Dot Indicator
                        CategoryDotSelector(
                            currentCategory = state.category,
                            onSelect = { viewModel.updateCategory(it) }
                        )
                    }

                    HorizontalDivider(color = UtilitarianBorderStroke, thickness = 1.dp)

                    // All Day Toggle Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "All day",
                            color = UtilitarianTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = state.isAllDay,
                            onCheckedChange = { viewModel.updateAllDay(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = UtilitarianTextPrimary,
                                checkedTrackColor = UtilitarianAccentRed,
                                uncheckedThumbColor = UtilitarianTextSecondary,
                                uncheckedTrackColor = UtilitarianPureBlack,
                                uncheckedBorderColor = UtilitarianBorderStroke
                            )
                        )
                    }

                    // Date & Time Range Section
                    DateTimeScheduleSection(
                        isReminder = state.entryType == EntryType.REMINDER,
                        isAllDay = state.isAllDay,
                        startMillis = state.startMillis,
                        endMillis = state.endMillis,
                        onAddOneHour = {
                            viewModel.updateStartMillis(state.startMillis + 3600000L)
                            viewModel.updateEndMillis(state.endMillis + 3600000L)
                        }
                    )

                    HorizontalDivider(color = UtilitarianBorderStroke, thickness = 1.dp)

                    // Option: Location
                    OptionRow(
                        icon = Icons.Outlined.LocationOn,
                        label = if (state.location.isEmpty()) "Location" else state.location,
                        textColor = if (state.location.isEmpty()) UtilitarianTextSecondary else UtilitarianTextPrimary
                    ) {
                        // Quick toggle location dialog or inline toggle
                        val nextLoc = if (state.location.isEmpty()) "Room 302" else if (state.location == "Room 302") "Online / Lab" else ""
                        viewModel.updateLocation(nextLoc)
                    }

                    HorizontalDivider(color = UtilitarianBorderStroke, thickness = 1.dp)

                    // Option: Alert / Reminder Offset
                    OptionRow(
                        icon = Icons.Outlined.Notifications,
                        label = state.reminderOffset.displayName,
                        textColor = UtilitarianTextPrimary
                    ) {
                        // Cycle through standard offsets
                        val next = when (state.reminderOffset) {
                            ReminderOffset.NONE -> ReminderOffset.AT_TIME
                            ReminderOffset.AT_TIME -> ReminderOffset.FIVE_MINUTES
                            ReminderOffset.FIVE_MINUTES -> ReminderOffset.TEN_MINUTES
                            ReminderOffset.TEN_MINUTES -> ReminderOffset.FIFTEEN_MINUTES
                            ReminderOffset.FIFTEEN_MINUTES -> ReminderOffset.THIRTY_MINUTES
                            ReminderOffset.THIRTY_MINUTES -> ReminderOffset.ONE_HOUR
                            ReminderOffset.ONE_HOUR -> ReminderOffset.NONE
                        }
                        viewModel.updateReminderOffset(next)
                    }

                    HorizontalDivider(color = UtilitarianBorderStroke, thickness = 1.dp)

                    // Option: Repeat / Recurrence
                    OptionRow(
                        icon = Icons.Outlined.Repeat,
                        label = state.recurrenceMode.displayName,
                        textColor = UtilitarianTextPrimary
                    ) {
                        val next = when (state.recurrenceMode) {
                            RecurrenceMode.NONE -> RecurrenceMode.DAILY
                            RecurrenceMode.DAILY -> RecurrenceMode.WEEKDAYS
                            RecurrenceMode.WEEKDAYS -> RecurrenceMode.WEEKLY
                            RecurrenceMode.WEEKLY -> RecurrenceMode.MONTHLY
                            RecurrenceMode.MONTHLY -> RecurrenceMode.NONE
                        }
                        viewModel.updateRecurrence(next)
                    }

                    HorizontalDivider(color = UtilitarianBorderStroke, thickness = 1.dp)

                    // Option: Notes Field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = "Notes",
                            tint = UtilitarianTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        TextField(
                            value = state.notes,
                            onValueChange = { viewModel.updateNotes(it) },
                            placeholder = {
                                Text(
                                    text = "Notes & workspace goals...",
                                    color = UtilitarianTextDisabled,
                                    fontSize = 14.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = UtilitarianTextPrimary,
                                unfocusedTextColor = UtilitarianTextPrimary,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    HorizontalDivider(color = UtilitarianBorderStroke, thickness = 1.dp)

                    // Option: Attachment Placeholder
                    OptionRow(
                        icon = Icons.Outlined.AttachFile,
                        label = if (state.attachmentPath.isEmpty()) "Attachment" else "1 File Attached",
                        textColor = if (state.attachmentPath.isEmpty()) UtilitarianTextSecondary else UtilitarianTextPrimary
                    ) {
                        // Toggle attachment flag
                    }

                    // Option: Priority (Visible in Reminder mode)
                    if (state.entryType == EntryType.REMINDER) {
                        HorizontalDivider(color = UtilitarianBorderStroke, thickness = 1.dp)
                        OptionRow(
                            icon = Icons.Outlined.Tune,
                            label = "Priority: ${state.priority.displayName}",
                            textColor = UtilitarianTextPrimary
                        ) {
                            val next = when (state.priority) {
                                ActivityPriority.LOW -> ActivityPriority.MEDIUM
                                ActivityPriority.MEDIUM -> ActivityPriority.HIGH
                                ActivityPriority.HIGH -> ActivityPriority.LOW
                            }
                            viewModel.updatePriority(next)
                        }
                    }
                }
            }
        }

        // Bottom Action Dock: Cancel | Save Pill
        BottomCancelSaveDock(
            onCancel = onDismiss,
            onSave = { viewModel.saveEntry() },
            isSaveEnabled = state.title.isNotBlank() && !state.isSaving,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SegmentedTopToggle(
    selectedType: EntryType,
    onSelectType: (EntryType) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(32.dp),
        color = UtilitarianCardBackground,
        border = BorderStroke(1.dp, UtilitarianBorderStroke)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SegmentItem(
                label = "Event",
                isSelected = selectedType == EntryType.ACTIVITY,
                onClick = { onSelectType(EntryType.ACTIVITY) }
            )
            SegmentItem(
                label = "Reminder",
                isSelected = selectedType == EntryType.REMINDER,
                onClick = { onSelectType(EntryType.REMINDER) }
            )
        }
    }
}

@Composable
private fun SegmentItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) UtilitarianPureBlack else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, UtilitarianBorderStroke) else null
    ) {
        Text(
            text = label,
            color = if (isSelected) UtilitarianTextPrimary else UtilitarianTextDisabled,
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CategoryDotSelector(
    currentCategory: ActivityCategory,
    onSelect: (ActivityCategory) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { isExpanded = !isExpanded }
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(currentCategory.colorHex))
        )
        if (isExpanded) {
            Spacer(modifier = Modifier.width(6.dp))
            ActivityCategory.entries.forEach { cat ->
                if (cat != currentCategory) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(cat.colorHex))
                            .clickable {
                                onSelect(cat)
                                isExpanded = false
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateTimeScheduleSection(
    isReminder: Boolean,
    isAllDay: Boolean,
    startMillis: Long,
    endMillis: Long,
    onAddOneHour: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddOneHour)
            .padding(vertical = 12.dp)
    ) {
        if (isReminder) {
            // Reminder: Single line Date | Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(startMillis)),
                    color = UtilitarianTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!isAllDay) {
                    Text(
                        text = "  |  ${timeFormat.format(Date(startMillis))}",
                        color = UtilitarianTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Activity: Start -> End Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dateFormat.format(Date(startMillis)),
                        color = UtilitarianTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (!isAllDay) {
                        Text(
                            text = timeFormat.format(Date(startMillis)),
                            color = UtilitarianTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "to",
                    tint = UtilitarianTextDisabled,
                    modifier = Modifier.size(16.dp)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = dateFormat.format(Date(endMillis)),
                        color = UtilitarianTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (!isAllDay) {
                        Text(
                            text = timeFormat.format(Date(endMillis)),
                            color = UtilitarianTextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    icon: ImageVector,
    label: String,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UtilitarianTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun BottomCancelSaveDock(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = RoundedCornerShape(32.dp),
            color = UtilitarianCardBackground,
            border = BorderStroke(1.dp, UtilitarianBorderStroke)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onCancel)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        color = UtilitarianTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(UtilitarianBorderStroke)
                )

                // Save
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = isSaveEnabled, onClick = onSave)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        color = if (isSaveEnabled) UtilitarianAccentRed else UtilitarianTextDisabled,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
