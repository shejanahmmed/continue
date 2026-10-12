package com.shejan.acontinue

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.shejan.acontinue.core.notification.AlarmScheduler
import com.shejan.acontinue.presentation.navigation.ContinueNavHost
import com.shejan.acontinue.presentation.navigation.Screen
import com.shejan.acontinue.ui.theme.ContinueTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationPermissionIfNeeded()

        val appContainer = (application as ContinueApplication).container

        setContent {
            ContinueTheme {
                val navController = rememberNavController()

                // Handle direct workspace deep link from notification
                LaunchedEffect(intent) {
                    val activityId = intent.getLongExtra(AlarmScheduler.EXTRA_ACTIVITY_ID, -1L)
                    if (activityId > 0) {
                        navController.navigate(Screen.Workspace.createRoute(activityId)) {
                            launchSingleTop = true
                        }
                    }
                }

                ContinueNavHost(
                    navController = navController,
                    container = appContainer
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}