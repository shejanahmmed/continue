package com.shejan.acontinue.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import java.util.Locale

/**
 * Compact, low-profile daily overview card featuring two dedicated light solid circular
 * progress bars: one for Events and one for Reminders. Strictly center-aligned with zero visual bloat.
 */
@Composable
fun UtilitarianDailyMetricCard(
    eventCount: Int,
    completedEventCount: Int,
    eventProgress: Float,
    reminderCount: Int,
    completedReminderCount: Int,
    pendingReminderCount: Int,
    reminderProgress: Float,
    modifier: Modifier = Modifier,
    onEventsClick: (() -> Unit)? = null,
    onRemindersClick: (() -> Unit)? = null
) {
    val colors = LocalUtilitarianColors.current

    // Tactile micro-interaction: subtle spring dampening on press
    val cardInteractionSource = remember { MutableInteractionSource() }
    val isPressed by cardInteractionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.988f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "metricCardPressScale"
    )

    // Animated number transitions
    val animatedEvents by animateIntAsState(
        targetValue = eventCount,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "animatedEvents"
    )
    val animatedReminders by animateIntAsState(
        targetValue = reminderCount,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "animatedReminders"
    )

    // Distinct progress animations for Events and Reminders
    val animatedEventProgress by animateFloatAsState(
        targetValue = eventProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "animatedEventProgress"
    )
    val animatedReminderProgress by animateFloatAsState(
        targetValue = reminderProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "animatedReminderProgress"
    )

    // Elevated "light dark" card surface to pop against the deep black canvas
    val cardBgColor = if (colors.isDark) Color(0xFF1E1E22) else Color(0xFFFFFFFF)
    val cardBorderColor = if (colors.isDark) Color(0xFF323238) else colors.borderStroke

    // Defined empty track color: noticeable & finished, but distinctly inactive/empty
    val trackColor = if (colors.isDark) Color(0xFF38383F) else Color(0xFFE2E2E6)

    // Stark high-contrast solid color for active filled progress
    val lightSolidProgressColor = if (colors.isDark) Color(0xFFFFFFFF) else Color(0xFF18181B)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale),
        shape = RoundedCornerShape(14.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Clean, Center-Aligned Card Header
            Text(
                text = "TODAY'S OVERVIEW",
                color = colors.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.3.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Symmetrical Dual Metric Columns: One Progress Ring for Events, One for Reminders
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Section 1: EVENTS (Circular Progress Bar + Metric)
                CompactMetricGaugeColumn(
                    title = "EVENTS",
                    count = animatedEvents,
                    progress = animatedEventProgress,
                    icon = Icons.Outlined.Event,
                    progressColor = lightSolidProgressColor,
                    trackColor = trackColor,
                    onClick = onEventsClick,
                    modifier = Modifier.weight(1f)
                )

                // Hairline Vertical Divider
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .width(1.dp)
                        .background(cardBorderColor.copy(alpha = 0.8f))
                )

                // Section 2: REMINDERS (Circular Progress Bar + Metric)
                CompactMetricGaugeColumn(
                    title = "REMINDERS",
                    count = animatedReminders,
                    progress = animatedReminderProgress,
                    icon = Icons.Outlined.Notifications,
                    progressColor = lightSolidProgressColor,
                    trackColor = trackColor,
                    onClick = onRemindersClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactMetricGaugeColumn(
    title: String,
    count: Int,
    progress: Float,
    icon: ImageVector,
    progressColor: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = LocalUtilitarianColors.current
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .then(clickableModifier)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Compact Light Solid Circular Progress Gauge with Number Enclosed
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(56.dp)
        ) {
            Canvas(modifier = Modifier.size(56.dp)) {
                val strokeWidth = 5.dp.toPx()

                // Muted Background Track
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                // Light Solid Progress Arc
                val sweepAngle = progress * 360f
                if (sweepAngle > 0f) {
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            // Tabular Monospaced Count inside the Progress Circle
            Text(
                text = String.format(Locale.US, "%02d", count),
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Center-Aligned Icon + Category Label
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                color = colors.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
