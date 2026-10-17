package com.shejan.acontinue.presentation.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.domain.model.CustomAlert
import kotlinx.coroutines.launch

// Color constants matching the design reference screenshots exactly
private val ScreenBackground = Color(0xFF000000)
private val CardBackground = Color(0xFF1C1C1E)
private val CardDividerColor = Color(0xFF2C2C2E)
private val AccentBlue = Color(0xFF3897F0)
private val AccentGreen = Color(0xFF00E676)
private val UnselectedCircleBorder = Color(0xFF5E5E62)
private val SwitchOffTrack = Color(0xFF3A3A3C)
private val TextMuted = Color(0xFF8E8E93)
private val DrumDimText = Color(0xFF48484A)

private data class BuiltInAlertPreset(
    val minutes: Int,
    val label: String
)

private val DEFAULT_PRESETS = listOf(
    BuiltInAlertPreset(0, "At time of event"),
    BuiltInAlertPreset(10, "10 mins before"),
    BuiltInAlertPreset(60, "1 hour before"),
    BuiltInAlertPreset(1440, "1 day before")
)

@Composable
fun AlertSelectionScreen(
    state: CreateEntryState,
    onToggleAlertsEnabled: (Boolean) -> Unit,
    onToggleAlertMinute: (Int) -> Unit,
    onAddCustomAlert: (value: Int, unit: String) -> Unit,
    onUpdateCustomAlert: (id: String, value: Int, unit: String) -> Unit,
    onDeleteCustomAlert: (id: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isCustomPickerVisible by remember { mutableStateOf(false) }
    var editingAlertId by remember { mutableStateOf<String?>(null) }
    var customNumber by remember { mutableIntStateOf(9) }
    var customUnit by remember { mutableStateOf("hours") }

    val scrollState = rememberScrollState()

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

        // Top Navigation Header: < Alert (Chevron icon directly adjacent to title)
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
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Alert",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Master Switch Card: "Off" / "On" (Capsule pill matching Screenshot 1 & 2)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(CardBackground)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (state.alertsEnabled) "On" else "Off",
                    color = if (state.alertsEnabled) AccentBlue else Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = state.alertsEnabled,
                    onCheckedChange = { onToggleAlertsEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = AccentBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = SwitchOffTrack,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alert Options List (Visible only when Master Switch is ON)
        AnimatedVisibility(
            visible = state.alertsEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Presets + Saved Custom Alerts Container Card (Screenshot 2 & 3)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(CardBackground)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // 1. Built-in Presets
                        DEFAULT_PRESETS.forEachIndexed { index, preset ->
                            val isSelected = state.selectedAlertMinutes.contains(preset.minutes)
                            AlertOptionRow(
                                label = preset.label,
                                isSelected = isSelected,
                                onClick = { onToggleAlertMinute(preset.minutes) }
                            )
                            if (index < DEFAULT_PRESETS.lastIndex || state.savedCustomAlerts.isNotEmpty()) {
                                HorizontalDivider(
                                    color = CardDividerColor,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 60.dp, end = 20.dp)
                                )
                            }
                        }

                        // 2. Saved Custom Alerts
                        state.savedCustomAlerts.forEachIndexed { index, customAlert ->
                            val isSelected = state.selectedAlertMinutes.contains(customAlert.minutesBefore)
                            CustomAlertRow(
                                customAlert = customAlert,
                                isSelected = isSelected,
                                onToggle = { onToggleAlertMinute(customAlert.minutesBefore) },
                                onEdit = {
                                    editingAlertId = customAlert.id
                                    customNumber = customAlert.value
                                    customUnit = customAlert.unit
                                    isCustomPickerVisible = true
                                },
                                onDelete = {
                                    onDeleteCustomAlert(customAlert.id)
                                }
                            )
                            if (index < state.savedCustomAlerts.lastIndex) {
                                HorizontalDivider(
                                    color = CardDividerColor,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 60.dp, end = 20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // "+ Custom" Button Card (Capsule pill matching Screenshot 2)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(CardBackground)
                        .clickable {
                            editingAlertId = null
                            customNumber = 9
                            customUnit = "hours"
                            isCustomPickerVisible = !isCustomPickerVisible
                        }
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add custom alert",
                            tint = AccentGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(18.dp))
                        Text(
                            text = "Custom",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Alert Roller Wheel Creator (Screenshot 3)
                AnimatedVisibility(
                    visible = isCustomPickerVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    CustomAlertWheelEditorCard(
                        number = customNumber,
                        unit = customUnit,
                        onNumberChange = { customNumber = it },
                        onUnitChange = { customUnit = it },
                        onSave = {
                            val id = editingAlertId
                            if (id != null) {
                                onUpdateCustomAlert(id, customNumber, customUnit)
                            } else {
                                onAddCustomAlert(customNumber, customUnit)
                            }
                            isCustomPickerVisible = false
                            editingAlertId = null
                        },
                        onCancel = {
                            isCustomPickerVisible = false
                            editingAlertId = null
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Helper text matching Screenshot 2
                Text(
                    text = "To receive alerts at customized intervals, add a custom alert above.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun AlertOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlertSelectionCircle(isSelected = isSelected)

        Spacer(modifier = Modifier.width(18.dp))

        Text(
            text = label,
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun CustomAlertRow(
    customAlert: CustomAlert,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AlertSelectionCircle(isSelected = isSelected)

            Spacer(modifier = Modifier.width(20.dp))

            Text(
                text = customAlert.displayName,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal
            )
        }

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Options",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp)
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
                    text = { Text("Edit", color = Color.White, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onEdit()
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
                        onDelete()
                    }
                )
            }
        }
    }
}

@Composable
private fun AlertSelectionCircle(isSelected: Boolean) {
    if (isSelected) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(AccentBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, UnselectedCircleBorder, CircleShape)
        )
    }
}

@Composable
private fun CustomAlertWheelEditorCard(
    number: Int,
    unit: String,
    onNumberChange: (Int) -> Unit,
    onUnitChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val numbersList = remember { (1..60).map { "$it" } }
    val unitsList = remember { listOf("minutes", "hours", "days", "weeks") }

    val singular = when (unit.lowercase()) {
        "minutes" -> "minute"
        "hours" -> "hour"
        "days" -> "day"
        "weeks" -> "week"
        else -> unit
    }
    val unitStr = if (number == 1) singular else "${singular}s"
    val previewText = "$number $unitStr before"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(CardBackground)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Live Preview Header (Matching Screenshot 3)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(AccentBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = previewText,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Two-column drum roller picker (Value & Unit - Screenshot 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Number (1..60) with continuous looping
                WheelPickerDrumColumn(
                    modifier = Modifier.weight(1f),
                    items = numbersList,
                    selectedIndex = (number - 1).coerceIn(0, numbersList.lastIndex),
                    isCyclic = true,
                    onItemSelected = { index ->
                        onNumberChange(index + 1)
                    }
                )

                // Column 2: Unit (minutes, hours, days, weeks) with continuous looping
                val currentUnitIndex = unitsList.indexOf(unit.lowercase()).let { if (it >= 0) it else 1 }
                WheelPickerDrumColumn(
                    modifier = Modifier.weight(1.4f),
                    items = unitsList,
                    selectedIndex = currentUnitIndex,
                    isCyclic = true,
                    onItemSelected = { index ->
                        onUnitChange(unitsList[index])
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Cancel",
                        color = TextMuted,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentBlue)
                        .clickable(onClick = onSave)
                        .padding(horizontal = 22.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Save",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun WheelPickerDrumColumn(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    isCyclic: Boolean = false,
    onItemSelected: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = 48.dp
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

    val cycleMultiplier = 10_000
    val totalItemCount = if (isCyclic && items.isNotEmpty()) items.size * cycleMultiplier else items.size

    val initialIndex = remember(items.size, isCyclic) {
        if (isCyclic && items.isNotEmpty()) {
            (cycleMultiplier / 2) * items.size + selectedIndex.coerceIn(0, items.lastIndex)
        } else {
            selectedIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        }
    }

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex
    )

    // Center item currently aligned in viewport
    val currentRawIndex by remember {
        derivedStateOf {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val index = if (offset > itemHeightPx / 2) firstVisible + 1 else firstVisible
            index.coerceIn(0, (totalItemCount - 1).coerceAtLeast(0))
        }
    }

    // Normalized index mapped back to 0 until items.size
    val currentNormalizedIndex by remember {
        derivedStateOf {
            if (isCyclic && items.isNotEmpty()) {
                (currentRawIndex % items.size + items.size) % items.size
            } else {
                currentRawIndex
            }
        }
    }

    // External selectedIndex sync
    LaunchedEffect(selectedIndex) {
        if (!listState.isScrollInProgress) {
            if (currentNormalizedIndex != selectedIndex && selectedIndex in items.indices) {
                if (isCyclic && items.isNotEmpty()) {
                    val currentCycle = currentRawIndex / items.size
                    var targetIndex = currentCycle * items.size + selectedIndex
                    val diff = targetIndex - currentRawIndex
                    if (diff > items.size / 2) {
                        targetIndex -= items.size
                    } else if (diff < -items.size / 2) {
                        targetIndex += items.size
                    }
                    listState.scrollToItem(targetIndex.coerceIn(0, totalItemCount - 1))
                } else {
                    listState.scrollToItem(selectedIndex)
                }
            }
        }
    }

    // Notify callback when scroll settles
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            if (currentNormalizedIndex in items.indices) {
                onItemSelected(currentNormalizedIndex)
            }
        }
    }

    val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Box(
        modifier = modifier.height(itemHeight * 3),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapFlingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(
                count = totalItemCount,
                key = if (isCyclic) { index -> index } else null
            ) { globalIndex ->
                val normalizedIndex = if (isCyclic && items.isNotEmpty()) {
                    (globalIndex % items.size + items.size) % items.size
                } else {
                    globalIndex
                }
                val text = items[normalizedIndex]
                val isSelected = globalIndex == currentRawIndex

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            coroutineScope.launch {
                                listState.animateScrollToItem(globalIndex)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        fontSize = if (isSelected) 28.sp else 20.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else DrumDimText,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
