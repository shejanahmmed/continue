package com.shejan.acontinue.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.util.RecurrenceEvaluator

interface ActivityScheduler {
    fun schedule(activity: ActivityItem)
    fun cancel(activityId: Long)
}

class AlarmScheduler(private val context: Context) : ActivityScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    override fun schedule(activity: ActivityItem) {
        val now = System.currentTimeMillis()
        val effectiveActivity = if (activity.recurrenceMode != RecurrenceMode.NONE) {
            val nextOccurrenceStart = RecurrenceEvaluator.getNextOccurrenceStart(activity, now - 60_000L)
            if (nextOccurrenceStart != null) {
                val duration = (activity.endTimeMillis - activity.startTimeMillis).coerceAtLeast(0L)
                activity.copy(
                    startTimeMillis = nextOccurrenceStart,
                    endTimeMillis = nextOccurrenceStart + duration
                )
            } else {
                activity
            }
        } else {
            activity
        }

        val rawOffsets = if (effectiveActivity.reminderMinutesList.isNotEmpty()) {
            effectiveActivity.reminderMinutesList
        } else if (effectiveActivity.reminderMinutesBefore >= 0) {
            listOf(effectiveActivity.reminderMinutesBefore)
        } else {
            emptyList()
        }

        // Reminders must always trigger at their scheduled time (offset 0) alongside any early alerts
        val offsets = if (effectiveActivity.entryType == EntryType.REMINDER) {
            (rawOffsets + 0).distinct().sorted()
        } else {
            rawOffsets
        }

        if (offsets.isEmpty()) return

        for (minutes in offsets) {
            if (minutes < 0) continue
            val triggerTimeMillis = effectiveActivity.startTimeMillis - (minutes * 60 * 1000L)
            if (triggerTimeMillis <= now) continue

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(EXTRA_ACTIVITY_ID, effectiveActivity.id)
                putExtra(EXTRA_ACTIVITY_TITLE, effectiveActivity.title)
                putExtra(EXTRA_ACTIVITY_LOCATION, effectiveActivity.location)
                putExtra(EXTRA_ACTIVITY_START_TIME, effectiveActivity.startTimeMillis)
                putExtra(EXTRA_ACTIVITY_END_TIME, effectiveActivity.endTimeMillis)
                putExtra(EXTRA_IS_ALL_DAY, effectiveActivity.isAllDay)
                putExtra(EXTRA_ACTIVITY_PRIORITY, effectiveActivity.priority.name)
                putExtra(EXTRA_ENTRY_TYPE, effectiveActivity.entryType.name)
                putExtra(EXTRA_REMINDER_CATEGORY, effectiveActivity.reminderCategory)
            }

            val requestCode = getRequestCode(activity.id, minutes)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
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
    }

    override fun cancel(activityId: Long) {
        // Cancel base request code as well as potential offset slots
        val baseIntent = Intent(context, AlarmReceiver::class.java)
        val basePending = PendingIntent.getBroadcast(
            context,
            activityId.toInt(),
            baseIntent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (basePending != null) {
            alarmManager?.cancel(basePending)
            basePending.cancel()
        }

        // Cancel common preset offset slots
        val commonOffsets = listOf(0, 5, 10, 15, 30, 60, 1440, 10080)
        for (m in commonOffsets) {
            val reqCode = getRequestCode(activityId, m)
            val pi = PendingIntent.getBroadcast(
                context,
                reqCode,
                baseIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pi != null) {
                alarmManager?.cancel(pi)
                pi.cancel()
            }
        }
    }

    private fun getRequestCode(activityId: Long, minutes: Int): Int {
        return ((activityId % 100000L) * 1000L + (minutes % 1000L)).toInt()
    }

    companion object {
        const val EXTRA_ACTIVITY_ID = "extra_activity_id"
        const val EXTRA_ACTIVITY_TITLE = "extra_activity_title"
        const val EXTRA_ACTIVITY_LOCATION = "extra_activity_location"
        const val EXTRA_ACTIVITY_START_TIME = "extra_activity_start_time"
        const val EXTRA_ACTIVITY_END_TIME = "extra_activity_end_time"
        const val EXTRA_IS_ALL_DAY = "extra_is_all_day"
        const val EXTRA_ACTIVITY_PRIORITY = "extra_activity_priority"
        const val EXTRA_ENTRY_TYPE = "extra_entry_type"
        const val EXTRA_REMINDER_CATEGORY = "extra_reminder_category"
        const val CHANNEL_ID = "activity_reminders_channel"
        const val CHANNEL_ID_LIGHT = "activity_reminders_light"
        const val CHANNEL_ID_MEDIUM = "activity_reminders_medium"
        const val CHANNEL_ID_STRONG = "activity_reminders_strong"
        const val CHANNEL_ID_EVENTS = "activity_events_channel"
    }
}
