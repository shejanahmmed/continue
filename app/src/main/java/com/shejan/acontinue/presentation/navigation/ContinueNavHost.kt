package com.shejan.acontinue.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.shejan.acontinue.presentation.workspace.ActivityWorkspaceScreen
import com.shejan.acontinue.presentation.workspace.ActivityWorkspaceViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Diary : Screen("diary")
    data object Calendar : Screen("calendar")
    data object Settings : Screen("settings")
    data object Create : Screen("create")
    data object Workspace : Screen("workspace/{activityId}") {
        fun createRoute(activityId: Long): String = "workspace/$activityId"
    }
}

@Composable
fun ContinueNavHost(
    navController: NavHostController,
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val isTopLevelRoute = currentRoute in listOf(
        Screen.Home.route,
        Screen.Diary.route,
        Screen.Calendar.route,
        Screen.Settings.route
    )

    UtilitarianScaffold(
        currentRoute = currentRoute,
        onNavigate = { targetRoute ->
            if (targetRoute != currentRoute) {
                navController.navigate(targetRoute) {
                    popUpTo(Screen.Home.route) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        },
        onCreateClick = {
            navController.navigate(Screen.Create.route)
        },
        showBottomBar = isTopLevelRoute
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Home Destination
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = container.provideHomeViewModelFactory()
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onActivityClick = { activityId ->
                        navController.navigate(Screen.Workspace.createRoute(activityId))
                    }
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
                    }
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
                    onCreateClick = {
                        navController.navigate(Screen.Create.route)
                    }
                )
            }

            // Settings Destination
            composable(Screen.Settings.route) {
                val settingsViewModel: com.shejan.acontinue.presentation.settings.SettingsViewModel = viewModel(
                    factory = container.provideSettingsViewModelFactory()
                )
                SettingsScreen(viewModel = settingsViewModel)
            }

            // Create Screen
            composable(Screen.Create.route) {
                val createViewModel: CreateEntryViewModel = viewModel(
                    factory = container.provideCreateEntryViewModelFactory()
                )
                CreateEntryScreen(
                    viewModel = createViewModel,
                    onDismiss = { navController.popBackStack() }
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
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
