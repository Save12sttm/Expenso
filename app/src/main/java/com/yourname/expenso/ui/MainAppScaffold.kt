package com.yourname.expenso.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.yourname.expenso.navigation.AppNavHost

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Dashboard : BottomNavItem("dashboard", Icons.Default.Home, "Dashboard")
    data object Transactions : BottomNavItem("transactions", Icons.Default.List, "Transactions")
}

@Composable
fun MainAppScaffold(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val items = listOf(BottomNavItem.Dashboard, BottomNavItem.Transactions)
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            windowSizeClass = windowSizeClass
        )
    }
}