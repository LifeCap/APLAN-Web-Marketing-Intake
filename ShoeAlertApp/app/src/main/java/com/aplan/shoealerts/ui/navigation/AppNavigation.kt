package com.aplan.shoealerts.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.aplan.shoealerts.notifications.NotificationHelper.Companion.DEEP_LINK_SCHEME
import com.aplan.shoealerts.ui.screens.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Deals    : Screen("deals",    "Deals",    Icons.Default.LocalOffer)
    object Search   : Screen("search",   "Search",   Icons.Default.Search)
    object Alerts   : Screen("alerts",   "Alerts",   Icons.Default.Notifications)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

private const val ROUTE_DEAL_DETAIL = "deal/{dealId}"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val topLevelScreens = listOf(Screen.Deals, Screen.Search, Screen.Alerts, Screen.Settings)

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            topLevelScreens.forEach { screen ->
                item(
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
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Deals.route
        ) {
            composable(Screen.Deals.route) {
                DealsScreen(onDealClick = { navController.navigate("deal/$it") })
            }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Alerts.route) { AlertsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }

            composable(
                route = ROUTE_DEAL_DETAIL,
                arguments = listOf(navArgument("dealId") { type = NavType.LongType }),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "$DEEP_LINK_SCHEME://deal/{dealId}" }
                )
            ) {
                DealDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
