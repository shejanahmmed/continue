package com.shejan.acontinue.presentation.create

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Job
import kotlin.math.roundToInt
import android.content.Intent
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import com.shejan.acontinue.ContinueApplication
import com.shejan.acontinue.core.designsystem.components.DynamicIslandRecordingBar
import com.shejan.acontinue.core.util.AttachmentHelper
import com.shejan.acontinue.core.util.AudioRecordingManager
import com.shejan.acontinue.core.util.AudioPlaybackState
import com.shejan.acontinue.core.util.RecordingEvent
import com.shejan.acontinue.core.util.RecordingState
import com.shejan.acontinue.core.util.ImageUtils
import java.io.File
import java.util.Locale
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.FormatColorText
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.shejan.acontinue.core.designsystem.components.AudioTrimDialog
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.outlined.FormatStrikethrough
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import kotlinx.coroutines.launch

const val DEFAULT_NOTE_COLOR_HEX = 0xFF000000L

fun isDefaultNoteColor(hex: Long): Boolean =
    hex == 0xFF000000L || hex == 0xFF121212L || hex == 0L

val NOTE_COLORS = listOf(
    DEFAULT_NOTE_COLOR_HEX, // Default (matches app background)
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
    val id: Long = java.util.UUID.randomUUID().mostSignificantBits,
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
    initialTitle: String = "",
    initialContent: String = "",
    initialColorHex: Long = DEFAULT_NOTE_COLOR_HEX,
    initialLabels: List<String> = emptyList(),
    onSaveNote: (title: String, content: String, colorHex: Long, labels: List<String>) -> Unit,
    onDiscard: () -> Unit,
    initialUpdatedAtMillis: Long? = null,
    onDeleteNote: (() -> Unit)? = null,
    onMakeCopy: ((title: String, content: String, colorHex: Long, labels: List<String>) -> Unit)? = null,
    allLabels: List<String> = emptyList(),
    onCreateLabel: (String) -> Unit = {},
    onEditLabel: (oldName: String, newName: String) -> Unit = { _, _ -> },
    onDeleteLabel: (name: String) -> Unit = {},
    editingActivityId: Long? = null,
    onEnsureDraftNoteId: (suspend () -> Long)? = null,
    audioRecordingManager: AudioRecordingManager? = null,
    startInViewMode: Boolean = (editingActivityId != null && editingActivityId > 0L),
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

    var isEditMode by remember { mutableStateOf(!startInViewMode) }

    val openedAtMillis = remember { initialUpdatedAtMillis ?: System.currentTimeMillis() }
    var lastEditedMillis by remember { mutableLongStateOf(openedAtMillis) }
    var hasUserModified by remember { mutableStateOf(false) }

    val parsedAudioUris = remember(initialContent) {
        AUDIO_TAG_REGEX.findAll(initialContent).mapNotNull { match ->
            runCatching { Uri.parse(match.groupValues[1]) }.getOrNull()
        }.toList()
    }
    val initialCleanContent = remember(initialContent) {
        initialContent.replace(AUDIO_TAG_REGEX, "").trim()
    }

    val initialParsedChecklist = remember(initialCleanContent) {
        val lines = initialCleanContent.lines().filter { it.isNotBlank() }
        if (lines.isNotEmpty() && lines.all { it.startsWith("[ ] ") || it.startsWith("[x] ") || it.startsWith("[X] ") }) {
            lines.map { line ->
                val isChecked = line.startsWith("[x] ") || line.startsWith("[X] ")
                ChecklistItem(text = line.substring(4), isChecked = isChecked)
            }
        } else null
    }

    val (initialCleanPlainText, initialParsedFormatState) = remember(initialCleanContent) {
        if (initialParsedChecklist != null) {
            Pair("", NoteFormatState())
        } else {
            NoteTextFormatter.parseMarkdownToState(initialCleanContent)
        }
    }

    var title by remember { mutableStateOf(initialTitle) }
    var noteTextState by remember {
        mutableStateOf(TextFieldValue(initialCleanPlainText))
    }
    var formatState by remember { mutableStateOf(initialParsedFormatState) }
    var selectedColorHex by remember { mutableLongStateOf(initialColorHex) }
    val isDefaultColor = remember(selectedColorHex) { isDefaultNoteColor(selectedColorHex) }
    val effectiveBackgroundColor = if (isDefaultColor) colors.background else Color(selectedColorHex)
    var isChecklistMode by remember { mutableStateOf(initialParsedChecklist != null) }
    var checklistItems by remember { mutableStateOf(initialParsedChecklist ?: listOf<ChecklistItem>()) }
    val checklistFocusRequesters = remember { mutableMapOf<Long, FocusRequester>() }
    var targetFocusChecklistId by remember { mutableStateOf<Long?>(null) }
    var draggingChecklistId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val settleAnim = remember { Animatable(0f) }
    var isSettling by remember { mutableStateOf(false) }
    val itemHeights = remember { mutableStateMapOf<Long, Int>() }

    LaunchedEffect(targetFocusChecklistId) {
        targetFocusChecklistId?.let { id ->
            kotlinx.coroutines.delay(60)
            checklistFocusRequesters[id]?.requestFocus()
            keyboardController?.show()
            targetFocusChecklistId = null
        }
    }

    var attachedImageUris by remember { mutableStateOf(listOf<Uri>()) }
    var recordedAudioUris by remember { mutableStateOf(parsedAudioUris) }
    var selectedLabels by remember { mutableStateOf(initialLabels.toSet()) }
    var showLabelScreen by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var showFormattingSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showDeleteNoteConfirmDialog by remember { mutableStateOf(false) }
    var showFindBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeMatchIndex by remember { mutableIntStateOf(0) }
    val searchFocusRequester = remember { FocusRequester() }

    var targetCursorPosition by remember { mutableIntStateOf(initialCleanPlainText.length) }
    var capturedPhotoFile by remember { mutableStateOf<File?>(null) }
    var selectedPreviewPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var autoFocusAfterCaptureTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(initialTitle) {
        if (!hasUserModified && title != initialTitle) {
            title = initialTitle
        }
    }

    LaunchedEffect(initialCleanPlainText, initialParsedFormatState, initialParsedChecklist) {
        if (!hasUserModified) {
            noteTextState = TextFieldValue(initialCleanPlainText)
            formatState = initialParsedFormatState
            isChecklistMode = initialParsedChecklist != null
            checklistItems = initialParsedChecklist ?: emptyList()
            targetCursorPosition = initialCleanPlainText.length
        }
    }

    LaunchedEffect(parsedAudioUris) {
        if (!hasUserModified) {
            recordedAudioUris = parsedAudioUris
        }
    }

    LaunchedEffect(initialColorHex) {
        if (!hasUserModified) {
            selectedColorHex = initialColorHex
        }
    }

    LaunchedEffect(initialLabels) {
        if (!hasUserModified) {
            selectedLabels = initialLabels.toSet()
        }
    }

    val enterEditMode: () -> Unit = {
        if (!isEditMode) {
            isEditMode = true
            val now = System.currentTimeMillis()
            lastEditedMillis = now
            hasUserModified = true
            coroutineScope.launch {
                kotlinx.coroutines.delay(50)
                if (isChecklistMode && checklistItems.isNotEmpty()) {
                    targetFocusChecklistId = checklistItems.last().id
                } else {
                    contentFocusRequester.requestFocus()
                    isContentFocused = true
                    val endPos = noteTextState.text.length
                    noteTextState = noteTextState.copy(selection = TextRange(endPos))
                    targetCursorPosition = endPos
                }
                keyboardController?.show()
            }
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(Unit) {
        if (!startInViewMode && editingActivityId != null && editingActivityId > 0L) {
            kotlinx.coroutines.delay(150)
            if (isChecklistMode && checklistItems.isNotEmpty()) {
                targetFocusChecklistId = checklistItems.last().id
            } else {
                runCatching {
                    contentFocusRequester.requestFocus()
                    isContentFocused = true
                    val endPos = noteTextState.text.length
                    noteTextState = noteTextState.copy(selection = TextRange(endPos))
                    targetCursorPosition = endPos
                }
            }
            keyboardController?.show()
        }
    }

    val activeRecordingManager = audioRecordingManager ?: remember { AudioRecordingManager(context) }
    var currentEditingId by remember { mutableStateOf(editingActivityId) }
    val recordingState by activeRecordingManager.state.collectAsState()

    var isRecordingIslandVisible by remember { mutableStateOf(recordingState.isRecording) }
    val islandScale = remember { Animatable(if (recordingState.isRecording) 1f else 0f) }
    val islandWidthDp = remember { Animatable(if (recordingState.isRecording) 204f else 48f) }
    val islandContentAlpha = remember { Animatable(if (recordingState.isRecording) 1f else 0f) }

    val searchMatches = remember(noteTextState.text, searchQuery) {
        if (searchQuery.isBlank()) emptyList() else NoteTextFormatter.findMatches(noteTextState.text, searchQuery)
    }
    val safeActiveMatchIndex = if (searchMatches.isEmpty()) -1 else activeMatchIndex.coerceIn(0, searchMatches.size - 1)
    val activeMatchRange = if (safeActiveMatchIndex in searchMatches.indices) searchMatches[safeActiveMatchIndex] else null

    val initialSnapshot = remember {
        NoteEditorSnapshot(
            title = initialTitle,
            noteTextState = TextFieldValue(initialCleanPlainText),
            formatState = initialParsedFormatState,
            checklistItems = initialParsedChecklist ?: emptyList(),
            isChecklistMode = initialParsedChecklist != null,
            attachedImageUris = emptyList(),
            selectedColorHex = initialColorHex,
            selectedLabels = initialLabels.toSet(),
            recordedAudioUris = parsedAudioUris
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
        newSelectedColorHex: Long = selectedColorHex,
        newSelectedLabels: Set<String> = selectedLabels,
        newRecordedAudioUris: List<Uri> = recordedAudioUris
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
            selectedColorHex = newSelectedColorHex,
            selectedLabels = newSelectedLabels,
            recordedAudioUris = newRecordedAudioUris
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
        selectedLabels = restored.selectedLabels
        recordedAudioUris = restored.recordedAudioUris
    }

    LaunchedEffect(isTitleFocused) {
        if (isTitleFocused) {
            showFormattingSheet = false
        }
    }

    LaunchedEffect(searchQuery) {
        activeMatchIndex = 0
    }

    LaunchedEffect(showFindBar) {
        if (showFindBar) {
            searchFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    LaunchedEffect(safeActiveMatchIndex, searchMatches) {
        if (activeMatchRange != null && noteTextState.text.isNotEmpty()) {
            val charOffset = activeMatchRange.first
            val lineIndex = noteTextState.text.take(charOffset).count { it == '\n' }
            scrollState.animateScrollTo((lineIndex * 45).coerceAtLeast(0))
        }
    }

    DisposableEffect(activity, selectedColorHex, colors.background, colors.isDark) {
        val window = activity?.window
        if (window != null) {
            val previousStatusBarColor = window.statusBarColor
            val previousNavColor = window.navigationBarColor
            val insetsController = WindowCompat.getInsetsController(window, view)
            val previousLightStatus = insetsController.isAppearanceLightStatusBars
            val previousLightNav = insetsController.isAppearanceLightNavigationBars

            val targetArgb = effectiveBackgroundColor.toArgb()
            window.statusBarColor = targetArgb
            window.navigationBarColor = targetArgb
            val isLightBars = !colors.isDark && isDefaultColor
            insetsController.isAppearanceLightStatusBars = isLightBars
            insetsController.isAppearanceLightNavigationBars = isLightBars

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
            val targetArgb = effectiveBackgroundColor.toArgb()
            window.statusBarColor = targetArgb
            window.navigationBarColor = targetArgb
            val isLightBars = !colors.isDark && isDefaultColor
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = isLightBars
                isAppearanceLightNavigationBars = isLightBars
            }
        }
    }

    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                val attachmentsDir = File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }
                var currentCursor = targetCursorPosition
                var currentText = noteTextState.text

                withContext(Dispatchers.IO) {
                    uris.forEachIndexed { index, uri ->
                        runCatching {
                            val permanentFile = File(attachmentsDir, "note_photo_${System.currentTimeMillis()}_$index.jpg")
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                permanentFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            val permanentUri = Uri.fromFile(permanentFile)
                            val (newText, newCursor) = NoteTextFormatter.insertPhotoTag(
                                currentText = currentText,
                                cursorOffset = currentCursor,
                                photoUri = permanentUri.toString()
                            )
                            currentText = newText
                            currentCursor = newCursor
                        }
                    }
                }

                val updatedState = TextFieldValue(text = currentText, selection = TextRange(currentCursor))
                noteTextState = updatedState
                targetCursorPosition = currentCursor
                undoManager.recordAction(takeSnapshot(newNoteTextState = updatedState))
                autoFocusAfterCaptureTrigger++
            }
        }
    }

    val insertPhotoAtCursor: (Uri, Int) -> Unit = { permanentUri, cursorOffset ->
        val (newText, newCursor) = NoteTextFormatter.insertPhotoTag(
            currentText = noteTextState.text,
            cursorOffset = cursorOffset,
            photoUri = permanentUri.toString()
        )
        val updatedState = TextFieldValue(text = newText, selection = TextRange(newCursor))
        noteTextState = updatedState
        targetCursorPosition = newCursor
        undoManager.recordAction(takeSnapshot(newNoteTextState = updatedState))
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val tempFile = capturedPhotoFile
        if (success && tempFile?.exists() == true && tempFile.length() > 0L) {
            val attachmentsDir = File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }
            val permanentFile = File(attachmentsDir, "note_photo_${System.currentTimeMillis()}.jpg")
            runCatching {
                tempFile.copyTo(permanentFile, overwrite = true)
                tempFile.delete()
            }
            val permanentUri = Uri.fromFile(permanentFile)
            insertPhotoAtCursor(permanentUri, targetCursorPosition)
            autoFocusAfterCaptureTrigger++
        } else {
            tempFile?.let { AttachmentHelper.deleteLocalFile(it.absolutePath) }
        }
        capturedPhotoFile = null
    }

    var scrollContainerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var autoScrollJob by remember { mutableStateOf<Job?>(null) }

    val smoothScrollCursorIntoView: (LayoutCoordinates, TextLayoutResult, Int) -> Unit = remember(scrollState) {
        { fieldCoordinates, textLayoutResult, cursorOffset ->
            autoScrollJob?.cancel()
            autoScrollJob = coroutineScope.launch {
                // Delay 20ms to allow layout pass and scrollState.maxValue to update
                kotlinx.coroutines.delay(20)

                val containerCoords = scrollContainerCoordinates ?: return@launch
                if (!containerCoords.isAttached || !fieldCoordinates.isAttached) return@launch

                val safeOffset = cursorOffset.coerceIn(0, textLayoutResult.layoutInput.text.length)
                val cursorRect = textLayoutResult.getCursorRect(safeOffset)

                val cursorInViewport = containerCoords.localPositionOf(
                    sourceCoordinates = fieldCoordinates,
                    relativeToSource = Offset(cursorRect.left, cursorRect.bottom)
                )

                val viewportHeight = containerCoords.size.height.toFloat()
                if (viewportHeight > 0f) {
                    // Comfort threshold: keep cursor at or above ~55% of visible viewport height (above the keyboard)
                    val comfortThreshold = (viewportHeight * 0.55f).coerceAtLeast(140f)
                    if (cursorInViewport.y > comfortThreshold) {
                        val excessPixels = (cursorInViewport.y - comfortThreshold).roundToInt()
                        val targetScroll = (scrollState.value + excessPixels).coerceAtMost(scrollState.maxValue)
                        if (targetScroll > scrollState.value) {
                            scrollState.animateScrollTo(
                                value = targetScroll,
                                animationSpec = tween(
                                    durationMillis = 140,
                                    easing = LinearOutSlowInEasing
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(autoFocusAfterCaptureTrigger) {
        if (autoFocusAfterCaptureTrigger > 0) {
            kotlinx.coroutines.delay(100)
            contentFocusRequester.requestFocus()
            isContentFocused = true
            keyboardController?.show()
        }
    }

    val launchCamera: () -> Unit = {
        val cameraCacheDir = File(context.cacheDir, "camera").apply { if (!exists()) mkdirs() }
        val tempFile = File(cameraCacheDir, "temp_capture_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
        capturedPhotoFile = tempFile
        cameraLauncher.launch(uri)
    }

    LaunchedEffect(activeRecordingManager) {
        activeRecordingManager.events.collect { event ->
            when (event) {
                is RecordingEvent.RecordingSaved -> {
                    if (event.noteId == null || event.noteId == currentEditingId) {
                        if (!recordedAudioUris.contains(event.audioUri)) {
                            val updated = recordedAudioUris + event.audioUri
                            recordedAudioUris = updated
                            undoManager.recordAction(takeSnapshot(newRecordedAudioUris = updated))
                        }
                    }
                }
                RecordingEvent.RecordingCancelled -> {}
            }
        }
    }

    LaunchedEffect(recordingState.isRecording) {
        if (recordingState.isRecording && !isRecordingIslandVisible) {
            isRecordingIslandVisible = true
            islandScale.snapTo(1f)
            islandWidthDp.snapTo(204f)
            islandContentAlpha.snapTo(1f)
        } else if (!recordingState.isRecording && isRecordingIslandVisible) {
            islandContentAlpha.animateTo(0f, tween(100))
            islandWidthDp.animateTo(48f, tween(180, easing = LinearOutSlowInEasing))
            islandScale.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
            isRecordingIslandVisible = false
        }
    }

    val startVoiceRecording: () -> Unit = {
        coroutineScope.launch {
            val targetId = currentEditingId ?: onEnsureDraftNoteId?.invoke()
            if (targetId != null) {
                currentEditingId = targetId
            }
            val started = activeRecordingManager.startRecording(targetId)
            if (!started) {
                Toast.makeText(context, "Could not start recording", Toast.LENGTH_SHORT).show()
                return@launch
            }

            isRecordingIslandVisible = true
            islandScale.snapTo(0f)
            islandWidthDp.snapTo(48f)
            islandContentAlpha.snapTo(0f)

            // Circle pops in with bouncy spring
            islandScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )

            // Extend horizontally outward into sleek compact dynamic island
            val expandJob = launch {
                islandWidthDp.animateTo(
                    targetValue = 204f,
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            islandContentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180)
            )
            expandJob.join()
        }
    }

    val stopAndSaveAudioRecording: () -> Unit = {
        coroutineScope.launch {
            val fadeJob = launch { islandContentAlpha.animateTo(0f, tween(100)) }
            islandWidthDp.animateTo(48f, tween(180, easing = LinearOutSlowInEasing))
            fadeJob.join()
            islandScale.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
            isRecordingIslandVisible = false

            val file = activeRecordingManager.stopRecording()
            if (file != null && file.exists() && file.length() > 0L) {
                val audioUri = Uri.fromFile(file)
                if (!recordedAudioUris.contains(audioUri)) {
                    val updated = recordedAudioUris + audioUri
                    recordedAudioUris = updated
                    undoManager.recordAction(takeSnapshot(newRecordedAudioUris = updated))
                }
            }
        }
    }

    val togglePauseResumeRecording: () -> Unit = {
        if (recordingState.isPaused) {
            activeRecordingManager.resumeRecording()
        } else {
            activeRecordingManager.pauseRecording()
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startVoiceRecording()
        } else {
            Toast.makeText(context, "Microphone permission required for voice recording", Toast.LENGTH_SHORT).show()
        }
    }

    val requestRecording: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecording()
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val buildFinalContent: () -> String = {
        val stringBuilder = StringBuilder()
        recordedAudioUris.forEach { audioUri ->
            stringBuilder.append("![audio]($audioUri)\n\n")
        }
        if (isChecklistMode) {
            val validItems = checklistItems.filter { it.text.isNotBlank() }
            validItems.forEach { item ->
                val box = if (item.isChecked) "[x]" else "[ ]"
                stringBuilder.append("$box ${item.text}\n")
            }
        } else {
            stringBuilder.append(NoteTextFormatter.serializeToMarkdown(noteTextState.text, formatState))
        }
        stringBuilder.toString().trim()
    }

    var isExiting by remember { mutableStateOf(false) }

    val handleExit = {
        if (!isExiting) {
            isExiting = true
            val finalContent = buildFinalContent()
            if (hasUserModified || (editingActivityId == null && (title.isNotBlank() || finalContent.isNotBlank() || attachedImageUris.isNotEmpty() || recordedAudioUris.isNotEmpty() || selectedLabels.isNotEmpty()))) {
                onSaveNote(title, finalContent, selectedColorHex, selectedLabels.toList())
            } else {
                onDiscard()
            }
        }
    }

    BackHandler {
        if (selectedPreviewPhotoUri != null) {
            selectedPreviewPhotoUri = null
        } else if (showDeleteNoteConfirmDialog) {
            showDeleteNoteConfirmDialog = false
        } else if (showLabelScreen) {
            showLabelScreen = false
        } else if (showColorSheet) {
            showColorSheet = false
        } else if (showBottomSheet) {
            showBottomSheet = false
        } else if (showMoreSheet) {
            showMoreSheet = false
        } else if (showFindBar) {
            showFindBar = false
            searchQuery = ""
        } else if (showFormattingSheet) {
            showFormattingSheet = false
        } else {
            handleExit()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(effectiveBackgroundColor)
            .then(
                if (!isEditMode) {
                    Modifier.pointerInput(isEditMode) {
                        detectTapGestures(
                            onDoubleTap = {
                                enterEditMode()
                            }
                        )
                    }
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
        ) {
        // Top Bar: Back button on the left (pin & archive intentionally excluded)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, end = 20.dp, top = 8.dp, bottom = 0.dp),
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
                    tint = colors.textPrimaryMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Body: Title, Attached Images, and Note/Checklist Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .onGloballyPositioned { scrollContainerCoordinates = it }
                .verticalScroll(scrollState)
                .then(
                    if (!isEditMode) {
                        Modifier.pointerInput(isEditMode) {
                            detectTapGestures(
                                onDoubleTap = {
                                    enterEditMode()
                                }
                            )
                        }
                    } else {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isChecklistMode) {
                                contentFocusRequester.requestFocus()
                                isContentFocused = true
                                noteTextState = noteTextState.copy(selection = TextRange(noteTextState.text.length))
                                targetCursorPosition = noteTextState.text.length
                                keyboardController?.show()
                            }
                        }
                    }
                )
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
        ) {
            // Title Input
            BasicTextField(
                value = title,
                onValueChange = { newTitle ->
                    if (isEditMode) {
                        title = newTitle
                        undoManager.recordTyping(takeSnapshot(newTitle = newTitle))
                    }
                },
                readOnly = !isEditMode,
                enabled = isEditMode,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(titleFocusRequester)
                    .onFocusChanged {
                        isTitleFocused = it.isFocused
                        if (it.isFocused) {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0, tween(150, easing = LinearOutSlowInEasing))
                            }
                        }
                    },
                textStyle = TextStyle(
                    color = colors.textPrimaryMuted,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(colors.textPrimaryMuted),
                decorationBox = { innerTextField ->
                    Box {
                        if (title.isEmpty()) {
                            Text(
                                text = "Title",
                                color = colors.textDisabled,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Attached Images Row (if any)
            if (attachedImageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(attachedImageUris) { uri ->
                        var thumbBitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }
                        LaunchedEffect(uri) {
                            thumbBitmap = withContext(Dispatchers.IO) {
                                ImageUtils.loadSampledBitmapFromUri(context, uri, reqWidth = 144, reqHeight = 144)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.cardBackground)
                                .border(1.dp, colors.borderStroke, RoundedCornerShape(8.dp))
                        ) {
                            val currentThumb = thumbBitmap
                            if (currentThumb != null) {
                                Image(
                                    bitmap = currentThumb.asImageBitmap(),
                                    contentDescription = "Attachment thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(20.dp)
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
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Recorded Voice Notes (Top of Note Section - Recording Session Style)
            if (recordedAudioUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    recordedAudioUris.forEachIndexed { index, audioUri ->
                        NoteAudioCard(
                            audioUri = audioUri,
                            index = index + 1,
                            onDelete = {
                                val updated = recordedAudioUris - audioUri
                                recordedAudioUris = updated
                                undoManager.recordAction(takeSnapshot(newRecordedAudioUris = updated))
                            },
                            onAudioUpdated = { oldUri, newUri ->
                                val updated = recordedAudioUris.map { if (it == oldUri) newUri else it }
                                recordedAudioUris = updated
                                undoManager.recordAction(takeSnapshot(newRecordedAudioUris = updated))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note Content or Checklist Mode
            if (!isChecklistMode) {
                val photoMatches = remember(noteTextState.text) {
                    PHOTO_TAG_REGEX.findAll(noteTextState.text).toList()
                }

                if (photoMatches.isEmpty()) {
                    var singleFieldCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                    var singleLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 300.dp)
                            .then(
                                if (isEditMode) {
                                    Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        contentFocusRequester.requestFocus()
                                        noteTextState = noteTextState.copy(selection = TextRange(noteTextState.text.length))
                                        targetCursorPosition = noteTextState.text.length
                                        keyboardController?.show()
                                    }
                                } else Modifier
                            )
                    ) {
                        BasicTextField(
                            value = noteTextState,
                            readOnly = !isEditMode,
                            enabled = isEditMode,
                            onValueChange = { incomingTfv ->
                                if (isEditMode) {
                                    val newTfv = NoteAutoNumbering.handleAutoNumbering(noteTextState, incomingTfv)
                                    if (newTfv.text != noteTextState.text) {
                                        val newFormatState = NoteTextFormatter.onTextChange(
                                            oldText = noteTextState.text,
                                            newText = newTfv.text,
                                            selection = newTfv.selection,
                                            state = formatState
                                        )
                                        formatState = newFormatState
                                        noteTextState = newTfv
                                        targetCursorPosition = newTfv.selection.start
                                        undoManager.recordTyping(takeSnapshot(newNoteTextState = newTfv, newFormatState = newFormatState))
                                        singleFieldCoords?.let { coords ->
                                            singleLayoutResult?.let { layout ->
                                                smoothScrollCursorIntoView(coords, layout, newTfv.selection.end)
                                            }
                                        }
                                    } else if (newTfv.selection != noteTextState.selection) {
                                        if (formatState.activeTypingStyles != null) {
                                            formatState = formatState.copy(activeTypingStyles = null)
                                        }
                                        noteTextState = newTfv
                                        targetCursorPosition = newTfv.selection.start
                                    }
                                }
                            },
                            onTextLayout = { layoutResult ->
                                singleLayoutResult = layoutResult
                            },
                            visualTransformation = remember(formatState, colors.textPrimaryMuted, searchQuery, activeMatchRange) {
                                NoteVisualTransformation(formatState, colors.textPrimaryMuted, searchQuery, activeMatchRange)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(contentFocusRequester)
                                .onGloballyPositioned { singleFieldCoords = it }
                                .onFocusChanged { state ->
                                    isContentFocused = state.isFocused
                                },
                            textStyle = TextStyle(
                                color = colors.textPrimaryMuted,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                lineHeight = 26.sp,
                                lineBreak = LineBreak.Simple
                            ),
                            cursorBrush = SolidColor(colors.textPrimaryMuted),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (noteTextState.text.isEmpty() && recordedAudioUris.isEmpty()) {
                                        Text(
                                            text = "Note",
                                            color = colors.textDisabled,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                } else {
                    val elements = remember(noteTextState.text) {
                        val matches = PHOTO_TAG_REGEX.findAll(noteTextState.text).toList()
                        val list = mutableListOf<NoteContentElement>()
                        var currentIndex = 0
                        for (match in matches) {
                            if (match.range.first > currentIndex) {
                                val raw = noteTextState.text.substring(currentIndex, match.range.first)
                                val segText = if (raw.endsWith("\n")) raw.substring(0, raw.length - 1) else raw
                                list.add(
                                    NoteContentElement.TextSegment(
                                        text = segText,
                                        startIndex = currentIndex
                                    )
                                )
                            }
                            val uriString = match.groupValues[1]
                            val uri = runCatching { Uri.parse(uriString) }.getOrNull()
                            if (uri != null) {
                                list.add(NoteContentElement.InlinePhoto(uri = uri, tagRange = match.range))
                            }
                            var nextIndex = match.range.last + 1
                            if (nextIndex < noteTextState.text.length && noteTextState.text[nextIndex] == '\n') {
                                nextIndex++
                            }
                            currentIndex = nextIndex
                        }
                        if (currentIndex < noteTextState.text.length) {
                            list.add(
                                NoteContentElement.TextSegment(
                                    text = noteTextState.text.substring(currentIndex),
                                    startIndex = currentIndex
                                )
                            )
                        } else {
                            list.add(NoteContentElement.TextSegment(text = "", startIndex = currentIndex))
                        }
                        list
                    }

                    val lastTextSegmentIndex = remember(elements) {
                        elements.indexOfLast { it is NoteContentElement.TextSegment }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 500.dp)
                    ) {
                        elements.forEachIndexed { elemIndex, element ->
                            when (element) {
                                is NoteContentElement.TextSegment -> {
                                    val isLastSegment = elemIndex == lastTextSegmentIndex
                                    val focusRequester = if (isLastSegment) {
                                        contentFocusRequester
                                    } else {
                                        remember(elemIndex) { FocusRequester() }
                                    }

                                    NoteTextSegmentField(
                                        text = element.text,
                                        startIndex = element.startIndex,
                                        isLastElement = isLastSegment,
                                        focusRequester = focusRequester,
                                        readOnly = !isEditMode,
                                        enabled = isEditMode,
                                        visualTransformation = remember(formatState, colors.textPrimaryMuted, searchQuery, activeMatchRange, element.startIndex, noteTextState.text) {
                                            NoteVisualTransformation(
                                                formatState = formatState,
                                                baseColor = colors.textPrimaryMuted,
                                                searchQuery = searchQuery,
                                                activeMatchRange = activeMatchRange,
                                                baseOffset = element.startIndex,
                                                fullNoteText = noteTextState.text
                                            )
                                        },
                                        onTextChange = { newText, localCursor ->
                                            if (isEditMode) {
                                                val start = element.startIndex
                                                val oldLen = element.text.length
                                                val newFullText = noteTextState.text.replaceRange(start, start + oldLen, newText)
                                                val updatedGlobalCursor = (start + localCursor).coerceIn(0, newFullText.length)
                                                val updated = TextFieldValue(text = newFullText, selection = TextRange(updatedGlobalCursor))
                                                val newFormatState = NoteTextFormatter.onTextChange(
                                                    oldText = noteTextState.text,
                                                    newText = newFullText,
                                                    selection = updated.selection,
                                                    state = formatState
                                                )
                                                formatState = newFormatState
                                                noteTextState = updated
                                                targetCursorPosition = updatedGlobalCursor
                                                undoManager.recordTyping(takeSnapshot(newNoteTextState = updated, newFormatState = newFormatState))
                                            }
                                        },
                                        onSelectionChange = { localSel ->
                                            val globalMin = (element.startIndex + localSel.min).coerceIn(0, noteTextState.text.length)
                                            val globalMax = (element.startIndex + localSel.max).coerceIn(0, noteTextState.text.length)
                                            if (formatState.activeTypingStyles != null) {
                                                formatState = formatState.copy(activeTypingStyles = null)
                                            }
                                            noteTextState = noteTextState.copy(selection = TextRange(globalMin, globalMax))
                                            targetCursorPosition = globalMin
                                        },
                                        onFocused = {
                                            isContentFocused = true
                                            targetCursorPosition = (element.startIndex + element.text.length).coerceIn(0, noteTextState.text.length)
                                        },
                                        onCursorPositionChanged = smoothScrollCursorIntoView
                                    )
                                }
                                is NoteContentElement.InlinePhoto -> {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    NotePhotoCard(
                                        uri = element.uri,
                                        onDelete = {
                                            val newFull = noteTextState.text.removeRange(element.tagRange).trim()
                                            val updated = TextFieldValue(newFull)
                                            noteTextState = updated
                                            targetCursorPosition = 0
                                            undoManager.recordAction(takeSnapshot(newNoteTextState = updated))
                                        },
                                        onClick = {
                                            selectedPreviewPhotoUri = element.uri
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                // Interactive Checklists (Google Keep Style)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    checklistItems.forEachIndexed { index, item ->
                        key(item.id) {
                            var itemCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                            var itemLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                            val itemFocusRequester = checklistFocusRequesters.getOrPut(item.id) { FocusRequester() }
                            val isCurrentDragging = draggingChecklistId == item.id
                            val animatedScale by animateFloatAsState(
                                targetValue = if (isCurrentDragging) 1.025f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                                label = "checklistDragScale"
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .zIndex(if (isCurrentDragging) 100f else 1f)
                                    .graphicsLayer {
                                        if (isCurrentDragging) {
                                            translationY = if (isSettling) settleAnim.value else dragOffsetY
                                            scaleX = animatedScale
                                            scaleY = animatedScale
                                        }
                                    }
                                    .onGloballyPositioned { coords ->
                                        itemHeights[item.id] = coords.size.height
                                    }
                                    .background(
                                        color = if (isCurrentDragging) Color(0xFF202020) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrentDragging) Color(0xFF383838) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(start = 2.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                // 6-Dot Drag Indicator Handle
                                Box(
                                    modifier = Modifier
                                        .size(width = 28.dp, height = 36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .pointerInput(item.id) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    keyboardController?.hide()
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    draggingChecklistId = item.id
                                                    dragOffsetY = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    dragOffsetY += dragAmount.y
                                                    val currIdx = checklistItems.indexOfFirst { it.id == item.id }
                                                    if (currIdx != -1) {
                                                        val spacingPx = 4.dp.toPx()
                                                        if (dragOffsetY > 0f && currIdx < checklistItems.lastIndex) {
                                                            val nextItemId = checklistItems[currIdx + 1].id
                                                            val nextHeight = itemHeights[nextItemId]?.toFloat() ?: 48.dp.toPx()
                                                            val swapDelta = nextHeight + spacingPx
                                                            val threshold = swapDelta * 0.5f
                                                            if (dragOffsetY > threshold) {
                                                                val updated = checklistItems.toMutableList()
                                                                val moved = updated.removeAt(currIdx)
                                                                updated.add(currIdx + 1, moved)
                                                                checklistItems = updated
                                                                dragOffsetY -= swapDelta
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            }
                                                        } else if (dragOffsetY < 0f && currIdx > 0) {
                                                            val prevItemId = checklistItems[currIdx - 1].id
                                                            val prevHeight = itemHeights[prevItemId]?.toFloat() ?: 48.dp.toPx()
                                                            val swapDelta = prevHeight + spacingPx
                                                            val threshold = swapDelta * 0.5f
                                                            if (dragOffsetY < -threshold) {
                                                                val updated = checklistItems.toMutableList()
                                                                val moved = updated.removeAt(currIdx)
                                                                updated.add(currIdx - 1, moved)
                                                                checklistItems = updated
                                                                dragOffsetY += swapDelta
                                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                            }
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    if (draggingChecklistId == item.id) {
                                                        val currentOffset = dragOffsetY
                                                        isSettling = true
                                                        coroutineScope.launch {
                                                            settleAnim.snapTo(currentOffset)
                                                            settleAnim.animateTo(
                                                                targetValue = 0f,
                                                                animationSpec = spring(
                                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                                    stiffness = Spring.StiffnessMedium
                                                                )
                                                            )
                                                            isSettling = false
                                                            draggingChecklistId = null
                                                            dragOffsetY = 0f
                                                            undoManager.recordAction(takeSnapshot(newChecklistItems = checklistItems))
                                                        }
                                                    }
                                                },
                                                onDragCancel = {
                                                    if (draggingChecklistId == item.id) {
                                                        val currentOffset = dragOffsetY
                                                        isSettling = true
                                                        coroutineScope.launch {
                                                            settleAnim.snapTo(currentOffset)
                                                            settleAnim.animateTo(
                                                                targetValue = 0f,
                                                                animationSpec = spring(
                                                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                                                    stiffness = Spring.StiffnessMedium
                                                                )
                                                            )
                                                            isSettling = false
                                                            draggingChecklistId = null
                                                            dragOffsetY = 0f
                                                        }
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DragIndicator,
                                        contentDescription = "Drag to reorder",
                                        tint = colors.textPrimaryMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Checkbox
                                Icon(
                                    imageVector = if (item.isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = if (item.isChecked) "Checked" else "Unchecked",
                                    tint = if (item.isChecked) Color(0xFF3897F0) else colors.textPrimaryMuted,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clickable {
                                            val currIdx = checklistItems.indexOfFirst { it.id == item.id }
                                            if (currIdx != -1) {
                                                val updated = checklistItems.toMutableList().also {
                                                    it[currIdx] = item.copy(isChecked = !item.isChecked)
                                                }
                                                checklistItems = updated
                                                undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                            }
                                        }
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                // Item Text Input
                                BasicTextField(
                                    value = item.text,
                                    readOnly = !isEditMode,
                                    enabled = isEditMode,
                                    onValueChange = { newText ->
                                        if (isEditMode) {
                                            val currIdx = checklistItems.indexOfFirst { it.id == item.id }
                                            if (currIdx != -1) {
                                                val updated = checklistItems.toMutableList().also {
                                                    it[currIdx] = item.copy(text = newText)
                                                }
                                                checklistItems = updated
                                                undoManager.recordTyping(takeSnapshot(newChecklistItems = updated))
                                            }
                                            itemCoords?.let { coords ->
                                                itemLayoutResult?.let { layout ->
                                                    smoothScrollCursorIntoView(coords, layout, newText.length)
                                                }
                                            }
                                        }
                                    },
                                    onTextLayout = { layoutResult ->
                                        itemLayoutResult = layoutResult
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(itemFocusRequester)
                                        .onGloballyPositioned { itemCoords = it }
                                        .onKeyEvent { keyEvent ->
                                            val currIdx = checklistItems.indexOfFirst { it.id == item.id }
                                            if (currIdx != -1 && keyEvent.type == KeyEventType.KeyDown) {
                                                if (keyEvent.key == Key.Enter) {
                                                    val newItem = ChecklistItem(text = "")
                                                    val updated = checklistItems.toMutableList()
                                                    val insertIdx = (currIdx + 1).coerceAtMost(updated.size)
                                                    updated.add(insertIdx, newItem)
                                                    checklistItems = updated
                                                    undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                                    targetFocusChecklistId = newItem.id
                                                    return@onKeyEvent true
                                                } else if (keyEvent.key == Key.Backspace && item.text.isEmpty() && checklistItems.size > 1) {
                                                    val updated = checklistItems.filterIndexed { i, _ -> i != currIdx }
                                                    checklistItems = updated
                                                    undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                                    val prevIdx = (currIdx - 1).coerceAtLeast(0)
                                                    targetFocusChecklistId = updated[prevIdx].id
                                                    return@onKeyEvent true
                                                }
                                            }
                                            false
                                        },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = {
                                            val currIdx = checklistItems.indexOfFirst { it.id == item.id }
                                            if (currIdx != -1) {
                                                val newItem = ChecklistItem(text = "")
                                                val updated = checklistItems.toMutableList()
                                                val insertIdx = (currIdx + 1).coerceAtMost(updated.size)
                                                updated.add(insertIdx, newItem)
                                                checklistItems = updated
                                                undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                                targetFocusChecklistId = newItem.id
                                            }
                                        }
                                    ),
                                    textStyle = TextStyle(
                                        color = if (item.isChecked) colors.textDisabled else colors.textPrimaryMuted,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Normal,
                                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    cursorBrush = SolidColor(colors.textPrimaryMuted),
                                    decorationBox = { inner ->
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            inner()
                                        }
                                    }
                                )

                                // "X" (Delete Item) Button
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            val currIdx = checklistItems.indexOfFirst { it.id == item.id }
                                            if (currIdx != -1) {
                                                val updated = checklistItems.filterIndexed { i, _ -> i != currIdx }
                                                if (updated.isEmpty()) {
                                                    val emptyItem = ChecklistItem(text = "")
                                                    checklistItems = listOf(emptyItem)
                                                    targetFocusChecklistId = emptyItem.id
                                                } else {
                                                    checklistItems = updated
                                                    val nextFocusIdx = (currIdx - 1).coerceAtLeast(0)
                                                    targetFocusChecklistId = updated[nextFocusIdx].id
                                                }
                                                undoManager.recordAction(takeSnapshot(newChecklistItems = checklistItems))
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remove item",
                                        tint = colors.textPrimaryMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // "+ List item" Row (Aligned precisely with checklist rows)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newItem = ChecklistItem(text = "")
                                val updated = checklistItems + newItem
                                checklistItems = updated
                                undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                targetFocusChecklistId = newItem.id
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(60)
                                    scrollState.animateScrollTo(
                                        value = scrollState.maxValue,
                                        animationSpec = tween(durationMillis = 180, easing = LinearOutSlowInEasing)
                                    )
                                }
                            }
                            .padding(start = 2.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
                    ) {
                        Spacer(modifier = Modifier.width(32.dp))
                        Box(
                            modifier = Modifier.size(22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Add item",
                                tint = colors.textPrimaryMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "List item",
                            color = colors.textPrimaryMuted,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            if (selectedLabels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    selectedLabels.forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF262C30))
                                .border(1.dp, Color(0xFF3E4348), RoundedCornerShape(8.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showLabelScreen = true
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }


            // Bottom overscroll buffer & tap-to-focus zone
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .then(
                        if (isEditMode) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isChecklistMode) {
                                    contentFocusRequester.requestFocus()
                                    isContentFocused = true
                                    val endPos = noteTextState.text.length
                                    noteTextState = noteTextState.copy(selection = TextRange(endPos))
                                    targetCursorPosition = endPos
                                    keyboardController?.show()
                                }
                            }
                        } else Modifier
                    )
            )
        }

        val isFormattingEnabled = !isTitleFocused && !isChecklistMode

        if (showFindBar) {
            // Find In Note Search Bar docked above keyboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 11.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input Box with cyan accent border
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF161A1D))
                        .border(1.5.dp, Color(0xFF76D1FF), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = colors.textPrimaryMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
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
                            cursorBrush = SolidColor(Color(0xFF76D1FF)),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Find in note",
                                            color = colors.textDisabled,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                }

                // Up Arrow
                val canNavigate = searchMatches.size > 1
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(enabled = canNavigate) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            activeMatchIndex = if (activeMatchIndex - 1 < 0) searchMatches.size - 1 else activeMatchIndex - 1
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowUp,
                        contentDescription = "Previous match",
                        tint = if (canNavigate) colors.textPrimaryMuted else colors.textDisabled,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Down Arrow
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(enabled = canNavigate) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            activeMatchIndex = (activeMatchIndex + 1) % searchMatches.size
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Next match",
                        tint = if (canNavigate) colors.textPrimaryMuted else colors.textDisabled,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Close Button [X]
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF202020))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showFindBar = false
                            searchQuery = ""
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close find",
                        tint = colors.textPrimaryMuted,
                        modifier = Modifier.size(18.dp)
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
                    .padding(horizontal = 11.dp, vertical = 6.dp),
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
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showFormattingSheet = true
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

    // Dynamic Island Recording Bar at top of note
    if (isRecordingIslandVisible && islandScale.value > 0.01f) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(999f)
                .statusBarsPadding()
                .padding(top = 6.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            DynamicIslandRecordingBar(
                scale = islandScale.value,
                widthDp = islandWidthDp.value,
                contentAlpha = islandContentAlpha.value,
                elapsedMillis = recordingState.elapsedMillis,
                isPaused = recordingState.isPaused,
                onTogglePause = togglePauseResumeRecording,
                onStop = stopAndSaveAudioRecording
            )
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
                    if (isChecklistMode) {
                        isChecklistMode = false
                        val converted = checklistItems.joinToString("\n") { it.text }
                        noteTextState = TextFieldValue(converted)
                    }
                    targetCursorPosition = noteTextState.selection.start.coerceIn(0, noteTextState.text.length)
                    launchCamera()
                }
            },
            NoteMenuOption("Add photo", Icons.Outlined.Image) {
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                    if (isChecklistMode) {
                        isChecklistMode = false
                        val converted = checklistItems.joinToString("\n") { it.text }
                        noteTextState = TextFieldValue(converted)
                    }
                    targetCursorPosition = noteTextState.selection.start.coerceIn(0, noteTextState.text.length)
                    galleryPickerLauncher.launch("image/*")
                }
            },
            NoteMenuOption(
                title = if (recordingState.isRecording) "Recording (stop)" else "Recording",
                icon = if (recordingState.isRecording) Icons.Filled.Stop else Icons.Outlined.Mic
            ) {
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                }
                if (recordingState.isRecording) {
                    stopAndSaveAudioRecording()
                } else {
                    requestRecording()
                }
            },
            if (!isChecklistMode) {
                NoteMenuOption("Checkboxes", Icons.Outlined.CheckBox) {
                    coroutineScope.launch {
                        sheetState.hide()
                        showBottomSheet = false
                        isChecklistMode = true
                        val existingLines = noteTextState.text.split("\n").filter { it.isNotBlank() }
                        val items = if (existingLines.isNotEmpty()) {
                            existingLines.map { ChecklistItem(text = it) }
                        } else {
                            listOf(ChecklistItem(text = ""))
                        }
                        checklistItems = items
                        undoManager.recordAction(takeSnapshot(newIsChecklistMode = true, newChecklistItems = items))
                        targetFocusChecklistId = items.first().id
                    }
                }
            } else {
                NoteMenuOption("Hide checkboxes", Icons.Outlined.CheckBoxOutlineBlank) {
                    coroutineScope.launch {
                        sheetState.hide()
                        showBottomSheet = false
                        isChecklistMode = false
                        val restoredText = checklistItems.joinToString("\n") { it.text }
                        val restoredTfv = TextFieldValue(restoredText, TextRange(restoredText.length))
                        noteTextState = restoredTfv
                        targetCursorPosition = restoredText.length
                        undoManager.recordAction(takeSnapshot(newIsChecklistMode = false, newNoteTextState = restoredTfv))
                        kotlinx.coroutines.delay(60)
                        contentFocusRequester.requestFocus()
                        isContentFocused = true
                        keyboardController?.show()
                    }
                }
            }
        )

        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF202020),
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
                            tint = colors.textPrimaryMuted,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = option.title,
                            color = colors.textPrimaryMuted,
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
                    text = "Color",
                    color = colors.textPrimaryMuted,
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
                        val isItemDefault = isDefaultNoteColor(colorHex)
                        val isSelected = if (isItemDefault) isDefaultColor else colorHex == selectedColorHex
                        val swatchColor = if (isItemDefault) colors.background else Color(colorHex)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF76D1FF) else colors.borderStroke,
                                    shape = CircleShape
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedColorHex = colorHex
                                    undoManager.recordAction(takeSnapshot(newSelectedColorHex = colorHex))
                                    coroutineScope.launch {
                                        colorSheetState.hide()
                                        showColorSheet = false
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Selected color",
                                    tint = if (isItemDefault) Color(0xFF76D1FF) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Text Formatting Bottom Sheet matching Color Palette drawer
    if (showFormattingSheet) {
        val formattingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val currentHeading = remember(formatState, noteTextState.text, noteTextState.selection) {
            val startLine = NoteTextFormatter.getLineIndex(noteTextState.text, noteTextState.selection.min)
            val endLine = NoteTextFormatter.getLineIndex(noteTextState.text, noteTextState.selection.max)
            val firstHeading = NoteTextFormatter.getHeadingForLine(formatState, startLine)
            val allSame = (startLine..endLine).all { NoteTextFormatter.getHeadingForLine(formatState, it) == firstHeading }
            if (allSame) firstHeading else null
        }

        ModalBottomSheet(
            onDismissRequest = { showFormattingSheet = false },
            sheetState = formattingSheetState,
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
                    text = "Text style",
                    color = colors.textPrimaryMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // H1
                    FormatToolbarTextItem(
                        label = "H1",
                        isSelected = currentHeading == HeadingType.H1,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val updated = NoteTextFormatter.setHeadingForSelection(
                                noteTextState.text,
                                noteTextState.selection,
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
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val updated = NoteTextFormatter.setHeadingForSelection(
                                noteTextState.text,
                                noteTextState.selection,
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
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val updated = NoteTextFormatter.setHeadingForSelection(
                                noteTextState.text,
                                noteTextState.selection,
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
                            .height(20.dp)
                            .background(Color(0xFF33383F))
                    )

                    // Bold
                    FormatToolbarTextItem(
                        label = "B",
                        isSelected = NoteTextFormatter.isStyleActiveAt(InlineStyle.BOLD, noteTextState.selection, formatState),
                        fontWeight = FontWeight.Bold,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                            .height(20.dp)
                            .background(Color(0xFF33383F))
                    )

                    // Close Button [X]
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF181818))
                            .border(1.dp, Color(0xFF2A2A2A), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                coroutineScope.launch {
                                    formattingSheetState.hide()
                                    showFormattingSheet = false
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close formatting",
                            tint = colors.textPrimaryMuted,
                            modifier = Modifier.size(20.dp)
                        )
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
                    showFormattingSheet = false
                    showFindBar = true
                }
            },
            NoteMenuOption("Make a copy", Icons.Outlined.ContentCopy) {
                coroutineScope.launch {
                    moreSheetState.hide()
                    showMoreSheet = false
                    val finalContent = buildFinalContent()
                    if (!isExiting && (title.isNotBlank() || finalContent.isNotBlank() || attachedImageUris.isNotEmpty() || recordedAudioUris.isNotEmpty() || selectedLabels.isNotEmpty())) {
                        isExiting = true
                        onMakeCopy?.invoke(title, finalContent, selectedColorHex, selectedLabels.toList())
                        Toast.makeText(context, "Note copied", Toast.LENGTH_SHORT).show()
                    }
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
                    showLabelScreen = true
                }
            },
            NoteMenuOption("Delete", Icons.Outlined.Delete) {
                coroutineScope.launch {
                    moreSheetState.hide()
                    showMoreSheet = false
                    showDeleteNoteConfirmDialog = true
                }
            }
        )

        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = moreSheetState,
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
                    text = NoteLastEditedFormatter.format(lastEditedMillis),
                    color = colors.textPrimaryMuted,
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
                            tint = colors.textPrimaryMuted,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Text(
                            text = option.title,
                            color = colors.textPrimaryMuted,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }

    if (showLabelScreen) {
        NoteLabelsScreen(
            allLabels = allLabels,
            selectedLabels = selectedLabels,
            onToggleLabel = { label ->
                val updated = if (label in selectedLabels) {
                    selectedLabels - label
                } else {
                    selectedLabels + label
                }
                selectedLabels = updated
                undoManager.recordAction(takeSnapshot(newSelectedLabels = updated))
            },
            onCreateLabel = onCreateLabel,
            onBack = { showLabelScreen = false },
            onEditLabel = { oldName, newName ->
                onEditLabel(oldName, newName)
                if (oldName in selectedLabels) {
                    val updated = selectedLabels.map { if (it == oldName) newName else it }.toSet()
                    selectedLabels = updated
                    undoManager.recordAction(takeSnapshot(newSelectedLabels = updated))
                }
            },
            onDeleteLabel = { name ->
                onDeleteLabel(name)
                if (name in selectedLabels) {
                    val updated = selectedLabels - name
                    selectedLabels = updated
                    undoManager.recordAction(takeSnapshot(newSelectedLabels = updated))
                }
            }
        )
    }

    if (showDeleteNoteConfirmDialog) {
        Dialog(
            onDismissRequest = { showDeleteNoteConfirmDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF202020))
                    .border(1.dp, Color(0xFF33383F), RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Delete note?",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "This note will be moved to trash and then deleted forever from all your devices after 7 days.",
                        color = colors.textSecondary.copy(alpha = 0.85f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel Button Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2C3136))
                                .border(1.dp, Color(0xFF3F454C), RoundedCornerShape(12.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showDeleteNoteConfirmDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                color = colors.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Delete Button Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD71921))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showDeleteNoteConfirmDialog = false
                                    if (!isExiting) {
                                        isExiting = true
                                        if (onDeleteNote != null) {
                                            onDeleteNote()
                                        } else {
                                            onDiscard()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Delete",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }



    if (selectedPreviewPhotoUri != null) {
        FullPhotoPreviewDialog(
            photoUri = selectedPreviewPhotoUri!!,
            onDismiss = { selectedPreviewPhotoUri = null }
        )
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
            .height(40.dp)
            .wrapContentWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF2E3338) else Color(0xFF181818))
            .border(
                1.dp,
                if (isSelected) Color(0xFF4A525A) else Color(0xFF2A2A2A),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else colors.textPrimaryMuted,
            fontSize = 15.sp,
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
            .size(40.dp)
            .clip(CircleShape)
            .background(if (isSelected) Color(0xFF2E3338) else Color(0xFF181818))
            .border(
                1.dp,
                if (isSelected) Color(0xFF4A525A) else Color(0xFF2A2A2A),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) Color.White else colors.textPrimaryMuted,
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
            .background(Color(0xFF202020))
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isEnabled) colors.textPrimaryMuted else colors.textDisabled,
            modifier = Modifier.size(24.dp)
        )
    }
}

private val PHOTO_TAG_REGEX = Regex("""!\[photo\]\(([^)]+)\)""")

private sealed interface NoteContentElement {
    data class TextSegment(val text: String, val startIndex: Int) : NoteContentElement
    data class InlinePhoto(val uri: Uri, val tagRange: IntRange) : NoteContentElement
}

@Composable
private fun NoteTextSegmentField(
    text: String,
    startIndex: Int,
    isLastElement: Boolean,
    focusRequester: FocusRequester,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextChange: (newText: String, localCursor: Int) -> Unit,
    onSelectionChange: ((localSelection: TextRange) -> Unit)? = null,
    onFocused: () -> Unit,
    onCursorPositionChanged: ((LayoutCoordinates, TextLayoutResult, Int) -> Unit)? = null
) {
    val colors = LocalUtilitarianColors.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var tfv by remember(startIndex) {
        mutableStateOf(TextFieldValue(text = text, selection = TextRange(text.length)))
    }
    var segmentCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var segmentLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    LaunchedEffect(text) {
        if (tfv.text != text) {
            val safeSelection = TextRange(tfv.selection.start.coerceIn(0, text.length))
            tfv = tfv.copy(text = text, selection = safeSelection)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isLastElement) {
                    Modifier.defaultMinSize(minHeight = 48.dp)
                } else {
                    Modifier.defaultMinSize(minHeight = 40.dp)
                }
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        tfv = tfv.copy(selection = TextRange(tfv.text.length))
                        focusRequester.requestFocus()
                        onFocused()
                        keyboardController?.show()
                    }
                } else Modifier
            )
    ) {
        BasicTextField(
            value = tfv,
            readOnly = readOnly,
            enabled = enabled,
            onValueChange = { incomingTfv ->
                if (enabled) {
                    val newTfv = NoteAutoNumbering.handleAutoNumbering(tfv, incomingTfv)
                    val textChanged = newTfv.text != tfv.text
                    tfv = newTfv
                    if (textChanged) {
                        onTextChange(newTfv.text, newTfv.selection.start)
                    } else {
                        onSelectionChange?.invoke(newTfv.selection)
                    }
                    segmentCoords?.let { coords ->
                        segmentLayoutResult?.let { layout ->
                            onCursorPositionChanged?.invoke(coords, layout, newTfv.selection.end)
                        }
                    }
                }
            },
            onTextLayout = { layoutResult ->
                segmentLayoutResult = layoutResult
            },
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned { segmentCoords = it }
                .onFocusChanged { state ->
                    if (state.isFocused) {
                        onFocused()
                    }
                }
                .padding(vertical = 4.dp),
            textStyle = TextStyle(
                color = colors.textPrimaryMuted,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 26.sp,
                lineBreak = LineBreak.Simple
            ),
            cursorBrush = SolidColor(colors.textPrimaryMuted),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun NotePhotoCard(
    uri: Uri,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            ImageUtils.loadSampledBitmapFromUri(context, uri, reqWidth = 900, reqHeight = 900)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.cardBackground)
            .border(1.dp, colors.borderStroke, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = "Note photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading photo...", color = colors.textSecondary, fontSize = 13.sp)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.65f))
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                .clickable(onClick = onDelete),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Remove photo",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}



@Composable
private fun FullPhotoPreviewDialog(
    photoUri: Uri,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember(photoUri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        bitmap = withContext(Dispatchers.IO) {
            ImageUtils.loadSampledBitmapFromUri(context, photoUri, reqWidth = 1080, reqHeight = 1920)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            val currentBitmap = bitmap
            if (currentBitmap != null) {
                Image(
                    bitmap = currentBitmap.asImageBitmap(),
                    contentDescription = "Full photo preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262626))
                    .border(1.dp, Color(0xFF404040), CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Close preview",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private val AUDIO_TAG_REGEX = Regex("""!\[audio\]\(([^)]+)\)""")

@Composable
private fun NoteAudioCard(
    audioUri: Uri,
    index: Int,
    onDelete: () -> Unit,
    onAudioUpdated: (oldUri: Uri, newUri: Uri) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val colors = LocalUtilitarianColors.current

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showTrimDialog by remember { mutableStateOf(false) }

    val playbackManager = remember(context) {
        (context.applicationContext as? ContinueApplication)?.container?.audioPlaybackManager
    }
    val activePlaybackState by playbackManager?.state?.collectAsState() ?: remember { mutableStateOf(AudioPlaybackState()) }

    val isCurrentAudio = activePlaybackState.currentUri == audioUri
    val isPlaying = isCurrentAudio && activePlaybackState.isPlaying
    var totalDurationMs by remember { mutableIntStateOf(0) }
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubProgress by remember { mutableFloatStateOf(0f) }
    var lastSeekTimestamp by remember { mutableLongStateOf(0L) }

    val currentPositionMs = if (isCurrentAudio) activePlaybackState.currentPositionMs else 0

    LaunchedEffect(audioUri) {
        val duration = withContext(Dispatchers.IO) {
            runCatching {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, audioUri)
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                durStr?.toIntOrNull() ?: 0
            }.getOrDefault(0)
        }
        if (duration > 0) totalDurationMs = duration
    }

    LaunchedEffect(isCurrentAudio, activePlaybackState.totalDurationMs) {
        if (isCurrentAudio && activePlaybackState.totalDurationMs > 0) {
            totalDurationMs = activePlaybackState.totalDurationMs
        }
    }

    val seekToFraction: (Float) -> Unit = { fraction ->
        val safeFraction = fraction.coerceIn(0f, 1f)
        val targetMs = NoteAudioPlaybackHelper.calculateSeekPosition(safeFraction, totalDurationMs)
        if (isCurrentAudio) {
            playbackManager?.seekTo(targetMs)
        } else {
            playbackManager?.play(audioUri, "Recording $index", targetMs)
        }
    }

    val throttleSeek: (Float) -> Unit = { fraction ->
        val now = System.currentTimeMillis()
        if (now - lastSeekTimestamp > 70L) {
            lastSeekTimestamp = now
            seekToFraction(fraction)
        }
    }

    val togglePlayback: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        playbackManager?.togglePlayPause(audioUri, "Recording $index")
    }

    val progress = NoteAudioPlaybackHelper.calculateProgress(currentPositionMs, totalDurationMs)
    val effectiveProgress = if (isScrubbing) scrubProgress else progress
    val displayMs = if (isScrubbing) {
        NoteAudioPlaybackHelper.calculateSeekPosition(scrubProgress, totalDurationMs)
    } else {
        currentPositionMs
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E2225))
            .border(1.dp, Color(0xFF33383F), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Play / Pause Circle
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) Color(0xFFD71921).copy(alpha = 0.15f) else Color(0xFF2C3136))
                    .border(
                        1.dp,
                        if (isPlaying) Color(0xFFD71921) else Color(0xFF3F454C),
                        CircleShape
                    )
                    .clickable(onClick = togglePlayback),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isPlaying) Color(0xFFD71921) else colors.textPrimaryMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Metadata & Progress
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recording $index",
                        color = colors.textPrimaryMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isPlaying || displayMs > 0 || isScrubbing) {
                            "${NoteAudioPlaybackHelper.formatDuration(displayMs)} / ${NoteAudioPlaybackHelper.formatDuration(totalDurationMs)}"
                        } else {
                            NoteAudioPlaybackHelper.formatDuration(totalDurationMs)
                        },
                        color = if (isScrubbing) Color(0xFFD71921) else colors.textPrimaryMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .pointerInput(totalDurationMs) {
                            if (totalDurationMs <= 0) return@pointerInput
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                isScrubbing = true
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                val barWidth = size.width.toFloat()
                                val currentFraction = if (barWidth > 0f) (down.position.x / barWidth).coerceIn(0f, 1f) else 0f
                                scrubProgress = currentFraction
                                seekToFraction(currentFraction)

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break
                                    if (!change.pressed) {
                                        change.consume()
                                        break
                                    }
                                    change.consume()
                                    val moveFraction = if (barWidth > 0f) (change.position.x / barWidth).coerceIn(0f, 1f) else 0f
                                    scrubProgress = moveFraction
                                    throttleSeek(moveFraction)
                                }
                                seekToFraction(scrubProgress)
                                isScrubbing = false
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    val widthPx = constraints.maxWidth.toFloat()
                    val thumbSize = if (isScrubbing) 12.dp else 8.dp

                    // Background Track
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF2C3136))
                    )

                    // Active Progress Fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = effectiveProgress)
                            .height(4.dp)
                            .align(Alignment.CenterStart)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPlaying || isScrubbing) Color(0xFFD71921) else colors.textPrimaryMuted)
                    )

                    // Scrubber Thumb Indicator
                    val density = LocalDensity.current
                    val thumbOffsetDp = with(density) {
                        ((effectiveProgress * widthPx) - (thumbSize.toPx() / 2f))
                            .coerceIn(0f, (widthPx - thumbSize.toPx()).coerceAtLeast(0f))
                            .toDp()
                    }

                    Box(
                        modifier = Modifier
                            .offset(x = thumbOffsetDp)
                            .align(Alignment.CenterStart)
                            .size(thumbSize)
                            .clip(CircleShape)
                            .background(if (isPlaying || isScrubbing) Color(0xFFD71921) else colors.textPrimaryMuted)
                            .border(
                                width = 1.dp,
                                color = if (isScrubbing) Color.White else Color(0xFF1E2225),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 3-Dot Overflow Menu
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Recording options",
                    tint = colors.textPrimaryMuted.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(Color(0xFF1E2225))
                        .border(1.dp, Color(0xFF33383F), RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("Edit", color = colors.textPrimary, fontSize = 14.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.ContentCut,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            showTrimDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text("Delete", color = Color(0xFFD71921), fontSize = 14.sp)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = Color(0xFFD71921),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = {
                            showMenu = false
                            showDeleteConfirmDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete recording?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this voice recording? This action cannot be undone.",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isCurrentAudio) {
                            playbackManager?.stop()
                        }
                        showDeleteConfirmDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = Color(0xFFD71921), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E2225),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showTrimDialog) {
        AudioTrimDialog(
            audioUri = audioUri,
            initialDurationMs = totalDurationMs,
            onDismiss = { showTrimDialog = false },
            onTrimSuccess = { newUri ->
                if (isCurrentAudio) {
                    playbackManager?.stop()
                }
                onAudioUpdated(audioUri, newUri)
            }
        )
    }
}


