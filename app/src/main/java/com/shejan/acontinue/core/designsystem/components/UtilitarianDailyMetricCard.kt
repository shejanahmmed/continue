package com.shejan.acontinue.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxSize
import com.shejan.acontinue.R
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import com.shejan.acontinue.ui.theme.UtilitarianAccentRed
import kotlin.math.hypot
import java.util.Locale

/**
 * Utilitarian overview card featuring left-aligned event and reminder counts
 * paired with a concentric dual-circular progress gauge on the right.
 * The outer ring visualizes interactive reminder completion, while the inner
 * ring reflects automatic timetable schedule progression.
 */
@Composable
fun UtilitarianDailyMetricCard(
    eventCount: Int,
    completedEventCount: Int,
    eventProgress: Float,
    reminderCount: Int,
    completedReminderCount: Int,
    reminderProgress: Float,
    modifier: Modifier = Modifier,
    pendingReminderCount: Int = (reminderCount - completedReminderCount).coerceAtLeast(0),
    missedReminderCount: Int = 0,
    onClick: (() -> Unit)? = null,
    onEventsClick: (() -> Unit)? = null,
    onRemindersClick: (() -> Unit)? = null
) {
    val colors = LocalUtilitarianColors.current

    // Animated number transitions for counts
    val animatedCompletedEvents by animateIntAsState(
        targetValue = completedEventCount,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "animatedCompletedEvents"
    )
    val animatedTotalEvents by animateIntAsState(
        targetValue = eventCount,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "animatedTotalEvents"
    )
    val animatedCompletedReminders by animateIntAsState(
        targetValue = completedReminderCount,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "animatedCompletedReminders"
    )
    val animatedTotalReminders by animateIntAsState(
        targetValue = reminderCount,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "animatedTotalReminders"
    )
    val animatedMissedReminders by animateIntAsState(
        targetValue = missedReminderCount,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "animatedMissedReminders"
    )

    // Animated progress arcs for concentric rings
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

    // Surface and border colors calibrated for dark canvas contrast
    val cardBgColor = if (colors.isDark) Color(0xFF1E1E22) else Color(0xFFFFFFFF)
    val cardBorderColor = if (colors.isDark) Color(0xFF323238) else colors.borderStroke

    // Subtle dark slate track for uncompleted progress
    val trackColor = if (colors.isDark) Color(0xFF2C2C32) else Color(0xFFE5E5EA)

    // Outer ring accent (Reminders): vibrant utilitarian red
    val reminderAccentColor = if (colors.isDark) Color(0xFFFF3B30) else Color(0xFFE02E24)
    // Inner ring accent (Events): electric sky blue
    val eventAccentColor = if (colors.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    val effectiveClick = onClick ?: onEventsClick ?: onRemindersClick
    val cardClickableModifier = if (effectiveClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            effectiveClick()
        }
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(cardClickableModifier),
        shape = RoundedCornerShape(14.dp),
        color = cardBgColor,
        border = BorderStroke(1.dp, cardBorderColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Layer: Abstract faded dot-matrix pattern
            AbstractDotMatrixTexture(
                isDark = colors.isDark,
                modifier = Modifier.matchParentSize()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            // Left Column: Header + Event Row + Reminder Row
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "TODAY'S OVERVIEW",
                    color = colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Line 1: Events Row
                MetricCountRow(
                    dotColor = eventAccentColor,
                    label = "EVENTS",
                    completed = animatedCompletedEvents,
                    total = animatedTotalEvents
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Line 2: Reminders Row
                MetricCountRow(
                    dotColor = reminderAccentColor,
                    label = "REMINDERS",
                    completed = animatedCompletedReminders,
                    total = animatedTotalReminders
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Line 3: Missed Reminders Row
                SingleMetricRow(
                    dotColor = if (animatedMissedReminders > 0) reminderAccentColor else colors.textDisabled,
                    label = "MISSED",
                    count = animatedMissedReminders
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Column: Concentric Dual Circular Progress Gauge
            ConcentricDualGauge(
                outerProgress = animatedReminderProgress,
                outerColor = reminderAccentColor,
                innerProgress = animatedEventProgress,
                innerColor = eventAccentColor,
                trackColor = trackColor,
                cardBgColor = cardBgColor,
                isDark = colors.isDark
            )
        }
    }
}
}

/**
 * Single metric row displaying a color dot, label, and formatted 00/00 count readout.
 */
@Composable
private fun MetricCountRow(
    dotColor: Color,
    label: String,
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicator dot tied directly to the corresponding gauge ring color
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color = dotColor, shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Fixed column width for label ensures counts align perfectly in a vertical column
        Text(
            text = label,
            color = colors.textSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            maxLines = 1,
            modifier = Modifier.width(82.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = String.format(Locale.US, "%02d / %02d", completed, total),
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Single metric row displaying an indicator dot, label, and 2-digit count readout (e.g. 00, 01).
 */
@Composable
private fun SingleMetricRow(
    dotColor: Color,
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Indicator dot tied directly to the alert status
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color = dotColor, shape = CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Fixed column width for label ensures counts align perfectly in a vertical column
        Text(
            text = label,
            color = colors.textSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            maxLines = 1,
            modifier = Modifier.width(82.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = String.format(Locale.US, "%02d", count),
            color = if (count > 0) dotColor else colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Concentric dual-circular progress bar rendered on a clean Canvas.
 * Outer ring represents Reminders, Inner ring represents Events.
 * Zero text or clutter is drawn inside the circle.
 */
@Composable
private fun ConcentricDualGauge(
    outerProgress: Float,
    outerColor: Color,
    innerProgress: Float,
    innerColor: Color,
    trackColor: Color,
    cardBgColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val logoResId = if (isDark) R.drawable.app_logo_light else R.drawable.app_logo_dark

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(84.dp)
    ) {
        Canvas(modifier = Modifier.size(84.dp)) {
            val outerStrokePx = 5.5.dp.toPx()
            val gapPx = 3.5.dp.toPx()
            val innerStrokePx = 5.0.dp.toPx()

            // 1. Outer Ring: Reminders
            val outerTopLeft = Offset(outerStrokePx / 2f, outerStrokePx / 2f)
            val outerArcSize = Size(size.width - outerStrokePx, size.height - outerStrokePx)

            // Outer background track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = outerTopLeft,
                size = outerArcSize,
                style = Stroke(width = outerStrokePx)
            )

            // Outer progress arc
            val outerSweep = (outerProgress * 360f).coerceIn(0f, 360f)
            if (outerSweep > 0f) {
                drawArc(
                    color = outerColor,
                    startAngle = -90f,
                    sweepAngle = outerSweep,
                    useCenter = false,
                    topLeft = outerTopLeft,
                    size = outerArcSize,
                    style = Stroke(width = outerStrokePx, cap = StrokeCap.Round)
                )
            }

            // 2. Inner Ring: Events
            val innerInset = outerStrokePx + gapPx + (innerStrokePx / 2f)
            val innerTopLeft = Offset(innerInset, innerInset)
            val innerArcSize = Size(size.width - (2f * innerInset), size.height - (2f * innerInset))

            // Inner background track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = innerTopLeft,
                size = innerArcSize,
                style = Stroke(width = innerStrokePx)
            )

            // Inner progress arc
            val innerSweep = (innerProgress * 360f).coerceIn(0f, 360f)
            if (innerSweep > 0f) {
                drawArc(
                    color = innerColor,
                    startAngle = -90f,
                    sweepAngle = innerSweep,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerArcSize,
                    style = Stroke(width = innerStrokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Dedicated inner circular background matching the card surface with scaled-down logo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .background(color = cardBgColor, shape = CircleShape)
        ) {
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = "App Logo",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Hardware-centric abstract dot-matrix background texture.
 * Features mathematical pitch alignment (14dp), distance-based subtle opacity falloff
 * to protect foreground text legibility, and a minimalist origin accent marker.
 */
@Composable
private fun AbstractDotMatrixTexture(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val baseDotColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000)
    val accentColor = UtilitarianAccentRed
    val shape = RoundedCornerShape(14.dp)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .clip(shape)
    ) {
        val pitch = 14.dp.toPx()
        val dotRadius = 1.1.dp.toPx()
        val width = size.width
        val height = size.height
        val maxDistance = hypot(width, height)

        var x = pitch / 2f
        var col = 0
        while (x < width) {
            var y = pitch / 2f
            var row = 0
            while (y < height) {
                // Distance to top-right corner where concentric gauge is placed
                val distanceToTopRight = hypot(width - x, y)
                val normalizedDistance = (distanceToTopRight / maxDistance).coerceIn(0f, 1f)

                // Subtle faded alpha: 0.03f behind text up to 0.13f in upper-right ambient area
                val baseAlpha = if (isDark) {
                    0.03f + (1f - normalizedDistance) * 0.10f
                } else {
                    0.02f + (1f - normalizedDistance) * 0.07f
                }

                // Origin reference micro-marker near top header
                val isOriginMarker = (col == 2 && row == 0)

                if (isOriginMarker) {
                    drawCircle(
                        color = accentColor.copy(alpha = if (isDark) 0.5f else 0.35f),
                        radius = dotRadius * 1.25f,
                        center = Offset(x, y)
                    )
                } else {
                    drawCircle(
                        color = baseDotColor.copy(alpha = baseAlpha),
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                }

                y += pitch
                row++
            }
            x += pitch
            col++
        }
    }
}
