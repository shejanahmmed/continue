package com.shejan.acontinue.presentation.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.domain.model.CustomAlert
import kotlinx.coroutines.launch

private val ScreenBackground = Color(0xFF000000)
private val CardBackground = Color(0xFF1C1C1E)
private val CardDividerColor = Color(0xFF2C2C2E)
private val AccentBlue = Color(0xFF3897F0)
private val UnselectedCircleBorder = Color(0xFF5E5E62)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8E8E93)
private val TextDimmed = Color(0xFF48484A)
private val MutedAlertBlue = Color(0xFF384B70)

private data class EarlyAlertPreset(
    val minutes: Int,
    val label: String
)

private val EARLY_ALERT_PRESETS = listOf(
    EarlyAlertPreset(-1, "No early alert"),
    EarlyAlertPreset(10, "10 mins before"),
    EarlyAlertPreset(15, "15 mins before"),
    EarlyAlertPreset(60, "1 hour before"),
    EarlyAlertPreset(1440, "1 day before")
)

@Composable
fun EarlyAlertSelectionScreen(
    selectedMinutes: Int,
    savedCustomAlerts: List<CustomAlert>,
    onSelectAlert: (Int) -> Unit,
    onAddCustomAlert: (value: Int, unit: String) -> Unit,
    onUpdateCustomAlert: (id: String, value: Int, unit: String) -> Unit,
    onDeleteCustomAlert: (id: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presetMinutes = remember { EARLY_ALERT_PRESETS.map { it.minutes } }
    val isCustomActive = selectedMinutes !in presetMinutes

    // Custom roller state (default: 30 minutes)
    var customNumber by remember {
        val initialNumber = if (isCustomActive && selectedMinutes > 0) {
            when {
                selectedMinutes % 10080 == 0 -> selectedMinutes / 10080
                selectedMinutes % 1440 == 0 -> selectedMinutes / 1440
                selectedMinutes % 60 == 0 -> selectedMinutes / 60
                else -> selectedMinutes
            }
        } else 30
        mutableIntStateOf(initialNumber)
    }

    var customUnit by remember {
        val initialUnit = if (isCustomActive && selectedMinutes > 0) {
            when {
                selectedMinutes % 10080 == 0 -> "weeks"
                selectedMinutes % 1440 == 0 -> "days"
                selectedMinutes % 60 == 0 -> "hours"
                else -> "minutes"
            }
        } else "minutes"
        mutableStateOf(initialUnit)
    }

    var editingAlertId by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    fun calculateMinutes(number: Int, unit: String): Int {
        return when (unit.lowercase()) {
            "minutes" -> number
            "hours" -> number * 60
            "days" -> number * 1440
            "weeks" -> number * 10080
            else -> number
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Navigation Header: < Early alert
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Early alert",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card Container holding presets, saved custom alerts, and the custom roller
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(CardBackground)
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Built-in presets
                EARLY_ALERT_PRESETS.forEachIndexed { index, preset ->
                    val isSelected = selectedMinutes == preset.minutes

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editingAlertId = null
                                onSelectAlert(preset.minutes)
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EarlyAlertRadioButton(isSelected = isSelected)
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = preset.label,
                            color = if (isSelected) TextPrimary else TextPrimary.copy(alpha = 0.9f),
                            fontSize = 17.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }

                    HorizontalDivider(
                        color = CardDividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 2. Saved Custom Alerts (Available for other reminders too)
                savedCustomAlerts.forEach { customAlert ->
                    val isSelected = selectedMinutes == customAlert.minutesBefore
                    var menuExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                editingAlertId = customAlert.id
                                customNumber = customAlert.value
                                customUnit = customAlert.unit
                                onSelectAlert(customAlert.minutesBefore)
                            }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EarlyAlertRadioButton(isSelected = isSelected)
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Custom",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = customAlert.displayName,
                                color = if (isSelected) AccentBlue else MutedAlertBlue,
                                fontSize = 13.sp
                            )
                        }

                        // 3-dots Menu for Edit & Delete
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.MoreVert,
                                    contentDescription = "Options",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier
                                    .background(Color(0xFF2C2C2E))
                                    .border(0.5.dp, Color(0xFF3A3A3C), RoundedCornerShape(12.dp))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit", color = TextPrimary, fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Edit",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        editingAlertId = customAlert.id
                                        customNumber = customAlert.value
                                        customUnit = customAlert.unit
                                        onSelectAlert(customAlert.minutesBefore)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color(0xFFEF5350), fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF5350),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        if (isSelected) {
                                            onSelectAlert(-1)
                                        }
                                        onDeleteCustomAlert(customAlert.id)
                                    }
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = CardDividerColor,
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // 3. Custom Roller Option Row
                val singular = when (customUnit.lowercase()) {
                    "minutes" -> "minute"
                    "hours" -> "hour"
                    "days" -> "day"
                    "weeks" -> "week"
                    else -> customUnit
                }
                val unitStr = if (customNumber == 1) singular else "${singular}s"
                val customSummaryText = "$customNumber $unitStr before"

                var customMenuExpanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val computed = calculateMinutes(customNumber, customUnit)
                            onSelectAlert(computed)
                        }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EarlyAlertRadioButton(isSelected = isCustomActive)
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Custom",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = if (isCustomActive) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = customSummaryText,
                            color = if (isCustomActive) AccentBlue else MutedAlertBlue,
                            fontSize = 13.sp
                        )
                    }

                    // 3-dots Menu for Edit / Save Preset / Delete
                    Box {
                        IconButton(
                            onClick = { customMenuExpanded = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Options",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = customMenuExpanded,
                            onDismissRequest = { customMenuExpanded = false },
                            modifier = Modifier
                                .background(Color(0xFF2C2C2E))
                                .border(0.5.dp, Color(0xFF3A3A3C), RoundedCornerShape(12.dp))
                        ) {
                            if (editingAlertId != null) {
                                DropdownMenuItem(
                                    text = { Text("Save changes", color = TextPrimary, fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Save changes",
                                            tint = AccentBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = {
                                        customMenuExpanded = false
                                        onUpdateCustomAlert(editingAlertId!!, customNumber, customUnit)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color(0xFFEF5350), fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF5350),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = {
                                        customMenuExpanded = false
                                        val toDelete = editingAlertId!!
                                        editingAlertId = null
                                        if (isCustomActive) {
                                            onSelectAlert(-1)
                                        }
                                        onDeleteCustomAlert(toDelete)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Save as preset", color = TextPrimary, fontSize = 14.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.BookmarkAdd,
                                            contentDescription = "Save as preset",
                                            tint = AccentBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    onClick = {
                                        customMenuExpanded = false
                                        onAddCustomAlert(customNumber, customUnit)
                                    }
                                )
                            }
                        }
                    }
                }

                // 4. Two-Column Drum Roller Wheels (Subtle dimmed colors when inactive, bright when active)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isCustomActive) {
                            val computed = calculateMinutes(customNumber, customUnit)
                            onSelectAlert(computed)
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    val numbersList = remember { (1..60).map { "$it" } }
                    val unitsList = remember { listOf("minutes", "hours", "days", "weeks") }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EarlyWheelPickerColumn(
                            modifier = Modifier.weight(1f),
                            items = numbersList,
                            selectedIndex = (customNumber - 1).coerceIn(0, numbersList.lastIndex),
                            isActive = isCustomActive,
                            onItemSelected = { index ->
                                customNumber = index + 1
                                if (isCustomActive) {
                                    val computed = calculateMinutes(customNumber, customUnit)
                                    onSelectAlert(computed)
                                }
                            }
                        )

                        val currentUnitIndex = unitsList.indexOf(customUnit.lowercase()).let { if (it >= 0) it else 0 }
                        EarlyWheelPickerColumn(
                            modifier = Modifier.weight(1.3f),
                            items = unitsList,
                            selectedIndex = currentUnitIndex,
                            isActive = isCustomActive,
                            onItemSelected = { index ->
                                customUnit = unitsList[index]
                                if (isCustomActive) {
                                    val computed = calculateMinutes(customNumber, customUnit)
                                    onSelectAlert(computed)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun EarlyAlertRadioButton(isSelected: Boolean) {
    if (isSelected) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, AccentBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(AccentBlue)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(2.dp, UnselectedCircleBorder, CircleShape)
        )
    }
}

@Composable
private fun EarlyWheelPickerColumn(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    isActive: Boolean,
    onItemSelected: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = 44.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    val cycleMultiplier = 10_000
    val totalItemCount = items.size * cycleMultiplier

    val initialIndex = remember(items.size) {
        (cycleMultiplier / 2) * items.size + selectedIndex.coerceIn(0, items.lastIndex)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    val currentRawIndex by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val index = if (offset > itemHeightPx / 2) firstVisible + 1 else firstVisible
            index.coerceIn(0, totalItemCount - 1)
        }
    }

    val currentNormalizedIndex by remember {
        derivedStateOf {
            (currentRawIndex % items.size + items.size) % items.size
        }
    }

    LaunchedEffect(currentNormalizedIndex) {
        if (currentNormalizedIndex in items.indices) {
            onItemSelected(currentNormalizedIndex)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
            contentPadding = PaddingValues(vertical = itemHeight),
            modifier = Modifier.fillMaxSize()
        ) {
            items(totalItemCount) { rawIdx ->
                val normIdx = (rawIdx % items.size + items.size) % items.size
                val isCenter = rawIdx == currentRawIndex

                val textColor = when {
                    !isActive -> if (isCenter) Color(0xFF636366) else Color(0xFF3A3A3C)
                    isCenter -> TextPrimary
                    else -> TextSecondary.copy(alpha = 0.5f)
                }

                val fontSize = if (isCenter) 24.sp else 18.sp
                val fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(rawIdx - 1)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[normIdx],
                        color = textColor,
                        fontSize = fontSize,
                        fontWeight = fontWeight
                    )
                }
            }
        }
    }
}
