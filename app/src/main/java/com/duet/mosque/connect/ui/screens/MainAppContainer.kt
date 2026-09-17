package com.duet.mosque.connect.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.duet.mosque.connect.ui.theme.EmeraldGreen
import com.duet.mosque.connect.ui.theme.TextLight
import com.duet.mosque.connect.ui.viewmodel.MosqueViewModel
import com.duet.mosque.connect.utils.NotificationHelper

/**
 * =========================================================================================
 * APPLICATION NAVIGATION & ROOT SCAFFOLD (DUET Mosque Connect)
 * =========================================================================================
 * [ENTRY POINT CONTINUATION]:
 * After MainActivity initializes the theme, it calls this root Composable function:
 *  `MainAppContainer(viewModel = viewModel)`
 *
 * Architecture & Execution Flow:
 *  1. [Startup Lifecycle]: Requests required runtime permissions (Location & Android 13+ Notifications).
 *  2. [Battery & Sensor Optimization]: Activates hardware magnetometer and gyroscope sensors
 *     ONLY when the user switches to the Qibla tab using `DisposableEffect`.
 *  3. [Bottom Navigation Bar]: Provides switching between the 5 primary tabs:
 *       - Home (HomeScreen)
 *       - Prayer Timetable (ScheduleScreen)
 *       - Qibla Compass (QiblaCompassScreen)
 *       - Updates & Janaza (EventsAndNoticesScreen)
 *       - Settings & Imam Portal (SettingsAndImamScreen)
 *
 * Kotlin Concepts Explained for Beginners:
 *  - `sealed class`: A restricted class hierarchy where all subclasses (`Home`, `Prayer`, etc.)
 *    are known at compile time, making `when (currentTab)` exhaustive and type-safe.
 *  - `DisposableEffect`: Lifecycle-aware block in Compose that runs cleanup (`onDispose`) when
 *    leaving a screen or when a tab changes.
 *  - `rememberLauncherForActivityResult`: Modern Android contract to request system permissions
 *    without writing legacy `onRequestPermissionsResult` boilerplate in the Activity.
 * =========================================================================================
 */

/**
 * Sealed class defining the 5 main navigation tabs in the application.
 */
sealed class TabScreen(val route: String, val title: String) {
    object Home : TabScreen("home", "Home")
    object Prayer : TabScreen("prayer", "Prayer")
    object Qibla : TabScreen("qibla", "Qibla")
    object Events : TabScreen("events", "Updates")
    object Settings : TabScreen("settings", "Settings")
}

@Composable
fun MainAppContainer(viewModel: MosqueViewModel) {
    // Current active tab state (defaults to Home tab)
    var currentTab by remember { mutableStateOf<TabScreen>(TabScreen.Home) }
    val context = LocalContext.current

    // Screen rotation listener for compass orientation correction
    val windowManager = remember { context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager }
    val displayRotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            context.display.rotation
        } catch (_: Exception) {
            Surface.ROTATION_0
        }
    } else {
        @Suppress("DEPRECATION")
        windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
    }

    LaunchedEffect(displayRotation) {
        viewModel.setCompassDisplayRotation(displayRotation)
    }

    // BATTERY & SENSOR LIFECYCLE: Observe compass sensors only when on Qibla tab
    DisposableEffect(currentTab) {
        val isQiblaScreen = currentTab == TabScreen.Qibla
        viewModel.enableCompass(isQiblaScreen)
        onDispose {
            if (isQiblaScreen) {
                viewModel.enableCompass(false)
            }
        }
    }

    // Runtime Permission Launcher for Location & Notifications
    val startupPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.refreshGPSLocation()
        }
    }

    // Startup initialization: setup notification channel and check permissions
    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)

        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        if ((!fineLocationGranted && !coarseLocationGranted) || !notificationGranted) {
            val permissionsToRequest = mutableListOf<String>().apply {
                if (!fineLocationGranted && !coarseLocationGranted) {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationGranted) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }.toTypedArray()
            startupPermissionLauncher.launch(permissionsToRequest)
        } else {
            viewModel.refreshGPSLocation()
        }
    }

    // Ensure GPS coordinates are refreshed when switching to Qibla screen
    LaunchedEffect(currentTab) {
        if (currentTab == TabScreen.Qibla) {
            val hasFine = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasFine || hasCoarse) {
                viewModel.refreshGPSLocation()
            } else {
                startupPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    // Main Scaffold Layout with Bottom Navigation Bar
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                val items = listOf(
                    TabScreen.Home to Icons.Default.Home,
                    TabScreen.Prayer to Icons.Default.Schedule,
                    TabScreen.Qibla to Icons.Default.Explore,
                    TabScreen.Events to Icons.Default.Event,
                    TabScreen.Settings to Icons.Default.Settings
                )

                items.forEach { (tab, icon) ->
                    val selected = currentTab == tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextLight,
                            selectedTextColor = EmeraldGreen,
                            indicatorColor = EmeraldGreen,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Render the active tab composable
            when (currentTab) {
                TabScreen.Home -> HomeScreen(viewModel, onNavigateToTab = { currentTab = it })
                TabScreen.Prayer -> ScheduleScreen(viewModel)
                TabScreen.Qibla -> QiblaCompassScreen(viewModel)
                TabScreen.Events -> EventsAndNoticesScreen(viewModel)
                TabScreen.Settings -> SettingsAndImamScreen(viewModel)
            }
        }
    }
}
