package com.shejan.acontinue.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.shejan.acontinue.core.designsystem.components.GlobalRecordingDynamicIsland
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.shejan.acontinue.core.designsystem.components.UtilitarianScaffold
import com.shejan.acontinue.core.di.AppContainer
import com.shejan.acontinue.presentation.calendar.CalendarScreen
import com.shejan.acontinue.presentation.calendar.CalendarViewModel
import com.shejan.acontinue.presentation.create.CreateEntryScreen
import com.shejan.acontinue.presentation.create.CreateEntryViewModel
import com.shejan.acontinue.presentation.diary.DiaryScreen
import com.shejan.acontinue.presentation.diary.DiaryViewModel
import com.shejan.acontinue.presentation.home.HomeScreen
import com.shejan.acontinue.presentation.home.HomeViewModel
import com.shejan.acontinue.presentation.settings.SettingsScreen
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.domain.model.UserSettings
import com.shejan.acontinue.presentation.create.CreateNoteScreen
import com.shejan.acontinue.presentation.welcome.WelcomeScreen
import com.shejan.acontinue.presentation.workspace.ActivityWorkspaceScreen
import com.shejan.acontinue.presentation.workspace.ActivityWorkspaceViewModel

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Home : Screen("home")
    data object Diary : Screen("diary")
    data object Calendar : Screen("calendar")
    data object Settings : Screen("settings")
    data object Create : Screen("create?initialDate={initialDate}&editActivityId={editActivityId}&entryType={entryType}") {
        fun createRoute(
            initialDate: Long? = null,
            editActivityId: Long? = null,
            entryType: EntryType? = null
        ): String {
            val params = mutableListOf<String>()
            if (initialDate != null) params.add("initialDate=$initialDate")
            if (editActivityId != null) params.add("editActivityId=$editActivityId")
            if (entryType != null) params.add("entryType=${entryType.name}")
            return if (params.isNotEmpty()) "create?${params.joinToString("&")}" else "create"
        }
    }
    data object NoteEditor : Screen("note_editor?editActivityId={editActivityId}") {
        fun createRoute(editActivityId: Long? = null): String {
            return if (editActivityId != null) "note_editor?editActivityId=$editActivityId" else "note_editor"
        }
    }
    data object Workspace : Screen("workspace/{activityId}") {
        fun createRoute(activityId: Long): String = "workspace/$activityId"
    }
}

