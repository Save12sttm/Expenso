package com.yourname.expenso.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier

// Animation Durations
object AnimationDurations {
    const val FAST = 100
    const val NORMAL = 200
    const val SLOW = 300
    const val EXTRA_SLOW = 400
}

// Easing Functions
object AnimationEasing {
    val FastOutSlowIn = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val LinearOutSlowIn = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val FastOutLinearIn = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
    val Bounce = CubicBezierEasing(0.68f, -0.55f, 0.265f, 1.55f)
}

// Reusable Animation Specs
object AnimationSpecs {
    val cardEnter = tween<Float>(
        durationMillis = AnimationDurations.NORMAL,
        easing = AnimationEasing.FastOutSlowIn
    )
    
    val cardExit = tween<Float>(
        durationMillis = AnimationDurations.FAST,
        easing = AnimationEasing.LinearOutSlowIn
    )
    
    val bounceIn = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val slideIn = tween<Float>(
        durationMillis = AnimationDurations.NORMAL,
        easing = AnimationEasing.FastOutSlowIn
    )
}

// Screen Transition Animations
object ScreenTransitions {
    val slideInFromRight = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(AnimationDurations.NORMAL, easing = AnimationEasing.FastOutSlowIn)
    ) + fadeIn(animationSpec = tween(AnimationDurations.NORMAL))
    
    val slideOutToLeft = slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(AnimationDurations.NORMAL, easing = AnimationEasing.FastOutSlowIn)
    ) + fadeOut(animationSpec = tween(AnimationDurations.NORMAL))
}

// Modifier Extensions for Common Animations
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha = transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ), label = "shimmer_alpha"
    )
    return this.graphicsLayer { this.alpha = alpha.value }
}

@Composable
fun Modifier.pulseEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale = transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = AnimationEasing.FastOutSlowIn),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse_scale"
    )
    return this.graphicsLayer { 
        scaleX = scale.value
        scaleY = scale.value
    }
}