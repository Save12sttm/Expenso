package com.yourname.expenso.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.optimizedPerformance(): Modifier = this
    .graphicsLayer {
        // Enable hardware acceleration
        renderEffect = null
        compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
    }

@Composable
fun <T> rememberStable(calculation: () -> T): T = remember { calculation() }

// Optimized list keys
fun stableKey(id: Int, version: Long = 0L): String = "$id-$version"