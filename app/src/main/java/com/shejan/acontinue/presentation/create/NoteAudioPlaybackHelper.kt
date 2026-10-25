package com.shejan.acontinue.presentation.create

import kotlin.math.roundToInt

/**
 * Pure helper for audio playback timestamp formatting and scrubber math.
 * Separated to ensure deterministic unit testing across boundary conditions.
 */
object NoteAudioPlaybackHelper {

    fun formatDuration(ms: Int): String {
        val totalSec = (ms / 1000).coerceAtLeast(0)
        val m = totalSec / 60
        val s = totalSec % 60
        return "%02d:%02d".format(m, s)
    }

    fun calculateProgress(currentMs: Int, totalDurationMs: Int): Float {
        if (totalDurationMs <= 0) return 0f
        return (currentMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    }

    fun calculateSeekPosition(fraction: Float, totalDurationMs: Int): Int {
        if (totalDurationMs <= 0) return 0
        val clampedFraction = fraction.coerceIn(0f, 1f)
        return (clampedFraction * totalDurationMs).roundToInt().coerceIn(0, totalDurationMs)
    }
}
