package com.shejan.acontinue.presentation.create

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shejan.acontinue.domain.model.ReminderCategory
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorySelectionScreen(
    selectedCategory: ReminderCategory,
    categories: List<ReminderCategory>,
    reminderCounts: Map<String, Int>,
    onSelectCategory: (ReminderCategory) -> Unit,
    onAddCategoryClick: () -> Unit,
    onNavigateBack: () -> Unit,
    onEditCategory: (ReminderCategory) -> Unit = {},
    onDeleteCategory: (ReminderCategory) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    var actionCategory by remember { mutableStateOf<ReminderCategory?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(scrollState)
    ) {
        // Navigation Header: Chevron Left + "Select category"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = "Back",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "Select category",
                color = colors.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Main Categories Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = colors.cardBackground,
            border = BorderStroke(1.dp, colors.borderStroke)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                categories.forEachIndexed { index, category ->
                    val isSelected = category.id == selectedCategory.id || category.name == selectedCategory.name
                    val count = reminderCounts[category.name] ?: 0

                    val rowClickModifier = if (category.isDefault) {
                        Modifier.clickable { onSelectCategory(category) }
                    } else {
                        Modifier.combinedClickable(
                            onClick = { onSelectCategory(category) },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                actionCategory = category
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(rowClickModifier)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Radio indicator
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .border(2.dp, Color(0xFF3897F0), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF3897F0), CircleShape)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .border(1.5.dp, Color(0xFF555555), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Category Icon in assigned color
                        Icon(
                            imageVector = getCategoryIcon(category.iconName),
                            contentDescription = category.name,
                            tint = Color(category.colorHex),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // Category Title
                        Text(
                            text = category.name,
                            color = colors.textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )

                        // Count badge
                        Text(
                            text = "$count",
                            color = colors.textSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }

                    HorizontalDivider(
                        color = colors.borderStroke.copy(alpha = 0.5f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 54.dp)
                    )
                }

                // "+ Add category" Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                        .clickable(onClick = onAddCategoryClick)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Add category",
                        tint = Color(0xFF2ECC71),
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Add category",
                        color = colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }

    // Long-press Contextual Dialog for Custom Categories
    if (actionCategory != null) {
        val targetCat = actionCategory!!
        var showDeleteConfirmation by remember(targetCat) { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { actionCategory = null }
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.cardBackground,
                border = BorderStroke(1.dp, colors.borderStroke),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!showDeleteConfirmation) {
                        // Category Icon preview
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(targetCat.colorHex).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(targetCat.iconName),
                                contentDescription = null,
                                tint = Color(targetCat.colorHex),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = targetCat.name,
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val count = reminderCounts[targetCat.name] ?: 0
                        Text(
                            text = "$count reminder${if (count == 1) "" else "s"}",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
                        )

                        HorizontalDivider(
                            color = colors.borderStroke.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Edit Category action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val catToEdit = targetCat
                                    actionCategory = null
                                    onEditCategory(catToEdit)
                                }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit category",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Edit category",
                                color = colors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Delete Category action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showDeleteConfirmation = true
                                }
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete category",
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Delete category",
                                color = Color(0xFFEF5350),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Delete Confirmation Alert
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFEF5350).copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Delete category?",
                            color = colors.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Are you sure you want to delete \"${targetCat.name}\"? Reminders in this category will be moved to My reminders.",
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cancel button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.dp, colors.borderStroke, RoundedCornerShape(20.dp))
                                    .clickable {
                                        showDeleteConfirmation = false
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = colors.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Delete button
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFEF5350), RoundedCornerShape(20.dp))
                                    .clickable {
                                        val catToDelete = targetCat
                                        actionCategory = null
                                        onDeleteCategory(catToDelete)
                                    }
                                    .padding(vertical = 12.dp),
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
    }
}
