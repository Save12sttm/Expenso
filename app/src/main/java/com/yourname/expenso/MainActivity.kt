package com.yourname.expenso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.yourname.expenso.data.AppTheme
import com.yourname.expenso.data.ThemeManager
import com.yourname.expenso.data.TransactionRepository
import com.yourname.expenso.ui.MainAppScaffold
import com.yourname.expenso.ui.theme.ExpensoTheme
import com.yourname.expenso.util.MonthlyExportScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager
    
    @Inject
    lateinit var repository: TransactionRepository
    
    @Inject
    lateinit var monthlyExportScheduler: MonthlyExportScheduler
    


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize default data and schedule exports
        lifecycleScope.launch {
            repository.initializeDefaultData()
            monthlyExportScheduler.scheduleMonthlyExport()
        }
        setContent {
            val theme by themeManager.theme.collectAsState(initial = AppTheme.SYSTEM)
            val useDarkTheme = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            val windowSizeClass = calculateWindowSizeClass(this)

            ExpensoTheme(darkTheme = useDarkTheme) {
                MainAppScaffold(windowSizeClass = windowSizeClass)
            }
        }
    }
}