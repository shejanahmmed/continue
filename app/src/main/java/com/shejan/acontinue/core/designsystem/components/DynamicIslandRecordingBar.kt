package com.shejan.acontinue.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.core.util.RecordingState
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DynamicIslandRecordingBar(
    scale: Float,
    widthDp: Float,
    contentAlpha: Float,
    elapsedMillis: Long,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    onIslandClick: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .width(widthDp.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Color(0xFF202020))
            .border(1.dp, Color(0xFF333336), RoundedCornerShape(percent = 50))
            .then(
                if (onIslandClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onIslandClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Amber circular recording badge
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF241908))
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFFFF9F0A).copy(alpha = if (isPaused) 0.4f else pulseAlpha),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Recording",
                    tint = Color(0xFFFF9F0A).copy(alpha = if (isPaused) 0.5f else 1f),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Controls revealed as pill expands
            if (widthDp > 80f && contentAlpha > 0f) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .alpha(contentAlpha)
                        .padding(start = 8.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // Elapsed timer in warm amber
                    Text(
                        text = formatElapsed(elapsedMillis),
                        color = Color(0xFFFF9F0A),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Pause / Resume Toggle Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2E))
                            .border(1.dp, Color(0xFF3E3E42), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTogglePause()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = if (isPaused) Color(0xFFFF9F0A) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Stop / Save Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2E))
                            .border(1.dp, Color(0xFF3E3E42), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onStop()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stop,
                            contentDescription = "Stop recording",
                            tint = Color(0xFFD71921),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GlobalRecordingDynamicIsland(
    recordingState: RecordingState,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    onIslandClick: (() -> Unit)? = null
) {
    val islandScale = remember { Animatable(0f) }
    val islandWidthDp = remember { Animatable(48f) }
    val islandContentAlpha = remember { Animatable(0f) }

    LaunchedEffect(recordingState.isRecording) {
        if (recordingState.isRecording) {
            islandScale.snapTo(0f)
            islandWidthDp.snapTo(48f)
            islandContentAlpha.snapTo(0f)

            islandScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )

            val expandJob = launch {
                islandWidthDp.animateTo(
                    targetValue = 204f,
                    animationSpec = spring(
                        dampingRatio = 0.78f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            islandContentAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180)
            )
            expandJob.join()
        } else {
            val fadeJob = launch { islandContentAlpha.animateTo(0f, tween(100)) }
            islandWidthDp.animateTo(48f, tween(180, easing = LinearOutSlowInEasing))
            fadeJob.join()
            islandScale.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
        }
    }

    if (islandScale.value > 0.01f) {
        DynamicIslandRecordingBar(
            scale = islandScale.value,
            widthDp = islandWidthDp.value,
            contentAlpha = islandContentAlpha.value,
            elapsedMillis = recordingState.elapsedMillis,
            isPaused = recordingState.isPaused,
            onTogglePause = onTogglePause,
            onStop = onStop,
            onIslandClick = onIslandClick,
            modifier = modifier
        )
    }
}

private fun formatElapsed(millis: Long): String {
    val totalSec = (millis / 1000).coerceAtLeast(0)
    val sec = totalSec % 60
    val min = (totalSec / 60) % 60
    val hours = totalSec / 3600
    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, min, sec)
    } else {
        String.format(Locale.US, "%02d:%02d", min, sec)
    }
}
