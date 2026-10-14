package com.shejan.acontinue.presentation.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.domain.model.ReminderCategory
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors

@Composable
fun CategorySelectionScreen(
    selectedCategory: ReminderCategory,
    categories: List<ReminderCategory>,
    reminderCounts: Map<String, Int>,
    onSelectCategory: (ReminderCategory) -> Unit,
    onAddCategoryClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCategory(category) }
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
}
