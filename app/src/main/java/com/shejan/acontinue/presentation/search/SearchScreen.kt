package com.shejan.acontinue.presentation.search

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.CheckBox
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.presentation.create.DEFAULT_NOTE_COLOR_HEX
import com.shejan.acontinue.presentation.create.NOTE_COLORS
import com.shejan.acontinue.presentation.diary.DiaryNoteItem
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
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
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
            badgeText = if ((uiState.searchQuery.isNotBlank() || uiState.selectedTypes.isNotEmpty()) && uiState.results.isNotEmpty()) {
                "${uiState.results.size} FOUND"
            } else null
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                    if (uiState.searchQuery.isEmpty()) {
                        Text(
                            text = "Search notes and labels...",
                            color = colors.textPrimaryMuted,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    BasicTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
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

                if (uiState.searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

        val isFilterOrSearchActive = uiState.searchQuery.isNotBlank() || uiState.selectedTypes.isNotEmpty()

        // 3. Search Results List / Discovery Filter Sections
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 48.dp)
        ) {
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

            if (!isFilterOrSearchActive) {
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
                                    isSelected = false,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.onSearchQueryChange(labelName)
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section 3: COLORS
                item(key = "search_section_colors") {
                    SearchSectionHeader(title = "COLORS")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SearchColorFilterPill(colorHex = null)
                        NOTE_COLORS.filter { it != DEFAULT_NOTE_COLOR_HEX }.forEach { colorHex ->
                            SearchColorFilterPill(colorHex = colorHex)
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else if (uiState.results.isEmpty() && !uiState.isLoading) {
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
    colorHex: Long?,
    onClick: () -> Unit = {}
) {
    val colors = LocalUtilitarianColors.current
    val isDefault = colorHex == null || colorHex == 0L || colorHex == 0xFF000000L || colorHex == 0xFF121212L
    val bgColor = if (isDefault) Color(0xFF161616) else Color(colorHex)
    Surface(
        modifier = Modifier
            .size(width = 48.dp, height = 30.dp)
            .clip(RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(percent = 50),
        color = bgColor,
        border = BorderStroke(1.dp, if (isDefault) colors.borderStroke else Color.White.copy(alpha = 0.25f))
    ) {
        if (isDefault) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Outlined.FormatColorReset,
                    contentDescription = "Default / No color",
                    tint = colors.textPrimaryMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchNoteItemCard(
    note: DiaryNoteItem,
    onClick: () -> Unit
) {
    val hasTitle = note.title.isNotBlank() && !note.title.equals("Untitled Note", ignoreCase = true)
    val hasContent = note.previewText.isNotBlank()
    val hasLabels = note.labels.isNotEmpty()

    val displayContent = remember(note.previewText) {
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

    val hasCustomColor = note.colorHex != 0L && note.colorHex != 0xFF000000L && note.colorHex != 0xFF121212L
    val baseBorderColor = if (hasCustomColor) {
        Color(note.colorHex).copy(alpha = 0.6f)
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
        color = UtilitarianCardBackground,
        border = BorderStroke(1.dp, baseBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

            if (hasTitle && hasContent) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (hasContent) {
                Text(
                    text = displayContent,
                    color = UtilitarianTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (hasLabels && (hasTitle || hasContent)) {
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
                                    color = UtilitarianBorderStroke,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = label,
                                color = UtilitarianTextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            val hasMedia = note.audioCount > 0 || note.photoCount > 0
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
                    if (note.photoCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Photo,
                                contentDescription = "Photo attached",
                                tint = UtilitarianTextDisabled,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${note.photoCount}",
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

