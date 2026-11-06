package com.shejan.acontinue.presentation.search

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FormatColorReset
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.shejan.acontinue.core.util.ImageUtils
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.presentation.create.DEFAULT_NOTE_COLOR_HEX
import com.shejan.acontinue.presentation.create.NOTE_COLORS
import com.shejan.acontinue.presentation.create.isDefaultNoteColor
import com.shejan.acontinue.presentation.diary.DiaryNoteItem
import com.shejan.acontinue.presentation.diary.NoteCardPreviewElement
import com.shejan.acontinue.presentation.diary.NoteCardPreviewParser
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianCardBackground
import com.shejan.acontinue.ui.theme.UtilitarianTextDisabled
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary
import com.shejan.acontinue.ui.theme.UtilitarianTextSecondary

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNoteClick: (activityId: Long, noteId: Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalUtilitarianColors.current
    val haptic = LocalHapticFeedback.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()
    var titleHeightPx by remember { mutableFloatStateOf(0f) }
    var headerHeightPx by remember { mutableFloatStateOf(0f) }
    var headerOffsetPx by rememberSaveable { mutableFloatStateOf(0f) }
    var animationJob by remember { mutableStateOf<Job?>(null) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (headerHeightPx <= 0f) return Offset.Zero

                animationJob?.cancel()
                animationJob = null

                val isAtTop = lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset <= 2
                val maxAllowedOffset = if (titleHeightPx > 0f && !isAtTop) {
                    -titleHeightPx
                } else {
                    0f
                }

                val delta = available.y
                val prevOffset = headerOffsetPx
                headerOffsetPx = (headerOffsetPx + delta).coerceIn(-headerHeightPx, maxAllowedOffset)
                val consumedY = headerOffsetPx - prevOffset
                return Offset(0f, consumedY)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (headerHeightPx > 0f) {
                    val isAtTop = lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset <= 2
                    val minOffset = -headerHeightPx
                    val maxOffset = if (titleHeightPx > 0f && !isAtTop) -titleHeightPx else 0f

                    if (headerOffsetPx > minOffset && headerOffsetPx < maxOffset) {
                        val midPoint = (minOffset + maxOffset) / 2f
                        val targetOffset = if (available.y < -300f) {
                            minOffset
                        } else if (available.y > 300f) {
                            maxOffset
                        } else if (headerOffsetPx > midPoint) {
                            maxOffset
                        } else {
                            minOffset
                        }

                        animationJob?.cancel()
                        animationJob = coroutineScope.launch {
                            animate(
                                initialValue = headerOffsetPx,
                                targetValue = targetOffset,
                                animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
                            ) { value, _ ->
                                headerOffsetPx = value
                            }
                        }
                    }
                }
                return Velocity.Zero
            }
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (index == 0 && offset <= 2) {
                    if (headerOffsetPx < 0f && animationJob?.isActive != true) {
                        animationJob = launch {
                            animate(
                                initialValue = headerOffsetPx,
                                targetValue = 0f,
                                animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                            ) { value, _ ->
                                headerOffsetPx = value
                            }
                        }
                    }
                }
            }
    }

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                text = uiState.searchQuery,
                selection = TextRange(uiState.searchQuery.length)
            )
        )
    }

    LaunchedEffect(uiState.searchQuery) {
        if (textFieldValue.text != uiState.searchQuery) {
            textFieldValue = TextFieldValue(
                text = uiState.searchQuery,
                selection = TextRange(uiState.searchQuery.length)
            )
            animationJob?.cancel()
            animationJob = null
            headerOffsetPx = 0f
        }
    }

    LaunchedEffect(Unit) {
        textFieldValue = textFieldValue.copy(
            selection = TextRange(textFieldValue.text.length)
        )
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
    ) {
        val isFilterOrSearchActive = uiState.searchQuery.isNotBlank() ||
            uiState.selectedTypes.isNotEmpty() ||
            uiState.selectedLabels.isNotEmpty() ||
            uiState.selectedColors.isNotEmpty()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val currentHeight = if (headerHeightPx > 0f) {
                        (placeable.height + headerOffsetPx.roundToInt()).coerceIn(0, placeable.height)
                    } else {
                        placeable.height
                    }
                    layout(placeable.width, currentHeight) {
                        placeable.placeRelative(0, headerOffsetPx.roundToInt())
                    }
                }
                .onSizeChanged { size ->
                    if (size.height > 0) {
                        headerHeightPx = size.height.toFloat()
                    }
                }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Title portion container (measured so search bar can align at top when title is collapsed)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            if (size.height > 0) {
                                titleHeightPx = size.height.toFloat()
                            }
                        }
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. DotMatrix Title Header with Note-Page Styled Left Back Button
                    DotMatrixHeader(
                        title = "SEARCH",
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onNavigateBack()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colors.textPrimaryMuted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        badgeText = if (isFilterOrSearchActive && uiState.results.isNotEmpty()) {
                            "${uiState.results.size} FOUND"
                        } else null
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 2. Utilitarian Search Bar Container with Crisp 1dp Border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, colors.borderStroke, RoundedCornerShape(8.dp))
                        .background(Color(0xFF161616))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = colors.textPrimaryMuted,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (textFieldValue.text.isEmpty()) {
                                Text(
                                    text = "Search notes and labels...",
                                    color = colors.textPrimaryMuted,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            BasicTextField(
                                value = textFieldValue,
                                onValueChange = { newTfv ->
                                    textFieldValue = newTfv
                                    animationJob?.cancel()
                                    animationJob = null
                                    headerOffsetPx = 0f
                                    if (newTfv.text != uiState.searchQuery) {
                                        viewModel.onSearchQueryChange(newTfv.text)
                                    }
                                },
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Normal
                                ),
                                cursorBrush = SolidColor(colors.textPrimary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester)
                            )
                        }

                        if (textFieldValue.text.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        textFieldValue = TextFieldValue("", TextRange.Zero)
                                        animationJob?.cancel()
                                        animationJob = null
                                        headerOffsetPx = 0f
                                        viewModel.onSearchQueryChange("")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Clear search",
                                    tint = colors.textPrimaryMuted,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        val isSearchingByText = uiState.searchQuery.isNotBlank()

        // 3. Search Results List / Discovery Filter Sections
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp)
        ) {
            if (!isSearchingByText) {
                // Section 1: TYPES
                item(key = "search_section_types") {
                    Spacer(modifier = Modifier.height(8.dp))
                    SearchSectionHeader(title = "TYPES")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SearchFilterPill(
                            label = "Lists",
                            icon = Icons.Outlined.CheckBox,
                            isSelected = uiState.selectedTypes.contains(SearchNoteType.LISTS),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleTypeFilter(SearchNoteType.LISTS)
                                keyboardController?.hide()
                            }
                        )
                        SearchFilterPill(
                            label = "URLs",
                            icon = Icons.Outlined.Link,
                            isSelected = uiState.selectedTypes.contains(SearchNoteType.URLS),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleTypeFilter(SearchNoteType.URLS)
                                keyboardController?.hide()
                            }
                        )
                        SearchFilterPill(
                            label = "Audio",
                            icon = Icons.Outlined.Mic,
                            isSelected = uiState.selectedTypes.contains(SearchNoteType.AUDIO),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleTypeFilter(SearchNoteType.AUDIO)
                                keyboardController?.hide()
                            }
                        )
                        SearchFilterPill(
                            label = "Photos",
                            icon = Icons.Outlined.Photo,
                            isSelected = uiState.selectedTypes.contains(SearchNoteType.PHOTOS),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleTypeFilter(SearchNoteType.PHOTOS)
                                keyboardController?.hide()
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Section 2: LABELS
                item(key = "search_section_labels") {
                    SearchSectionHeader(title = "LABELS")
                    if (uiState.availableLabels.isEmpty()) {
                        Text(
                            text = "No labels",
                            color = colors.textDisabled,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.availableLabels.forEach { labelName ->
                                SearchFilterPill(
                                    label = labelName,
                                    icon = Icons.AutoMirrored.Outlined.Label,
                                    isSelected = uiState.selectedLabels.contains(labelName),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.toggleLabelFilter(labelName)
                                        keyboardController?.hide()
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Section 3: COLORS
                item(key = "search_section_colors") {
                    SearchSectionHeader(title = "COLORS")
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val colorRows = remember { NOTE_COLORS.chunked(5) }
                        colorRows.forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowColors.forEach { colorHex ->
                                    val isSelected = uiState.selectedColors.contains(colorHex)
                                    SearchColorFilterPill(
                                        colorHex = colorHex,
                                        isSelected = isSelected,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.toggleColorFilter(colorHex)
                                            keyboardController?.hide()
                                        }
                                    )
                                }
                                repeat(5 - rowColors.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (isFilterOrSearchActive) {
                if (uiState.results.isEmpty() && !uiState.isLoading) {
                    item(key = "search_no_results") {
                        MonochromeCard {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No matching notes found.",
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Try selecting different filters or keywords.",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.results, key = { "search_note_${it.activityId}_${it.noteId}" }) { note ->
                        SearchNoteItemCard(
                            note = note,
                            onClick = {
                                onNoteClick(note.activityId, note.noteId)
                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    val colors = LocalUtilitarianColors.current
    Text(
        text = title.uppercase(),
        color = colors.textSecondary,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
private fun SearchFilterPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val colors = LocalUtilitarianColors.current
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = Color(0xFF161616),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) colors.textPrimary else colors.borderStroke
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.textPrimary else colors.textPrimaryMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SearchColorFilterPill(
    colorHex: Long,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val colors = LocalUtilitarianColors.current
    val isDefault = isDefaultNoteColor(colorHex)
    val bgColor = if (isDefault) Color(0xFF161616) else Color(colorHex)
    val borderColor = if (isSelected) {
        colors.textPrimary
    } else if (isDefault) {
        colors.borderStroke
    } else {
        Color.White.copy(alpha = 0.25f)
    }

    Surface(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isDefault) {
                Icon(
                    imageVector = Icons.Outlined.FormatColorReset,
                    contentDescription = "Default / No color",
                    tint = if (isSelected) colors.textPrimary else colors.textPrimaryMuted,
                    modifier = Modifier.size(16.dp)
                )
            } else if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Selected color",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchPhotoThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var bitmap by remember(uri) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(uri) {
        bitmap = withContext(Dispatchers.IO) {
            ImageUtils.loadSampledBitmapFromUri(context, uri, reqWidth = 600, reqHeight = 600)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF161616))
    ) {
        val currentBitmap = bitmap
        if (currentBitmap != null) {
            Image(
                bitmap = currentBitmap.asImageBitmap(),
                contentDescription = "Note image preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Photo,
                    contentDescription = null,
                    tint = UtilitarianTextDisabled,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun SearchAudioRows(
    audioCount: Int,
    hasCustomColor: Boolean,
    modifier: Modifier = Modifier
) {
    if (audioCount <= 0) return
    val count = minOf(audioCount, 4)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 1..count) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Audio recording",
                    tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recording $i",
                    color = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (audioCount > 4) {
            val remaining = audioCount - 4
            Text(
                text = "+ $remaining more",
                color = if (hasCustomColor) Color.White.copy(alpha = 0.6f) else UtilitarianTextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchNoteItemCard(
    note: DiaryNoteItem,
    onClick: () -> Unit
) {
    val parsedChecklistItems = remember(note.content, note.previewText, note.isChecklist) {
        if (!note.isChecklist) return@remember null
        val raw = if (note.previewText.isNotBlank()) note.previewText else note.content
        val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isNotEmpty() && lines.all { it.startsWith("[ ] ") || it.startsWith("[x] ", ignoreCase = true) || it.startsWith("[X] ") }) {
            lines.map { line ->
                val isChecked = line.startsWith("[x] ", ignoreCase = true) || line.startsWith("[X] ")
                val text = if (line.length >= 4) line.substring(4).trim() else ""
                isChecked to text
            }
        } else {
            null
        }
    }
    val hasChecklist = !parsedChecklistItems.isNullOrEmpty()
    val hasTitle = note.title.isNotBlank() && !note.title.equals("Untitled Note", ignoreCase = true)
    val hasContent = note.previewText.isNotBlank() && !hasChecklist
    val hasLabels = note.labels.isNotEmpty()
    val hasAudio = note.audioCount > 0
    val hasPhoto = note.photoCount > 0
    val isMediaOnly = (hasAudio || hasPhoto) && !hasContent && !hasChecklist

    val firstPhotoUri = remember(note.content, hasPhoto, isMediaOnly) {
        if (hasPhoto && isMediaOnly) {
            NoteCardPreviewParser.PHOTO_TAG_REGEX.find(note.content)?.groupValues?.getOrNull(1)?.let { uriStr ->
                runCatching { Uri.parse(uriStr) }.getOrNull()
            }
        } else {
            null
        }
    }

    val parsedCardPreview = remember(note.content, note.photoCount, isMediaOnly, hasChecklist) {
        if (hasPhoto && !isMediaOnly && !hasChecklist) {
            NoteCardPreviewParser.parse(content = note.content, photoCount = note.photoCount, maxLines = 8)
        } else {
            null
        }
    }

    val displayContent = remember(note.previewText, hasContent, parsedCardPreview) {
        if (!hasContent || parsedCardPreview != null) {
            ""
        } else {
            val lines = note.previewText.lines()
            if (lines.size > 8) {
                val first7 = lines.take(7)
                val eighth = lines[7].trimEnd()
                val eighthWithDots = if (eighth.endsWith("...")) eighth else "$eighth..."
                (first7 + eighthWithDots).joinToString("\n")
            } else {
                note.previewText
            }
        }
    }

    val hasCustomColor = !isDefaultNoteColor(note.colorHex)
    val baseBorderColor = if (hasCustomColor) {
        Color.White.copy(alpha = 0.15f)
    } else {
        UtilitarianBorderStroke
    }

    val cardShape = RoundedCornerShape(12.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .clickable(onClick = onClick),
        shape = cardShape,
        color = if (hasCustomColor) Color(note.colorHex) else UtilitarianCardBackground,
        border = BorderStroke(1.dp, baseBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isMediaOnly) {
                if (hasTitle) {
                    Text(
                        text = note.title,
                        color = UtilitarianTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (hasPhoto && firstPhotoUri != null) {
                    SearchPhotoThumbnail(uri = firstPhotoUri)
                    if (hasAudio) {
                        Spacer(modifier = Modifier.height(6.dp))
                        SearchAudioRows(
                            audioCount = note.audioCount,
                            hasCustomColor = hasCustomColor
                        )
                    }
                } else if (hasAudio) {
                    SearchAudioRows(
                        audioCount = note.audioCount,
                        hasCustomColor = hasCustomColor
                    )
                }
            } else {
                if (hasTitle) {
                    Text(
                        text = note.title,
                        color = UtilitarianTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (hasTitle && (hasContent || hasChecklist)) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (parsedChecklistItems != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val displayItems = parsedChecklistItems.take(8)
                        displayItems.forEach { (isChecked, itemText) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (isChecked) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank,
                                    contentDescription = if (isChecked) "Completed" else "To do",
                                    tint = if (isChecked) {
                                        Color(0xFF3897F0)
                                    } else {
                                        if (hasCustomColor) Color.White.copy(alpha = 0.7f) else UtilitarianTextSecondary
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = itemText,
                                    color = if (isChecked) {
                                        if (hasCustomColor) Color.White.copy(alpha = 0.5f) else UtilitarianTextDisabled
                                    } else {
                                        UtilitarianTextPrimary
                                    },
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (parsedChecklistItems.size > 8) {
                            val remaining = parsedChecklistItems.size - 8
                            Text(
                                text = "+ $remaining more",
                                color = if (hasCustomColor) Color.White.copy(alpha = 0.6f) else UtilitarianTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 24.dp, top = 2.dp)
                            )
                        }
                    }
                } else if (hasContent) {
                    if (parsedCardPreview != null && parsedCardPreview.elements.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            parsedCardPreview.elements.forEach { elem ->
                                when (elem) {
                                    is NoteCardPreviewElement.TextSegment -> {
                                        Text(
                                            text = elem.text,
                                            color = UtilitarianTextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    is NoteCardPreviewElement.PhotoLine -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(20.dp)
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Photo,
                                                contentDescription = "Photo attached",
                                                tint = if (hasCustomColor) Color.White.copy(alpha = 0.85f) else UtilitarianTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = displayContent,
                            color = UtilitarianTextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (hasLabels && (hasTitle || hasContent || hasChecklist || isMediaOnly)) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (hasLabels) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    note.labels.forEach { label ->
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = if (hasCustomColor) Color.White.copy(alpha = 0.2f) else UtilitarianBorderStroke,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .background(
                                    if (hasCustomColor) Color.Black.copy(alpha = 0.25f) else Color(0xFF161616),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (hasCustomColor) Color.White.copy(alpha = 0.9f) else UtilitarianTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            if (!isMediaOnly) {
                val hiddenPhotoCount = parsedCardPreview?.hiddenPhotoCount ?: (if (hasPhoto) note.photoCount else 0)
                val hasMedia = note.audioCount > 0 || hiddenPhotoCount > 0
                if (hasMedia) {
                    if (hasTitle || hasContent || hasLabels) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.audioCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Mic,
                                    contentDescription = "Audio attached",
                                    tint = UtilitarianTextDisabled,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${note.audioCount}",
                                    color = UtilitarianTextDisabled,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        if (hiddenPhotoCount > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Photo,
                                    contentDescription = "Photo attached",
                                    tint = UtilitarianTextDisabled,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+ $hiddenPhotoCount",
                                    color = UtilitarianTextDisabled,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

