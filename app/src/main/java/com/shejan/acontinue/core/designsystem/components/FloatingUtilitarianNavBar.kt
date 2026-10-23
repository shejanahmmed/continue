package com.shejan.acontinue.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianCardBackground
import com.shejan.acontinue.ui.theme.UtilitarianPureBlack
import com.shejan.acontinue.ui.theme.UtilitarianTextDisabled
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimaryMuted

enum class NavDestination(val route: String, val label: String) {
    HOME("home", "Home"),
    DIARY("diary", "Diary"),
    CALENDAR("calendar", "Calendar"),
    SETTINGS("settings", "Settings")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingUtilitarianNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCreateClick: () -> Unit,
    onOpenSpeedDial: () -> Unit = {},
    isSpeedDialOpen: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape,
            color = UtilitarianCardBackground,
            border = BorderStroke(1.dp, UtilitarianBorderStroke),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home
                NavBarItem(
                    icon = Icons.Outlined.Home,
                    contentDescription = "Home",
                    isSelected = currentRoute == NavDestination.HOME.route,
                    onClick = { onNavigate(NavDestination.HOME.route) }
                )

                // Diary
                NavBarItem(
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = "Diary",
                    isSelected = currentRoute == NavDestination.DIARY.route,
                    onClick = { onNavigate(NavDestination.DIARY.route) }
                )

                // Create (Prominent center action button with tap & hold easy access)
                val colors = LocalUtilitarianColors.current
                val iconTint = if (colors.accent == UtilitarianTextPrimary) UtilitarianPureBlack else UtilitarianTextPrimary
                val haptic = LocalHapticFeedback.current

                val rotationDegrees by animateFloatAsState(
                    targetValue = if (isSpeedDialOpen) 45f else 0f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "plusRotation"
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCreateClick,
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onOpenSpeedDial()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = if (isSpeedDialOpen) "Close" else "Create",
                        tint = iconTint,
                        modifier = Modifier
                            .size(21.dp)
                            .graphicsLayer { rotationZ = rotationDegrees }
                    )
                }

                // Calendar
                NavBarItem(
                    icon = Icons.Outlined.CalendarToday,
                    contentDescription = "Calendar",
                    isSelected = currentRoute == NavDestination.CALENDAR.route,
                    onClick = { onNavigate(NavDestination.CALENDAR.route) }
                )

                // Settings
                NavBarItem(
                    icon = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    isSelected = currentRoute == NavDestination.SETTINGS.route,
                    onClick = { onNavigate(NavDestination.SETTINGS.route) }
                )
            }
        }
    }
}

@Composable
fun EasyAccessSpeedDialPills(
    onSelect: (EntryType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Option 1: Event
        SpeedDialPill(
            icon = Icons.Outlined.CalendarToday,
            label = "Event",
            onClick = { onSelect(EntryType.ACTIVITY) }
        )

        // Option 2: Reminder
        SpeedDialPill(
            icon = Icons.Outlined.Notifications,
            label = "Reminder",
            onClick = { onSelect(EntryType.REMINDER) }
        )

        // Option 3: Note
        SpeedDialPill(
            icon = Icons.AutoMirrored.Outlined.StickyNote2,
            label = "Note",
            onClick = { onSelect(EntryType.NOTE) }
        )
    }
}

@Composable
private fun SpeedDialPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(142.dp)
            .clip(RoundedCornerShape(32.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(32.dp),
        color = Color(0xFF202020),
        border = BorderStroke(1.dp, UtilitarianBorderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UtilitarianTextPrimaryMuted,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = UtilitarianTextPrimaryMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.6.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) UtilitarianTextPrimary else UtilitarianTextDisabled,
            modifier = Modifier.size(22.dp)
        )
    }
}

