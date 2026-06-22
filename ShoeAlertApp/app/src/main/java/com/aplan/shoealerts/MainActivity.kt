package com.aplan.shoealerts

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.aplan.shoealerts.ui.navigation.AppNavigation
import com.aplan.shoealerts.ui.theme.ShoeAlertTheme
import com.aplan.shoealerts.worker.DealSearchWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* permissions handled gracefully; app works without SMS */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestRequiredPermissions()

        // Schedule the periodic background search (default 4-hour interval)
        DealSearchWorker.schedulePeriodicWork(this, intervalHours = 4)

        setContent {
            ShoeAlertTheme {
                AppNavigation()
            }
        }
    }

    private fun requestRequiredPermissions() {
        val permissions = mutableListOf<String>()

        // Android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        // SMS permission (optional — only needed if user enables SMS alerts)
        permissions.add(Manifest.permission.SEND_SMS)

        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }
}
