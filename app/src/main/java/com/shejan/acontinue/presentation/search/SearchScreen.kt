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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.presentation.diary.DiaryNoteItem
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
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // Top Bar: Back Button + Utilitarian Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF202020))
                    .border(
                        1.dp,
                        UtilitarianBorderStroke.copy(alpha = 0.5f),
                        RoundedCornerShape(5.dp)
                    )
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = UtilitarianTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Search Input Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF202020))
                    .border(
                        1.dp,
                        UtilitarianBorderStroke.copy(alpha = 0.5f),
                        RoundedCornerShape(5.dp)
                    )
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = UtilitarianTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (uiState.searchQuery.isEmpty()) {
                            Text(
                                text = "Search notes and labels...",
                                color = UtilitarianTextDisabled,
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = UtilitarianTextPrimary,
                                fontSize = 13.sp
                            ),
                            cursorBrush = SolidColor(UtilitarianTextPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )
                    }

                    if (uiState.searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            tint = UtilitarianTextSecondary,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.onSearchQueryChange("")
                                }
                        )
                    }
                }
            }
        }

        // Search Results List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
        ) {
            if (uiState.searchQuery.isBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = null,
                                tint = UtilitarianTextDisabled,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Search notes by title, keyword, or label.",
                                color = UtilitarianTextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else if (uiState.results.isEmpty() && !uiState.isLoading) {
                item {
                    MonochromeCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No matching notes found.",
                                color = UtilitarianTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching with different keywords.",
                                color = UtilitarianTextSecondary,
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

