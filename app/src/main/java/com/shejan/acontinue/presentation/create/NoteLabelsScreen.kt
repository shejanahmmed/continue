package com.shejan.acontinue.presentation.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.HighContrastButton
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import com.shejan.acontinue.ui.theme.UtilitarianAccentRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteLabelsScreen(
    allLabels: List<String>,
    selectedLabels: Set<String>,
    onToggleLabel: (String) -> Unit,
    onCreateLabel: (String) -> Unit,
    onBack: () -> Unit,
    onEditLabel: (oldName: String, newName: String) -> Unit = { _, _ -> },
    onDeleteLabel: (name: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val searchFocusRequester = remember { FocusRequester() }

    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var labelForOptions by remember { mutableStateOf<String?>(null) }
    var showEditDialogForLabel by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmForLabel by remember { mutableStateOf<String?>(null) }

    BackHandler {
        if (showCreateDialog) {
            showCreateDialog = false
        } else if (showEditDialogForLabel != null) {
            showEditDialogForLabel = null
        } else if (showDeleteConfirmForLabel != null) {
            showDeleteConfirmForLabel = null
        } else if (labelForOptions != null) {
            labelForOptions = null
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        if (allLabels.isEmpty()) {
            searchFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val trimmedQuery = searchQuery.trim()
    val isExactMatchExisting = remember(trimmedQuery, allLabels) {
        allLabels.any { it.equals(trimmedQuery, ignoreCase = true) }
    }

    val filteredLabels = remember(trimmedQuery, allLabels) {
        if (trimmedQuery.isBlank()) {
            allLabels
        } else {
            allLabels.filter { it.contains(trimmedQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top Navigation Header: Clean typographic header without icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "LABELS",
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            if (selectedLabels.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.borderStroke
                ) {
                    Text(
                        text = "${selectedLabels.size} SELECTED",
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else if (allLabels.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.cardBackground,
                    border = BorderStroke(1.dp, colors.borderStroke)
                ) {
                    Text(
                        text = "${allLabels.size} TOTAL",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search & Quick-Add Input Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colors.cardBackground,
            border = BorderStroke(1.dp, colors.borderStroke)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(searchFocusRequester),
                    textStyle = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(colors.textPrimary),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (trimmedQuery.isNotBlank() && !isExactMatchExisting) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCreateLabel(trimmedQuery)
                                onToggleLabel(trimmedQuery)
                                searchQuery = ""
                                keyboardController?.hide()
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search or add label...",
                                    color = colors.textSecondary.copy(alpha = 0.6f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { searchQuery = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Case A: Query is typed and NOT an exact match of an existing label: Quick-Create Card
            if (trimmedQuery.isNotBlank() && !isExactMatchExisting) {
                item {
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCreateLabel(trimmedQuery)
                            onToggleLabel(trimmedQuery)
                            searchQuery = ""
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = colors.cardBackground,
                        border = BorderStroke(1.dp, colors.borderStroke),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Create \"$trimmedQuery\"",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colors.borderStroke
                            ) {
                                Text(
                                    text = "NEW",
                                    color = colors.textPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Case B: Query is empty and labels exist: Show "+ NEW LABEL" quick action
            if (trimmedQuery.isBlank() && allLabels.isNotEmpty()) {
                item {
                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showCreateDialog = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = colors.cardBackground,
                        border = BorderStroke(1.dp, colors.borderStroke),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "NEW LABEL",
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Case C: Empty state when no labels exist at all
            if (allLabels.isEmpty() && trimmedQuery.isBlank()) {
                item {
                    MonochromeCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Label,
                                contentDescription = null,
                                tint = colors.textSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(40.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "NO LABELS YET",
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Create labels to organize and quickly filter your notes.",
                                color = colors.textSecondary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            HighContrastButton(
                                text = "+ CREATE FIRST LABEL",
                                onClick = { showCreateDialog = true }
                            )
                        }
                    }
                }
            }

            // Case D: Filtered Labels List Card
            if (filteredLabels.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.cardBackground,
                        border = BorderStroke(1.dp, colors.borderStroke),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            filteredLabels.forEachIndexed { index, label ->
                                val isChecked = label in selectedLabels

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onToggleLabel(label)
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                labelForOptions = label
                                            }
                                        )
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom Utilitarian Checkbox
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (isChecked) colors.textPrimary else Color.Transparent)
                                            .border(
                                                1.5.dp,
                                                if (isChecked) colors.textPrimary else colors.textSecondary.copy(alpha = 0.6f),
                                                RoundedCornerShape(4.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChecked) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = colors.background,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    // Label text
                                    Text(
                                        text = label,
                                        color = colors.textPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Explicit Edit Button
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                labelForOptions = label
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Edit label",
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                if (index < filteredLabels.lastIndex) {
                                    HorizontalDivider(
                                        color = colors.borderStroke.copy(alpha = 0.5f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Label Dialog
    if (showCreateDialog) {
        var newLabelName by remember { mutableStateOf("") }
        val dialogFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            dialogFocusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "NEW LABEL",
                    color = colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                BasicTextField(
                    value = newLabelName,
                    onValueChange = { newLabelName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(dialogFocusRequester)
                        .background(colors.background, RoundedCornerShape(8.dp))
                        .border(1.dp, colors.borderStroke, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    textStyle = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(colors.textPrimary),
                    singleLine = true,
                    decorationBox = { inner ->
                        Box {
                            if (newLabelName.isEmpty()) {
                                Text(
                                    text = "Enter label name",
                                    color = colors.textSecondary.copy(alpha = 0.6f),
                                    fontSize = 15.sp
                                )
                            }
                            inner()
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newLabelName.trim()
                        if (trimmed.isNotBlank()) {
                            onCreateLabel(trimmed)
                            onToggleLabel(trimmed)
                        }
                        showCreateDialog = false
                    }
                ) {
                    Text(
                        text = "CREATE",
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(
                        text = "CANCEL",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Label Options Bottom Sheet
    if (labelForOptions != null) {
        val targetLabel = labelForOptions!!
        val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { labelForOptions = null },
            sheetState = optionsSheetState,
            containerColor = Color(0xFF202020),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 28.dp)
                    .navigationBarsPadding()
            ) {
                Text(
                    text = targetLabel,
                    color = colors.textPrimaryMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                optionsSheetState.hide()
                                labelForOptions = null
                                showEditDialogForLabel = targetLabel
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit label",
                        tint = colors.textPrimaryMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Edit label",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                optionsSheetState.hide()
                                labelForOptions = null
                                showDeleteConfirmForLabel = targetLabel
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete label",
                        tint = UtilitarianAccentRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Delete label",
                        color = UtilitarianAccentRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }

    // Edit Label Dialog
    if (showEditDialogForLabel != null) {
        val oldName = showEditDialogForLabel!!
        var editedLabelValue by remember(oldName) {
            mutableStateOf(
                TextFieldValue(
                    text = oldName,
                    selection = TextRange(oldName.length)
                )
            )
        }
        val editFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            editFocusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { showEditDialogForLabel = null },
            title = {
                Text(
                    text = "EDIT LABEL",
                    color = colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                BasicTextField(
                    value = editedLabelValue,
                    onValueChange = { editedLabelValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(editFocusRequester)
                        .background(colors.background, RoundedCornerShape(8.dp))
                        .border(1.dp, colors.borderStroke, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    textStyle = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(colors.textPrimary),
                    singleLine = true,
                    decorationBox = { inner ->
                        Box {
                            if (editedLabelValue.text.isEmpty()) {
                                Text(
                                    text = "Enter label name",
                                    color = colors.textSecondary.copy(alpha = 0.6f),
                                    fontSize = 15.sp
                                )
                            }
                            inner()
                        }
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = editedLabelValue.text.trim()
                        if (trimmed.isNotBlank() && !trimmed.equals(oldName, ignoreCase = true)) {
                            onEditLabel(oldName, trimmed)
                        }
                        showEditDialogForLabel = null
                    }
                ) {
                    Text(
                        text = "SAVE",
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialogForLabel = null }) {
                    Text(
                        text = "CANCEL",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Delete Label Confirmation Dialog
    if (showDeleteConfirmForLabel != null) {
        val targetLabel = showDeleteConfirmForLabel!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmForLabel = null },
            title = {
                Text(
                    text = "DELETE LABEL?",
                    color = colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This label will be removed from all associated notes. Notes will not be deleted.",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLabel(targetLabel)
                        showDeleteConfirmForLabel = null
                    }
                ) {
                    Text(
                        text = "DELETE",
                        color = UtilitarianAccentRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmForLabel = null }) {
                    Text(
                        text = "CANCEL",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
