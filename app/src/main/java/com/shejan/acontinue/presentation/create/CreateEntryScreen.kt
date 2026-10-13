package com.shejan.acontinue.presentation.create

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.ReminderOffset
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CreateEntryScreen(
    viewModel: CreateEntryViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val colors = LocalUtilitarianColors.current
    val context = LocalContext.current

    var showColorPicker by remember { mutableStateOf(false) }
    var showEmojiKeyboard by remember { mutableStateOf(false) }

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

    val localTimeZoneString = remember {
        val tz = TimeZone.getDefault()
        val offsetHours = tz.rawOffset / (1000 * 60 * 60)
        val sign = if (offsetHours >= 0) "+$offsetHours" else "$offsetHours"
        "(GMT$sign) ${tz.getDisplayName(tz.inDaylightTime(Date()), TimeZone.LONG, Locale.getDefault())}"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = if (showEmojiKeyboard) 360.dp else 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Segmented Pill Toggle: Event vs Reminder
            SegmentedTopToggle(
                selectedType = state.entryType,
                onSelectType = { viewModel.updateEntryType(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Card Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = colors.cardBackground,
                border = BorderStroke(1.dp, colors.borderStroke)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Title Input, Emoji Button & Color Picker Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = state.title,
                                onValueChange = { viewModel.updateTitle(it) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                textStyle = TextStyle(
                                    color = colors.textPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                cursorBrush = SolidColor(colors.accent),
                                decorationBox = { innerTextField ->
                                    if (state.title.isEmpty()) {
                                        Text(
                                            text = "Title",
                                            color = colors.textDisabled,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            // Emoji / Sticker Button (Opens Bottom Emoji Keyboard)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        showColorPicker = false
                                        showEmojiKeyboard = !showEmojiKeyboard
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (state.emoji.isNotBlank()) {
                                    Text(
                                        text = state.emoji,
                                        fontSize = 18.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Outlined.SentimentSatisfied,
                                        contentDescription = "Emoji",
                                        tint = if (showEmojiKeyboard) colors.accent else colors.textSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Color Circle Anchor (Opens 12-Color Popup)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        showEmojiKeyboard = false
                                        showColorPicker = !showColorPicker
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(state.category.colorHex))
                                )
                            }
                        }

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // "All day" Row with Clock icon on the left and Switch on the right
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "All day",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "All day",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            Switch(
                                checked = state.isAllDay,
                                onCheckedChange = { viewModel.updateAllDay(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.textPrimary,
                                    checkedTrackColor = colors.accent,
                                    uncheckedThumbColor = colors.textSecondary,
                                    uncheckedTrackColor = colors.background,
                                    uncheckedBorderColor = colors.borderStroke
                                )
                            )
                        }

                        // Date & Time Multi-Column Range Row
                        DateTimeRow(
                            isReminder = state.entryType == EntryType.REMINDER,
                            isAllDay = state.isAllDay,
                            startMillis = state.startMillis,
                            endMillis = state.endMillis,
                            onTimeClick = {
                                viewModel.updateStartMillis(state.startMillis + 3600000L)
                                viewModel.updateEndMillis(state.endMillis + 3600000L)
                            }
                        )

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Location Row
                        FormOptionRow(
                            icon = Icons.Outlined.LocationOn,
                            label = state.location.ifEmpty { "Location" },
                            isPlaceholder = state.location.isEmpty(),
                            onClick = {
                                val nextLoc = when (state.location) {
                                    "" -> "Room 302"
                                    "Room 302" -> "Main Hall"
                                    else -> ""
                                }
                                viewModel.updateLocation(nextLoc)
                            }
                        )

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Account / Calendar Row
                        FormOptionRow(
                            icon = Icons.Outlined.CalendarMonth,
                            label = "beingshejanahmmed@gmail.com",
                            isPlaceholder = false,
                            leadingDotColor = Color(state.category.colorHex),
                            onClick = { /* Account switch / calendar scope */ }
                        )

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Alert / Reminder Offset Row
                        FormOptionRow(
                            icon = Icons.Outlined.Notifications,
                            label = if (state.reminderOffset == ReminderOffset.NONE) "Alert" else state.reminderOffset.displayName,
                            isPlaceholder = state.reminderOffset == ReminderOffset.NONE,
                            onClick = {
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
                        )

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Repeat Row
                        FormOptionRow(
                            icon = Icons.Outlined.Repeat,
                            label = state.recurrenceMode.displayName,
                            isPlaceholder = state.recurrenceMode == RecurrenceMode.NONE,
                            onClick = {
                                val next = when (state.recurrenceMode) {
                                    RecurrenceMode.NONE -> RecurrenceMode.DAILY
                                    RecurrenceMode.DAILY -> RecurrenceMode.WEEKDAYS
                                    RecurrenceMode.WEEKDAYS -> RecurrenceMode.WEEKLY
                                    RecurrenceMode.WEEKLY -> RecurrenceMode.MONTHLY
                                    RecurrenceMode.MONTHLY -> RecurrenceMode.NONE
                                }
                                viewModel.updateRecurrence(next)
                            }
                        )

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Notes Field Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = "Notes",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            BasicTextField(
                                value = state.notes,
                                onValueChange = { viewModel.updateNotes(it) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(
                                    color = colors.textPrimary,
                                    fontSize = 15.sp
                                ),
                                cursorBrush = SolidColor(colors.accent),
                                decorationBox = { innerTextField ->
                                    if (state.notes.isEmpty()) {
                                        Text(
                                            text = "Notes",
                                            color = colors.textSecondary,
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Video Conference Row
                        FormOptionRow(
                            icon = Icons.Outlined.Videocam,
                            label = "Video conference",
                            isPlaceholder = true,
                            onClick = { /* Toggle video conference integration */ }
                        )

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Attachment Row (Singular label matching screenshot)
                        FormOptionRow(
                            icon = Icons.Outlined.AttachFile,
                            label = if (state.attachmentPath.isEmpty()) "Attachment" else "1 File Attached",
                            isPlaceholder = state.attachmentPath.isEmpty(),
                            onClick = { /* File picker integration */ }
                        )

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Invitees Row
                        FormOptionRow(
                            icon = Icons.Outlined.PersonOutline,
                            label = "Invitees",
                            isPlaceholder = true,
                            onClick = { /* Add invitees */ }
                        )

                        HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)

                        // Dynamic Timezone Row
                        FormOptionRow(
                            icon = Icons.Outlined.Language,
                            label = localTimeZoneString,
                            isPlaceholder = false,
                            onClick = { /* Timezone change */ }
                        )

                        // Priority Row (Reminder mode)
                        if (state.entryType == EntryType.REMINDER) {
                            HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.6f), thickness = 0.5.dp)
                            FormOptionRow(
                                icon = Icons.Outlined.Tune,
                                label = "Priority: ${state.priority.displayName}",
                                isPlaceholder = false,
                                onClick = {
                                    val next = when (state.priority) {
                                        ActivityPriority.LOW -> ActivityPriority.MEDIUM
                                        ActivityPriority.MEDIUM -> ActivityPriority.HIGH
                                        ActivityPriority.HIGH -> ActivityPriority.LOW
                                    }
                                    viewModel.updatePriority(next)
                                }
                            )
                        }
                    }

                    // 12-Color Floating Popup Picker Box
                    if (showColorPicker) {
                        val density = LocalDensity.current
                        val yOffsetPx = with(density) { 56.dp.roundToPx() }

                        Popup(
                            alignment = Alignment.TopCenter,
                            offset = IntOffset(x = 0, y = yOffsetPx),
                            onDismissRequest = { showColorPicker = false },
                            properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
                        ) {
                            EventColorPickerPopup(
                                selectedCategory = state.category,
                                onSelectCategory = {
                                    viewModel.updateCategory(it)
                                    showColorPicker = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Dock: Cancel | Save Pill (Visible when Emoji Keyboard is closed)
        if (!showEmojiKeyboard) {
            BottomCancelSaveDock(
                onCancel = onDismiss,
                onSave = { viewModel.saveEntry() },
                isSaveEnabled = state.title.isNotBlank() && !state.isSaving,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Bottom Emoji Keyboard Panel (Opened by Emoji button)
        AnimatedVisibility(
            visible = showEmojiKeyboard,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomEmojiKeyboard(
                recentEmojis = state.recentEmojis,
                onEmojiSelected = { emoji ->
                    viewModel.appendEmojiToTitle(emoji)
                },
                onClose = { showEmojiKeyboard = false }
            )
        }
    }
}

@Composable
private fun BottomEmojiKeyboard(
    recentEmojis: List<String>,
    onEmojiSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    var selectedCategoryIndex by remember { mutableIntStateOf(1) } // 1 is Celebrations (default matching screenshot)

    val categoryTabs = listOf(
        "recent" to "",
        "celebrations" to "🎂",
        "music" to "🎸",
        "mood" to "🤍",
        "food" to "🍔",
        "smileys" to "😊",
        "places" to "🏖️",
        "work" to "🏫",
        "more" to ""
    )

    val emojiCollections = listOf(
        // 0: Celebrations (matches reference screenshot items)
        listOf(
            "🎂", "🎁", "🎉", "💍", "💌",
            "🥂", "🎆", "🎡", "🎙️", "🎬",
            "🏖️", "🏔️", "⛺", "🏫", "📝",
            "🏠", "💼", "🌳", "🏥", "💊",
            "🛒", "💵", "💳", "🗓️", "📅"
        ),
        // 1: Music & Entertainment
        listOf("🎸", "🎵", "🎶", "🎹", "🎺", "🎻", "🎧", "🎤", "🎬", "🎨", "🎭", "🎪", "🎟️", "🎮", "🕹️"),
        // 2: Mood & Symbols
        listOf("🤍", "❤️", "🧡", "💛", "💚", "💙", "💜", "✨", "🔥", "⭐", "🌟", "💡", "🎯", "💯", "✅"),
        // 3: Food & Treats
        listOf("🍔", "🍕", "☕", "🍣", "🍦", "🍩", "🥑", "🍜", "🥗", "🍹", "🍫", "🥐", "🥪", "🍿", "🍳"),
        // 4: Smileys
        listOf("😊", "🥳", "😎", "🤩", "🙌", "😍", "😇", "🤔", "😴", "👍", "👏", "🤝", "💪", "✌️", "👋"),
        // 5: Places & Travel
        listOf("🏖️", "🏔️", "⛺", "🌴", "✈️", "🚗", "🚲", "🌅", "🛶", "🚂", "🚤", "🏨", "🗺️", "🗽", "🗼"),
        // 6: Campus & Work
        listOf("🏫", "📝", "💼", "📚", "💻", "🔬", "🎯", "📊", "🎓", "📎", "📁", "🖋️", "📅", "📐", "📌"),
        // 7: More & Sports
        listOf("⚽", "🏀", "🎾", "🏋️", "🧘", "🏊", "🚴", "🥊", "🏸", "⛳", "🛹", "🎿", "🎳", "🎣", "🏆")
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        color = Color(0xFF1B1B1B),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        border = BorderStroke(1.dp, colors.borderStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Category Tabs Header Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161616))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(categoryTabs) { index, (_, icon) ->
                    val isSelected = index == selectedCategoryIndex
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedCategoryIndex = index }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        when (index) {
                            0 -> {
                                // Recent Tab: Single Monochrome Outlined Clock Vector Icon
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "Recent History",
                                    tint = if (isSelected) Color(0xFF3897F0) else colors.textSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            categoryTabs.lastIndex -> {
                                // More Tab: Single Monochrome Outlined Add Vector Icon
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "More",
                                    tint = if (isSelected) Color(0xFF3897F0) else colors.textSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            else -> {
                                Text(
                                    text = icon,
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(2.dp)
                                .background(if (isSelected) Color(0xFF3897F0) else Color.Transparent)
                        )
                    }
                }
            }

            HorizontalDivider(color = colors.borderStroke.copy(alpha = 0.5f), thickness = 0.5.dp)

            // Dynamic Content Grid: Zero Hardcoding for History
            if (selectedCategoryIndex == 0) {
                // Tab 0: Real User History
                if (recentEmojis.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recently used emojis",
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentEmojis) { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onEmojiSelected(emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 30.sp
                                )
                            }
                        }
                    }
                }
            } else {
                // Tabs 1+: Preset Category Grids
                val collectionIndex = (selectedCategoryIndex - 1).coerceIn(0, emojiCollections.lastIndex)
                val activeList = emojiCollections[collectionIndex]
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeList) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onEmojiSelected(emoji) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 30.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventColorPickerPopup(
    selectedCategory: ActivityCategory,
    onSelectCategory: (ActivityCategory) -> Unit
) {
    val allColors = ActivityCategory.EVENT_PALETTE_COLORS
    val row1 = allColors.take(6)
    val row2 = allColors.drop(6).take(6)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF262626),
        border = BorderStroke(1.dp, Color(0xFF3E3E3E)),
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1: 6 Colors
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row1.forEach { cat ->
                    ColorPaletteCircle(
                        category = cat,
                        isSelected = cat == selectedCategory,
                        onClick = { onSelectCategory(cat) }
                    )
                }
            }

            // Row 2: 6 Colors
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row2.forEach { cat ->
                    ColorPaletteCircle(
                        category = cat,
                        isSelected = cat == selectedCategory,
                        onClick = { onSelectCategory(cat) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPaletteCircle(
    category: ActivityCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = Color(category.colorHex)

    Box(
        modifier = Modifier
            .size(30.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Concentric double-ring indicator matching screenshot
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        } else {
            // Regular filled circle
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun SegmentedTopToggle(
    selectedType: EntryType,
    onSelectType: (EntryType) -> Unit
) {
    val colors = LocalUtilitarianColors.current

    Surface(
        shape = RoundedCornerShape(32.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, colors.borderStroke)
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
    val colors = LocalUtilitarianColors.current

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) colors.background else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, colors.borderStroke) else null
    ) {
        Text(
            text = label,
            color = if (isSelected) colors.textPrimary else colors.textDisabled,
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun DateTimeRow(
    isReminder: Boolean,
    isAllDay: Boolean,
    startMillis: Long,
    endMillis: Long,
    onTimeClick: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTimeClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isReminder) {
            // Reminder: Single line Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(startMillis)),
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!isAllDay) {
                    Text(
                        text = "  ${timeFormat.format(Date(startMillis))}",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            // Activity: Start -> End 2-column range matching screenshot
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateFormat.format(Date(startMillis)),
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!isAllDay) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeFormat.format(Date(startMillis)),
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "to",
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(16.dp)
                    .padding(horizontal = 2.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = dateFormat.format(Date(endMillis)),
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!isAllDay) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeFormat.format(Date(endMillis)),
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun FormOptionRow(
    icon: ImageVector,
    label: String,
    isPlaceholder: Boolean,
    leadingDotColor: Color? = null,
    onClick: () -> Unit
) {
    val colors = LocalUtilitarianColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
            if (leadingDotColor != null) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(leadingDotColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            color = if (isPlaceholder) colors.textSecondary else colors.textPrimary,
            fontSize = 15.sp,
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
    val colors = LocalUtilitarianColors.current

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
            color = colors.cardBackground,
            border = BorderStroke(1.dp, colors.borderStroke)
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
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(colors.borderStroke)
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
                        color = if (isSaveEnabled) colors.accent else colors.textDisabled,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
