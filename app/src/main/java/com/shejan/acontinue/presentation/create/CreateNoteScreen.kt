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
import com.shejan.acontinue.core.util.AttachmentHelper
import com.shejan.acontinue.core.util.AudioRecordHelper
import com.shejan.acontinue.core.util.ImageUtils
import java.io.File
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FormatColorText
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.runtime.mutableIntStateOf
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
    initialColorHex: Long = 0xFF121212L,
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

    val parsedAudioUris = remember(initialContent) {
        AUDIO_TAG_REGEX.findAll(initialContent).mapNotNull { match ->
            runCatching { Uri.parse(match.groupValues[1]) }.getOrNull()
        }.toList()
    }
    val initialCleanContent = remember(initialContent) {
        initialContent.replace(AUDIO_TAG_REGEX, "").trimEnd()
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

    var title by remember { mutableStateOf(initialTitle) }
    var noteTextState by remember {
        mutableStateOf(
            if (initialParsedChecklist != null) TextFieldValue("") else TextFieldValue(initialCleanContent)
        )
    }
    var formatState by remember { mutableStateOf(NoteFormatState()) }
    var selectedColorHex by remember { mutableLongStateOf(initialColorHex) }
    var isChecklistMode by remember { mutableStateOf(initialParsedChecklist != null) }
    var checklistItems by remember { mutableStateOf(initialParsedChecklist ?: listOf<ChecklistItem>()) }
    val checklistFocusRequesters = remember { mutableMapOf<Long, FocusRequester>() }
    var targetFocusChecklistId by remember { mutableStateOf<Long?>(null) }
    var draggingChecklistId by remember { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

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
    var showFormattingBar by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var showDeleteNoteConfirmDialog by remember { mutableStateOf(false) }
    var showFindBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var activeMatchIndex by remember { mutableIntStateOf(0) }
    val searchFocusRequester = remember { FocusRequester() }

    var targetCursorPosition by remember { mutableIntStateOf(initialCleanContent.length) }
    var capturedPhotoFile by remember { mutableStateOf<File?>(null) }
    var selectedPreviewPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var autoFocusAfterCaptureTrigger by remember { mutableIntStateOf(0) }

    val audioRecordHelper = remember { AudioRecordHelper(context) }
    var isRecordingIslandVisible by remember { mutableStateOf(false) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var isRecordingPaused by remember { mutableStateOf(false) }
    var recordingElapsedMillis by remember { mutableLongStateOf(0L) }

    val islandScale = remember { Animatable(0f) }
    val islandWidthDp = remember { Animatable(44f) }
    val islandContentAlpha = remember { Animatable(0f) }

    val searchMatches = remember(noteTextState.text, searchQuery) {
        if (searchQuery.isBlank()) emptyList() else NoteTextFormatter.findMatches(noteTextState.text, searchQuery)
    }
    val safeActiveMatchIndex = if (searchMatches.isEmpty()) -1 else activeMatchIndex.coerceIn(0, searchMatches.size - 1)
    val activeMatchRange = if (safeActiveMatchIndex in searchMatches.indices) searchMatches[safeActiveMatchIndex] else null

    val initialSnapshot = remember {
        NoteEditorSnapshot(
            title = initialTitle,
            noteTextState = if (initialParsedChecklist != null) TextFieldValue("") else TextFieldValue(initialCleanContent),
            formatState = NoteFormatState(),
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
            showFormattingBar = false
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

    LaunchedEffect(isRecordingAudio, isRecordingPaused) {
        if (isRecordingAudio && !isRecordingPaused) {
            var lastTime = System.currentTimeMillis()
            while (isRecordingAudio && !isRecordingPaused) {
                kotlinx.coroutines.delay(33)
                val now = System.currentTimeMillis()
                recordingElapsedMillis += (now - lastTime)
                lastTime = now
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (audioRecordHelper.isRecording) {
                audioRecordHelper.stopRecording()
            }
            audioRecordHelper.release()
        }
    }

    val startRecordingWithIslandAnimation: () -> Unit = {
        coroutineScope.launch {
            val attachmentsDir = File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }
            val audioFile = File(attachmentsDir, "note_audio_${System.currentTimeMillis()}.m4a")

            // 1. Show island, reset state, pop circle in at top center
            isRecordingIslandVisible = true
            isRecordingPaused = false
            recordingElapsedMillis = 0L
            islandScale.snapTo(0f)
            islandWidthDp.snapTo(44f)
            islandContentAlpha.snapTo(0f)

            // Circle pops in with bouncy spring
            islandScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )

            // 2. Extend horizontally outward left and right into full dynamic island
            val expandJob = launch {
                islandWidthDp.animateTo(
                    targetValue = 296f,
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

            // 3. Audio recording begins after circle has extended into pill
            if (audioRecordHelper.startRecording(audioFile)) {
                isRecordingAudio = true
            } else {
                Toast.makeText(context, "Could not start recording", Toast.LENGTH_SHORT).show()
                islandScale.animateTo(0f, tween(100))
                isRecordingIslandVisible = false
            }
        }
    }

    val stopAndSaveAudioRecording: () -> Unit = {
        coroutineScope.launch {
            val file = audioRecordHelper.stopRecording()
            isRecordingAudio = false
            isRecordingPaused = false

            // Collapse animation: fade controls, retract horizontally to circle, shrink scale to 0
            val fadeJob = launch { islandContentAlpha.animateTo(0f, tween(100)) }
            islandWidthDp.animateTo(44f, tween(180, easing = LinearOutSlowInEasing))
            fadeJob.join()
            islandScale.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
            isRecordingIslandVisible = false

            if (file != null && file.exists() && file.length() > 0L) {
                val audioUri = Uri.fromFile(file)
                val updated = recordedAudioUris + audioUri
                recordedAudioUris = updated
                undoManager.recordAction(takeSnapshot(newRecordedAudioUris = updated))
            }
        }
    }

    val togglePauseResumeRecording: () -> Unit = {
        if (isRecordingPaused) {
            if (audioRecordHelper.resumeRecording()) {
                isRecordingPaused = false
            }
        } else {
            if (audioRecordHelper.pauseRecording()) {
                isRecordingPaused = true
            }
        }
    }

    val cancelAudioRecording: () -> Unit = {
        coroutineScope.launch {
            audioRecordHelper.cancelRecording()
            isRecordingAudio = false
            isRecordingPaused = false
            val fadeJob = launch { islandContentAlpha.animateTo(0f, tween(100)) }
            islandWidthDp.animateTo(44f, tween(180, easing = LinearOutSlowInEasing))
            fadeJob.join()
            islandScale.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
            isRecordingIslandVisible = false
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecordingWithIslandAnimation()
        } else {
            Toast.makeText(context, "Microphone permission required for voice recording", Toast.LENGTH_SHORT).show()
        }
    }

    val requestRecording: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startRecordingWithIslandAnimation()
        } else {
            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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
        recordedAudioUris.forEach { audioUri ->
            stringBuilder.append("\n\n![audio]($audioUri)")
        }
        stringBuilder.toString().trim()
    }

    val handleExit = {
        if (isRecordingIslandVisible) {
            val file = audioRecordHelper.stopRecording()
            isRecordingAudio = false
            isRecordingIslandVisible = false
            if (file != null && file.exists() && file.length() > 0L) {
                val audioUri = Uri.fromFile(file)
                recordedAudioUris = recordedAudioUris + audioUri
            }
        }
        val finalContent = buildFinalContent()
        if (title.isNotBlank() || finalContent.isNotBlank() || attachedImageUris.isNotEmpty() || recordedAudioUris.isNotEmpty() || selectedLabels.isNotEmpty()) {
            onSaveNote(title, finalContent, selectedColorHex, selectedLabels.toList())
        } else {
            onDiscard()
        }
    }

    BackHandler {
        if (isRecordingIslandVisible) {
            stopAndSaveAudioRecording()
        } else if (selectedPreviewPhotoUri != null) {
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
                .imePadding()
                .navigationBarsPadding()
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
                .onGloballyPositioned { scrollContainerCoordinates = it }
                .verticalScroll(scrollState)
                .clickable(
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
                    .onFocusChanged {
                        isTitleFocused = it.isFocused
                        if (it.isFocused) {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0, tween(150, easing = LinearOutSlowInEasing))
                            }
                        }
                    },
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
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                contentFocusRequester.requestFocus()
                                noteTextState = noteTextState.copy(selection = TextRange(noteTextState.text.length))
                                targetCursorPosition = noteTextState.text.length
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
                                targetCursorPosition = newTfv.selection.start
                                undoManager.recordTyping(takeSnapshot(newNoteTextState = newTfv, newFormatState = newFormatState))
                                singleFieldCoords?.let { coords ->
                                    singleLayoutResult?.let { layout ->
                                        smoothScrollCursorIntoView(coords, layout, newTfv.selection.end)
                                    }
                                }
                            },
                            onTextLayout = { layoutResult ->
                                singleLayoutResult = layoutResult
                            },
                            visualTransformation = remember(formatState, colors.textPrimary, searchQuery, activeMatchRange) {
                                NoteVisualTransformation(formatState, colors.textPrimary, searchQuery, activeMatchRange)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(contentFocusRequester)
                                .onGloballyPositioned { singleFieldCoords = it }
                                .onFocusChanged { state ->
                                    isContentFocused = state.isFocused
                                },
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
                                        onTextChange = { newText, localCursor ->
                                            val start = element.startIndex
                                            val oldLen = element.text.length
                                            val newFullText = noteTextState.text.replaceRange(start, start + oldLen, newText)
                                            val updatedGlobalCursor = (start + localCursor).coerceIn(0, newFullText.length)
                                            val updated = TextFieldValue(text = newFullText, selection = TextRange(updatedGlobalCursor))
                                            noteTextState = updated
                                            targetCursorPosition = updatedGlobalCursor
                                            undoManager.recordTyping(takeSnapshot(newNoteTextState = updated))
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
                        var itemCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                        var itemLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                        val itemFocusRequester = checklistFocusRequesters.getOrPut(item.id) { FocusRequester() }
                        val isDragging = draggingChecklistId == item.id

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(if (isDragging) 10f else 1f)
                                .graphicsLayer {
                                    if (isDragging) {
                                        translationY = dragOffsetY
                                        scaleX = 1.02f
                                        scaleY = 1.02f
                                    }
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            // 6-Dot Drag Indicator Handle
                            Icon(
                                imageVector = Icons.Outlined.DragIndicator,
                                contentDescription = "Drag to reorder",
                                tint = colors.textSecondary.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .pointerInput(item.id) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                draggingChecklistId = item.id
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                val itemHeightPx = 48.dp.toPx()
                                                val currIdx = checklistItems.indexOfFirst { it.id == item.id }
                                                if (currIdx != -1) {
                                                    if (dragOffsetY > itemHeightPx * 0.7f && currIdx < checklistItems.lastIndex) {
                                                        val updated = checklistItems.toMutableList()
                                                        val moved = updated.removeAt(currIdx)
                                                        updated.add(currIdx + 1, moved)
                                                        checklistItems = updated
                                                        dragOffsetY -= itemHeightPx
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    } else if (dragOffsetY < -itemHeightPx * 0.7f && currIdx > 0) {
                                                        val updated = checklistItems.toMutableList()
                                                        val moved = updated.removeAt(currIdx)
                                                        updated.add(currIdx - 1, moved)
                                                        checklistItems = updated
                                                        dragOffsetY += itemHeightPx
                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                draggingChecklistId = null
                                                dragOffsetY = 0f
                                                undoManager.recordAction(takeSnapshot(newChecklistItems = checklistItems))
                                            },
                                            onDragCancel = {
                                                draggingChecklistId = null
                                                dragOffsetY = 0f
                                            }
                                        )
                                    }
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            // Checkbox
                            Icon(
                                imageVector = if (item.isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = if (item.isChecked) "Checked" else "Unchecked",
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

                            Spacer(modifier = Modifier.width(14.dp))

                            // Item Text Input
                            BasicTextField(
                                value = item.text,
                                onValueChange = { newText ->
                                    val updated = checklistItems.toMutableList().also {
                                        it[index] = item.copy(text = newText)
                                    }
                                    checklistItems = updated
                                    undoManager.recordTyping(takeSnapshot(newChecklistItems = updated))
                                    itemCoords?.let { coords ->
                                        itemLayoutResult?.let { layout ->
                                            smoothScrollCursorIntoView(coords, layout, newText.length)
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
                                        if (keyEvent.type == KeyEventType.KeyDown) {
                                            if (keyEvent.key == Key.Enter) {
                                                val newItem = ChecklistItem(text = "")
                                                val updated = checklistItems.toMutableList()
                                                val insertIdx = (index + 1).coerceAtMost(updated.size)
                                                updated.add(insertIdx, newItem)
                                                checklistItems = updated
                                                undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                                targetFocusChecklistId = newItem.id
                                                return@onKeyEvent true
                                            } else if (keyEvent.key == Key.Backspace && item.text.isEmpty() && checklistItems.size > 1) {
                                                val updated = checklistItems.filterIndexed { i, _ -> i != index }
                                                checklistItems = updated
                                                undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                                val prevIdx = (index - 1).coerceAtLeast(0)
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
                                        val newItem = ChecklistItem(text = "")
                                        val updated = checklistItems.toMutableList()
                                        val insertIdx = (index + 1).coerceAtMost(updated.size)
                                        updated.add(insertIdx, newItem)
                                        checklistItems = updated
                                        undoManager.recordAction(takeSnapshot(newChecklistItems = updated))
                                        targetFocusChecklistId = newItem.id
                                    }
                                ),
                                textStyle = TextStyle(
                                    color = if (item.isChecked) colors.textDisabled else colors.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
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

                            // "X" (Delete Item) Button
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        val updated = checklistItems.filterIndexed { i, _ -> i != index }
                                        if (updated.isEmpty()) {
                                            val emptyItem = ChecklistItem(text = "")
                                            checklistItems = listOf(emptyItem)
                                            targetFocusChecklistId = emptyItem.id
                                        } else {
                                            checklistItems = updated
                                            val nextFocusIdx = (index - 1).coerceAtLeast(0)
                                            targetFocusChecklistId = updated[nextFocusIdx].id
                                        }
                                        undoManager.recordAction(takeSnapshot(newChecklistItems = checklistItems))
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Remove item",
                                    tint = colors.textSecondary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // "+ List item" Row (Indented 34dp to align with checkboxes)
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
                            .padding(vertical = 8.dp)
                    ) {
                        Spacer(modifier = Modifier.width(34.dp))
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add item",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "List item",
                            color = colors.textSecondary,
                            fontSize = 16.sp
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

            // Recorded Voice Notes Card-Style View
            if (recordedAudioUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
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
                            }
                        )
                    }
                }
            }

            // Bottom overscroll buffer & tap-to-focus zone
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clickable(
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
            )
        }

        val isFormattingEnabled = !isTitleFocused && !isChecklistMode

        if (showFindBar) {
            // Find In Note Search Bar docked above keyboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
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
                            tint = colors.textPrimary,
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
                                            color = colors.textSecondary.copy(alpha = 0.6f),
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
                        tint = if (canNavigate) colors.textPrimary else colors.textDisabled.copy(alpha = 0.35f),
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
                        tint = if (canNavigate) colors.textPrimary else colors.textDisabled.copy(alpha = 0.35f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Close Button [X]
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF262C30))
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
                        tint = colors.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else if (showFormattingBar) {
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

    // Dynamic Island Recording Bar at top of page (direct child of root Box)
    if (isRecordingIslandVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(99f)
                .statusBarsPadding()
                .padding(top = 6.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            DynamicIslandRecordingBar(
                scale = islandScale.value,
                widthDp = islandWidthDp.value,
                contentAlpha = islandContentAlpha.value,
                elapsedMillis = recordingElapsedMillis,
                isPaused = isRecordingPaused,
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
            NoteMenuOption("Recording", Icons.Outlined.Mic) {
                coroutineScope.launch {
                    showBottomSheet = false
                    sheetState.hide()
                }
                requestRecording()
            },
            NoteMenuOption("Drawing", Icons.Outlined.Edit) {
                coroutineScope.launch {
                    sheetState.hide()
                    showBottomSheet = false
                    Toast.makeText(context, "Drawing feature", Toast.LENGTH_SHORT).show()
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
                    showFormattingBar = false
                    showFindBar = true
                }
            },
            NoteMenuOption("Make a copy", Icons.Outlined.ContentCopy) {
                coroutineScope.launch {
                    moreSheetState.hide()
                    showMoreSheet = false
                    val finalContent = buildFinalContent()
                    if (title.isNotBlank() || finalContent.isNotBlank() || attachedImageUris.isNotEmpty() || recordedAudioUris.isNotEmpty() || selectedLabels.isNotEmpty()) {
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
                    .background(Color(0xFF1E2225))
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
                                    if (onDeleteNote != null) {
                                        onDeleteNote()
                                    } else {
                                        onDiscard()
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
    onTextChange: (newText: String, localCursor: Int) -> Unit,
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                tfv = tfv.copy(selection = TextRange(tfv.text.length))
                focusRequester.requestFocus()
                onFocused()
                keyboardController?.show()
            }
    ) {
        BasicTextField(
            value = tfv,
            onValueChange = { newTfv ->
                tfv = newTfv
                onTextChange(newTfv.text, newTfv.selection.start)
                segmentCoords?.let { coords ->
                    segmentLayoutResult?.let { layout ->
                        onCursorPositionChanged?.invoke(coords, layout, newTfv.selection.end)
                    }
                }
            },
            onTextLayout = { layoutResult ->
                segmentLayoutResult = layoutResult
            },
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
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(colors.textPrimary),
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
private fun DynamicIslandRecordingBar(
    scale: Float,
    widthDp: Float,
    contentAlpha: Float,
    elapsedMillis: Long,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    fun formatElapsed(millis: Long): String {
        val totalCentis = millis / 10
        val centis = totalCentis % 100
        val totalSec = totalCentis / 100
        val sec = totalSec % 60
        val min = totalSec / 60
        return "%02d:%02d,%02d".format(min, sec, centis)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .width(widthDp.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color.Black)
            .border(1.dp, Color(0xFF242426), RoundedCornerShape(percent = 50))
            .padding(horizontal = 7.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Amber circular recording badge
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF241908))
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFFFF9F0A).copy(alpha = if (isPaused) 0.4f else pulseAlpha),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Recording",
                    tint = Color(0xFFFF9F0A).copy(alpha = if (isPaused) 0.5f else 1f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Right-side controls (revealed as pill expands)
            if (widthDp > 80f && contentAlpha > 0f) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(contentAlpha)
                        .padding(start = 10.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // Elapsed timer in warm amber (matching reference image)
                    Text(
                        text = formatElapsed(elapsedMillis),
                        color = Color(0xFFFF9F0A),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Pause / Resume Toggle Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1C1E))
                            .border(1.dp, Color(0xFF333336), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTogglePause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = if (isPaused) Color(0xFFFF9F0A) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Stop / Save Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1C1C1E))
                            .border(1.dp, Color(0xFF333336), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onStop()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = "Stop recording",
                            tint = Color(0xFFD71921),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteAudioCard(
    audioUri: Uri,
    index: Int,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val colors = LocalUtilitarianColors.current

    var isPlaying by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var totalDurationMs by remember { mutableIntStateOf(0) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

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

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            mediaPlayer?.let { player ->
                if (runCatching { player.isPlaying }.getOrDefault(false)) {
                    currentPositionMs = player.currentPosition
                }
            }
            kotlinx.coroutines.delay(200)
        }
    }

    DisposableEffect(audioUri) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (_: Exception) {}
            mediaPlayer = null
            isPlaying = false
        }
    }

    val togglePlayback: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (isPlaying) {
            try {
                mediaPlayer?.pause()
                isPlaying = false
            } catch (_: Exception) {}
        } else {
            try {
                if (mediaPlayer == null) {
                    val player = MediaPlayer.create(context, audioUri)
                    if (player != null) {
                        player.setOnCompletionListener {
                            isPlaying = false
                            currentPositionMs = 0
                        }
                        if (totalDurationMs == 0) {
                            totalDurationMs = player.duration
                        }
                        mediaPlayer = player
                    }
                }
                mediaPlayer?.start()
                isPlaying = true
            } catch (_: Exception) {
                isPlaying = false
            }
        }
    }

    fun formatMs(ms: Int): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val s = totalSec % 60
        return "%02d:%02d".format(m, s)
    }

    val progress = if (totalDurationMs > 0) {
        (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

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
                    tint = if (isPlaying) Color(0xFFD71921) else colors.textPrimary,
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
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isPlaying || currentPositionMs > 0) {
                            "${formatMs(currentPositionMs)} / ${formatMs(totalDurationMs)}"
                        } else {
                            formatMs(totalDurationMs)
                        },
                        color = colors.textSecondary.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF2C3136))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isPlaying) Color(0xFFD71921) else colors.textPrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Delete action
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        try {
                            mediaPlayer?.stop()
                            mediaPlayer?.release()
                        } catch (_: Exception) {}
                        mediaPlayer = null
                        isPlaying = false
                        onDelete()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete recording",
                    tint = colors.textSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}


