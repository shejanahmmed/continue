package com.shejan.acontinue.presentation.year

import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.model.EntryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class YearViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var viewModel: YearViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        viewModel = YearViewModel(activityRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial uiState has 12 months with correct names and current year`() = runTest(testDispatcher) {
        val state = viewModel.uiState.value
        val expectedYear = Calendar.getInstance().get(Calendar.YEAR)
        val expectedMonth = Calendar.getInstance().get(Calendar.MONTH)

        assertEquals(expectedYear, state.year)
        assertEquals(12, state.months.size)

        val expectedNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sept", "Oct", "Nov", "Dec"
        )
        state.months.forEachIndexed { index, monthData ->
            assertEquals(index, monthData.monthIndex)
            assertEquals(expectedNames[index], monthData.monthName)
            if (index == expectedMonth) {
                assertTrue("Expected month $index to be marked as current month", monthData.isCurrentMonth)
            } else {
                assertFalse("Expected month $index not to be current month", monthData.isCurrentMonth)
            }
        }
    }

    @Test
    fun `test weekday leadingEmptySlots calculation for known date`() = runTest(testDispatcher) {
        // Set to 2026: Jan 1, 2026 is Thursday
        // In our Saturday-first layout:
        // Sat=0, Sun=1, Mon=2, Tue=3, Wed=4, Thu=5, Fri=6
        // Thursday must be index 5
        viewModel.setYear(2026)
        val jan = viewModel.uiState.value.months[0]
        assertEquals(5, jan.leadingEmptySlots)
        assertEquals(31, jan.days.size)
    }

    @Test
    fun `test leap year days in February`() = runTest(testDispatcher) {
        // 2024 is a leap year (29 days in Feb)
        viewModel.setYear(2024)
        val feb2024 = viewModel.uiState.value.months[1]
        assertEquals(29, feb2024.days.size)

        // 2025 is a normal year (28 days in Feb)
        viewModel.setYear(2025)
        val feb2025 = viewModel.uiState.value.months[1]
        assertEquals(28, feb2025.days.size)
    }

    @Test
    fun `test activities correctly map to corresponding day item`() = runTest(testDispatcher) {
        // Schedule an activity on March 15, 2026
        val targetCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 15)
            set(Calendar.HOUR_OF_DAY, 10)
        }
        val activity = ActivityItem(
            id = 101L,
            title = "Special Project Review",
            category = ActivityCategory.PROJECT,
            startTimeMillis = targetCal.timeInMillis,
            endTimeMillis = targetCal.timeInMillis + 3600000L,
            entryType = EntryType.ACTIVITY
        )
        activityRepository.insertActivity(activity)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setYear(2026)
        val march = viewModel.uiState.value.months[2]
        val day15 = march.days.first { it.dayOfMonth == 15 }

        assertTrue(day15.hasActivity)
        assertEquals(ActivityCategory.PROJECT.colorHex, day15.categoryColorHex)
    }

    @Test
    fun `test onSearchQueryChange updates uiState`() = runTest(testDispatcher) {
        viewModel.onSearchQueryChange("August")
        assertEquals("August", viewModel.uiState.value.searchQuery)
    }
}
