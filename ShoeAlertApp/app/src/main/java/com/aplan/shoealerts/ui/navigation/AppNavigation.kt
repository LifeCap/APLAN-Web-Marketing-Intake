package com.aplan.shoealerts.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aplan.shoealerts.ui.screens.AlertsScreen
import com.aplan.shoealerts.ui.screens.DealsScreen
import com.aplan.shoealerts.ui.screens.SearchScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Deals : Screen("deals", "Deals", Icons.Default.LocalOffer)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screens = listOf(Screen.Deals, Screen.Search, Screen.Alerts)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Deals.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Deals.route) { DealsScreen() }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Alerts.route) { AlertsScreen() }
        }
    }
}
