package com.shejan.acontinue.core.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.shejan.acontinue.ContinueApplication
import com.shejan.acontinue.MainActivity
import com.shejan.acontinue.R
import com.shejan.acontinue.presentation.create.NoteAudioPlaybackHelper

/**
 * Foreground service managing active audio playback across navigation destinations,
 * background states, and providing media controls via system notifications.
 */
class AudioPlaybackService : Service() {

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        val appContainer = (application as? ContinueApplication)?.container
        val playbackManager = appContainer?.audioPlaybackManager

        when (action) {
            ACTION_START -> {
                val state = playbackManager?.state?.value
                val notification = buildPlaybackNotification(
                    title = state?.title ?: "Voice Recording",
                    isPlaying = state?.isPlaying ?: true,
                    currentMs = state?.currentPositionMs ?: 0,
                    totalMs = state?.totalDurationMs ?: 0
                )
                try {
                    val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                    } else 0
                    ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, foregroundType)
                } catch (_: Exception) {
                    try {
                        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, 0)
                    } catch (_: Exception) {}
                }
            }
            ACTION_TOGGLE_PAUSE -> {
                val state = playbackManager?.state?.value
                if (state != null) {
                    if (state.isPlaying) {
                        playbackManager.pause()
                    } else {
                        playbackManager.resume()
                    }
                }
            }
            ACTION_STOP -> {
                playbackManager?.stop()
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_UPDATE -> {
                val state = playbackManager?.state?.value
                if (state != null) {
                    val notification = buildPlaybackNotification(
                        title = state.title,
                        isPlaying = state.isPlaying,
                        currentMs = state.currentPositionMs,
                        totalMs = state.totalDurationMs
                    )
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            }
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        val appContainer = (application as? ContinueApplication)?.container
        appContainer?.audioPlaybackManager?.stop()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active audio recording playback status and controls"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildPlaybackNotification(
        title: String,
        isPlaying: Boolean,
        currentMs: Int,
        totalMs: Int
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, AudioPlaybackService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AudioPlaybackService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = NotificationCompat.Action.Builder(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (isPlaying) "Pause" else "Play",
            togglePendingIntent
        ).build()

        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            stopPendingIntent
        ).build()

        val timeText = if (totalMs > 0) {
            "${NoteAudioPlaybackHelper.formatDuration(currentMs)} / ${NoteAudioPlaybackHelper.formatDuration(totalMs)}"
        } else {
            NoteAudioPlaybackHelper.formatDuration(currentMs)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title.ifBlank { "Voice Recording" })
            .setContentText(timeText)
            .setProgress(totalMs.coerceAtLeast(1), currentMs.coerceIn(0, totalMs.coerceAtLeast(1)), false)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent)
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "audio_playback_channel"
        const val NOTIFICATION_ID = 2002

        const val ACTION_START = "com.shejan.acontinue.action.START_PLAYBACK"
        const val ACTION_TOGGLE_PAUSE = "com.shejan.acontinue.action.TOGGLE_PAUSE_PLAYBACK"
        const val ACTION_STOP = "com.shejan.acontinue.action.STOP_PLAYBACK"
        const val ACTION_UPDATE = "com.shejan.acontinue.action.UPDATE_PLAYBACK"
    }
}
