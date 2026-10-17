package com.shejan.acontinue.domain.util

import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.MonthlyRepeatType
import com.shejan.acontinue.domain.model.RecurrenceEndType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.model.YearlyRepeatType
import java.util.Calendar

object RecurrenceEvaluator {

    fun getStartOfDayMillis(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun getEndOfDayMillis(millis: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    fun occursOnDay(activity: ActivityItem, targetDayMillis: Long): Boolean {
        val targetDayStart = getStartOfDayMillis(targetDayMillis)
        val targetDayEnd = getEndOfDayMillis(targetDayMillis)
        val activityStartDate = getStartOfDayMillis(activity.startTimeMillis)

        if (targetDayStart < activityStartDate) return false

        if (activity.recurrenceMode == RecurrenceMode.NONE) {
            return activity.startTimeMillis <= targetDayEnd && activity.endTimeMillis >= targetDayStart
        }

        // Check UNTIL_DATE end condition
        if (activity.recurrenceEndType == RecurrenceEndType.UNTIL_DATE && activity.recurrenceEndDateMillis > 0L) {
            val endLimit = getEndOfDayMillis(activity.recurrenceEndDateMillis)
            if (targetDayStart > endLimit) return false
        }

        val startCal = Calendar.getInstance().apply { timeInMillis = activity.startTimeMillis }
        val targetCal = Calendar.getInstance().apply { timeInMillis = targetDayStart }
        val interval = activity.recurrenceInterval.coerceAtLeast(1)

        return when (activity.recurrenceMode) {
            RecurrenceMode.NONE -> false
            RecurrenceMode.MINUTELY, RecurrenceMode.HOURLY, RecurrenceMode.DAILY -> {
                val daysDiff = ((targetDayStart - activityStartDate) / (24 * 60 * 60 * 1000L)).toInt()
                if (daysDiff < 0 || daysDiff % interval != 0) return false
                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val occurrenceIndex = daysDiff / interval
                    if (occurrenceIndex >= activity.recurrenceEndCount) return false
                }
                true
            }
            RecurrenceMode.WEEKDAYS -> {
                val dow = targetCal.get(Calendar.DAY_OF_WEEK)
                if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) return false
                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val countBefore = countWeekdaysBetween(activityStartDate, targetDayStart)
                    if (countBefore >= activity.recurrenceEndCount) return false
                }
                true
            }
            RecurrenceMode.WEEKLY -> {
                val dow = targetCal.get(Calendar.DAY_OF_WEEK)
                val matchesDow = if (activity.recurrenceDaysOfWeek.isNotEmpty()) {
                    dow in activity.recurrenceDaysOfWeek
                } else {
                    dow == startCal.get(Calendar.DAY_OF_WEEK)
                }
                if (!matchesDow) return false

                val startWeekCal = (startCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val targetWeekCal = (targetCal.clone() as Calendar).apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                val weeksDiff = ((targetWeekCal.timeInMillis - startWeekCal.timeInMillis) / (7 * 24 * 60 * 60 * 1000L)).toInt()
                if (weeksDiff < 0 || weeksDiff % interval != 0) return false
                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val countBefore = countWeeklyOccurrences(activity, startCal, targetCal)
                    if (countBefore >= activity.recurrenceEndCount) return false
                }
                true
            }
            RecurrenceMode.MONTHLY -> {
                val monthsDiff = (targetCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)) * 12 +
                        (targetCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH))
                if (monthsDiff < 0 || monthsDiff % interval != 0) return false

