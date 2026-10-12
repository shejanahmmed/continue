package com.shejan.acontinue.presentation.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.HighContrastButton
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.core.designsystem.components.UtilitarianBadge
import com.shejan.acontinue.domain.model.AccentPalette
import com.shejan.acontinue.domain.model.ThemeMode
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val colors = LocalUtilitarianColors.current
    val context = LocalContext.current

    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showLicenseDialog by remember { mutableStateOf(false) }

    val notificationsSystemEnabled = remember(context) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val exactAlarmSystemEnabled = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.canScheduleExactAlarms() ?: true
        } else {
            true
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        item {
            DotMatrixHeader(
                title = "SETTINGS",
                leadingIcon = Icons.Outlined.Settings,
                badgeText = "V1.0",
                isAccentBadge = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 1: Appearance & Accent
        item {
            SettingsSectionHeader(
                title = "APPEARANCE & ACCENT",
                icon = Icons.Outlined.ColorLens
            )

            MonochromeCard {
                Column {
                    Text(
                        text = "THEME MODE",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ThemeModeSelector(
                        selectedMode = settings.themeMode,
                        onModeSelected = { viewModel.setThemeMode(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.borderStroke, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "ACCENT PALETTE",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hardware-inspired color applied to navigation highlights, active badges, and focus indicators.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AccentPaletteGrid(
                        selectedPalette = settings.accentPalette,
                        onPaletteSelected = { viewModel.setAccentPalette(it) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section 2: System & Preferences
        item {
            SettingsSectionHeader(
                title = "SYSTEM & PREFERENCES",
                icon = Icons.Outlined.Notifications
            )

            MonochromeCard {
                Column {
                    // Haptic Feedback Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setHapticFeedbackEnabled(!settings.hapticFeedbackEnabled) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Vibration,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Haptic Feedback",
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tactile vibration when checking items or switching tabs.",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = settings.hapticFeedbackEnabled,
                            onCheckedChange = { viewModel.setHapticFeedbackEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (colors.accent == Color.White) Color.Black else Color.White,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.borderStroke
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.borderStroke, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // In-app Notifications Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setNotificationsEnabled(!settings.notificationsEnabled) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scheduled Reminders",
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Trigger 10-minute alerts before scheduled activities.",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (colors.accent == Color.White) Color.Black else Color.White,
                                checkedTrackColor = colors.accent,
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.borderStroke
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = colors.borderStroke, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // OS Permission Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Permission Status",
                                color = colors.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "OS POST_NOTIFICATIONS & EXACT_ALARM",
                                color = colors.textSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }

                        val isAllGranted = notificationsSystemEnabled && exactAlarmSystemEnabled
                        UtilitarianBadge(
                            text = if (isAllGranted) "ACTIVE" else "LIMITED",
                            backgroundColor = if (isAllGranted) colors.borderStroke else colors.accent,
                            textColor = if (isAllGranted) colors.textPrimary else (if (colors.accent == Color.White) Color.Black else Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HighContrastButton(
                        text = "Open App System Settings",
                        isOutlined = true,
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section 3: Legal & Privacy
        item {
            SettingsSectionHeader(
                title = "LEGAL & PRIVACY",
                icon = Icons.Outlined.Security
            )

            MonochromeCard {
                Column {
                    SettingsNavigationItem(
                        icon = Icons.Outlined.Security,
                        title = "Privacy Policy",
                        subtitle = "100% offline, zero-tracking, zero telemetry architecture.",
                        onClick = { showPrivacyPolicyDialog = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = colors.borderStroke, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsNavigationItem(
                        icon = Icons.Outlined.Description,
                        title = "Open Source License",
                        subtitle = "GNU General Public License v3.0 (GPL-3.0)",
                        onClick = { showLicenseDialog = true }
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section 4: About & Specifications
        item {
            SettingsSectionHeader(
                title = "ABOUT & SPECIFICATIONS",
                icon = Icons.Outlined.Info
            )

            MonochromeCard {
                Column {
                    Text(
                        text = "CONTINUE",
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "A minimalist, context-bound productivity application that treats every scheduled activity as an isolated execution workspace.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = colors.borderStroke, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "AUTHOR & MAINTAINER",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Farjan Ahmmed",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "farjan.swe@gmail.com",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExternalLinkChip(
                            label = "GitHub",
                            url = "https://github.com/shejanahmmed",
                            modifier = Modifier.weight(1f)
                        )
                        ExternalLinkChip(
                            label = "LinkedIn",
                            url = "https://www.linkedin.com/in/farjan-ahmmed/",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = colors.borderStroke, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "TECHNICAL SPECIFICATION",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    SpecificationRow("VERSION", "1.0.0 (Build 1)")
                    SpecificationRow("PACKAGE", "com.shejan.acontinue")
                    SpecificationRow("MIN SDK", "API 24 (Android 7.0)")
                    SpecificationRow("TARGET SDK", "API 37")
                    SpecificationRow("PERSISTENCE", "Room Database (SQLite)")
                    SpecificationRow("ARCHITECTURE", "Offline-First Clean Architecture + MVVM")
                }
            }
        }
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = {
                Text(
                    text = "PRIVACY POLICY",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Text(
                            text = "Zero-Telemetry & 100% Offline\n\n" +
                                "1. Data Storage: All activities, notes, checklists, and links created within 'continue' are stored strictly on your local device within an encrypted/sandboxed SQLite database via Room.\n\n" +
                                "2. No Analytics: We do not integrate third-party analytics, behavioral tracking, advertising SDKs, or cloud synchronization services.\n\n" +
                                "3. Network Activity: The app operates completely without internet transmission. Your private schedules and notes never leave your hardware.\n\n" +
                                "4. Permissions: System permissions (Notifications, Alarms) are utilized exclusively on-device to dispatch local notifications at your configured reminder intervals.",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text(
                        text = "DISMISS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }
            },
            containerColor = colors.cardBackground,
            textContentColor = colors.textSecondary
        )
    }

    // GPL-3.0 License Dialog
    if (showLicenseDialog) {
        AlertDialog(
            onDismissRequest = { showLicenseDialog = false },
            title = {
                Text(
                    text = "GPL-3.0 LICENSE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Text(
                            text = "GNU GENERAL PUBLIC LICENSE\nVersion 3, 29 June 2007\n\n" +
                                "Copyright (C) 2026 Farjan Ahmmed\n\n" +
                                "Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.\n\n" +
                                "This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.\n\n" +
                                "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicenseDialog = false }) {
                    Text(
                        text = "AGREE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                }
            },
            containerColor = colors.cardBackground,
            textContentColor = colors.textSecondary
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    icon: ImageVector
) {
    val colors = LocalUtilitarianColors.current
    Row(
        modifier = Modifier.padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = colors.textSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val colors = LocalUtilitarianColors.current
    val modes = listOf(
        ThemeMode.SYSTEM to "SYSTEM",
        ThemeMode.DARK to "DARK",
        ThemeMode.LIGHT to "LIGHT"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.borderStroke, RoundedCornerShape(20.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        modes.forEach { (mode, label) ->
            val isSelected = selectedMode == mode
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onModeSelected(mode) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) colors.textPrimary else Color.Transparent
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) colors.background else colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun AccentPaletteGrid(
    selectedPalette: AccentPalette,
    onPaletteSelected: (AccentPalette) -> Unit
) {
    val colors = LocalUtilitarianColors.current
    val palettes = AccentPalette.values()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaletteCard(
                palette = palettes[0],
                isSelected = selectedPalette == palettes[0],
                modifier = Modifier.weight(1f),
                onClick = { onPaletteSelected(palettes[0]) }
            )
            PaletteCard(
                palette = palettes[1],
                isSelected = selectedPalette == palettes[1],
                modifier = Modifier.weight(1f),
                onClick = { onPaletteSelected(palettes[1]) }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaletteCard(
                palette = palettes[2],
                isSelected = selectedPalette == palettes[2],
                modifier = Modifier.weight(1f),
                onClick = { onPaletteSelected(palettes[2]) }
            )
            PaletteCard(
                palette = palettes[3],
                isSelected = selectedPalette == palettes[3],
                modifier = Modifier.weight(1f),
                onClick = { onPaletteSelected(palettes[3]) }
            )
        }
    }
}

@Composable
private fun PaletteCard(
    palette: AccentPalette,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    val paletteColor = Color(palette.colorHex)

    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) colors.borderStroke else Color.Transparent,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) colors.textPrimary else colors.borderStroke
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(paletteColor)
                    .border(1.dp, colors.borderStroke, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = palette.displayName,
                color = if (isSelected) colors.textPrimary else colors.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalUtilitarianColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
            contentDescription = null,
            tint = colors.textSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ExternalLinkChip(
    label: String,
    url: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    val context = LocalContext.current

    Surface(
        modifier = modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.borderStroke)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun SpecificationRow(label: String, value: String) {
    val colors = LocalUtilitarianColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
