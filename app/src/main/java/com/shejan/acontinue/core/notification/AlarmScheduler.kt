package com.shejan.acontinue.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shejan.acontinue.domain.model.ActivityItem

interface ActivityScheduler {
    fun schedule(activity: ActivityItem)
    fun cancel(activityId: Long)
}

class AlarmScheduler(private val context: Context) : ActivityScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun schedule(activity: ActivityItem) {
        if (activity.reminderMinutesBefore < 0) return

        val triggerTimeMillis = activity.startTimeMillis - (activity.reminderMinutesBefore * 60 * 1000L)
        if (triggerTimeMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ACTIVITY_ID, activity.id)
            putExtra(EXTRA_ACTIVITY_TITLE, activity.title)
            putExtra(EXTRA_ACTIVITY_LOCATION, activity.location)
            putExtra(EXTRA_ACTIVITY_START_TIME, activity.startTimeMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            activity.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager?.let { manager ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (manager.canScheduleExactAlarms()) {
                    manager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    manager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                manager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        }
    }

    override fun cancel(activityId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            activityId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        const val EXTRA_ACTIVITY_ID = "extra_activity_id"
        const val EXTRA_ACTIVITY_TITLE = "extra_activity_title"
        const val EXTRA_ACTIVITY_LOCATION = "extra_activity_location"
        const val EXTRA_ACTIVITY_START_TIME = "extra_activity_start_time"
        const val CHANNEL_ID = "activity_reminders_channel"
    }
}
