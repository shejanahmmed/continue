package com.shejan.acontinue.core.notification

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object ReminderSoundManager {

    @Volatile
    private var activeRingtone: Ringtone? = null

    @Volatile
    private var activeVibrator: Vibrator? = null

    private val vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)

    @Synchronized
    fun startContinuousAlarm(context: Context) {
        stop()

        try {
            val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val ringtone = RingtoneManager.getRingtone(context.applicationContext, alarmUri)
            ringtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            // Enable continuous playback loop where supported on Android 9+ (API 28)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ringtone?.isLooping = true
            }
            ringtone?.play()
            activeRingtone = ringtone

            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(vibrationPattern, 0)
            }
            activeVibrator = vibrator
        } catch (_: Exception) {
            // Safe fallback: hardware/permission failure must never crash the alarm receiver
        }
    }

    @Synchronized
    fun playShortAlert(context: Context) {
        stop()

        try {
            val notifUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, notifUri)
            ringtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone?.play()
            activeRingtone = ringtone

            val vibrator = getVibrator(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(300L, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300L)
            }
        } catch (_: Exception) {
            // Suppress audio hardware errors
        }
    }

    @Synchronized
    fun stop() {
        try {
            activeRingtone?.stop()
            activeRingtone = null
        } catch (_: Exception) {}

        try {
            activeVibrator?.cancel()
            activeVibrator = null
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