@Composable
fun ContinueNavHost(
    navController: NavHostController,
    container: AppContainer,
    userSettings: UserSettings = UserSettings(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val recordingState by container.audioRecordingManager.state.collectAsState()
    val startDestination = if (userSettings.isOnboardingCompleted) Screen.Home.route else Screen.Welcome.route
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Home.route,
        Screen.Diary.route,
        Screen.Calendar.route,
        Screen.Settings.route
    )

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry == null) {
            navController.navigate(startDestination) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    UtilitarianScaffold(
        currentRoute = currentRoute,
        onNavigate = { targetRoute ->
            if (targetRoute != currentRoute) {
                if (targetRoute == Screen.Home.route) {
                    val popped = navController.popBackStack(Screen.Home.route, inclusive = false)
                    if (!popped) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                } else {
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        },
        onCreateClick = {
            if (currentRoute == Screen.Diary.route) {
                navController.navigate(Screen.NoteEditor.createRoute())
            } else {
                navController.navigate(Screen.Create.route)
            }
        },
        onQuickCreate = { entryType ->
            if (entryType == EntryType.NOTE) {
                navController.navigate(Screen.NoteEditor.createRoute())
            } else {
                navController.navigate(Screen.Create.createRoute(entryType = entryType))
            }
        },
        showBottomBar = isTopLevelRoute
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = modifier.fillMaxSize()
            ) {
            // Starter / Welcome Destination
            composable(Screen.Welcome.route) {
                val scope = rememberCoroutineScope()
                WelcomeScreen(
                    onGetStarted = {
                        scope.launch {
                            container.settingsRepository.setOnboardingCompleted(true)
                        }
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // Home Destination
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = container.provideHomeViewModelFactory()
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onActivityClick = { activityId ->
                        navController.navigate(Screen.Workspace.createRoute(activityId))
                    },
                    onNavigateToCalendar = {
                        navController.navigate(Screen.Calendar.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.statusBarsPadding()
                )
            }

            // Diary Destination
            composable(Screen.Diary.route) {
                val diaryViewModel: DiaryViewModel = viewModel(
                    factory = container.provideDiaryViewModelFactory()
                )
                DiaryScreen(
                    viewModel = diaryViewModel,
                    onActivityClick = { activityId ->
                        navController.navigate(Screen.Workspace.createRoute(activityId))
                    },
                    onNoteClick = { activityId ->
                        navController.navigate(Screen.NoteEditor.createRoute(activityId))
                    },
                    modifier = Modifier.statusBarsPadding()
                )
            }

            // Calendar Destination
            composable(Screen.Calendar.route) {
                val calendarViewModel: CalendarViewModel = viewModel(
                    factory = container.provideCalendarViewModelFactory()
                )
                CalendarScreen(
                    viewModel = calendarViewModel,
                    onActivityClick = { activityId ->
                        navController.navigate(Screen.Workspace.createRoute(activityId))
                    },
                    onCreateClick = { dateMillis ->
                        navController.navigate(Screen.Create.createRoute(dateMillis))
                    },
                    modifier = Modifier.statusBarsPadding()
                )
            }

            // Settings Destination
            composable(Screen.Settings.route) {
                val settingsViewModel: com.shejan.acontinue.presentation.settings.SettingsViewModel = viewModel(
                    factory = container.provideSettingsViewModelFactory()
                )
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToWelcome = {
                        navController.navigate(Screen.Welcome.route)
                    },
                    modifier = Modifier.statusBarsPadding()
                )
            }

            // Create Screen
            composable(
                route = Screen.Create.route,
                arguments = listOf(
                    navArgument("initialDate") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("editActivityId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("entryType") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val initialDate = backStackEntry.arguments?.getLong("initialDate") ?: -1L
                val editActivityId = backStackEntry.arguments?.getLong("editActivityId") ?: -1L
                val entryTypeStr = backStackEntry.arguments?.getString("entryType")
                val createViewModel: CreateEntryViewModel = viewModel(
                    factory = container.provideCreateEntryViewModelFactory()
                )
                if (editActivityId > 0L) {
                    createViewModel.loadActivityForEditing(editActivityId)
                } else if (initialDate > 0L) {
                    createViewModel.setInitialDate(initialDate)
                }
                if (entryTypeStr != null) {
                    runCatching { EntryType.valueOf(entryTypeStr) }.getOrNull()?.let {
                        createViewModel.updateEntryType(it)
                    }
                }
                CreateEntryScreen(
                    viewModel = createViewModel,
                    allowNotes = initialDate <= 0L,
                    onDismiss = { navController.safePopBackStack("create") }
                )
            }

            // Note Editor Destination (Dedicated Note Creation & Editing)
            composable(
                route = Screen.NoteEditor.route,
                arguments = listOf(
                    navArgument("editActivityId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val editActivityId = backStackEntry.arguments?.getLong("editActivityId") ?: -1L
                val noteViewModel: CreateEntryViewModel = viewModel(
                    key = if (editActivityId > 0L) "note_editor_$editActivityId" else "note_editor_new",
                    factory = container.provideCreateEntryViewModelFactory()
                )
                if (editActivityId > 0L) {
                    noteViewModel.loadActivityForEditing(editActivityId)
                } else {
                    noteViewModel.updateEntryType(EntryType.NOTE)
                }
                val state by noteViewModel.state.collectAsState()
                val initialLabels = remember(state.reminderCategory) {
                    if (state.reminderCategory.isBlank() || state.reminderCategory == "Notes") {
                        emptyList()
                    } else {
                        state.reminderCategory.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    }
                }
                CreateNoteScreen(
                    initialTitle = state.title,
                    initialContent = state.notes,
                    initialColorHex = if (state.reminderCategoryColorHex != 0L && state.reminderCategoryColorHex != 0xFF121212L) state.reminderCategoryColorHex else 0xFF000000L,
                    initialLabels = initialLabels,
                    editingActivityId = editActivityId,
                    onEnsureDraftNoteId = {
                        noteViewModel.ensureDraftNoteId(
                            title = "",
                            content = "",
                            colorHex = if (state.reminderCategoryColorHex != 0L && state.reminderCategoryColorHex != 0xFF121212L) state.reminderCategoryColorHex else 0xFF000000L,
                            labels = initialLabels
                        )
                    },
                    audioRecordingManager = container.audioRecordingManager,
                    onSaveNote = { title, content, colorHex, labels ->
                        noteViewModel.saveNote(title, content, colorHex, labels)
                        navController.safePopBackStack("note_editor")
                    },
                    onDiscard = {
                        navController.safePopBackStack("note_editor")
                    },
                    onDeleteNote = {
                        container.audioRecordingManager.cancelRecording()
                        noteViewModel.deleteCurrentNote()
                        navController.safePopBackStack("note_editor")
                    },
                    onMakeCopy = { title, content, colorHex, labels ->
                        noteViewModel.makeCopyOfNote(title, content, colorHex, labels)
                        navController.safePopBackStack("note_editor")
                    },
                    allLabels = state.savedNoteLabels,
                    onCreateLabel = { noteViewModel.addNoteLabel(it) },
                    onEditLabel = { oldName, newName -> noteViewModel.updateNoteLabel(oldName, newName) },
                    onDeleteLabel = { noteViewModel.deleteNoteLabel(it) }
                )
            }

            // Activity Workspace Destination
            composable(
                route = Screen.Workspace.route,
                arguments = listOf(
                    navArgument("activityId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getLong("activityId") ?: 0L
                val workspaceViewModel: ActivityWorkspaceViewModel = viewModel(
                    key = "workspace_$activityId",
                    factory = container.provideWorkspaceViewModelFactory(activityId)
                )
                ActivityWorkspaceScreen(
                    viewModel = workspaceViewModel,
                    onNavigateBack = { navController.safePopBackStack("workspace") },
                    onEditActivity = { id ->
                        navController.navigate(Screen.Create.createRoute(editActivityId = id))
                    },
                    modifier = Modifier.statusBarsPadding().navigationBarsPadding()
                )
            }
        }

        if (currentRoute != Screen.NoteEditor.route && !currentRoute.startsWith("note_editor")) {
            GlobalRecordingDynamicIsland(
            recordingState = recordingState,
            onTogglePause = {
                if (recordingState.isPaused) {
                    container.audioRecordingManager.resumeRecording()
                } else {
                    container.audioRecordingManager.pauseRecording()
                }
            },
            onStop = {
                container.audioRecordingManager.stopRecording()
            },
            onIslandClick = {
                val targetNoteId = recordingState.noteActivityId
                if (targetNoteId != null) {
                    val currentDest = navController.currentBackStackEntry?.destination?.route
                    val targetRoute = Screen.NoteEditor.createRoute(targetNoteId)
                    if (currentDest != targetRoute) {
                        navController.navigate(targetRoute) {
                            launchSingleTop = true
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 6.dp)
                .zIndex(999f)
        )
        }
    }
}
}

private fun NavHostController.safePopBackStack(expectedRoutePrefix: String? = null) {
    if (previousBackStackEntry == null) return
    val currentRoute = currentBackStackEntry?.destination?.route
    if (expectedRoutePrefix == null || currentRoute?.startsWith(expectedRoutePrefix) == true) {
        popBackStack()
    }
}

