package com.shejan.acontinue.presentation.calendar

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var viewModel: CalendarViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        viewModel = CalendarViewModel(activityRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test calendar queries activities on selected date`() = runTest(testDispatcher) {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 16, 10, 0, 0)
        }
        val startTime = cal.timeInMillis
        val endTime = startTime + 3600000L

        activityRepository.insertActivity(
            ActivityItem(
                id = 1L,
                title = "Club Meeting",
                category = ActivityCategory.MEETING,
                startTimeMillis = startTime,
                endTimeMillis = endTime
            )
        )

        viewModel.selectDate(startTime)
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.activities.size)
            assertEquals("Club Meeting", state.activities.first().title)
        }
    }
}
