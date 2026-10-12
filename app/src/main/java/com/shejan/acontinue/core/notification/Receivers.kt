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
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val activityId = intent.getLongExtra(AlarmScheduler.EXTRA_ACTIVITY_ID, -1L)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_ACTIVITY_TITLE) ?: "Upcoming Activity"
        val location = intent.getStringExtra(AlarmScheduler.EXTRA_ACTIVITY_LOCATION).orEmpty()
        val startTime = intent.getLongExtra(AlarmScheduler.EXTRA_ACTIVITY_START_TIME, 0L)

        val timeString = if (startTime > 0) {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(startTime))
        } else ""

        val contentText = buildString {
            if (timeString.isNotEmpty()) append("Starts at $timeString")
            if (location.isNotEmpty()) append(" • $location")
            if (isEmpty()) append("Activity workspace ready.")
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AlarmScheduler.CHANNEL_ID,
                "Activity Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for scheduled activity workspaces"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmScheduler.EXTRA_ACTIVITY_ID, activityId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            activityId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AlarmScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(contentText)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        notificationManager.notify(activityId.toInt(), notification)
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
