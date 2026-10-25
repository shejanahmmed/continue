package com.shejan.acontinue.core.designsystem.components

import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.shejan.acontinue.core.util.AudioTrimHelper
import com.shejan.acontinue.presentation.create.NoteAudioPlaybackHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@Composable
fun AudioTrimDialog(
    audioUri: Uri,
    initialDurationMs: Int,
    onDismiss: () -> Unit,
    onTrimSuccess: (newUri: Uri) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var totalDurationMs by remember { mutableIntStateOf(initialDurationMs) }
    var startMs by remember { mutableIntStateOf(0) }
    var endMs by remember { mutableIntStateOf(initialDurationMs) }

    var isPreviewPlaying by remember { mutableStateOf(false) }
    var previewPositionMs by remember { mutableIntStateOf(0) }
    var previewPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var isTrimming by remember { mutableStateOf(false) }

    LaunchedEffect(audioUri) {
        if (totalDurationMs <= 0) {
            val dur = withContext(Dispatchers.IO) {
                AudioTrimHelper.getAudioDurationMs(context, audioUri)
            }
            if (dur > 0) {
                totalDurationMs = dur
                endMs = dur
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                previewPlayer?.stop()
                previewPlayer?.release()
            } catch (_: Exception) {}
            previewPlayer = null
        }
    }

    LaunchedEffect(isPreviewPlaying) {
        while (isPreviewPlaying) {
            previewPlayer?.let { player ->
                if (runCatching { player.isPlaying }.getOrDefault(false)) {
                    val pos = player.currentPosition
                    previewPositionMs = pos
                    if (pos >= endMs) {
                        player.pause()
                        player.seekTo(startMs)
                        previewPositionMs = startMs
                        isPreviewPlaying = false
                    }
                }
            }
            delay(100)
        }
    }

    val togglePreview: () -> Unit = {
        if (isPreviewPlaying) {
            try {
                previewPlayer?.pause()
            } catch (_: Exception) {}
            isPreviewPlaying = false
        } else {
            try {
                if (previewPlayer == null) {
                    val player = MediaPlayer.create(context, audioUri)
                    if (player != null) {
                        player.setOnCompletionListener {
                            isPreviewPlaying = false
                            previewPositionMs = startMs
                        }
                        previewPlayer = player
                    }
                }
                previewPlayer?.let { player ->
                    player.seekTo(startMs)
                    player.start()
                    previewPositionMs = startMs
                    isPreviewPlaying = true
                }
            } catch (_: Exception) {
                isPreviewPlaying = false
            }
        }
    }

    Dialog(
        onDismissRequest = {
            if (!isTrimming) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2225))
                .border(1.dp, Color(0xFF33383F), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Text(
                    text = "Trim audio",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                val selectedDuration = (endMs - startMs).coerceAtLeast(0)
                Text(
                    text = "Keep: ${NoteAudioPlaybackHelper.formatDuration(selectedDuration)} (Total: ${NoteAudioPlaybackHelper.formatDuration(totalDurationMs)})",
                    color = Color(0xFF8E959E),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Range Slider
                val maxFloat = totalDurationMs.toFloat().coerceAtLeast(1000f)
                RangeSlider(
                    value = startMs.toFloat().coerceIn(0f, maxFloat)..endMs.toFloat().coerceIn(0f, maxFloat),
                    onValueChange = { range ->
                        if (isPreviewPlaying) {
                            previewPlayer?.pause()
                            isPreviewPlaying = false
                        }
                        val newStart = range.start.roundToInt()
                        val newEnd = range.endInclusive.roundToInt()
                        if (newEnd - newStart >= 500) {
                            startMs = newStart
                            endMs = newEnd
                        }
                    },
                    valueRange = 0f..maxFloat,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFFD71921),
                        inactiveTrackColor = Color(0xFF2C3136)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Fine-tune Steppers Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Start Stepper
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF262C30))
                            .border(1.dp, Color(0xFF383E44), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "START",
                            color = Color(0xFF8E959E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = NoteAudioPlaybackHelper.formatDuration(startMs),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StepperChip(text = "-1s") {
                                startMs = (startMs - 1000).coerceAtLeast(0)
                            }
                            StepperChip(text = "+1s") {
                                if (startMs + 1000 < endMs - 500) startMs += 1000
                            }
                        }
                    }

                    // End Stepper
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF262C30))
                            .border(1.dp, Color(0xFF383E44), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "END",
                            color = Color(0xFF8E959E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = NoteAudioPlaybackHelper.formatDuration(endMs),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StepperChip(text = "-1s") {
                                if (endMs - 1000 > startMs + 500) endMs -= 1000
                            }
                            StepperChip(text = "+1s") {
                                endMs = (endMs + 1000).coerceAtMost(totalDurationMs)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Preview Player Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF242A2E))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPreviewPlaying) Color(0xFFD71921).copy(alpha = 0.2f) else Color(0xFF2C3136))
                            .border(1.dp, if (isPreviewPlaying) Color(0xFFD71921) else Color(0xFF3F454C), CircleShape)
                            .clickable(onClick = togglePreview),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPreviewPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            contentDescription = if (isPreviewPlaying) "Pause preview" else "Play preview",
                            tint = if (isPreviewPlaying) Color(0xFFD71921) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isPreviewPlaying) "Playing preview..." else "Test selected section",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${NoteAudioPlaybackHelper.formatDuration(previewPositionMs)} / ${NoteAudioPlaybackHelper.formatDuration(endMs)}",
                            color = Color(0xFF8E959E),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isTrimming
                    ) {
                        Text("Cancel", color = Color.White, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFD71921))
                            .clickable(enabled = !isTrimming && endMs > startMs) {
                                isTrimming = true
                                try {
                                    previewPlayer?.stop()
                                    previewPlayer?.release()
                                } catch (_: Exception) {}
                                previewPlayer = null
                                isPreviewPlaying = false

                                coroutineScope.launch {
                                    val attachmentsDir = File(context.filesDir, "attachments").apply {
                                        if (!exists()) mkdirs()
                                    }
                                    val trimmedFile = File(
                                        attachmentsDir,
                                        "note_audio_trimmed_${System.currentTimeMillis()}.m4a"
                                    )

                                    val success = withContext(Dispatchers.IO) {
                                        AudioTrimHelper.trimAudio(
                                            context = context,
                                            sourceUri = audioUri,
                                            destFile = trimmedFile,
                                            startMs = startMs.toLong(),
                                            endMs = endMs.toLong()
                                        )
                                    }

                                    isTrimming = false
                                    if (success) {
                                        onTrimSuccess(Uri.fromFile(trimmedFile))
                                        onDismiss()
                                    }
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTrimming) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "Trim & Save",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepperChip(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1E2225))
            .border(1.dp, Color(0xFF383E44), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
