package com.yourname.expenso.navigation

import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.yourname.expenso.ui.BottomNavItem
import com.yourname.expenso.ui.accounts.ManageAccountsScreen
import com.yourname.expenso.ui.add_transaction.AddTransactionScreen
import com.yourname.expenso.ui.categories.ManageCategoriesScreen

import com.yourname.expenso.ui.dashboard.DashboardScreen
import com.yourname.expenso.ui.reports.ReportsScreen
import com.yourname.expenso.ui.settings.RecentlyDeletedScreen
import com.yourname.expenso.ui.settings.SettingsScreen
import com.yourname.expenso.ui.transactions.TransactionsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Dashboard.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Dashboard.route) {
            DashboardScreen(navController = navController, windowSizeClass = windowSizeClass)
        }
        composable(BottomNavItem.Transactions.route) {
            TransactionsScreen(navController = navController)
        }
        
        // Other screens
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable("add_transaction") {
            AddTransactionScreen(navController = navController)
        }

        composable("recently_deleted") {
            RecentlyDeletedScreen(navController = navController)
        }
        composable("manage_categories") {
            ManageCategoriesScreen(navController = navController)
        }
        composable("manage_accounts") {
            ManageAccountsScreen(navController = navController)
        }
        composable("reports") {
            ReportsScreen(navController = navController)
        }

    }
}