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
import com.aplan.shoealerts.ui.screens.AlertsScreen
import com.aplan.shoealerts.ui.screens.DealDetailScreen
import com.aplan.shoealerts.ui.screens.DealsScreen
import com.aplan.shoealerts.ui.screens.SearchScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Deals  : Screen("deals",  "Deals",  Icons.Default.LocalOffer)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
}

private const val ROUTE_DEAL_DETAIL = "deal/{dealId}"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val topLevelScreens = listOf(Screen.Deals, Screen.Search, Screen.Alerts)

    // Hide bottom nav on detail screens
    val showBottomNav = currentDestination?.route?.let { route ->
        topLevelScreens.any { it.route == route }
    } ?: true

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
                DealsScreen(
                    onDealClick = { dealId ->
                        navController.navigate("deal/$dealId")
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen()
            }
            composable(Screen.Alerts.route) {
                AlertsScreen()
            }
            composable(
                route = ROUTE_DEAL_DETAIL,
                arguments = listOf(navArgument("dealId") { type = NavType.LongType })
            ) {
                DealDetailScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
