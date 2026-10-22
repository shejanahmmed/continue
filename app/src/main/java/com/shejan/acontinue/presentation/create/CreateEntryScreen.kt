package com.shejan.acontinue.presentation.create

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.shejan.acontinue.core.util.AttachmentCategory
import com.shejan.acontinue.core.util.AttachmentHelper
import com.shejan.acontinue.core.util.ProcessedAttachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.ui.unit.TextUnit
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import com.shejan.acontinue.core.util.LinkHelper
import com.shejan.acontinue.core.util.LocationHelper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
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
import com.shejan.acontinue.domain.model.ReminderCategory
import com.shejan.acontinue.domain.model.ReminderOffset
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ActivePickerField {
    NONE,
    START_DATE,
    END_DATE,
    START_TIME,
    END_TIME
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateEntryScreen(
    viewModel: CreateEntryViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val colors = LocalUtilitarianColors.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                uris.forEach { uri ->
                    AttachmentHelper.processAndCopyUri(context, uri)
                        .onSuccess { attachment ->
                            viewModel.addAttachment(attachment)
                        }
                        .onFailure { error ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    error.message ?: "Failed to process attachment",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            }
        }
    }

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

    var isAlertScreenVisible by remember { mutableStateOf(false) }
    var isRepeatScreenVisible by remember { mutableStateOf(false) }
    var isEarlyAlertScreenVisible by remember { mutableStateOf(false) }
    var isCategoryScreenVisible by remember { mutableStateOf(false) }
    var isAddCategoryScreenVisible by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<ReminderCategory?>(null) }
    var showIntensityPopup by remember { mutableStateOf(false) }

    BackHandler(enabled = isAlertScreenVisible || isRepeatScreenVisible || isEarlyAlertScreenVisible || showIntensityPopup || isCategoryScreenVisible || isAddCategoryScreenVisible) {
        if (showIntensityPopup) showIntensityPopup = false
        else if (isAddCategoryScreenVisible) {
            editingCategory = null
            isAddCategoryScreenVisible = false
        }
        else if (isCategoryScreenVisible) isCategoryScreenVisible = false
        else if (isEarlyAlertScreenVisible) isEarlyAlertScreenVisible = false
        else if (isAlertScreenVisible) isAlertScreenVisible = false
        else if (isRepeatScreenVisible) isRepeatScreenVisible = false
    }

    if (isAddCategoryScreenVisible) {
        AddCategoryScreen(
            initialCategory = editingCategory,
            existingCategories = state.savedReminderCategories,
            onSaveCategory = { name, colorHex, iconName ->
                val currentEditing = editingCategory
                if (currentEditing != null) {
                    viewModel.updateReminderCategory(currentEditing, name, colorHex, iconName)
                } else {
                    viewModel.addReminderCategory(name, colorHex, iconName)
                }
                editingCategory = null
                isAddCategoryScreenVisible = false
            },
            onNavigateBack = {
                editingCategory = null
                isAddCategoryScreenVisible = false
            }
        )
        return
    }

    if (isCategoryScreenVisible) {
        CategorySelectionScreen(
            selectedCategory = state.selectedReminderCategory,
            categories = state.savedReminderCategories,
            reminderCounts = state.categoryReminderCounts,
            onSelectCategory = { category ->
                viewModel.selectReminderCategory(category)
                isCategoryScreenVisible = false
            },
            onAddCategoryClick = {
                editingCategory = null
                isAddCategoryScreenVisible = true
            },
            onEditCategory = { category ->
                editingCategory = category
                isAddCategoryScreenVisible = true
            },
            onDeleteCategory = { category ->
                viewModel.deleteReminderCategory(category)
            },
            onNavigateBack = { isCategoryScreenVisible = false }
        )
        return
    }

    if (isEarlyAlertScreenVisible) {
        EarlyAlertSelectionScreen(
            selectedMinutes = state.earlyAlertMinutes,
            savedCustomAlerts = state.savedCustomAlerts,
            onSelectAlert = { viewModel.updateEarlyAlertMinutes(it) },
            onAddCustomAlert = { value, unit -> viewModel.addCustomAlert(value, unit) },
            onUpdateCustomAlert = { id, value, unit -> viewModel.updateCustomAlert(id, value, unit) },
            onDeleteCustomAlert = { id -> viewModel.deleteCustomAlert(id) },
            onNavigateBack = { isEarlyAlertScreenVisible = false }
        )
        return
    }

    if (isAlertScreenVisible) {
        AlertSelectionScreen(
            state = state,
            onToggleAlertsEnabled = { viewModel.toggleAlertsEnabled(it) },
            onToggleAlertMinute = { viewModel.toggleAlertMinute(it) },
            onAddCustomAlert = { value, unit -> viewModel.addCustomAlert(value, unit) },
            onUpdateCustomAlert = { id, value, unit -> viewModel.updateCustomAlert(id, value, unit) },
            onDeleteCustomAlert = { id -> viewModel.deleteCustomAlert(id) },
            onNavigateBack = { isAlertScreenVisible = false }
        )
        return
    }

    if (isRepeatScreenVisible) {
        RepeatSelectionScreen(
            state = state,
            onSelectRecurrence = { mode, interval -> viewModel.updateRecurrence(mode, interval) },
            onUpdateInterval = { viewModel.updateRecurrenceInterval(it) },
            onToggleRecurrenceDay = { viewModel.toggleRecurrenceDay(it) },
            onUpdateMonthlyType = { viewModel.updateMonthlyRepeatType(it) },
            onToggleMonthlyDay = { viewModel.toggleMonthlySelectedDay(it) },
            onUpdateYearlyType = { viewModel.updateYearlyRepeatType(it) },
            onToggleYearlyMonth = { viewModel.toggleYearlySelectedMonth(it) },
            onUpdateEndType = { viewModel.updateRecurrenceEndType(it) },
            onUpdateEndCount = { viewModel.updateRecurrenceEndCount(it) },
            onUpdateEndDate = { viewModel.updateRecurrenceEndDate(it) },
            onNavigateBack = { isRepeatScreenVisible = false }
        )
        return
    }

    if (state.entryType == EntryType.NOTE) {
        CreateNoteScreen(
            onSaveNote = { title, content, colorHex, labels ->
                viewModel.saveNote(title, content, colorHex, labels)
            },
            onDiscard = {
                onDismiss()
            },
            onMakeCopy = { title, content, colorHex, labels ->
                viewModel.makeCopyOfNote(title, content, colorHex, labels)
            },
            allLabels = state.savedNoteLabels,
            onCreateLabel = { viewModel.addNoteLabel(it) },
            onEditLabel = { oldName, newName -> viewModel.updateNoteLabel(oldName, newName) },
            onDeleteLabel = { viewModel.deleteNoteLabel(it) }
        )
        return
    }

    var showColorPicker by remember { mutableStateOf(false) }
    var showEmojiKeyboard by remember { mutableStateOf(false) }
    var activePickerField by remember { mutableStateOf(ActivePickerField.NONE) }
    var isRollerMode by remember { mutableStateOf(false) }
    var displayedYear by remember {
        val cal = Calendar.getInstance().apply { timeInMillis = state.startMillis }
        mutableIntStateOf(cal.get(Calendar.YEAR))
    }
    var displayedMonth by remember {
        val cal = Calendar.getInstance().apply { timeInMillis = state.startMillis }
        mutableIntStateOf(cal.get(Calendar.MONTH))
    }
    var slideDirection by remember { mutableIntStateOf(1) }

    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val notesBringIntoViewRequester = remember { BringIntoViewRequester() }
    val notesFocusRequester = remember { FocusRequester() }
    var isNotesFocused by remember { mutableStateOf(false) }
    val isImeVisible = WindowInsets.isImeVisible
    val density = LocalDensity.current

    var notesValue by remember {
        mutableStateOf(TextFieldValue(text = state.notes, selection = TextRange(state.notes.length)))
    }
    var notesTextLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    LaunchedEffect(state.notes) {
        if (state.notes != notesValue.text) {
            notesValue = notesValue.copy(
                text = state.notes,
                selection = TextRange(state.notes.length)
            )
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val imeBottomPx = WindowInsets.ime.getBottom(density)
    val imeBottomDp = with(density) { imeBottomPx.toDp() }
    val visibleViewportDp = (screenHeightDp - imeBottomDp).coerceAtLeast(240.dp)
    // Dynamic, device-adaptive clearance: scales to ~42% of visible screen height above keyboard (180dp - 280dp)
    val smartClearanceDp = (visibleViewportDp * 0.42f).coerceIn(180.dp, 280.dp)
    val smartClearancePx = with(density) { smartClearanceDp.toPx() }

    // Keep Notes field and active cursor line lifted comfortably above the software keyboard on any device
    LaunchedEffect(isNotesFocused, isImeVisible, imeBottomPx, notesValue.selection, notesValue.text, notesTextLayoutResult) {
        if (isNotesFocused && isImeVisible) {
            if (imeBottomPx == 0) {
                delay(150)
            }
            val layout = notesTextLayoutResult
            val targetRect = if (layout != null) {
                val textLength = layout.layoutInput.text.length
                val cursorIndex = notesValue.selection.start.coerceIn(0, textLength)
                val cursorRect = layout.getCursorRect(cursorIndex)
                Rect(
                    left = cursorRect.left,
                    top = cursorRect.top,
                    right = cursorRect.right,
                    bottom = cursorRect.bottom + smartClearancePx
                )
            } else {
                val estimatedLinePx = with(density) { 24.dp.toPx() }
                Rect(
                    left = 0f,
                    top = 0f,
                    right = 0f,
                    bottom = estimatedLinePx + smartClearancePx
                )
            }
            notesBringIntoViewRequester.bringIntoView(targetRect)
        }
    }

    LaunchedEffect(activePickerField) {
        if (activePickerField != ActivePickerField.NONE) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    var showDiscardSheet by remember { mutableStateOf(false) }

    val hasModifications = state.title.isNotBlank() ||
            state.emoji.isNotBlank() ||
            state.location.isNotBlank() ||
            state.notes.isNotBlank() ||
            state.attachments.isNotEmpty() ||
            state.selectedAlertMinutes.isNotEmpty() ||
            state.recurrenceMode != RecurrenceMode.NONE ||
            state.earlyAlertMinutes != -1

    BackHandler(enabled = true) {
        if (showDiscardSheet) {
            showDiscardSheet = false
        } else if (showIntensityPopup) {
            showIntensityPopup = false
        } else if (showEmojiKeyboard) {
            showEmojiKeyboard = false
        } else if (showColorPicker) {
            showColorPicker = false
        } else if (activePickerField != ActivePickerField.NONE) {
            activePickerField = ActivePickerField.NONE
        } else if (hasModifications) {
            showDiscardSheet = true
        } else {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = if (showEmojiKeyboard) 360.dp else if (isImeVisible) 280.dp else 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Edit Mode Header vs New Entry Segmented Toggle
            if (state.editingActivityId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (hasModifications) {
                                    showDiscardSheet = true
                                } else {
                                    onDismiss()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (state.entryType == EntryType.REMINDER) "EDIT REMINDER" else "EDIT EVENT",
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 1.2.sp
                    )
                }
            } else {
                SegmentedTopToggle(
                    selectedType = state.entryType,
                    onSelectType = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        viewModel.updateEntryType(it)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Main Card Container (Elevated surface matching Home overview card)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = if (colors.isDark) Color(0xFF1E1E22) else colors.cardBackground,
                border = BorderStroke(1.dp, if (colors.isDark) Color(0xFF323238) else colors.borderStroke)
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
                                    .padding(vertical = 8.dp)
                                    .onFocusChanged { focusState ->
                                        if (focusState.isFocused) {
                                            activePickerField = ActivePickerField.NONE
                                            showColorPicker = false
                                            showEmojiKeyboard = false
                                        }
                                    },
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
                                            color = colors.textSecondary,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    innerTextField()
                                }
                            )

                            if (state.entryType == EntryType.ACTIVITY) {
                                // Emoji / Sticker Button with Sequential Shrink-and-Pop Physics
                                EmojiPopButton(
                                    emoji = state.emoji,
                                    isOpen = showEmojiKeyboard,
                                    onClick = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        showColorPicker = false
                                        showEmojiKeyboard = !showEmojiKeyboard
                                    }
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Color Circle Anchor (Opens 12-Color Popup)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
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
                        }

                        SectionDivider()

                        // "All day" Row with Clock icon on the left and Switch on the right
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(22.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = "All day",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = "All day",
                                    color = colors.textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            Switch(
                                checked = state.isAllDay,
                                onCheckedChange = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    if (it && (activePickerField == ActivePickerField.START_TIME || activePickerField == ActivePickerField.END_TIME)) {
                                        activePickerField = ActivePickerField.NONE
                                    }
                                    viewModel.updateAllDay(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = colors.textPrimary,
                                    checkedTrackColor = colors.accent,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFF3A3A3C),
                                    uncheckedBorderColor = Color.Transparent
                                )
                            )
                        }

                        if (state.entryType == EntryType.REMINDER) {
                            // Reminder mode: Indented single date & time line directly under All day
                            val dateFormat = remember { SimpleDateFormat("EEE, d MMM", Locale.ENGLISH) }
                            val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.ENGLISH) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 42.dp, top = 0.dp, bottom = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateFormat.format(Date(state.startMillis)),
                                    color = if (activePickerField == ActivePickerField.START_DATE) colors.accent else colors.textPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = if (activePickerField == ActivePickerField.START_DATE) FontWeight.SemiBold else FontWeight.Normal,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            focusManager.clearFocus()
                                            keyboardController?.hide()
                                            showColorPicker = false
                                            showEmojiKeyboard = false
                                            activePickerField = if (activePickerField == ActivePickerField.START_DATE) ActivePickerField.NONE else ActivePickerField.START_DATE
                                            val cal = Calendar.getInstance().apply { timeInMillis = state.startMillis }
                                            displayedYear = cal.get(Calendar.YEAR)
                                            displayedMonth = cal.get(Calendar.MONTH)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                                if (!state.isAllDay) {
                                    Text(
                                        text = "|",
                                        color = colors.textSecondary.copy(alpha = 0.5f),
                                        fontSize = 16.sp,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    Text(
                                        text = timeFormat.format(Date(state.startMillis)).lowercase(Locale.ENGLISH),
                                        color = if (activePickerField == ActivePickerField.START_TIME) colors.accent else colors.textPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = if (activePickerField == ActivePickerField.START_TIME) FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                                showColorPicker = false
                                                showEmojiKeyboard = false
                                                activePickerField = if (activePickerField == ActivePickerField.START_TIME) ActivePickerField.NONE else ActivePickerField.START_TIME
                                            }
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            // Event mode: Date & Time Multi-Column Range Row
                            DateTimeRow(
                                isReminder = false,
                                isAllDay = state.isAllDay,
                                startMillis = state.startMillis,
                                endMillis = state.endMillis,
                                activePickerField = activePickerField,
                                onDateFieldClick = { clickedField ->
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    showColorPicker = false
                                    showEmojiKeyboard = false
                                    val newActive = if (activePickerField == clickedField) ActivePickerField.NONE else clickedField
                                    activePickerField = newActive
                                    if (newActive == ActivePickerField.START_DATE || newActive == ActivePickerField.END_DATE) {
                                        val targetMillis = if (newActive == ActivePickerField.START_DATE) state.startMillis else state.endMillis
                                        val cal = Calendar.getInstance().apply { timeInMillis = targetMillis }
                                        displayedYear = cal.get(Calendar.YEAR)
                                        displayedMonth = cal.get(Calendar.MONTH)
                                    }
                                },
                                onTimeFieldClick = { clickedField ->
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    showColorPicker = false
                                    showEmojiKeyboard = false
                                    activePickerField = if (activePickerField == clickedField) ActivePickerField.NONE else clickedField
                                }
                            )
                        }

                        // Inline Expandable Calendar & Roller Date Picker
                        AnimatedVisibility(
                            visible = activePickerField == ActivePickerField.START_DATE || activePickerField == ActivePickerField.END_DATE,
                            enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(200)),
                            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(150))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SectionDivider()
                                InlineCalendarDatePicker(
                                    selectedMillis = if (activePickerField == ActivePickerField.START_DATE) state.startMillis else state.endMillis,
                                    displayedYear = displayedYear,
                                    displayedMonth = displayedMonth,
                                    isRollerMode = isRollerMode,
                                    slideDirection = slideDirection,
                                    onToggleRollerMode = { isRollerMode = !isRollerMode },
                                    onNavigateMonth = { delta ->
                                        slideDirection = delta
                                        val cal = Calendar.getInstance().apply {
                                            clear()
                                            set(displayedYear, displayedMonth, 1)
                                            add(Calendar.MONTH, delta)
                                        }
                                        displayedYear = cal.get(Calendar.YEAR)
                                        displayedMonth = cal.get(Calendar.MONTH)
                                    },
                                    onDateSelected = { y, m, d ->
                                        if (activePickerField == ActivePickerField.START_DATE) {
                                            viewModel.updateStartDate(y, m, d)
                                        } else if (activePickerField == ActivePickerField.END_DATE) {
                                            viewModel.updateEndDate(y, m, d)
                                        }
                                        displayedYear = y
                                        displayedMonth = m
                                    }
                                )
                            }
                        }

                        // Inline Expandable 3-Column Wheel Time Picker
                        AnimatedVisibility(
                            visible = !state.isAllDay && (activePickerField == ActivePickerField.START_TIME || activePickerField == ActivePickerField.END_TIME),
                            enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(200)),
                            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(150))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                SectionDivider()
                                InlineWheelTimePicker(
                                    selectedMillis = if (activePickerField == ActivePickerField.START_TIME) state.startMillis else state.endMillis,
                                    onTimeSelected = { hour12, minute, isPm ->
                                        if (activePickerField == ActivePickerField.START_TIME) {
                                            viewModel.updateStartTime(hour12, minute, isPm)
                                        } else if (activePickerField == ActivePickerField.END_TIME) {
                                            viewModel.updateEndTime(hour12, minute, isPm)
                                        }
                                    }
                                )
                            }
                        }

                        if (state.entryType == EntryType.REMINDER) {
                            // --- REMINDER OPTIONS ---
                            SectionDivider()

                            // Early alert Row
                            val earlyAlertLabel = when (state.earlyAlertMinutes) {
                                -1 -> "No early alert"
                                10 -> "10 mins before"
                                15 -> "15 mins before"
                                60 -> "1 hour before"
                                1440 -> "1 day before"
                                else -> {
                                    val custom = state.savedCustomAlerts.firstOrNull { it.minutesBefore == state.earlyAlertMinutes }
                                    custom?.displayName ?: when {
                                        state.earlyAlertMinutes % 10080 == 0 -> "${state.earlyAlertMinutes / 10080} weeks before"
                                        state.earlyAlertMinutes % 1440 == 0 -> "${state.earlyAlertMinutes / 1440} days before"
                                        state.earlyAlertMinutes % 60 == 0 -> "${state.earlyAlertMinutes / 60} hours before"
                                        else -> "${state.earlyAlertMinutes} mins before"
                                    }
                                }
                            }

                            FormOptionRow(
                                customLeadingIcon = {
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Notifications,
                                            contentDescription = "Early alert",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(10.dp)
                                                .background(colors.cardBackground, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.AccessTime,
                                                contentDescription = null,
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(8.dp)
                                            )
                                        }
                                    }
                                },
                                label = earlyAlertLabel,
                                isPlaceholder = state.earlyAlertMinutes == -1,
                                fontSize = 15.sp,
                                onClick = { isEarlyAlertScreenVisible = true }
                            )

                            SectionDivider()

                            // Repeat Row
                            val repeatLabel = if (state.recurrenceMode == RecurrenceMode.NONE) "Don't repeat" else formatRecurrenceLabel(state)
                            FormOptionRow(
                                icon = Icons.Outlined.Repeat,
                                label = repeatLabel,
                                isPlaceholder = state.recurrenceMode == RecurrenceMode.NONE,
                                fontSize = 15.sp,
                                onClick = { isRepeatScreenVisible = true }
                            )

                            SectionDivider()

                            // Priority / Sound Row (Speaker icon)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                FormOptionRow(
                                    icon = Icons.AutoMirrored.Outlined.VolumeUp,
                                    label = state.priority.reminderTitle,
                                    textColor = colors.textPrimary,
                                    fontSize = 15.sp,
                                    onClick = {
                                        showColorPicker = false
                                        showEmojiKeyboard = false
                                        activePickerField = ActivePickerField.NONE
                                        showIntensityPopup = !showIntensityPopup
                                    }
                                )

                                if (showIntensityPopup) {
                                    val density = LocalDensity.current
                                    val yOffsetPx = with(density) { 48.dp.roundToPx() }
                                    val xOffsetPx = with(density) { 8.dp.roundToPx() }

                                    Popup(
                                        alignment = Alignment.TopEnd,
                                        offset = IntOffset(x = xOffsetPx, y = yOffsetPx),
                                        onDismissRequest = { showIntensityPopup = false },
                                        properties = PopupProperties(focusable = true, dismissOnClickOutside = true)
                                    ) {
                                        ReminderIntensityPopup(
                                            selectedPriority = state.priority,
                                            onSelectPriority = { priority ->
                                                viewModel.updatePriority(priority)
                                                showIntensityPopup = false
                                            }
                                        )
                                    }
                                }
                            }

                            SectionDivider()

                            // Reminder Category Row
                            FormOptionRow(
                                icon = getCategoryIcon(state.selectedReminderCategory.iconName),
                                iconTint = Color(state.selectedReminderCategory.colorHex),
                                label = state.selectedReminderCategory.name,
                                textColor = colors.textPrimary,
                                fontSize = 15.sp,
                                onClick = { isCategoryScreenVisible = true }
                            )

                        } else {
                            // --- EVENT (ACTIVITY) OPTIONS ---
                            SectionDivider()

                            // Location Row with free typing, clear button & "Open map" when a map link is entered
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = "Location",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                BasicTextField(
                                    value = state.location,
                                    onValueChange = { viewModel.updateLocation(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp)
                                        .onFocusChanged { focusState ->
                                            if (focusState.isFocused) {
                                                activePickerField = ActivePickerField.NONE
                                                showColorPicker = false
                                                showEmojiKeyboard = false
                                            }
                                        },
                                    textStyle = TextStyle(
                                        color = colors.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    cursorBrush = SolidColor(colors.accent),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            if (state.location.isEmpty()) {
                                                Text(
                                                    text = "Location",
                                                    color = colors.textSecondary,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Normal
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                )

                                // Quick clear button if location is not empty
                                if (state.location.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { viewModel.updateLocation("") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Clear location",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Open map icon button only appears if user pastes/enters a map location link
                                if (LocationHelper.isMapLocationLink(state.location)) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) { LocationHelper.openLocationInMapApp(context, state.location) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                            contentDescription = "Open map",
                                            tint = Color(0xFF3897F0),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            SectionDivider()

                            // Alert / Reminder Offset Row
                            val alertLabel = if (!state.alertsEnabled || state.selectedAlertMinutes.isEmpty()) {
                                "Alert"
                            } else {
                                val labels = state.selectedAlertMinutes.sorted().map { minutes ->
                                    when (minutes) {
                                        0 -> "At time of event"
                                        10 -> "10 mins before"
                                        60 -> "1 hour before"
                                        1440 -> "1 day before"
                                        else -> {
                                            val custom = state.savedCustomAlerts.firstOrNull { it.minutesBefore == minutes }
                                            custom?.displayName ?: "$minutes mins before"
                                        }
                                    }
                                }
                                if (labels.size == 1) labels.first() else "${labels.size} alerts"
                            }

                            FormOptionRow(
                                icon = Icons.Outlined.Notifications,
                                label = alertLabel,
                                isPlaceholder = !state.alertsEnabled || state.selectedAlertMinutes.isEmpty(),
                                fontSize = 15.sp,
                                onClick = { isAlertScreenVisible = true }
                            )

                            SectionDivider()

                            // Repeat Row
                            FormOptionRow(
                                icon = Icons.Outlined.Repeat,
                                label = formatRecurrenceLabel(state),
                                isPlaceholder = state.recurrenceMode == RecurrenceMode.NONE,
                                fontSize = 15.sp,
                                onClick = { isRepeatScreenVisible = true }
                            )

                            SectionDivider()

                            // Notes Field Container with Full Row Tap Target
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { notesFocusRequester.requestFocus() }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Description,
                                            contentDescription = "Notes",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        BasicTextField(
                                            value = notesValue,
                                            onValueChange = { newValue ->
                                                notesValue = newValue
                                                if (newValue.text != state.notes) {
                                                    viewModel.updateNotes(newValue.text)
                                                }
                                            },
                                            onTextLayout = { layoutResult ->
                                                notesTextLayoutResult = layoutResult
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .focusRequester(notesFocusRequester)
                                                .bringIntoViewRequester(notesBringIntoViewRequester)
                                                .onFocusChanged { focusState ->
                                                    isNotesFocused = focusState.isFocused
                                                    if (focusState.isFocused) {
                                                        activePickerField = ActivePickerField.NONE
                                                        showColorPicker = false
                                                        showEmojiKeyboard = false
                                                    }
                                                },
                                            textStyle = TextStyle(
                                                color = colors.textPrimary,
                                                fontSize = 15.sp,
                                            ),
                                            cursorBrush = SolidColor(colors.accent),
                                            decorationBox = { innerTextField ->
                                                if (notesValue.text.isEmpty()) {
                                                    Text(
                                                        text = "Notes",
                                                        color = colors.textSecondary,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )

                                        val detectedUrls = remember(state.notes) { LinkHelper.extractUrls(state.notes) }
                                        if (detectedUrls.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                detectedUrls.forEach { url ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(12.dp))
                                                            .background(Color(0xFF1E3A5F).copy(alpha = 0.4f))
                                                            .border(1.dp, Color(0xFF3897F0), RoundedCornerShape(12.dp))
                                                            .clickable(
                                                                interactionSource = remember { MutableInteractionSource() },
                                                                indication = null
                                                            ) { LinkHelper.openUrl(context, url) }
                                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Outlined.Link,
                                                                contentDescription = "Open Link",
                                                                tint = Color(0xFF3897F0),
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Text(
                                                                text = url.removePrefix("https://").removePrefix("http://").take(30) + if (url.length > 36) "…" else "",
                                                                color = Color(0xFF3897F0),
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                maxLines = 1
                                                            )
                                                            Icon(
                                                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                                                contentDescription = null,
                                                                tint = Color(0xFF3897F0),
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            SectionDivider()

                            // Attachment Row
                            FormOptionRow(
                                icon = Icons.Outlined.AttachFile,
                                label = if (state.attachments.isEmpty()) "Attachment" else "${state.attachments.size} ${if (state.attachments.size == 1) "File" else "Files"} Attached",
                                isPlaceholder = state.attachments.isEmpty(),
                                fontSize = 15.sp,
                                onClick = {
                                    filePickerLauncher.launch(AttachmentHelper.SUPPORTED_MIME_TYPES)
                                }
                            )

                            if (state.attachments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.attachments.forEach { attachment ->
                                        AttachmentItemCard(
                                            attachment = attachment,
                                            onOpen = {
                                                AttachmentHelper.openAttachmentInDefaultApp(
                                                    context = context,
                                                    filePath = attachment.filePath,
                                                    mimeType = attachment.mimeType
                                                ).onFailure { error ->
                                                    Toast.makeText(
                                                        context,
                                                        error.message ?: "Cannot open file",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            onRemove = {
                                                viewModel.removeAttachment(attachment.id)
                                            }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
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

        // Bottom Action Dock: Cancel | Save / Update Pill (Visible when Emoji Keyboard is closed)
        if (!showEmojiKeyboard) {
            BottomCancelSaveDock(
                onCancel = {
                    if (hasModifications && state.editingActivityId == null) {
                        showDiscardSheet = true
                    } else {
                        onDismiss()
                    }
                },
                onSave = { viewModel.saveEntry() },
                isSaveEnabled = state.title.isNotBlank() && !state.isSaving,
                saveButtonText = if (state.editingActivityId != null) "Update" else "Save",
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
                    viewModel.selectEmoji(emoji)
                    showEmojiKeyboard = false
                },
                onClose = { showEmojiKeyboard = false }
            )
        }

        // Discard Changes Modal Scrim
        AnimatedVisibility(
            visible = showDiscardSheet,
            enter = fadeIn(tween(180)),
            exit = fadeOut(tween(140)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { showDiscardSheet = false }
                    )
            )
        }

        // Discard Changes Bottom Sheet Card
        AnimatedVisibility(
            visible = showDiscardSheet,
            enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(220)) + fadeIn(tween(180)),
            exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(180)) + fadeOut(tween(140)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Intercept click so scrim doesn't dismiss */ }
                    ),
                shape = RoundedCornerShape(20.dp),
                color = if (colors.isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                border = BorderStroke(1.dp, if (colors.isDark) Color(0xFF38383A) else Color(0xFFD0D0D0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp, bottom = 18.dp, start = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = "Save your changes or discard them?",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Cancel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { showDiscardSheet = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(18.dp)
                                .background(if (colors.isDark) Color(0xFF48484A) else Color(0xFFD0D0D0))
                        )

                        // Discard
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showDiscardSheet = false
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Discard",
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(18.dp)
                                .background(if (colors.isDark) Color(0xFF48484A) else Color(0xFFD0D0D0))
                        )

                        // Save
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    showDiscardSheet = false
                                    viewModel.saveEntry()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Save",
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
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
            // Category Tabs Header Row with Done checkmark
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161616))
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(categoryTabs) { index, (_, icon) ->
                        val isSelected = index == selectedCategoryIndex
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { selectedCategoryIndex = index }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            when (index) {
                                0 -> {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = "Recent History",
                                        tint = if (isSelected) Color(0xFF3897F0) else colors.textSecondary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                categoryTabs.lastIndex -> {
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

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onClose
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Done",
                        tint = Color(0xFF3897F0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            SectionDivider()

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
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { onEmojiSelected(emoji) },
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
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onEmojiSelected(emoji) },
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
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
        color = if (colors.isDark) Color(0xFF1E1E22) else colors.cardBackground,
        border = BorderStroke(1.dp, if (colors.isDark) Color(0xFF323238) else colors.borderStroke)
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
            SegmentItem(
                label = "Notes",
                isSelected = selectedType == EntryType.NOTE,
                onClick = { onSelectType(EntryType.NOTE) }
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) (if (colors.isDark) Color(0xFF282828) else Color(0xFFE2E2E6)) else Color.Transparent,
        border = if (isSelected) BorderStroke(1.dp, colors.textPrimary) else null
    ) {
        Text(
            text = label,
            color = if (isSelected) colors.textPrimary else colors.textSecondary,
            fontFamily = FontFamily.SansSerif,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun DateTimeRow(
    isReminder: Boolean,
    isAllDay: Boolean,
    startMillis: Long,
    endMillis: Long,
    activePickerField: ActivePickerField,
    onDateFieldClick: (ActivePickerField) -> Unit,
    onTimeFieldClick: (ActivePickerField) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEE, d MMM", Locale.ENGLISH) }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.ENGLISH) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                SelectablePill(
                    text = dateFormat.format(Date(startMillis)),
                    isActive = activePickerField == ActivePickerField.START_DATE,
                    onClick = { onDateFieldClick(ActivePickerField.START_DATE) }
                )
                if (!isAllDay) {
                    Spacer(modifier = Modifier.width(8.dp))
                    SelectablePill(
                        text = timeFormat.format(Date(startMillis)).lowercase(Locale.ENGLISH),
                        isActive = activePickerField == ActivePickerField.START_TIME,
                        onClick = { onTimeFieldClick(ActivePickerField.START_TIME) }
                    )
                }
            }
        } else {
            // Activity: Start -> End 2-column range matching reference
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SelectablePill(
                    text = dateFormat.format(Date(startMillis)),
                    isActive = activePickerField == ActivePickerField.START_DATE,
                    onClick = { onDateFieldClick(ActivePickerField.START_DATE) }
                )
                if (!isAllDay) {
                    Spacer(modifier = Modifier.height(4.dp))
                    SelectablePill(
                        text = timeFormat.format(Date(startMillis)).lowercase(Locale.ENGLISH),
                        isActive = activePickerField == ActivePickerField.START_TIME,
                        onClick = { onTimeFieldClick(ActivePickerField.START_TIME) }
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "to",
                tint = LocalUtilitarianColors.current.textSecondary,
                modifier = Modifier
                    .size(16.dp)
                    .padding(horizontal = 2.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SelectablePill(
                    text = dateFormat.format(Date(endMillis)),
                    isActive = activePickerField == ActivePickerField.END_DATE,
                    onClick = { onDateFieldClick(ActivePickerField.END_DATE) }
                )
                if (!isAllDay) {
                    Spacer(modifier = Modifier.height(4.dp))
                    SelectablePill(
                        text = timeFormat.format(Date(endMillis)).lowercase(Locale.ENGLISH),
                        isActive = activePickerField == ActivePickerField.END_TIME,
                        onClick = { onTimeFieldClick(ActivePickerField.END_TIME) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectablePill(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) Color(0xFF282828) else Color.Transparent,
        border = if (isActive) BorderStroke(1.dp, colors.textPrimary) else null,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            color = colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun InlineCalendarDatePicker(
    selectedMillis: Long,
    displayedYear: Int,
    displayedMonth: Int,
    isRollerMode: Boolean,
    slideDirection: Int,
    onToggleRollerMode: () -> Unit,
    onNavigateMonth: (Int) -> Unit,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val colors = LocalUtilitarianColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Navigation Header
        if (!isRollerMode) {
            // Mode A Header: < Month Year ▼ > (Centered)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigateMonth(-1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronLeft,
                        contentDescription = "Previous Month",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleRollerMode
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val monthTitle = remember(displayedYear, displayedMonth) {
                        val cal = Calendar.getInstance().apply {
                            clear()
                            set(displayedYear, displayedMonth, 1)
                        }
                        SimpleDateFormat("MMMM yyyy", Locale.ENGLISH).format(cal.time)
                    }
                    Text(
                        text = monthTitle,
                        color = colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Canvas(modifier = Modifier.size(9.dp)) {
                        val path = Path().apply {
                            moveTo(0f, size.height * 0.25f)
                            lineTo(size.width, size.height * 0.25f)
                            lineTo(size.width / 2f, size.height * 0.85f)
                            close()
                        }
                        drawPath(path, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigateMonth(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = "Next Month",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day Headers Row: SAT SUN MON TUE WED THU FRI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val dayHeaders = listOf(
                    "SAT" to Color(0xFF3897F0),
                    "SUN" to Color(0xFFEF5350),
                    "MON" to Color(0xFF8E8E93),
                    "TUE" to Color(0xFF8E8E93),
                    "WED" to Color(0xFF8E8E93),
                    "THU" to Color(0xFF8E8E93),
                    "FRI" to Color(0xFF8E8E93)
                )
                dayHeaders.forEach { (name, color) ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name,
                            color = color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Monthly 42-Day Grid with Horizontal Slide Animation
            AnimatedContent(
                targetState = Pair(displayedYear, displayedMonth),
                transitionSpec = {
                    if (slideDirection > 0) {
                        slideInHorizontally(animationSpec = tween(220)) { width -> width } + fadeIn(animationSpec = tween(220)) togetherWith
                                slideOutHorizontally(animationSpec = tween(220)) { width -> -width } + fadeOut(animationSpec = tween(220))
                    } else {
                        slideInHorizontally(animationSpec = tween(220)) { width -> -width } + fadeIn(animationSpec = tween(220)) togetherWith
                                slideOutHorizontally(animationSpec = tween(220)) { width -> width } + fadeOut(animationSpec = tween(220))
                    }
                },
                label = "CalendarMonthTransition"
            ) { (targetYear, targetMonth) ->
                val days = remember(targetYear, targetMonth) {
                    generate42Days(targetYear, targetMonth)
                }
                val selCal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                val selYear = selCal.get(Calendar.YEAR)
                val selMonth = selCal.get(Calendar.MONTH)
                val selDay = selCal.get(Calendar.DAY_OF_MONTH)

                val todayCal = remember { Calendar.getInstance() }
                val todayYear = todayCal.get(Calendar.YEAR)
                val todayMonth = todayCal.get(Calendar.MONTH)
                val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    for (week in 0 until 6) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (col in 0 until 7) {
                                val item = days[week * 7 + col]
                                val isSelected = item.year == selYear && item.month == selMonth && item.day == selDay
                                val isToday = item.year == todayYear && item.month == todayMonth && item.day == todayDay

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            onDateSelected(item.year, item.month, item.day)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF3897F0)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${item.day}",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .border(1.5.dp, Color(0xFF3897F0), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${item.day}",
                                                color = Color(0xFF3897F0),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else {
                                        val textColor = when {
                                            !item.isCurrentMonth -> {
                                                when {
                                                    item.isSaturday -> Color(0xFF3897F0).copy(alpha = 0.35f)
                                                    item.isSunday -> Color(0xFFEF5350).copy(alpha = 0.35f)
                                                    else -> Color(0xFF4A4A4A)
                                                }
                                            }
                                            item.isSaturday -> Color(0xFF3897F0)
                                            item.isSunday -> Color(0xFFEF5350)
                                            else -> Color.White
                                        }
                                        Text(
                                            text = "${item.day}",
                                            color = textColor,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Mode B Header: Centered Month Year ▲
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onToggleRollerMode
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val selCal = Calendar.getInstance().apply { timeInMillis = selectedMillis }
                    val rollerMonth = SimpleDateFormat("MMMM", Locale.ENGLISH).format(selCal.time)
                    val rollerYear = selCal.get(Calendar.YEAR)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$rollerMonth $rollerYear",
                            color = colors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Canvas(modifier = Modifier.size(9.dp)) {
                            val path = Path().apply {
                                moveTo(size.width / 2f, size.height * 0.15f)
                                lineTo(size.width, size.height * 0.85f)
                                lineTo(0f, size.height * 0.85f)
                                close()
                            }
                            drawPath(path, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3-Column Native Smooth Snapping Roller Wheel Picker
            CalendarRollerPicker(
                selectedMillis = selectedMillis,
                onDateSelected = onDateSelected
            )
        }
    }
}

@Composable
private fun CalendarRollerPicker(
    selectedMillis: Long,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val cal = remember(selectedMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedMillis }
    }
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH)
    val currentDay = cal.get(Calendar.DAY_OF_MONTH)

    val maxDays = remember(currentYear, currentMonth) {
        val temp = Calendar.getInstance().apply {
            clear()
            set(currentYear, currentMonth, 1)
        }
        temp.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val daysList = remember(maxDays) {
        (1..maxDays).map { "$it" }
    }

    val monthNames = remember {
        listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEPT", "OCT", "NOV", "DEC")
    }

    val startYear = 1970
    val endYear = 2050
    val yearsList = remember {
        (startYear..endYear).map { "$it" }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Column 1: Day (1..maxDays) - Circular loop
        WheelPickerColumn(
            modifier = Modifier.weight(1f),
            items = daysList,
            selectedIndex = (currentDay - 1).coerceIn(0, daysList.lastIndex),
            isCyclic = true,
            onItemSelected = { dayIndex ->
                val newDay = dayIndex + 1
                onDateSelected(currentYear, currentMonth, newDay)
            }
        )

        // Column 2: Month (JAN..DEC) - Circular loop
        WheelPickerColumn(
            modifier = Modifier.weight(1.3f),
            items = monthNames,
            selectedIndex = currentMonth.coerceIn(0, monthNames.lastIndex),
            isCyclic = true,
            onItemSelected = { monthIndex ->
                val newMaxDays = Calendar.getInstance().apply {
                    clear()
                    set(currentYear, monthIndex, 1)
                }.getActualMaximum(Calendar.DAY_OF_MONTH)
                val safeDay = currentDay.coerceAtMost(newMaxDays)
                onDateSelected(currentYear, monthIndex, safeDay)
            }
        )

        // Column 3: Year (1970..2050) - Bounded
        WheelPickerColumn(
            modifier = Modifier.weight(1.3f),
            items = yearsList,
            selectedIndex = (currentYear - startYear).coerceIn(0, yearsList.lastIndex),
            isCyclic = false,
            onItemSelected = { yearIndex ->
                val newYear = startYear + yearIndex
                val newMaxDays = Calendar.getInstance().apply {
                    clear()
                    set(newYear, currentMonth, 1)
                }.getActualMaximum(Calendar.DAY_OF_MONTH)
                val safeDay = currentDay.coerceAtMost(newMaxDays)
                onDateSelected(newYear, currentMonth, safeDay)
            }
        )
    }
}

@Composable
private fun WheelPickerColumn(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    isCyclic: Boolean = false,
    onItemSelected: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = 48.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    val cycleMultiplier = 10_000
    val totalItemCount = if (isCyclic && items.isNotEmpty()) items.size * cycleMultiplier else items.size

    val initialIndex = remember(items.size, isCyclic) {
        if (isCyclic && items.isNotEmpty()) {
            (cycleMultiplier / 2) * items.size + selectedIndex.coerceIn(0, items.lastIndex)
        } else {
            selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        }
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )

    // Handle dynamic items.size change while remaining in the same cycle
    LaunchedEffect(items.size) {
        if (isCyclic && items.isNotEmpty()) {
            val centerCycle = (listState.firstVisibleItemIndex / items.size).coerceAtLeast(1)
            val safeIndex = selectedIndex.coerceIn(0, items.lastIndex)
            val newTarget = centerCycle * items.size + safeIndex
            listState.scrollToItem(newTarget)
        }
    }

    // Centered raw item in the viewport
    val currentRawIndex by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val index = if (offset > itemHeightPx / 2) firstVisible + 1 else firstVisible
            index.coerceIn(0, (totalItemCount - 1).coerceAtLeast(0))
        }
    }

    // Normalized index in 0 until items.size
    val currentNormalizedIndex by remember {
        derivedStateOf {
            if (isCyclic && items.isNotEmpty()) {
                (currentRawIndex % items.size + items.size) % items.size
            } else {
                currentRawIndex
            }
        }
    }

    // Sync external selectedIndex changes
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            if (currentNormalizedIndex != selectedIndex && selectedIndex in items.indices) {
                if (isCyclic && items.isNotEmpty()) {
                    val currentCycle = currentRawIndex / items.size
                    var targetIndex = currentCycle * items.size + selectedIndex
                    val diff = targetIndex - currentRawIndex
                    if (diff > items.size / 2) {
                        targetIndex -= items.size
                    } else if (diff < -items.size / 2) {
                        targetIndex += items.size
                    }
                    listState.scrollToItem(targetIndex.coerceIn(0, totalItemCount - 1))
                } else {
                    listState.scrollToItem(selectedIndex)
                }
            }
        }
    }

    // Trigger onItemSelected when scroll settles on a new center item
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            if (currentNormalizedIndex != selectedIndex && currentNormalizedIndex in items.indices) {
                onItemSelected(currentNormalizedIndex)
            }
        }
    }

    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Box(
        modifier = modifier.height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                count = totalItemCount,
                key = if (isCyclic) { index -> index } else null
            ) { globalIndex ->
                val normalizedIndex = if (isCyclic && items.isNotEmpty()) {
                    (globalIndex % items.size + items.size) % items.size
                } else {
                    globalIndex
                }
                val text = items[normalizedIndex]
                val isSelected = globalIndex == currentRawIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                listState.animateScrollToItem(globalIndex)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        color = if (isSelected) Color.White else Color(0xFF555555),
                        fontSize = if (isSelected) 28.sp else 20.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineWheelTimePicker(
    selectedMillis: Long,
    onTimeSelected: (hour12: Int, minute: Int, isPm: Boolean) -> Unit
) {
    val cal = remember(selectedMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedMillis }
    }
    val currentHour24 = cal.get(Calendar.HOUR_OF_DAY)
    val currentMinuteRaw = cal.get(Calendar.MINUTE)
    val isPm = currentHour24 >= 12
    val currentHour12 = when {
        currentHour24 == 0 -> 12
        currentHour24 > 12 -> currentHour24 - 12
        else -> currentHour24
    }

    val hoursList = remember { (1..12).map { "$it" } }
    val minutesList = remember { (0..55 step 5).map { String.format(Locale.ENGLISH, "%02d", it) } }
    val amPmList = remember { listOf("am", "pm") }

    val currentHourIndex = (currentHour12 - 1).coerceIn(0, 11)
    val currentMinuteIndex = ((currentMinuteRaw + 2) / 5).coerceIn(0, 11)
    val currentAmPmIndex = if (isPm) 1 else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Column 1: Hour (1..12) - Circular loop
        WheelPickerColumn(
            modifier = Modifier.width(68.dp),
            items = hoursList,
            selectedIndex = currentHourIndex,
            isCyclic = true,
            onItemSelected = { hourIdx ->
                val newHour12 = hourIdx + 1
                val minVal = minutesList[currentMinuteIndex].toInt()
                onTimeSelected(newHour12, minVal, isPm)
            }
        )

        // Colon separator
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ":",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Column 2: Minute (00..55 step 5) - Circular loop
        WheelPickerColumn(
            modifier = Modifier.width(68.dp),
            items = minutesList,
            selectedIndex = currentMinuteIndex,
            isCyclic = true,
            onItemSelected = { minIdx ->
                val newMinute = minutesList[minIdx].toInt()
                onTimeSelected(currentHour12, newMinute, isPm)
            }
        )

        Spacer(modifier = Modifier.width(24.dp))

        // Column 3: AM / PM - Bounded 2-state toggle
        WheelPickerColumn(
            modifier = Modifier.width(68.dp),
            items = amPmList,
            selectedIndex = currentAmPmIndex,
            isCyclic = false,
            onItemSelected = { amPmIdx ->
                val newIsPm = amPmIdx == 1
                val minVal = minutesList[currentMinuteIndex].toInt()
                onTimeSelected(currentHour12, minVal, newIsPm)
            }
        )
    }
}

private data class CalendarDay(
    val year: Int,
    val month: Int,
    val day: Int,
    val isCurrentMonth: Boolean,
    val isSaturday: Boolean,
    val isSunday: Boolean
)

private fun generate42Days(year: Int, month: Int): List<CalendarDay> {
    val cal = Calendar.getInstance().apply {
        clear()
        set(year, month, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysBefore = firstDayOfWeek % 7

    cal.add(Calendar.DAY_OF_MONTH, -daysBefore)
    val days = ArrayList<CalendarDay>(42)
    for (i in 0 until 42) {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH)
        val d = cal.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        days.add(
            CalendarDay(
                year = y,
                month = m,
                day = d,
                isCurrentMonth = m == month,
                isSaturday = dayOfWeek == Calendar.SATURDAY,
                isSunday = dayOfWeek == Calendar.SUNDAY
            )
        )
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}

@Composable
private fun SectionDivider(modifier: Modifier = Modifier) {
    val colors = LocalUtilitarianColors.current
    HorizontalDivider(
        modifier = modifier,
        color = if (colors.isDark) Color(0xFF38383A) else Color(0xFFD0D0D0),
        thickness = 1.dp
    )
}

@Composable
private fun EmojiPopButton(
    emoji: String,
    isOpen: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    var displayedEmoji by remember { mutableStateOf(emoji) }
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(emoji) {
        if (emoji != displayedEmoji) {
            if (displayedEmoji.isNotBlank()) {
                // Step 1: Distinct shrink down into a tiny dot
                coroutineScope {
                    launch {
                        alpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 110, easing = FastOutLinearInEasing)
                        )
                    }
                    scale.animateTo(
                        targetValue = 0.15f,
                        animationSpec = tween(durationMillis = 110, easing = FastOutLinearInEasing)
                    )
                }
            } else {
                scale.snapTo(0.15f)
                alpha.snapTo(0f)
            }

            displayedEmoji = emoji

            // Step 2: Pop out new emoji from the dot with noticeable overshoot bounce
            coroutineScope {
                launch {
                    alpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 140)
                    )
                }
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.42f, // Tactile overshoot bounce (~1.32x scale)
                        stiffness = Spring.StiffnessMediumLow // Clearly visible settling time (~260ms)
                    )
                )
            }
        }
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            },
            contentAlignment = Alignment.Center
        ) {
            if (displayedEmoji.isNotBlank()) {
                Text(
                    text = displayedEmoji,
                    fontSize = 18.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.SentimentSatisfied,
                    contentDescription = "Emoji",
                    tint = if (isOpen) colors.accent else colors.textSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun FormOptionRow(
    icon: ImageVector? = null,
    label: String,
    isPlaceholder: Boolean = false,
    fontSize: TextUnit = 15.sp,
    textColor: Color? = null,
    leadingDotColor: Color? = null,
    iconTint: Color? = null,
    customLeadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
                keyboardController?.hide()
                onClick()
            }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (customLeadingIcon != null) {
                customLeadingIcon()
            } else if (icon != null) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint ?: colors.textSecondary,
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
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            color = textColor ?: if (isPlaceholder) colors.textSecondary else colors.textPrimary,
            fontSize = fontSize,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun BottomCancelSaveDock(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    isSaveEnabled: Boolean,
    saveButtonText: String = "Save",
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
            color = if (colors.isDark) Color(0xFF1E1E22) else colors.cardBackground,
            border = BorderStroke(1.dp, if (colors.isDark) Color(0xFF323238) else colors.borderStroke)
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
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCancel
                        )
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

                // Save / Update
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = isSaveEnabled,
                            onClick = onSave
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = saveButtonText,
                        color = if (isSaveEnabled) colors.accent else colors.textDisabled,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

private fun formatRecurrenceLabel(state: CreateEntryState): String {
    if (state.recurrenceMode == RecurrenceMode.NONE) return "Don't repeat"

    val base = when (state.recurrenceMode) {
        RecurrenceMode.NONE -> "Don't repeat"
        RecurrenceMode.MINUTELY -> if (state.recurrenceInterval == 1) "Every minute" else "Every ${state.recurrenceInterval} minutes"
        RecurrenceMode.HOURLY -> if (state.recurrenceInterval == 1) "Hourly" else "Every ${state.recurrenceInterval} hours"
        RecurrenceMode.DAILY -> if (state.recurrenceInterval == 1) "Daily" else "Every ${state.recurrenceInterval} days"
        RecurrenceMode.WEEKDAYS -> "Every weekday"
        RecurrenceMode.WEEKLY -> {
            val dayNames = state.recurrenceDaysOfWeek.sorted().map { day ->
                when (day) {
                    Calendar.SUNDAY -> "Sun"
                    Calendar.MONDAY -> "Mon"
                    Calendar.TUESDAY -> "Tue"
                    Calendar.WEDNESDAY -> "Wed"
                    Calendar.THURSDAY -> "Thu"
                    Calendar.FRIDAY -> "Fri"
                    Calendar.SATURDAY -> "Sat"
                    else -> ""
                }
            }.filter { it.isNotEmpty() }

            val suffix = if (dayNames.isNotEmpty()) " (${dayNames.joinToString(", ")})" else ""
            if (state.recurrenceInterval == 1) "Weekly$suffix" else "Every ${state.recurrenceInterval} weeks$suffix"
        }
        RecurrenceMode.MONTHLY -> {
            val cal = Calendar.getInstance().apply { timeInMillis = state.startMillis }
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            when (state.monthlyRepeatType) {
                com.shejan.acontinue.domain.model.MonthlyRepeatType.SAME_DAY_OF_MONTH -> {
                    if (state.recurrenceInterval == 1) "Monthly (${dayOfMonth}th)" else "Every ${state.recurrenceInterval} months"
                }
                com.shejan.acontinue.domain.model.MonthlyRepeatType.DAY_OF_WEEK_OF_MONTH -> {
                    val nth = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH)
                    val dayName = when (cal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.SUNDAY -> "Sun"
                        Calendar.MONDAY -> "Mon"
                        Calendar.TUESDAY -> "Tue"
                        Calendar.WEDNESDAY -> "Wed"
                        Calendar.THURSDAY -> "Thu"
                        Calendar.FRIDAY -> "Fri"
                        Calendar.SATURDAY -> "Sat"
                        else -> "Day"
                    }
                    "Monthly (${nth}th $dayName)"
                }
                com.shejan.acontinue.domain.model.MonthlyRepeatType.SPECIFIC_DATES -> {
                    val daysStr = state.monthlySelectedDays.sorted().joinToString(",")
                    "Monthly ($daysStr)"
                }
            }
        }
        RecurrenceMode.YEARLY -> {
            val cal = Calendar.getInstance().apply { timeInMillis = state.startMillis }
            val monthStr = when (cal.get(Calendar.MONTH)) {
                0 -> "Jan"; 1 -> "Feb"; 2 -> "Mar"; 3 -> "Apr"; 4 -> "May"; 5 -> "Jun"; 6 -> "Jul"; 7 -> "Aug"; 8 -> "Sept"; 9 -> "Oct"; 10 -> "Nov"; 11 -> "Dec"; else -> ""
            }
            if (state.recurrenceInterval == 1) "Yearly ($monthStr)" else "Every ${state.recurrenceInterval} years ($monthStr)"
        }
    }

    val durationSuffix = when (state.recurrenceEndType) {
        com.shejan.acontinue.domain.model.RecurrenceEndType.FOREVER -> ""
        com.shejan.acontinue.domain.model.RecurrenceEndType.COUNT -> " (${state.recurrenceEndCount} times)"
        com.shejan.acontinue.domain.model.RecurrenceEndType.UNTIL_DATE -> {
            val untilCal = Calendar.getInstance().apply { timeInMillis = state.recurrenceEndDateMillis }
            val mStr = when (untilCal.get(Calendar.MONTH)) {
                0 -> "Jan"; 1 -> "Feb"; 2 -> "Mar"; 3 -> "Apr"; 4 -> "May"; 5 -> "Jun"; 6 -> "Jul"; 7 -> "Aug"; 8 -> "Sept"; 9 -> "Oct"; 10 -> "Nov"; 11 -> "Dec"; else -> ""
            }
            " (until $mStr ${untilCal.get(Calendar.DAY_OF_MONTH)})"
        }
    }

    return "$base$durationSuffix"
}

@Composable
private fun AttachmentItemCard(
    attachment: ProcessedAttachment,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    val category = remember(attachment.fileName, attachment.mimeType) {
        AttachmentHelper.getCategory(attachment.fileName, attachment.mimeType)
    }

    val icon = when (category) {
        AttachmentCategory.IMAGE -> Icons.Outlined.Image
        AttachmentCategory.PDF -> Icons.Outlined.PictureAsPdf
        AttachmentCategory.DOCUMENT -> Icons.Outlined.Description
        AttachmentCategory.SPREADSHEET -> Icons.Outlined.TableChart
        AttachmentCategory.TEXT -> Icons.AutoMirrored.Outlined.Article
        AttachmentCategory.OTHER -> Icons.AutoMirrored.Outlined.InsertDriveFile
    }

    Surface(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E20),
        border = BorderStroke(1.dp, colors.borderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = attachment.fileName,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = AttachmentHelper.formatFileSize(attachment.sizeBytes),
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRemove
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove attachment",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ReminderIntensityPopup(
    selectedPriority: ActivityPriority,
    onSelectPriority: (ActivityPriority) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF252528),
        border = BorderStroke(1.dp, Color(0xFF38383A)),
        shadowElevation = 14.dp,
        modifier = Modifier.width(248.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            ActivityPriority.entries.forEach { priority ->
                val isSelected = priority == selectedPriority
                val activeBlue = Color(0xFF3897F0)
                val titleColor = if (isSelected) activeBlue else Color.White
                val subtitleColor = if (isSelected) activeBlue else Color(0xFF8E8E93)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelectPriority(priority) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = priority.reminderTitle,
                            color = titleColor,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = priority.reminderDescription,
                            color = subtitleColor,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 16.sp
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = "Selected",
                            tint = activeBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

