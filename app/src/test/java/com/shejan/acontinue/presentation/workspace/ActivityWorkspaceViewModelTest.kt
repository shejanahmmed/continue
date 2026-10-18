package com.shejan.acontinue.presentation.workspace

import app.cash.turbine.test
import com.shejan.acontinue.FakeActivityRepository
import com.shejan.acontinue.FakeLinkRepository
import com.shejan.acontinue.FakeNoteRepository
import com.shejan.acontinue.FakeTaskRepository
import com.shejan.acontinue.domain.model.ActivityCategory
import com.shejan.acontinue.domain.model.ActivityItem
import com.shejan.acontinue.domain.usecase.GetActivityWorkspaceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityWorkspaceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var activityRepository: FakeActivityRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var noteRepository: FakeNoteRepository
    private lateinit var linkRepository: FakeLinkRepository
    private lateinit var getActivityWorkspaceUseCase: GetActivityWorkspaceUseCase
    private lateinit var viewModel: ActivityWorkspaceViewModel

    private val testActivityId = 42L

    @Before
    fun setUp() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        activityRepository = FakeActivityRepository()
        taskRepository = FakeTaskRepository()
        noteRepository = FakeNoteRepository()
        linkRepository = FakeLinkRepository()

        activityRepository.insertActivity(
            ActivityItem(
                id = testActivityId,
                title = "Laboratory Work",
                category = ActivityCategory.LAB,
                startTimeMillis = 1000L,
                endTimeMillis = 2000L
            )
        )

        getActivityWorkspaceUseCase = GetActivityWorkspaceUseCase(
            activityRepository,
            taskRepository,
            noteRepository,
            linkRepository
        )

        viewModel = ActivityWorkspaceViewModel(
            testActivityId,
            getActivityWorkspaceUseCase,
            activityRepository,
            taskRepository,
            noteRepository,
            linkRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test workspace loads aggregate successfully`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.aggregate)
            assertEquals("Laboratory Work", state.aggregate?.activity?.title)
            assertEquals(0, state.aggregate?.notes?.size)
            assertEquals(0, state.aggregate?.tasks?.size)
        }
    }

    @Test
    fun `test adding note, task, and link reflects in workspace`() = runTest(testDispatcher) {
        testScheduler.advanceUntilIdle()

        viewModel.addNote("Lab Results", "Observed reaction at 25C")
        viewModel.addTask("Submit Lab Report")
        viewModel.addLink("https://lab-manual.edu", "Lab Manual")

        testScheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.aggregate?.notes?.size)
            assertEquals("Lab Results", state.aggregate?.notes?.first()?.title)
            assertEquals(1, state.aggregate?.tasks?.size)
            assertEquals("Submit Lab Report", state.aggregate?.tasks?.first()?.title)
            assertEquals(1, state.aggregate?.links?.size)
            assertEquals("https://lab-manual.edu", state.aggregate?.links?.first()?.url)
        }
    }

    @Test
    fun `test toggle task completion updates status in workspace`() = runTest(testDispatcher) {
        viewModel.addTask("Complete Analysis")
        testScheduler.advanceUntilIdle()

        val taskId = taskRepository.tasksFlow.value.first().id
        viewModel.toggleTask(taskId, true)
        testScheduler.advanceUntilIdle()

        val updatedTask = taskRepository.tasksFlow.value.first()
        assertEquals(true, updatedTask.isCompleted)
    }

    @Test
    fun `test toggle activity completion on event is ignored and remains false`() = runTest(testDispatcher) {
        viewModel.toggleActivityCompletion(true)
        testScheduler.advanceUntilIdle()

        val activity = activityRepository.activitiesFlow.value.first { it.id == testActivityId }
        assertEquals(false, activity.isCompleted)
    }

    @Test
    fun `test toggle activity completion on reminder updates status`() = runTest(testDispatcher) {
        val reminder = ActivityItem(
            id = 99L,
            title = "Reminder Task",
            entryType = com.shejan.acontinue.domain.model.EntryType.REMINDER,
            startTimeMillis = 1000L,
            endTimeMillis = 1000L,
            isCompleted = false
        )
        activityRepository.insertActivity(reminder)
        testScheduler.advanceUntilIdle()

        activityRepository.toggleActivityCompletion(99L, true)
        testScheduler.advanceUntilIdle()

        val updated = activityRepository.activitiesFlow.value.first { it.id == 99L }
        assertEquals(true, updated.isCompleted)
    }

    @Test
    fun `test workspace gracefully exits if loaded activity is a reminder`() = runTest(testDispatcher) {
        val reminderId = 101L
        activityRepository.insertActivity(
            ActivityItem(
                id = reminderId,
                title = "Doctor Appointment Alert",
                entryType = com.shejan.acontinue.domain.model.EntryType.REMINDER,
                startTimeMillis = 5000L,
                endTimeMillis = 5000L
            )
        )

        val reminderViewModel = ActivityWorkspaceViewModel(
            reminderId,
            getActivityWorkspaceUseCase,
            activityRepository,
            taskRepository,
            noteRepository,
            linkRepository
        )

        reminderViewModel.events.test {
            testScheduler.advanceUntilIdle()
            val event = awaitItem()
            assertEquals(WorkspaceEvent.ActivityDeleted, event)
        }
    }
}
