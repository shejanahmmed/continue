package com.shejan.acontinue.domain.usecase

import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

data class TodayTimeline(
    val activities: List<ActivityItem>,
    val currentActivity: ActivityItem? = null,
    val nextActivity: ActivityItem? = null,
    val currentEvent: ActivityItem? = null,
    val nextEvent: ActivityItem? = null,
    val nextReminder: ActivityItem? = null
)

class GetTodayTimelineUseCase(
    private val activityRepository: ActivityRepository
) {
    operator fun invoke(targetDayMillis: Long = System.currentTimeMillis()): Flow<TodayTimeline> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = targetDayMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        val now = System.currentTimeMillis()

        return activityRepository.getActivitiesForDateRange(startOfDay, endOfDay).map { list ->
            val sorted = list.sortedBy { it.startTimeMillis }
            val current = sorted.firstOrNull {
                it.entryType == EntryType.ACTIVITY && now in it.startTimeMillis..it.endTimeMillis
            }
            val next = sorted.firstOrNull {
                if (it.entryType == EntryType.REMINDER) {
                    !it.isCompleted
                } else {
                    it.startTimeMillis > now
                }
            }

            val events = sorted.filter { it.entryType == EntryType.ACTIVITY }
            val currentEvt = events.firstOrNull { now in it.startTimeMillis..it.endTimeMillis }
            val nextEvt = events.firstOrNull { it.startTimeMillis > now }
            val nextRem = sorted.firstOrNull { it.entryType == EntryType.REMINDER && !it.isCompleted }

            TodayTimeline(
                activities = sorted,
                currentActivity = current,
                nextActivity = next,
                currentEvent = currentEvt,
                nextEvent = nextEvt,
                nextReminder = nextRem
            )
        }
    }
}
