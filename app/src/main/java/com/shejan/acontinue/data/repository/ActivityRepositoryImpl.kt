package com.shejan.acontinue.data.repository

import com.shejan.acontinue.data.local.dao.ActivityDao
import com.shejan.acontinue.data.mapper.toDomain
import com.shejan.acontinue.data.mapper.toEntity
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.RecurrenceMode
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.util.RecurrenceEvaluator
import com.shejan.acontinue.core.notification.ActivityScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ActivityRepositoryImpl(
    private val activityDao: ActivityDao,
    private val activityScheduler: ActivityScheduler? = null
) : ActivityRepository {

    override fun getActivitiesForDateRange(startMillis: Long, endMillis: Long): Flow<List<ActivityItem>> {
        return activityDao.getActivitiesForDateRange(startMillis, endMillis).map { list ->
            list.flatMap { entity ->
                val domain = entity.toDomain()
                RecurrenceEvaluator.expandOccurrencesForRange(domain, startMillis, endMillis)
            }.sortedBy { it.startTimeMillis }
        }
    }

    override fun getActivityById(id: Long): Flow<ActivityItem?> {
        return activityDao.getActivityById(id).map { it?.toDomain() }
    }

    override fun getAllActivities(): Flow<List<ActivityItem>> {
        return activityDao.getAllActivities().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertActivity(activity: ActivityItem): Long {
        return activityDao.insertActivity(activity.toEntity())
    }

    override suspend fun updateActivity(activity: ActivityItem) {
        activityDao.updateActivity(activity.toEntity())
    }

    override suspend fun deleteActivity(id: Long) {
        activityScheduler?.cancel(id)
        activityDao.moveToTrash(id, System.currentTimeMillis())
    }

    override suspend fun restoreActivity(id: Long) {
        activityDao.restoreFromTrash(id)
        val restored = activityDao.getActivityByIdSync(id)?.toDomain()
        if (restored != null && !restored.isCompleted && (restored.startTimeMillis > System.currentTimeMillis() || restored.recurrenceMode != RecurrenceMode.NONE)) {
            activityScheduler?.schedule(restored)
        }
    }

    override suspend fun permanentlyDeleteActivity(id: Long) {
        activityScheduler?.cancel(id)
        activityDao.permanentlyDeleteActivity(id)
    }

    override suspend fun emptyTrash() {
        activityDao.emptyTrashActivities()
    }

    override fun getDeletedActivities(): Flow<List<ActivityItem>> {
        return activityDao.getDeletedActivities().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getArchivedActivities(): Flow<List<ActivityItem>> {
        return activityDao.getArchivedActivities().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun setActivityArchived(id: Long, archived: Boolean) {
        activityDao.setActivityArchived(
            id = id,
            isArchived = archived,
            archivedAtMillis = if (archived) System.currentTimeMillis() else 0L
        )
    }

    override suspend fun toggleActivityCompletion(id: Long, isCompleted: Boolean, occurrenceDateMillis: Long) {
        val existing = activityDao.getActivityByIdSync(id) ?: return
        if (existing.entryType != EntryType.REMINDER.name) return

        if (existing.recurrenceMode != RecurrenceMode.NONE.name) {
            val occurrenceDayStart = RecurrenceEvaluator.getStartOfDayMillis(occurrenceDateMillis)
            val currentCompletedDates = if (existing.completedDatesCsv.isNotBlank()) {
                existing.completedDatesCsv.split(",").mapNotNull { it.trim().toLongOrNull() }.toMutableSet()
            } else {
                mutableSetOf()
            }

            if (isCompleted) {
                currentCompletedDates.add(occurrenceDayStart)
            } else {
                currentCompletedDates.remove(occurrenceDayStart)
            }

            val newCsv = currentCompletedDates.sorted().joinToString(",")
            activityDao.updateCompletedDatesCsv(id, newCsv)
        } else {
            activityDao.updateCompletionStatus(id, isCompleted)
        }
    }

    override suspend fun toggleActivitySkipped(id: Long, isSkipped: Boolean) {
        val existing = activityDao.getActivityByIdSync(id)
        if (existing?.entryType == EntryType.REMINDER.name) {
            activityDao.updateSkippedStatus(id, isSkipped)
        }
    }

    override fun searchActivities(query: String): Flow<List<ActivityItem>> {
        return activityDao.searchActivities(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun updateReminderCategoryDetails(categoryName: String, colorHex: Long, icon: String) {
        activityDao.updateReminderCategoryDetails(categoryName, colorHex, icon)
    }

    override suspend fun updateActivityReminderCategory(activityId: Long, newCategory: String, newColorHex: Long, newIcon: String) {
        activityDao.updateActivityReminderCategory(activityId, newCategory, newColorHex, newIcon)
    }

    override suspend fun renameReminderCategoryAcrossActivities(oldName: String, newName: String, newColorHex: Long, newIcon: String) {
        activityDao.renameReminderCategoryAcrossActivities(oldName, newName, newColorHex, newIcon)
    }

    override suspend fun resetActivitiesToDefaultCategory(deletedName: String, fallbackName: String, fallbackColorHex: Long, fallbackIcon: String) {
        activityDao.resetActivitiesToDefaultCategory(deletedName, fallbackName, fallbackColorHex, fallbackIcon)
    }
}
