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
import com.shejan.acontinue.core.notification.AlarmScheduler

/**
 * Foreground service ensuring continuous audio recording across app destinations
 * and background states. Persists audio to note on task completion or process termination.
 */
class AudioRecordingService : Service() {

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var currentNoteId: Long? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        val noteId = intent?.getLongExtra(EXTRA_NOTE_ID, -1L)?.takeIf { it > 0L }
        if (noteId != null) {
            currentNoteId = noteId
        }

        val appContainer = (application as? ContinueApplication)?.container
        val manager = appContainer?.audioRecordingManager

        when (action) {
            ACTION_START -> {
                try {
                    val notification = buildRecordingNotification(manager?.state?.value?.isPaused == true)
                    val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    } else 0
                    ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, foregroundType)
                } catch (_: Exception) {
                    try {
                        val notification = buildRecordingNotification(manager?.state?.value?.isPaused == true)
                        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, 0)
                    } catch (_: Exception) {}
                }
            }
            ACTION_TOGGLE_PAUSE -> {
                if (manager?.state?.value?.isPaused == true) {
                    manager.resumeRecording()
                } else {
                    manager?.pauseRecording()
                }
                updateNotification(manager?.state?.value?.isPaused == true)
            }
            ACTION_STOP -> {
                manager?.stopRecording()
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_UPDATE -> {
                updateNotification(manager?.state?.value?.isPaused == true)
            }
        }

        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Cleanly stop recording and persist to Room database when app task is swiped away
        val appContainer = (application as? ContinueApplication)?.container
        appContainer?.audioRecordingManager?.stopAndSaveBlocking()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voice Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active background audio recording status and controls"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildRecordingNotification(isPaused: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            currentNoteId?.let { putExtra(AlarmScheduler.EXTRA_ACTIVITY_ID, it) }
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, AudioRecordingService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val togglePendingIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AudioRecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseResumeAction = NotificationCompat.Action.Builder(
            if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
            if (isPaused) "Resume" else "Pause",
            togglePendingIntent
        ).build()

        val stopAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_save,
            "Stop & Save",
            stopPendingIntent
        ).build()

        val subText = if (isPaused) "Paused" else "Recording voice note..."

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Voice Recording")
            .setContentText(subText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentPendingIntent)
            .addAction(pauseResumeAction)
            .addAction(stopAction)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification(isPaused: Boolean) {
        val notification = buildRecordingNotification(isPaused)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "voice_recording_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.shejan.acontinue.action.START_RECORDING"
        const val ACTION_TOGGLE_PAUSE = "com.shejan.acontinue.action.TOGGLE_PAUSE_RECORDING"
        const val ACTION_STOP = "com.shejan.acontinue.action.STOP_RECORDING"
        const val ACTION_UPDATE = "com.shejan.acontinue.action.UPDATE_RECORDING"

        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}
