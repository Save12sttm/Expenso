package com.yourname.expenso.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Enhanced Dark Color Scheme
private val EnhancedDarkColorScheme = darkColorScheme(
    primary = FinancialColors.SavingsBlue,
    onPrimary = Color.White,
    primaryContainer = FinancialColors.SavingsBlueDark,
    onPrimaryContainer = Color.White,
    
    secondary = FinancialColors.IncomeGreen,
    onSecondary = Color.White,
    secondaryContainer = FinancialColors.IncomeGreenDark,
    onSecondaryContainer = Color.White,
    
    tertiary = FinancialColors.FoodOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF8A4A00),
    onTertiaryContainer = Color.White,
    
    error = FinancialColors.ExpenseRed,
    onError = Color.White,
    errorContainer = FinancialColors.ExpenseRedDark,
    onErrorContainer = Color.White,
    
    background = Color(0xFF0F0F0F),
    onBackground = Color(0xFFE6E6E6),
    surface = Color(0xFF1A1A1A),
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = Color(0xFFB0B0B0),
    
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF2A2A2A)
)

// Enhanced Light Color Scheme
private val EnhancedLightColorScheme = lightColorScheme(
    primary = FinancialColors.SavingsBlue,
    onPrimary = Color.White,
    primaryContainer = FinancialColors.SavingsBlueLight,
    onPrimaryContainer = Color(0xFF001D36),
    
    secondary = FinancialColors.IncomeGreen,
    onSecondary = Color.White,
    secondaryContainer = FinancialColors.IncomeGreenLight,
    onSecondaryContainer = Color(0xFF002114),
    
    tertiary = FinancialColors.FoodOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC2),
    onTertiaryContainer = Color(0xFF2D1600),
    
    error = FinancialColors.ExpenseRed,
    onError = Color.White,
    errorContainer = FinancialColors.ExpenseRedLight,
    onErrorContainer = Color(0xFF410002),
    
    background = Color(0xFFFCFCFC),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Color(0xFF424242),
    
    outline = Color(0xFF737373),
    outlineVariant = Color(0xFFC4C7C5)
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun ExpensoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> EnhancedDarkColorScheme
        else -> EnhancedLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EnhancedTypography,
        content = content
    )
}