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
import com.yourname.expenso.data.AppPreferencesManager
import com.yourname.expenso.data.TransactionRepository
import com.yourname.expenso.ui.MainAppScaffold
import com.yourname.expenso.ui.theme.ExpensoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: AppPreferencesManager
    
    @Inject
    lateinit var repository: TransactionRepository
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize default data
        lifecycleScope.launch {
            repository.initializeDefaultData()
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