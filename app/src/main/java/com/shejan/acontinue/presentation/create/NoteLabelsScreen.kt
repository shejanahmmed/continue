package com.shejan.acontinue.presentation.create

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors

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
    ) {
        // Top App Bar: Back button and Search text input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(searchFocusRequester),
                textStyle = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(colors.textPrimary),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Enter label name",
                                color = colors.textSecondary.copy(alpha = 0.6f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Case A: Query is NOT empty and does NOT match an existing label: Show `+ Create "<query>"`
            if (trimmedQuery.isNotBlank() && !isExactMatchExisting) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCreateLabel(trimmedQuery)
                                onToggleLabel(trimmedQuery)
                                searchQuery = ""
                            }
                            .padding(horizontal = 24.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Create label",
                            tint = Color(0xFF76D1FF),
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = "Create \"$trimmedQuery\"",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            // Case B: Query is empty: Always show `+ Create a label`
            if (trimmedQuery.isBlank()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showCreateDialog = true
                            }
                            .padding(horizontal = 24.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Create a label",
                            tint = Color(0xFF76D1FF),
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = "Create a label",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            // Existing labels with Tag Outline icon and Checkbox
            items(filteredLabels, key = { it }) { label ->
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
                        .padding(horizontal = 24.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Label,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    Text(
                        text = label,
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = if (isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                        contentDescription = if (isChecked) "Checked" else "Unchecked",
                        tint = if (isChecked) Color(0xFF76D1FF) else colors.textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

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
                    text = "New label",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                BasicTextField(
                    value = newLabelName,
                    onValueChange = { newLabelName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(dialogFocusRequester)
                        .border(1.dp, colors.borderStroke.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    textStyle = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 16.sp,
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
                                    fontSize = 16.sp
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
                    Text("Create", color = Color(0xFF76D1FF), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = Color(0xFF1E2225),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (labelForOptions != null) {
        val targetLabel = labelForOptions!!
        val optionsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { labelForOptions = null },
            sheetState = optionsSheetState,
            containerColor = Color(0xFF1E2225),
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
                    color = colors.textPrimary,
                    fontSize = 17.sp,
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
                        .padding(horizontal = 24.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit label",
                        tint = colors.textPrimary,
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
                        .padding(horizontal = 24.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete label",
                        tint = Color(0xFFD71921),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "Delete label",
                        color = Color(0xFFD71921),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }

    if (showEditDialogForLabel != null) {
        val oldName = showEditDialogForLabel!!
        var editedLabelName by remember(oldName) { mutableStateOf(oldName) }
        val editFocusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            editFocusRequester.requestFocus()
        }

        AlertDialog(
            onDismissRequest = { showEditDialogForLabel = null },
            title = {
                Text(
                    text = "Edit label",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                BasicTextField(
                    value = editedLabelName,
                    onValueChange = { editedLabelName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(editFocusRequester)
                        .border(1.dp, colors.borderStroke.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    textStyle = TextStyle(
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    cursorBrush = SolidColor(colors.textPrimary),
                    singleLine = true,
                    decorationBox = { inner ->
                        Box {
                            if (editedLabelName.isEmpty()) {
                                Text(
                                    text = "Enter label name",
                                    color = colors.textSecondary.copy(alpha = 0.6f),
                                    fontSize = 16.sp
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
                        val trimmed = editedLabelName.trim()
                        if (trimmed.isNotBlank() && !trimmed.equals(oldName, ignoreCase = true)) {
                            onEditLabel(oldName, trimmed)
                        }
                        showEditDialogForLabel = null
                    }
                ) {
                    Text("Save", color = Color(0xFF76D1FF), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialogForLabel = null }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = Color(0xFF1E2225),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteConfirmForLabel != null) {
        val targetLabel = showDeleteConfirmForLabel!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmForLabel = null },
            title = {
                Text(
                    text = "Delete label?",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "We will delete this label and remove it from all of your notes. Your notes won't be deleted.",
                    color = colors.textSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLabel(targetLabel)
                        showDeleteConfirmForLabel = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFD71921), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmForLabel = null }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            },
            containerColor = Color(0xFF1E2225),
            shape = RoundedCornerShape(16.dp)
        )
    }
}
