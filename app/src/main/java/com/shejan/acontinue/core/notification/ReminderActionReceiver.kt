package com.shejan.acontinue.core.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.shejan.acontinue.data.local.ContinueDatabase
import com.shejan.acontinue.data.mapper.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val activityId = intent.getLongExtra(AlarmScheduler.EXTRA_ACTIVITY_ID, -1L)
        if (activityId <= 0) return

        ReminderSoundManager.stop()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(activityId.toInt())

        when (intent.action) {
            ACTION_COMPLETE -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val dao = ContinueDatabase.getInstance(context).activityDao()
                        dao.updateCompletionStatus(activityId, true)
                        AlarmScheduler(context).cancel(activityId)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_SNOOZE -> {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val dao = ContinueDatabase.getInstance(context).activityDao()
                        val entity = dao.getActivityByIdSync(activityId)
                        if (entity != null) {
                            val snoozeMillis = System.currentTimeMillis() + (10 * 60 * 1000L)
                            val snoozedActivity = entity.toDomain().copy(
                                startTimeMillis = snoozeMillis,
                                reminderMinutesList = listOf(0),
                                reminderMinutesBefore = 0
                            )
                            AlarmScheduler(context).schedule(snoozedActivity)
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            ACTION_DISMISS -> {
                // Sound and notification already cleared above
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.shejan.acontinue.ACTION_COMPLETE_REMINDER"
        const val ACTION_SNOOZE = "com.shejan.acontinue.ACTION_SNOOZE_REMINDER"
        const val ACTION_DISMISS = "com.shejan.acontinue.ACTION_DISMISS_REMINDER"
    }
}
