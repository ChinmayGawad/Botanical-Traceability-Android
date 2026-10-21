package com.florachain.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.florachain.designsystem.BotanicalEmerald
import com.florachain.designsystem.MintAccent

@Composable
fun LaserScannerOverlay(
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    laserColor: Color = BotanicalEmerald
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LaserSweep")

    // Laser vertical progress: 0f (top) to 1f (bottom)
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserY"
    )

    // Pulse for corner targeting brackets
    val bracketPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BracketPulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // ── Draw 4 Sci-Fi Corner Brackets ──
            val bracketLen = width * 0.16f
            val strokeWidth = 4.dp.toPx()
            val bracketColor = if (isLocked) MintAccent else laserColor.copy(alpha = bracketPulse)

            // Top-Left
            drawLine(
                color = bracketColor,
                start = Offset(0f, 0f),
                end = Offset(bracketLen, 0f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(0f, 0f),
                end = Offset(0f, bracketLen),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Top-Right
            drawLine(
                color = bracketColor,
                start = Offset(width, 0f),
                end = Offset(width - bracketLen, 0f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(width, 0f),
                end = Offset(width, bracketLen),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Bottom-Left
            drawLine(
                color = bracketColor,
                start = Offset(0f, height),
                end = Offset(bracketLen, height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(0f, height),
                end = Offset(0f, height - bracketLen),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Bottom-Right
            drawLine(
                color = bracketColor,
                start = Offset(width, height),
                end = Offset(width - bracketLen, height),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = bracketColor,
                start = Offset(width, height),
                end = Offset(width, height - bracketLen),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // ── Center Reticle Crosshairs ──
            val centerX = width / 2f
            val centerY = height / 2f
            val crosshairSize = 14.dp.toPx()
            val crosshairColor = laserColor.copy(alpha = 0.35f)

            drawLine(
                color = crosshairColor,
                start = Offset(centerX - crosshairSize, centerY),
                end = Offset(centerX + crosshairSize, centerY),
                strokeWidth = 1.5.dp.toPx()
            )
            drawLine(
                color = crosshairColor,
                start = Offset(centerX, centerY - crosshairSize),
                end = Offset(centerX, centerY + crosshairSize),
                strokeWidth = 1.5.dp.toPx()
            )

            // ── Sweeping Laser Beam ──
            if (!isLocked) {
                val currentLaserY = height * laserProgress
                val beamHeight = 24.dp.toPx()

                // Diffuse glow
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            laserColor.copy(alpha = 0.25f),
                            MintAccent.copy(alpha = 0.5f),
                            laserColor.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        startY = currentLaserY - beamHeight,
                        endY = currentLaserY + beamHeight
                    ),
                    topLeft = Offset(0f, currentLaserY - beamHeight),
                    size = Size(width, beamHeight * 2)
                )

                // Crisp Center Laser Line
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            laserColor,
                            MintAccent,
                            laserColor,
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, currentLaserY),
                    end = Offset(width, currentLaserY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
