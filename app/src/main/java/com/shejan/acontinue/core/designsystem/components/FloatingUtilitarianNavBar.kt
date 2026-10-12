package com.shejan.acontinue.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shejan.acontinue.ui.theme.UtilitarianAccentRed
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianCardBackground
import com.shejan.acontinue.ui.theme.UtilitarianTextDisabled
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary

enum class NavDestination(val route: String, val label: String) {
    HOME("home", "Home"),
    DIARY("diary", "Diary"),
    CALENDAR("calendar", "Calendar"),
    SETTINGS("settings", "Settings")
}

@Composable
fun FloatingUtilitarianNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCreateClick: () -> Unit,
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

                // Create (Prominent center action button)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(UtilitarianAccentRed)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCreateClick
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Create",
                        tint = UtilitarianTextPrimary,
                        modifier = Modifier.size(21.dp)
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
