package com.shejan.acontinue.domain.usecase

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTodayTimelineUseCaseTest {

    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var useCase: GetTodayTimelineUseCase

    @Before
    fun setUp() {
        activityRepository = FakeActivityRepository()
        useCase = GetTodayTimelineUseCase(activityRepository)
    }

    @Test
    fun `invoke excludes EntryType NOTE and includes ACTIVITY and REMINDER`() = runTest {
        val now = System.currentTimeMillis()
        val note = ActivityItem(
            id = 1L,
            title = "My Freeform Note",
            entryType = EntryType.NOTE,
            startTimeMillis = now,
            endTimeMillis = now
        )
        val event = ActivityItem(
            id = 2L,
            title = "Project Standup",
            entryType = EntryType.ACTIVITY,
            startTimeMillis = now + 1000L,
            endTimeMillis = now + 3600000L
        )
        val reminder = ActivityItem(
            id = 3L,
            title = "Drink Water",
            entryType = EntryType.REMINDER,
            startTimeMillis = now + 2000L,
            endTimeMillis = now + 2000L
        )

        activityRepository.insertActivity(note)
        activityRepository.insertActivity(event)
        activityRepository.insertActivity(reminder)

        useCase().test {
            val timeline = awaitItem()
            // Should contain exactly 2 items: event and reminder (note excluded)
            assertEquals(2, timeline.activities.size)
            assertTrue(timeline.activities.none { it.entryType == EntryType.NOTE })
            assertTrue(timeline.activities.any { it.id == 2L && it.entryType == EntryType.ACTIVITY })
            assertTrue(timeline.activities.any { it.id == 3L && it.entryType == EntryType.REMINDER })
        }
    }
}
