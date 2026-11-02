package com.shejan.acontinue.presentation.menu

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.Calendar
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
import com.shejan.acontinue.core.designsystem.components.DotMatrixHeader
import com.shejan.acontinue.core.designsystem.components.MonochromeCard
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary
import com.shejan.acontinue.ui.theme.UtilitarianTextSecondary

@Composable
fun MenuScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToWelcome: (() -> Unit)? = null,
    onNavigateToArchive: (() -> Unit)? = null,
    onNavigateToBin: (() -> Unit)? = null,
    onNavigateToYear: (() -> Unit)? = null
) {
    val colors = LocalUtilitarianColors.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
    ) {
        // Header
        item {
            DotMatrixHeader(
                title = "MENU",
                badgeText = "V1.0",
                isAccentBadge = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section 1: Preferences & Navigation
        item {
            MenuSectionHeader(
                title = "PREFERENCES",
                icon = Icons.Outlined.Tune
            )

            MonochromeCard(onClick = onNavigateToSettings) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF161616))
                            .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SETTINGS",
                            color = colors.textPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Themes, accent colors, reminders, alerts & sounds",
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                        contentDescription = "Open Settings",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section: Year Overview
        if (onNavigateToYear != null) {
            item {
                MenuSectionHeader(
                    title = "YEAR",
                    icon = Icons.Outlined.CalendarMonth
                )

                val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

                MonochromeCard(onClick = onNavigateToYear) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF161616))
                                .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Year Calendar",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$currentYear CALENDAR",
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Full year calendar overview and monthly matrix",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                            contentDescription = "Open Year Calendar",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Section 2: Storage & Repository
        if (onNavigateToArchive != null || onNavigateToBin != null) {
            item {
                MenuSectionHeader(
                    title = "STORAGE & REPOSITORY",
                    icon = Icons.Outlined.Folder
                )

                if (onNavigateToArchive != null) {
                    MonochromeCard(onClick = onNavigateToArchive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161616))
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Archive,
                                    contentDescription = "Archive",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ARCHIVE",
                                    color = colors.textPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Archived notes and records",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                                contentDescription = "Open Archive",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                if (onNavigateToBin != null) {
                    MonochromeCard(onClick = onNavigateToBin) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161616))
                                    .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Bin",
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BIN / TRASH",
                                    color = colors.textPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Deleted events, reminders, and notes",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                                contentDescription = "Open Bin",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }

        // Section 2: Quick Actions
        if (onNavigateToWelcome != null) {
            item {
                MenuSectionHeader(
                    title = "QUICK ACTIONS",
                    icon = Icons.Outlined.TouchApp
                )

                MonochromeCard(onClick = onNavigateToWelcome) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF161616))
                                .border(1.dp, UtilitarianBorderStroke, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RestartAlt,
                                contentDescription = "Replay Onboarding",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WELCOME WALKTHROUGH",
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "View the welcome walkthrough and feature overview",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                            contentDescription = "Replay walkthrough",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Section 3: About & Developer
        item {
            MenuSectionHeader(
                title = "ABOUT & DEVELOPER",
                icon = Icons.Outlined.Info
            )

            MonochromeCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MenuSpecificationRow(label = "APPLICATION", value = "continue")
                    MenuSpecificationRow(label = "VERSION", value = "1.0.0")
                    MenuSpecificationRow(label = "LICENSE", value = "GNU GPL v3.0")
                    MenuSpecificationRow(label = "DEVELOPER", value = "Farjan Ahmmed")
                    MenuSpecificationRow(label = "CONTACT", value = "farjan.swe@gmail.com")

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MenuExternalLinkChip(
                            label = "GITHUB",
                            url = "https://github.com/shejanahmmed",
                            modifier = Modifier.weight(1f)
                        )
                        MenuExternalLinkChip(
                            label = "LINKEDIN",
                            url = "https://www.linkedin.com/in/farjan-ahmmed/",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuSectionHeader(
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
private fun MenuSpecificationRow(label: String, value: String) {
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

@Composable
private fun MenuExternalLinkChip(
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
        color = Color(0xFF161616),
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
