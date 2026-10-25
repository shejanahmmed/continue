package com.shejan.acontinue.core.util

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import com.shejan.acontinue.core.service.AudioPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * State representing audio recording playback across the application.
 */
data class AudioPlaybackState(
    val currentUri: Uri? = null,
    val title: String = "",
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val totalDurationMs: Int = 0
)

/**
 * Global singleton managing audio playback sessions, background foreground service lifecycle,
 * and synchronized UI/notification playback controls.
 */
class AudioPlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())
) {
    private var mediaPlayer: MediaPlayer? = null
    private var tickerJob: Job? = null
    private var lastNotificationUpdateMs = 0L

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    fun play(uri: Uri, title: String = "Voice Recording", startPositionMs: Int = 0): Boolean {
        stopInternal(resetState = false)

        return try {
            val player = MediaPlayer.create(context, uri) ?: return false
            val duration = player.duration.coerceAtLeast(0)

            player.setOnCompletionListener {
                _state.update { it.copy(isPlaying = false, currentPositionMs = 0) }
                stopTicker()
                stopServiceForeground()
            }

            player.setOnErrorListener { _, _, _ ->
                stop()
                true
            }

            if (startPositionMs > 0 && startPositionMs < duration) {
                player.seekTo(startPositionMs)
            }

            player.start()
            mediaPlayer = player

            _state.update {
                AudioPlaybackState(
                    currentUri = uri,
                    title = title,
                    isPlaying = true,
                    currentPositionMs = startPositionMs,
                    totalDurationMs = duration
                )
            }

            startServiceForeground()
            startTicker()
            true
        } catch (_: Exception) {
            stop()
            false
        }
    }

    fun pause(): Boolean {
        val player = mediaPlayer ?: return false
        return try {
            if (player.isPlaying) {
                player.pause()
            }
            _state.update { it.copy(isPlaying = false) }
            stopTicker()
            notifyServiceAction(AudioPlaybackService.ACTION_UPDATE)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun resume(): Boolean {
        val player = mediaPlayer
        val currentUri = _state.value.currentUri
        if (player == null && currentUri != null) {
            return play(currentUri, _state.value.title, _state.value.currentPositionMs)
        }
        if (player == null) return false

        return try {
            player.start()
            _state.update { it.copy(isPlaying = true) }
            startServiceForeground()
            startTicker()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun togglePlayPause(uri: Uri, title: String): Boolean {
        val current = _state.value
        return if (current.currentUri == uri && mediaPlayer != null) {
            if (current.isPlaying) {
                pause()
            } else {
                resume()
            }
        } else {
            play(uri, title)
        }
    }

    fun seekTo(positionMs: Int) {
        val total = _state.value.totalDurationMs
        val clamped = positionMs.coerceIn(0, total.coerceAtLeast(0))
        _state.update { it.copy(currentPositionMs = clamped) }

        try {
            mediaPlayer?.seekTo(clamped)
            notifyServiceAction(AudioPlaybackService.ACTION_UPDATE)
        } catch (_: Exception) {}
    }

    fun stop() {
        stopInternal(resetState = true)
        stopServiceForeground()
    }

    private fun stopInternal(resetState: Boolean) {
        stopTicker()
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null

        if (resetState) {
            _state.update { AudioPlaybackState() }
        }
    }

    private fun startTicker() {
        stopTicker()
        lastNotificationUpdateMs = System.currentTimeMillis()
        tickerJob = scope.launch {
            while (isActive && _state.value.isPlaying) {
                val player = mediaPlayer
                if (player != null && runCatching { player.isPlaying }.getOrDefault(false)) {
                    val currentPos = runCatching { player.currentPosition }.getOrDefault(0)
                    _state.update { it.copy(currentPositionMs = currentPos) }

                    val now = System.currentTimeMillis()
                    if (now - lastNotificationUpdateMs >= 1000L) {
                        lastNotificationUpdateMs = now
                        notifyServiceAction(AudioPlaybackService.ACTION_UPDATE)
                    }
                }
                delay(200)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun startServiceForeground() {
        try {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = AudioPlaybackService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {}
    }

    private fun stopServiceForeground() {
        try {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = AudioPlaybackService.ACTION_STOP
            }
            context.startService(intent)
        } catch (_: Exception) {}
    }

    private fun notifyServiceAction(action: String) {
        try {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                this.action = action
            }
            context.startService(intent)
        } catch (_: Exception) {}
    }
}
