package com.shejan.acontinue.core.notification

import com.shejan.acontinue.data.local.dao.ActivityDao
import com.shejan.acontinue.data.local.entity.ActivityEntity
import com.shejan.acontinue.data.repository.ActivityRepositoryImpl
import com.shejan.acontinue.domain.model.ActivityItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmSchedulerTest {

    @Test
    fun `getRequestCode produces collision-free distinct codes for various alert offsets on the same activity`() {
        val activityId = 42L
        val offsets = listOf(0, 5, 10, 15, 30, 45, 60, 80, 440, 1440, 10080)

        val requestCodes = offsets.map { offset ->
            AlarmScheduler.getRequestCode(activityId, offset)
        }

        // Must all be unique
        val uniqueCodes = requestCodes.toSet()
        assertEquals(
            "Expected each alert offset to generate a distinct request code",
            offsets.size,
            uniqueCodes.size
        )

        // Specifically test the critical collision edge cases that broke in the previous formula:
        // 0 vs 1000, 440 vs 1440, 80 vs 10080
        val code0 = AlarmScheduler.getRequestCode(activityId, 0)
        val code1000 = AlarmScheduler.getRequestCode(activityId, 1000)
        val code440 = AlarmScheduler.getRequestCode(activityId, 440)
        val code1440 = AlarmScheduler.getRequestCode(activityId, 1440)
        val code80 = AlarmScheduler.getRequestCode(activityId, 80)
        val code10080 = AlarmScheduler.getRequestCode(activityId, 10080)

        assertTrue(code0 != code1000)
        assertTrue(code440 != code1440)
        assertTrue(code80 != code10080)
    }

    @Test
    fun `getRequestCode values are strictly non-negative and within valid integer bounds`() {
        val testCases = listOf(
            0L to 0,
            1L to 15,
            1999L to 999999,
            500000L to 1440,
            Long.MAX_VALUE to 10080
        )

        for ((id, minutes) in testCases) {
            val code = AlarmScheduler.getRequestCode(id, minutes)
            assertTrue("Request code $code for id=$id, min=$minutes must be >= 0", code >= 0)
            assertTrue("Request code $code for id=$id, min=$minutes must be <= Int.MAX_VALUE", code <= Int.MAX_VALUE)
        }
    }

    @Test
    fun `ActivityRepositoryImpl automatically cancels alarms when deleting an activity`() = runBlocking {
        var cancelledActivityId: Long? = null
        var deletedDaoId: Long? = null

        val fakeScheduler = object : ActivityScheduler {
            override fun schedule(activity: ActivityItem) {}
            override fun cancel(activityId: Long) {
                cancelledActivityId = activityId
            }
        }

        val fakeDao = object : ActivityDao {
            override fun getActivitiesForDateRange(startMillis: Long, endMillis: Long): Flow<List<ActivityEntity>> = flowOf(emptyList())
            override fun getActivityById(id: Long): Flow<ActivityEntity?> = flowOf(null)
            override suspend fun getActivityByIdSync(id: Long): ActivityEntity? = null
            override fun getAllActivities(): Flow<List<ActivityEntity>> = flowOf(emptyList())
            override suspend fun insertActivity(activity: ActivityEntity): Long = 1L
            override suspend fun updateActivity(activity: ActivityEntity) {}
            override suspend fun deleteActivity(id: Long) {
                deletedDaoId = id
            }
            override suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean) {}
            override suspend fun updateSkippedStatus(id: Long, isSkipped: Boolean) {}
            override suspend fun getUpcomingIncompleteActivities(now: Long): List<ActivityEntity> = emptyList()
            override fun searchActivities(query: String): Flow<List<ActivityEntity>> = flowOf(emptyList())
            override suspend fun updateReminderCategoryDetails(categoryName: String, colorHex: Long, icon: String) {}
            override suspend fun updateActivityReminderCategory(activityId: Long, newCategory: String, newColorHex: Long, newIcon: String) {}
            override suspend fun renameReminderCategoryAcrossActivities(oldName: String, newName: String, newColorHex: Long, newIcon: String) {}
            override suspend fun resetActivitiesToDefaultCategory(deletedName: String, fallbackName: String, fallbackColorHex: Long, fallbackIcon: String) {}
        }

        val repository = ActivityRepositoryImpl(
            activityDao = fakeDao,
            activityScheduler = fakeScheduler
        )

        val targetId = 777L
        repository.deleteActivity(targetId)

        assertEquals("ActivityScheduler must receive cancel for the deleted activity id", targetId, cancelledActivityId)
        assertEquals("ActivityDao must receive delete for the target id", targetId, deletedDaoId)
    }
}

