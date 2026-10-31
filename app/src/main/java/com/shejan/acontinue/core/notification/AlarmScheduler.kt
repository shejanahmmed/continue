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
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun schedule(activity: ActivityItem) {
        if (activity.id > 0L) {
            cancel(activity.id)
        }

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

        val scheduledRequestCodes = mutableSetOf<String>()

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
                putExtra(EXTRA_REMINDER_CATEGORY_COLOR, effectiveActivity.reminderCategoryColorHex)
                putExtra(EXTRA_REMINDER_CATEGORY_ICON, effectiveActivity.reminderCategoryIcon)
            }

            val requestCode = getRequestCode(effectiveActivity.id, minutes)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduledRequestCodes.add(requestCode.toString())

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

        if (effectiveActivity.id > 0L && scheduledRequestCodes.isNotEmpty()) {
            saveScheduledRequestCodes(effectiveActivity.id, scheduledRequestCodes)
        }
    }

    override fun cancel(activityId: Long) {
        val baseIntent = Intent(context, AlarmReceiver::class.java)

        // 1. Cancel exact tracked request codes (including custom alert offsets)
        val trackedCodes = getTrackedRequestCodes(activityId)
        for (codeStr in trackedCodes) {
            val reqCode = codeStr.toIntOrNull() ?: continue
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
        clearTrackedRequestCodes(activityId)

        // 2. Cancel base request code as well as potential legacy offset slots
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

        // 3. Cancel common preset offset slots (safety net for legacy alarms)
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

    private fun getTrackedRequestCodes(activityId: Long): Set<String> {
        return prefs.getStringSet(getKey(activityId), emptySet()) ?: emptySet()
    }

    private fun saveScheduledRequestCodes(activityId: Long, codes: Set<String>) {
        prefs.edit().putStringSet(getKey(activityId), codes).apply()
    }

    private fun clearTrackedRequestCodes(activityId: Long) {
        prefs.edit().remove(getKey(activityId)).apply()
    }

    private fun getKey(activityId: Long): String = "tracked_alarm_reqs_$activityId"

    companion object {
        private const val PREFS_NAME = "continue_alarm_scheduler_prefs"

        fun getRequestCode(activityId: Long, minutes: Int): Int {
            val safeActivitySlot = (activityId % 2000L).coerceAtLeast(0L)
            val safeMinutes = (minutes.toLong() % 1000000L).coerceAtLeast(0L)
            return (safeActivitySlot * 1000000L + safeMinutes).toInt()
        }

        const val EXTRA_ACTIVITY_ID = "extra_activity_id"
        const val EXTRA_ACTIVITY_TITLE = "extra_activity_title"
        const val EXTRA_ACTIVITY_LOCATION = "extra_activity_location"
        const val EXTRA_ACTIVITY_START_TIME = "extra_activity_start_time"
        const val EXTRA_ACTIVITY_END_TIME = "extra_activity_end_time"
        const val EXTRA_IS_ALL_DAY = "extra_is_all_day"
        const val EXTRA_ACTIVITY_PRIORITY = "extra_activity_priority"
        const val EXTRA_ENTRY_TYPE = "extra_entry_type"
        const val EXTRA_REMINDER_CATEGORY = "extra_reminder_category"
        const val EXTRA_REMINDER_CATEGORY_COLOR = "extra_reminder_category_color"
        const val EXTRA_REMINDER_CATEGORY_ICON = "extra_reminder_category_icon"
        const val CHANNEL_ID = "activity_reminders_channel"
        const val CHANNEL_ID_LIGHT = "activity_reminders_light"
        const val CHANNEL_ID_MEDIUM = "activity_reminders_medium"
        const val CHANNEL_ID_STRONG = "activity_reminders_strong"
        const val CHANNEL_ID_EVENTS = "activity_events_channel"
    }
}
