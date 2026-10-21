package com.florachain.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*

@Composable
fun TrustScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    gaugeSize: Dp = 100.dp,
    strokeWidth: Dp = 8.dp,
    label: String = "TRUST SCORE"
) {
    val colors = FloraChainTheme.colors

    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat().coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "GaugeScore"
    )

    val gaugeColor = when {
        score >= 90 -> colors.primary
        score >= 70 -> colors.warningAmber
        else -> colors.errorRose
    }

    val gradientBrush = remember(colors.secondary, gaugeColor) {
        Brush.sweepGradient(
            colors = listOf(
                colors.secondary,
                gaugeColor,
                colors.secondary
            )
        )
    }

    Box(
        modifier = modifier.size(gaugeSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val canvasW = this.size.width
            val canvasH = this.size.height
            val arcSize = Size(canvasW - strokePx, canvasH - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track
            drawArc(
                color = if (colors.isDark) Color(0x3310B981) else Color(0x2610B981),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc
            val progressSweep = (animatedScore / 100f) * 270f
            if (progressSweep > 0f) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = 135f,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${animatedScore.toInt()}%",
                fontSize = (gaugeSize.value * 0.22f).sp,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary
            )
            Text(
                text = label,
                fontSize = (gaugeSize.value * 0.085f).sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                letterSpacing = 0.5.sp
            )
        }
    }
}
