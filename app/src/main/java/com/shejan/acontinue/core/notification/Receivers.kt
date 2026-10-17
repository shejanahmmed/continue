package com.shejan.acontinue.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.shejan.acontinue.MainActivity
import com.shejan.acontinue.R
import com.shejan.acontinue.data.local.ContinueDatabase
import com.shejan.acontinue.data.mapper.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import android.media.AudioAttributes
import android.media.RingtoneManager
import com.shejan.acontinue.domain.model.ActivityPriority
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val activityId = intent.getLongExtra(AlarmScheduler.EXTRA_ACTIVITY_ID, -1L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_ACTIVITY_TITLE) ?: "Upcoming Activity"
        val location = intent.getStringExtra(AlarmScheduler.EXTRA_ACTIVITY_LOCATION).orEmpty()
        val startTime = intent.getLongExtra(AlarmScheduler.EXTRA_ACTIVITY_START_TIME, 0L)
        val endTime = intent.getLongExtra(AlarmScheduler.EXTRA_ACTIVITY_END_TIME, 0L)
        val isAllDay = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_ALL_DAY, false)
        val priorityStr = intent.getStringExtra(AlarmScheduler.EXTRA_ACTIVITY_PRIORITY)
        val priority = runCatching { ActivityPriority.valueOf(priorityStr.orEmpty()) }.getOrDefault(ActivityPriority.MEDIUM)
        val entryTypeStr = intent.getStringExtra(AlarmScheduler.EXTRA_ENTRY_TYPE).orEmpty()
        val isEvent = entryTypeStr.isEmpty() || entryTypeStr == EntryType.ACTIVITY.name

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = if (isEvent) {
            AlarmScheduler.CHANNEL_ID_EVENTS
        } else {
            when (priority) {
                ActivityPriority.LOW -> AlarmScheduler.CHANNEL_ID_LIGHT
                ActivityPriority.MEDIUM -> AlarmScheduler.CHANNEL_ID_MEDIUM
                ActivityPriority.HIGH -> AlarmScheduler.CHANNEL_ID_STRONG
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isEvent) {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val channel = NotificationChannel(
                    AlarmScheduler.CHANNEL_ID_EVENTS,
                    "Event Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts and notifications for scheduled events"
                    enableVibration(true)
                    setSound(
                        soundUri,
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                }
                notificationManager.createNotificationChannel(channel)
            } else {
                when (priority) {
                    ActivityPriority.LOW -> {
                        val lightSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val channel = NotificationChannel(
                            channelId,
                            "Light Reminders",
                            NotificationManager.IMPORTANCE_DEFAULT
                        ).apply {
                            description = "Standard notification and short sound"
                            setSound(
                                lightSound,
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                        }
                        notificationManager.createNotificationChannel(channel)
                    }
                    ActivityPriority.MEDIUM -> {
                        val mediumSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val channel = NotificationChannel(
                            channelId,
                            "Medium Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = "Full screen alert and short sound"
                            enableVibration(true)
                            setSound(
                                mediumSound,
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                        }
                        notificationManager.createNotificationChannel(channel)
                    }
                    ActivityPriority.HIGH -> {
                        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val channel = NotificationChannel(
                            channelId,
                            "Strong Reminders",
                            NotificationManager.IMPORTANCE_HIGH
                        ).apply {
                            description = "Full screen alert and long sound"
                            enableVibration(true)
                            vibrationPattern = longArrayOf(0, 500, 250, 500, 250, 500)
                            setSound(
                                alarmSound,
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                        }
                        notificationManager.createNotificationChannel(channel)
                    }
                }
            }
        }

        // Tapping event alert opens directly to the home screen without navigating away
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!isEvent) {
                putExtra(AlarmScheduler.EXTRA_ACTIVITY_ID, activityId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            activityId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (isEvent) {
            val eventTimeString = EventNotificationFormatter.formatEventDateTime(startTime, endTime, isAllDay)
            val displayTitle = title.ifBlank { "Event" }
            val cleanLocation = EventNotificationFormatter.formatSingleLineLocation(location)

            notificationBuilder
                .setSubText("EVENT")
                .setContentTitle(displayTitle)
                .setContentText(eventTimeString)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)

            val expandedText = if (cleanLocation.isNotEmpty()) {
                "$eventTimeString\n📍 $cleanLocation"
            } else {
                eventTimeString
            }
            notificationBuilder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(expandedText)
            )
        } else {
            val timeString = if (startTime > 0) {
                SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(startTime))
            } else ""

            val contentText = buildString {
                if (timeString.isNotEmpty()) append("Starts at $timeString")
                if (location.isNotEmpty()) append(" • $location")
                if (isEmpty()) append("Activity workspace ready.")
            }

            val reminderCategory = intent.getStringExtra(AlarmScheduler.EXTRA_REMINDER_CATEGORY) ?: ""
            notificationBuilder
                .setContentTitle(title)
                .setContentText(contentText)

            if (reminderCategory.isNotBlank()) {
                notificationBuilder.setSubText(reminderCategory)
            }

            when (priority) {
                ActivityPriority.LOW -> {
                    notificationBuilder
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                }
                ActivityPriority.MEDIUM -> {
                    notificationBuilder
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                }
                ActivityPriority.HIGH -> {
                    val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    notificationBuilder
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVibrate(longArrayOf(0, 500, 250, 500, 250, 500))
                        .setSound(alarmSound)
                }
            }
        }

        notificationManager.notify(activityId.toInt(), notificationBuilder.build())

        // If activity is repeating, re-arm the next upcoming occurrence
        if (activityId > 0) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = ContinueDatabase.getInstance(context).activityDao()
                    val entity = dao.getActivityByIdSync(activityId)
                    if (entity != null && entity.recurrenceMode != RecurrenceMode.NONE.name) {
                        val scheduler = AlarmScheduler(context)
                        scheduler.schedule(entity.toDomain())
                    }
                } catch (e: Exception) {
                    // Suppress background rescheduling exceptions
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scheduler = AlarmScheduler(context)
            val dao = ContinueDatabase.getInstance(context).activityDao()

            CoroutineScope(Dispatchers.IO).launch {
                val now = System.currentTimeMillis()
                val upcoming = dao.getUpcomingIncompleteActivities(now)
                upcoming.forEach { entity ->
                    scheduler.schedule(entity.toDomain())
                }
            }
        }
    }
}