                val matchesDay = when (activity.monthlyRepeatType) {
                    MonthlyRepeatType.SAME_DAY_OF_MONTH -> {
                        targetCal.get(Calendar.DAY_OF_MONTH) == startCal.get(Calendar.DAY_OF_MONTH)
                    }
                    MonthlyRepeatType.DAY_OF_WEEK_OF_MONTH -> {
                        targetCal.get(Calendar.DAY_OF_WEEK) == startCal.get(Calendar.DAY_OF_WEEK) &&
                                targetCal.get(Calendar.DAY_OF_WEEK_IN_MONTH) == startCal.get(Calendar.DAY_OF_WEEK_IN_MONTH)
                    }
                    MonthlyRepeatType.SPECIFIC_DATES -> {
                        val targetDom = targetCal.get(Calendar.DAY_OF_MONTH)
                        if (activity.monthlySelectedDays.isNotEmpty()) {
                            targetDom in activity.monthlySelectedDays
                        } else {
                            targetDom == startCal.get(Calendar.DAY_OF_MONTH)
                        }
                    }
                }
                if (!matchesDay) return false

                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val occurrenceIndex = monthsDiff / interval
                    if (occurrenceIndex >= activity.recurrenceEndCount) return false
                }
                true
            }
            RecurrenceMode.YEARLY -> {
                val yearsDiff = targetCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
                if (yearsDiff < 0 || yearsDiff % interval != 0) return false

                val matchesYearly = when (activity.yearlyRepeatType) {
                    YearlyRepeatType.SAME_DATE -> {
                        targetCal.get(Calendar.MONTH) == startCal.get(Calendar.MONTH) &&
                                targetCal.get(Calendar.DAY_OF_MONTH) == startCal.get(Calendar.DAY_OF_MONTH)
                    }
                    YearlyRepeatType.DAY_OF_WEEK_OF_MONTH -> {
                        targetCal.get(Calendar.MONTH) == startCal.get(Calendar.MONTH) &&
                                targetCal.get(Calendar.DAY_OF_WEEK) == startCal.get(Calendar.DAY_OF_WEEK) &&
                                targetCal.get(Calendar.DAY_OF_WEEK_IN_MONTH) == startCal.get(Calendar.DAY_OF_WEEK_IN_MONTH)
                    }
                    YearlyRepeatType.SPECIFIC_MONTHS -> {
                        val targetMonth = targetCal.get(Calendar.MONTH)
                        val matchesMonth = if (activity.yearlySelectedMonths.isNotEmpty()) {
                            targetMonth in activity.yearlySelectedMonths
                        } else {
                            targetMonth == startCal.get(Calendar.MONTH)
                        }
                        matchesMonth && targetCal.get(Calendar.DAY_OF_MONTH) == startCal.get(Calendar.DAY_OF_MONTH)
                    }
                }
                if (!matchesYearly) return false

                if (activity.recurrenceEndType == RecurrenceEndType.COUNT) {
                    val occurrenceIndex = yearsDiff / interval
                    if (occurrenceIndex >= activity.recurrenceEndCount) return false
                }
                true
            }
        }
    }

    fun createOccurrenceForDay(activity: ActivityItem, targetDayMillis: Long): ActivityItem {
        val originalCal = Calendar.getInstance().apply { timeInMillis = activity.startTimeMillis }
        val targetDayCal = Calendar.getInstance().apply {
            timeInMillis = targetDayMillis
            set(Calendar.HOUR_OF_DAY, originalCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, originalCal.get(Calendar.MINUTE))
            set(Calendar.SECOND, originalCal.get(Calendar.SECOND))
            set(Calendar.MILLISECOND, originalCal.get(Calendar.MILLISECOND))
        }

        val occurrenceStart = targetDayCal.timeInMillis
        val duration = (activity.endTimeMillis - activity.startTimeMillis).coerceAtLeast(0L)
        val occurrenceEnd = occurrenceStart + duration

        return activity.copy(
            startTimeMillis = occurrenceStart,
            endTimeMillis = occurrenceEnd
        )
    }

    fun expandOccurrencesForRange(
        activity: ActivityItem,
        rangeStartMillis: Long,
        rangeEndMillis: Long
    ): List<ActivityItem> {
        if (activity.recurrenceMode == RecurrenceMode.NONE) {
            return if (activity.startTimeMillis <= rangeEndMillis && activity.endTimeMillis >= rangeStartMillis) {
                listOf(activity)
            } else {
                emptyList()
            }
        }

        val results = mutableListOf<ActivityItem>()
        val startDay = getStartOfDayMillis(rangeStartMillis.coerceAtLeast(activity.startTimeMillis))
        val endDay = getStartOfDayMillis(rangeEndMillis)

        val cal = Calendar.getInstance().apply { timeInMillis = startDay }
        while (cal.timeInMillis <= endDay) {
            val currentDayMillis = cal.timeInMillis
            if (occursOnDay(activity, currentDayMillis)) {
                results.add(createOccurrenceForDay(activity, currentDayMillis))
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return results
    }

    fun getNextOccurrenceStart(activity: ActivityItem, afterMillis: Long): Long? {
        val baseOccurrence = activity.startTimeMillis
        if (activity.recurrenceMode == RecurrenceMode.NONE) {
            return if (baseOccurrence > afterMillis) baseOccurrence else null
        }

        val searchStart = getStartOfDayMillis(afterMillis.coerceAtLeast(activity.startTimeMillis))
        val maxDaysToScan = 730 // Scan up to 2 years ahead
        val cal = Calendar.getInstance().apply { timeInMillis = searchStart }

        for (i in 0 until maxDaysToScan) {
            val dayMillis = cal.timeInMillis
            if (occursOnDay(activity, dayMillis)) {
                val occurrence = createOccurrenceForDay(activity, dayMillis)
                if (occurrence.startTimeMillis > afterMillis) {
                    return occurrence.startTimeMillis
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return null
    }

    private fun countWeekdaysBetween(startMillis: Long, targetMillis: Long): Int {
        if (targetMillis <= startMillis) return 0
        var count = 0
        val cal = Calendar.getInstance().apply { timeInMillis = startMillis }
        val targetCal = Calendar.getInstance().apply { timeInMillis = targetMillis }
        while (cal.get(Calendar.YEAR) < targetCal.get(Calendar.YEAR) ||
            (cal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) < targetCal.get(Calendar.DAY_OF_YEAR))
        ) {
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) {
                count++
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return count
    }

    private fun countWeeklyOccurrences(activity: ActivityItem, startCal: Calendar, targetCal: Calendar): Int {
        var count = 0
        val cal = (startCal.clone() as Calendar)
        val interval = activity.recurrenceInterval.coerceAtLeast(1)
        val targetStartOfDay = getStartOfDayMillis(targetCal.timeInMillis)

        while (getStartOfDayMillis(cal.timeInMillis) < targetStartOfDay) {
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            val matchesDow = if (activity.recurrenceDaysOfWeek.isNotEmpty()) {
                dow in activity.recurrenceDaysOfWeek
            } else {
                dow == startCal.get(Calendar.DAY_OF_WEEK)
            }
            if (matchesDow) {
                count++
            }

            if (dow == Calendar.SATURDAY) {
                if (interval > 1) {
                    cal.add(Calendar.WEEK_OF_YEAR, interval - 1)
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return count
    }
}
