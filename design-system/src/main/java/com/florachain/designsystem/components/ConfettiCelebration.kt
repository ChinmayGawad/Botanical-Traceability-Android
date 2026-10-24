package com.florachain.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Particle(
    val initialX: Float,
    val initialY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val size: Float,
    val color: Color,
    val rotationSpeed: Float
)

@Composable
fun ConfettiCelebration(
    triggerKey: Any,
    modifier: Modifier = Modifier,
    particleCount: Int = 45
) {
    val progress = remember(triggerKey) { Animatable(0f) }

    val colors = remember {
        listOf(
            Color(0xFF10B981), // Emerald
            Color(0xFF34D399), // Mint
            Color(0xFF059669), // Dark emerald
            Color(0xFFF59E0B), // Amber gold
            Color(0xFF06B6D4), // Cyan
            Color(0xFFE11D48)  // Rose
        )
    }

    val particles = remember(triggerKey) {
        val random = Random(triggerKey.hashCode())
        List(particleCount) {
            Particle(
                initialX = 0.5f + (random.nextFloat() - 0.5f) * 0.4f,
                initialY = 0.35f,
                velocityX = (random.nextFloat() - 0.5f) * 1.6f,
                velocityY = -1.2f - random.nextFloat() * 1.5f,
                size = 14f + random.nextFloat() * 16f,
                color = colors[random.nextInt(colors.size)],
                rotationSpeed = (random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    LaunchedEffect(triggerKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
        )
    }

    if (progress.value < 1f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val t = progress.value
            val alpha = (1f - (t * 0.9f)).coerceIn(0f, 1f)

            particles.forEach { p ->
                val gravity = 3.5f * t * t
                val x = (p.initialX + p.velocityX * t) * canvasWidth
                val y = (p.initialY + p.velocityY * t + gravity) * canvasHeight

                drawRect(
                    color = p.color.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(p.size, p.size * 0.65f)
                )
            }
        }
    }
}
