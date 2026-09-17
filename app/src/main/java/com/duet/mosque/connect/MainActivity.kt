package com.duet.mosque.connect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duet.mosque.connect.ui.screens.MainAppContainer
import com.duet.mosque.connect.ui.theme.MyApplicationTheme
import com.duet.mosque.connect.ui.viewmodel.MosqueViewModel

/**
 * =====================================================================================
 * PROJECT ENTRY POINT
 * =====================================================================================
 * [MainActivity] is the primary Android Activity and the entry point of the application.
 * When the app launches, the Android OS invokes [onCreate], which initializes the theme,
 * requests necessary permissions (such as push notifications for Android 13+), and sets
 * the root Compose UI container ([MainAppContainer]).
 *
 * Kotlin Syntax Note for Beginners:
 * - `class MainActivity : ComponentActivity()`: Inherits from AndroidX [ComponentActivity].
 * - `override fun onCreate(...)`: Overrides the lifecycle method called when the activity is created.
 * - `val`: Defines an immutable (read-only) variable.
 * =====================================================================================
 */
class MainActivity : ComponentActivity() {

    /**
     * Entry point lifecycle callback where the UI and ViewModel are initialized.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // setContent establishes the Jetpack Compose declarative UI tree
        setContent {
            MyApplicationTheme {
                // Initialize the central ViewModel for state and business logic
                val viewModel: MosqueViewModel = viewModel()
                val context = LocalContext.current

                // Launcher for requesting runtime notification permissions on Android 13 (Tiramisu) and above
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Permission result callback
                }

                // LaunchedEffect runs once when entering composition to check and request notification permission
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Surface wraps the app content with the theme's background color
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Flow exit point: handing over control to the main UI container view
                    MainAppContainer(viewModel = viewModel)
                }
            }
        }
    }
}
