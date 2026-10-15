package com.shejan.acontinue.presentation.create

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatColorText
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import kotlinx.coroutines.launch

val NOTE_COLORS = listOf(
    0xFF121212L, // Default
    0xFF5C1324L, // Crimson / Burgundy
    0xFF6E2B1AL, // Rust / Terracotta
    0xFF6B4407L, // Amber / Warm Ochre
    0xFF1C4635L, // Forest Green
    0xFF144A56L, // Deep Teal
    0xFF1E435EL, // Slate Blue / Cyan
    0xFF283556L, // Navy / Midnight
    0xFF3E2055L, // Deep Purple / Violet
    0xFF542544L, // Mauve / Wine
    0xFF403B31L, // Taupe / Olive
    0xFF252528L  // Charcoal / Carbon
)

data class ChecklistItem(
    val id: Long = System.currentTimeMillis() + (0..999).random(),
    val text: String,
    val isChecked: Boolean = false
)

private data class NoteMenuOption(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreen(
    onSaveNote: (title: String, content: String, colorHex: Long) -> Unit,
    onDiscard: () -> Unit,
    initialUpdatedAtMillis: Long? = null,
    onDeleteNote: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    val context = LocalContext.current
    val view = LocalView.current
    val activity = remember(context) { context.findActivity() }
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val titleFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }
    var isTitleFocused by remember { mutableStateOf(false) }
    var isContentFocused by remember { mutableStateOf(false) }

    val openedAtMillis = remember { initialUpdatedAtMillis ?: System.currentTimeMillis() }
    var lastEditedMillis by remember { mutableLongStateOf(openedAtMillis) }
    var hasUserModified by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var noteTextState by remember { mutableStateOf(TextFieldValue("")) }
    var formatState by remember { mutableStateOf(NoteFormatState()) }
    var selectedColorHex by remember { mutableLongStateOf(0xFF121212L) }
    var isChecklistMode by remember { mutableStateOf(false) }
    var checklistItems by remember { mutableStateOf(listOf<ChecklistItem>()) }
    var attachedImageUris by remember { mutableStateOf(listOf<Uri>()) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var showFormattingBar by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }

    val initialSnapshot = remember {
        NoteEditorSnapshot(
            title = "",
            noteTextState = TextFieldValue(""),
            formatState = NoteFormatState(),
            checklistItems = emptyList(),
            isChecklistMode = false,
            attachedImageUris = emptyList(),
            selectedColorHex = 0xFF121212L
        )
    }
    val undoManager = remember { NoteUndoManager(initialSnapshot = initialSnapshot) }

    fun takeSnapshot(
        newTitle: String = title,
        newNoteTextState: TextFieldValue = noteTextState,
        newFormatState: NoteFormatState = formatState,
        newChecklistItems: List<ChecklistItem> = checklistItems,
        newIsChecklistMode: Boolean = isChecklistMode,
        newAttachedImageUris: List<Uri> = attachedImageUris,
        newSelectedColorHex: Long = selectedColorHex
    ): NoteEditorSnapshot {
        hasUserModified = true
        lastEditedMillis = System.currentTimeMillis()
        return NoteEditorSnapshot(
            title = newTitle,
            noteTextState = newNoteTextState,
            formatState = newFormatState,
            checklistItems = newChecklistItems,
            isChecklistMode = newIsChecklistMode,
            attachedImageUris = newAttachedImageUris,
            selectedColorHex = newSelectedColorHex
        )
    }

    val restoreSnapshot: (NoteEditorSnapshot) -> Unit = { restored ->
        hasUserModified = true
        lastEditedMillis = System.currentTimeMillis()
        title = restored.title
        noteTextState = restored.noteTextState
        formatState = restored.formatState
        checklistItems = restored.checklistItems
        isChecklistMode = restored.isChecklistMode
        attachedImageUris = restored.attachedImageUris
        selectedColorHex = restored.selectedColorHex
    }

    LaunchedEffect(isTitleFocused) {
        if (isTitleFocused) {
            showFormattingBar = false
        }
    }

    DisposableEffect(activity, selectedColorHex) {
        val window = activity?.window
        if (window != null) {
            val previousStatusBarColor = window.statusBarColor
            val previousNavColor = window.navigationBarColor
            val insetsController = WindowCompat.getInsetsController(window, view)
            val previousLightStatus = insetsController.isAppearanceLightStatusBars
            val previousLightNav = insetsController.isAppearanceLightNavigationBars

            val targetArgb = Color(selectedColorHex).toArgb()
            window.statusBarColor = targetArgb
            window.navigationBarColor = targetArgb
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false

            onDispose {
                window.statusBarColor = previousStatusBarColor
                window.navigationBarColor = previousNavColor
                insetsController.isAppearanceLightStatusBars = previousLightStatus
                insetsController.isAppearanceLightNavigationBars = previousLightNav
            }
        } else {
            onDispose {}
        }
    }

    SideEffect {
        activity?.window?.let { window ->
            val targetArgb = Color(selectedColorHex).toArgb()
            window.statusBarColor = targetArgb
            window.navigationBarColor = targetArgb
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val updated = attachedImageUris + uri
            attachedImageUris = updated
            undoManager.recordAction(takeSnapshot(newAttachedImageUris = updated))
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            Toast.makeText(context, "Photo captured", Toast.LENGTH_SHORT).show()
        }
    }

    val buildFinalContent: () -> String = {
        val stringBuilder = StringBuilder()
        if (isChecklistMode) {
            checklistItems.forEach { item ->
                val box = if (item.isChecked) "[x]" else "[ ]"
                stringBuilder.append("$box ${item.text}\n")
            }
        } else {
            stringBuilder.append(NoteTextFormatter.serializeToMarkdown(noteTextState.text, formatState))
        }
        stringBuilder.toString().trim()
    }

    val handleExit = {
        val finalContent = buildFinalContent()
        if (title.isNotBlank() || finalContent.isNotBlank() || attachedImageUris.isNotEmpty()) {
            onSaveNote(title, finalContent, selectedColorHex)
        } else {
            onDiscard()
        }
    }

    BackHandler {
        if (showColorSheet) {
            showColorSheet = false
        } else if (showBottomSheet) {
            showBottomSheet = false
        } else if (showMoreSheet) {
            showMoreSheet = false
        } else if (showFormattingBar) {
            showFormattingBar = false
        } else {
            handleExit()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(selectedColorHex))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
        ) {
        // Top Bar: Back button on the left (pin & archive intentionally excluded)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = handleExit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Body: Title, Attached Images, and Note/Checklist Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (!isChecklistMode) {
                        contentFocusRequester.requestFocus()
                        noteTextState = noteTextState.copy(selection = TextRange(noteTextState.text.length))
                        keyboardController?.show()
                    }
                }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Title Input
            BasicTextField(
                value = title,
                onValueChange = { newTitle ->
                    title = newTitle
                    undoManager.recordTyping(takeSnapshot(newTitle = newTitle))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
                    .onFocusChanged { isTitleFocused = it.isFocused },
                textStyle = TextStyle(
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(colors.textPrimary),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                text = "Title",
                                color = colors.textSecondary.copy(alpha = 0.6f),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Attached Images Row (if any)
            if (attachedImageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(attachedImageUris) { uri ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cardBackground)
                                .border(1.dp, colors.borderStroke, RoundedCornerShape(8.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable {
                                    val updated = attachedImageUris - uri
                                    attachedImageUris = updated
                                    undoManager.recordAction(takeSnapshot(newAttachedImageUris = updated))
                                },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Remove image",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note Content or Checklist Mode
            if (!isChecklistMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 500.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            contentFocusRequester.requestFocus()
                            noteTextState = noteTextState.copy(selection = TextRange(noteTextState.text.length))
                            keyboardController?.show()
                        }
                ) {
                    BasicTextField(
                        value = noteTextState,
                        onValueChange = { newTfv ->
                            val newFormatState = NoteTextFormatter.onTextChange(
                                oldText = noteTextState.text,
                                newText = newTfv.text,
                                selection = newTfv.selection,
                                state = formatState
                            )
                            formatState = newFormatState
                            noteTextState = newTfv
                            undoManager.recordTyping(takeSnapshot(newNoteTextState = newTfv, newFormatState = newFormatState))
                        },
                        visualTransformation = remember(formatState, colors.textPrimary) {
                            NoteVisualTransformation(formatState, colors.textPrimary)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(contentFocusRequester)
                            .onFocusChanged { isContentFocused = it.isFocused },
                        textStyle = TextStyle(
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(colors.textPrimary),
                        decorationBox = { innerTextField ->
                            Box {
                                if (noteTextState.text.isEmpty()) {
                                    Text(
                                        text = "Note",
                                        color = colors.textSecondary.copy(alpha = 0.6f),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            } else {
                // Interactive Checklists
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    checklistItems.forEachIndexed { index, item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (item.isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (item.isChecked) Color(0xFF3897F0) else colors.textSecondary,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable {
                                        val updated = checklistItems.toMutableList().also {
                                            it[index] = item.copy(isChecked = !item.isChecked)
                                        }
                                        checklistItems = updated
                                        undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                    }
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            BasicTextField(
                                value = item.text,
                                onValueChange = { newText ->
                                    val updated = checklistItems.toMutableList().also {
                                        it[index] = item.copy(text = newText)
                                    }
                                    checklistItems = updated
                                    undoManager.recordTyping(takeSnapshot(newChecklistItems = updated))
                                },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    color = if (item.isChecked) colors.textDisabled else colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(colors.textPrimary),
                                decorationBox = { inner ->
                                    Box {
                                        if (item.text.isEmpty()) {
                                            Text(
                                                text = "List item",
                                                color = colors.textSecondary.copy(alpha = 0.6f),
                                                fontSize = 16.sp
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val updated = checklistItems.filterIndexed { i, _ -> i != index }
                                        checklistItems = updated
                                        undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Remove item",
                                    tint = colors.textSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Add item row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val updated = checklistItems + ChecklistItem(text = "")
                                checklistItems = updated
                                undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                            }
                            .padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddBox,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "List item",
                            color = colors.textSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        val isFormattingEnabled = !isTitleFocused && !isChecklistMode

        if (showFormattingBar) {
            val currentLine = remember(noteTextState.text, noteTextState.selection.min) {
                NoteTextFormatter.getLineIndex(noteTextState.text, noteTextState.selection.min)
            }
            val currentHeading = remember(formatState, currentLine) {
                NoteTextFormatter.getHeadingForLine(formatState, currentLine)
            }

            // Rich Text Formatting Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // H1
                FormatToolbarTextItem(
                    label = "H1",
                    isSelected = currentHeading == HeadingType.H1,
                    onClick = {
                        val updated = NoteTextFormatter.setHeadingForLine(
                            noteTextState.text,
                            noteTextState.selection.min,
                            HeadingType.H1,
                            formatState
                        )
                        formatState = updated
                        undoManager.recordAction(takeSnapshot(newFormatState = updated))
                    }
                )

                // H2
                FormatToolbarTextItem(
                    label = "H2",
                    isSelected = currentHeading == HeadingType.H2,
                    onClick = {
                        val updated = NoteTextFormatter.setHeadingForLine(
                            noteTextState.text,
                            noteTextState.selection.min,
                            HeadingType.H2,
                            formatState
                        )
                        formatState = updated
                        undoManager.recordAction(takeSnapshot(newFormatState = updated))
                    }
                )

                // Aa (Body Text)
                FormatToolbarTextItem(
                    label = "Aa",
                    isSelected = currentHeading == HeadingType.BODY,
                    onClick = {
                        val updated = NoteTextFormatter.setHeadingForLine(
                            noteTextState.text,
                            noteTextState.selection.min,
                            HeadingType.BODY,
                            formatState
                        )
                        formatState = updated
                        undoManager.recordAction(takeSnapshot(newFormatState = updated))
                    }
                )

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(1.dp)
                        .height(18.dp)
                        .background(colors.borderStroke.copy(alpha = 0.5f))
                )

                // Bold
                FormatToolbarTextItem(
                    label = "B",
                    isSelected = NoteTextFormatter.isStyleActiveAt(InlineStyle.BOLD, noteTextState.selection, formatState),
                    fontWeight = FontWeight.Bold,
                    onClick = {
                        val updated = NoteTextFormatter.toggleStyle(noteTextState.selection, InlineStyle.BOLD, formatState)
                        formatState = updated
                        undoManager.recordAction(takeSnapshot(newFormatState = updated))
                    }
                )

                // Italic
                FormatToolbarTextItem(
                    label = "I",
                    isSelected = NoteTextFormatter.isStyleActiveAt(InlineStyle.ITALIC, noteTextState.selection, formatState),
                    fontStyle = FontStyle.Italic,
                    onClick = {
                        val updated = NoteTextFormatter.toggleStyle(noteTextState.selection, InlineStyle.ITALIC, formatState)
                        formatState = updated
                        undoManager.recordAction(takeSnapshot(newFormatState = updated))
                    }
                )

                // Underline
                FormatToolbarTextItem(
                    label = "U",
                    isSelected = NoteTextFormatter.isStyleActiveAt(InlineStyle.UNDERLINE, noteTextState.selection, formatState),
                    textDecoration = TextDecoration.Underline,
                    onClick = {
                        val updated = NoteTextFormatter.toggleStyle(noteTextState.selection, InlineStyle.UNDERLINE, formatState)
                        formatState = updated
                        undoManager.recordAction(takeSnapshot(newFormatState = updated))
                    }
                )

                // Strikethrough
                FormatToolbarIconItem(
                    icon = Icons.Outlined.FormatStrikethrough,
                    contentDescription = "Strikethrough",
                    isSelected = NoteTextFormatter.isStyleActiveAt(InlineStyle.STRIKETHROUGH, noteTextState.selection, formatState),
                    onClick = {
                        val updated = NoteTextFormatter.toggleStyle(noteTextState.selection, InlineStyle.STRIKETHROUGH, formatState)
                        formatState = updated
                        undoManager.recordAction(takeSnapshot(newFormatState = updated))
                    }
                )

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(1.dp)
                        .height(18.dp)
                        .background(colors.borderStroke.copy(alpha = 0.5f))
                )

                // Close Button [X]
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { showFormattingBar = false },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close formatting",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            // Standard Bottom Bar matching reference screenshot:
            // Left: [+] | Palette | Format A
            // Right: Undo | Redo | 3-dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left group: [+] | Palette | Format A
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomBarIconButton(
                        icon = Icons.Outlined.AddBox,
                        contentDescription = "Add options",
                        isEnabled = true,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showBottomSheet = true
                        }
                    )

                    BottomBarIconButton(
                        icon = Icons.Outlined.Palette,
                        contentDescription = "Background color",
                        isEnabled = true,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showColorSheet = true
                        }
                    )

                    BottomBarIconButton(
                        icon = Icons.Outlined.FormatColorText,
                        contentDescription = "Text style",
                        isEnabled = isFormattingEnabled,
                        onClick = {
                            showFormattingBar = true
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Right group: Undo | Redo | 3-dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomBarIconButton(
                        icon = Icons.AutoMirrored.Outlined.Undo,
                        contentDescription = "Undo",
                        isEnabled = undoManager.canUndo,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val restored = undoManager.undo()
                            if (restored != null) {
                                restoreSnapshot(restored)
                            }
                        }
                    )

                    BottomBarIconButton(
                        icon = Icons.AutoMirrored.Outlined.Redo,
                        contentDescription = "Redo",
                        isEnabled = undoManager.canRedo,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val restored = undoManager.redo()
                            if (restored != null) {
                                restoreSnapshot(restored)
                            }
                        }
                    )

                    BottomBarIconButton(
                        icon = Icons.Outlined.MoreVert,
                        contentDescription = "More options",
                        isEnabled = true,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMoreSheet = true
                        }
                    )
                }
            }
        }
    }
}

    // Exact Bottom Sheet matching user's reference screenshot
    if (showBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val menuOptions = listOf(
            NoteMenuOption("Take photo", Icons.Outlined.PhotoCamera) {
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                    cameraLauncher.launch(null)
                }
            },
            NoteMenuOption("Add image", Icons.Outlined.Image) {
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                    imagePickerLauncher.launch("image/*")
                }
            },
            NoteMenuOption("Recording", Icons.Outlined.Mic) {
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                    Toast.makeText(context, "Recording feature", Toast.LENGTH_SHORT).show()
                }
            },
            NoteMenuOption("Drawing", Icons.Outlined.Edit) {
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                    Toast.makeText(context, "Drawing feature", Toast.LENGTH_SHORT).show()
                }
            },
            NoteMenuOption("Checkboxes", Icons.Outlined.CheckBox) {
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                    if (!isChecklistMode) {
                        isChecklistMode = true
                        val existingLines = noteTextState.text.split("\n").filter { it.isNotBlank() }
                        val items = if (existingLines.isNotEmpty()) {
                            existingLines.map { ChecklistItem(text = it) }
                        } else {
                            listOf(ChecklistItem(text = ""))
                        }
                        checklistItems = items
                        undoManager.recordAction(takeSnapshot(newIsChecklistMode = true, newChecklistItems = items))
                    } else {
                        isChecklistMode = false
                        val restoredTfv = TextFieldValue(checklistItems.joinToString("\n") { it.text })
                        noteTextState = restoredTfv
                        undoManager.recordAction(takeSnapshot(newIsChecklistMode = false, newNoteTextState = restoredTfv))
                    }
                }
            }
        )

        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = colors.cardBackground,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 28.dp)
                    .navigationBarsPadding()
            ) {
                menuOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                option.onClick()
                            }
                            .padding(horizontal = 24.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.title,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = option.title,
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }

    // Color Palette Bottom Sheet matching reference screenshot
    if (showColorSheet) {
        val colorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showColorSheet = false },
            sheetState = colorSheetState,
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
                    text = "Color",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(NOTE_COLORS) { colorHex ->
                        val isSelected = colorHex == selectedColorHex
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF76D1FF) else Color.White.copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedColorHex = colorHex
                                    undoManager.recordAction(takeSnapshot(newSelectedColorHex = colorHex))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected color",
                                    tint = if (colorHex == 0xFF121212L) Color(0xFF76D1FF) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // More Options Bottom Sheet matching reference screenshot
    if (showMoreSheet) {
        val moreSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val clipboardManager = LocalClipboardManager.current

        val moreOptions = listOf(
            NoteMenuOption("Find in note", Icons.Outlined.Search) {
                coroutineScope.launch {
                    moreSheetState.hide()
                    showMoreSheet = false
                    Toast.makeText(context, "Find in note", Toast.LENGTH_SHORT).show()
                }
            },
            NoteMenuOption("Make a copy", Icons.Outlined.ContentCopy) {
                coroutineScope.launch {
                    moreSheetState.hide()
                    showMoreSheet = false
                    val finalContent = buildFinalContent()
                    val fullNoteText = if (title.isNotBlank()) "$title\n\n$finalContent" else finalContent
                    clipboardManager.setText(AnnotatedString(fullNoteText))
                    Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            },
            NoteMenuOption("Send", Icons.Outlined.Share) {
                coroutineScope.launch {
                    moreSheetState.hide()
                    showMoreSheet = false
                    val finalContent = buildFinalContent()
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, title)
                        putExtra(Intent.EXTRA_TEXT, if (title.isNotBlank()) "$title\n\n$finalContent" else finalContent)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share note via"))
                }
            },
            NoteMenuOption("Labels", Icons.AutoMirrored.Outlined.Label) {
                coroutineScope.launch {
                    moreSheetState.hide()
                    showMoreSheet = false
                    Toast.makeText(context, "Labels", Toast.LENGTH_SHORT).show()
                }
            },
            NoteMenuOption("Delete", Icons.Outlined.Delete) {
                coroutineScope.launch {
                    moreSheetState.hide()
                    showMoreSheet = false
                    if (onDeleteNote != null) {
                        onDeleteNote()
                    } else {
                        onDiscard()
                    }
                }
            }
        )

        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = moreSheetState,
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
                    text = NoteLastEditedFormatter.format(lastEditedMillis),
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                moreOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                option.onClick()
                            }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = option.title,
                            tint = colors.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = option.title,
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun FormatToolbarTextItem(
    label: String,
    isSelected: Boolean,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null,
    onClick: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    Box(
        modifier = Modifier
            .height(36.dp)
            .wrapContentWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) Color(0xFF2B3035) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.textPrimary.copy(alpha = 0.85f),
            fontSize = 16.sp,
            fontWeight = fontWeight ?: if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            fontStyle = fontStyle,
            textDecoration = textDecoration
        )
    }
}

@Composable
private fun FormatToolbarIconItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) Color(0xFF2B3035) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) Color.White else colors.textPrimary.copy(alpha = 0.85f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BottomBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF24282C))
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isEnabled) colors.textPrimary else colors.textPrimary.copy(alpha = 0.35f),
            modifier = Modifier.size(24.dp)
        )
    }
}

