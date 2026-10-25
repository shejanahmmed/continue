package com.shejan.acontinue.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.shejan.acontinue.core.notification.AlarmScheduler
import com.shejan.acontinue.core.util.AudioRecordingManager
import com.shejan.acontinue.data.local.ContinueDatabase
import com.shejan.acontinue.data.repository.ActivityRepositoryImpl
import com.shejan.acontinue.data.repository.LinkRepositoryImpl
import com.shejan.acontinue.data.repository.NoteRepositoryImpl
import com.shejan.acontinue.data.repository.SettingsRepositoryImpl
import com.shejan.acontinue.data.repository.TaskRepositoryImpl
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.LinkRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import com.shejan.acontinue.domain.repository.SettingsRepository
import com.shejan.acontinue.domain.repository.TaskRepository
import com.shejan.acontinue.domain.usecase.GetActivityWorkspaceUseCase
import com.shejan.acontinue.domain.usecase.GetTodayTimelineUseCase
import com.shejan.acontinue.domain.usecase.ToggleTaskStatusUseCase
import com.shejan.acontinue.presentation.calendar.CalendarViewModel
import com.shejan.acontinue.presentation.create.CreateEntryViewModel
import com.shejan.acontinue.presentation.diary.DiaryViewModel
import com.shejan.acontinue.presentation.home.HomeViewModel
import com.shejan.acontinue.presentation.settings.SettingsViewModel
import com.shejan.acontinue.presentation.workspace.ActivityWorkspaceViewModel

class AppContainer(context: Context) {

    private val database: ContinueDatabase = ContinueDatabase.getInstance(context)

    val activityRepository: ActivityRepository = ActivityRepositoryImpl(database.activityDao())
    val taskRepository: TaskRepository = TaskRepositoryImpl(database.taskDao())
    val noteRepository: NoteRepository = NoteRepositoryImpl(database.noteDao())
    val linkRepository: LinkRepository = LinkRepositoryImpl(database.linkDao())
    val settingsRepository: SettingsRepository = SettingsRepositoryImpl(context)

    val alarmScheduler: AlarmScheduler = AlarmScheduler(context)
    val audioRecordingManager: AudioRecordingManager by lazy {
        AudioRecordingManager(
            context = context,
            activityRepository = activityRepository,
            noteRepository = noteRepository
        )
    }

    val getTodayTimelineUseCase = GetTodayTimelineUseCase(activityRepository)
    val getActivityWorkspaceUseCase = GetActivityWorkspaceUseCase(
        activityRepository,
        taskRepository,
        noteRepository,
        linkRepository
    )
    val toggleTaskStatusUseCase = ToggleTaskStatusUseCase(taskRepository)

    fun provideHomeViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(
                    getTodayTimelineUseCase,
                    taskRepository,
                    activityRepository
                ) as T
            }
        }

    fun provideCalendarViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CalendarViewModel(
                    activityRepository = activityRepository
                ) as T
            }
        }

    fun provideDiaryViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DiaryViewModel(activityRepository, noteRepository) as T
            }
        }

    fun provideCreateEntryViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateEntryViewModel(
                    activityRepository,
                    noteRepository,
                    alarmScheduler,
                    settingsRepository,
                    linkRepository
                ) as T
            }
        }

    fun provideWorkspaceViewModelFactory(activityId: Long): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ActivityWorkspaceViewModel(
                    activityId,
                    getActivityWorkspaceUseCase,
                    activityRepository,
                    taskRepository,
                    noteRepository,
                    linkRepository,
                    settingsRepository
                ) as T
            }
        }

    fun provideSettingsViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsRepository) as T
            }
        }
}
