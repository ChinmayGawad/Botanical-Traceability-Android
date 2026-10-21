package com.florachain.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.FloraChainTheme

@Composable
fun ColdChainTempChart(
    modifier: Modifier = Modifier,
    temperatures: List<Float> = listOf(4.2f, 3.8f, 4.5f, 5.1f, 4.0f, 4.3f, 3.9f),
    labels: List<String> = listOf("02:00", "04:00", "06:00", "08:00", "10:00", "12:00", "14:00")
) {
    val colors = FloraChainTheme.colors

    val fillColors = remember(colors.primary) {
        listOf(
            colors.primary.copy(alpha = 0.25f),
            Color.Transparent
        )
    }
    val curveColors = remember(colors.primary, colors.secondary) {
        listOf(colors.primary, colors.secondary)
    }
    val curvePath = remember { Path() }
    val fillAreaPath = remember { Path() }

    CyberGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        // Title & Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COLD-CHAIN IOT TELEMATICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Real-time BLE Sensor Log (2.0°C – 8.0°C Standard)",
                    fontSize = 10.sp,
                    color = colors.textMuted
                )
            }

            Surface(
                color = colors.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "OPTIMAL 4.1°C AVG",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chart Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val width = size.width
            val height = size.height

            val minTemp = 0f
            val maxTemp = 10f
            val tempRange = maxTemp - minTemp

            // ── 2°C to 8°C Safe Zone Band ──
            val topSafeY = height * (1f - (8f - minTemp) / tempRange)
            val bottomSafeY = height * (1f - (2f - minTemp) / tempRange)

            drawRect(
                color = colors.primary.copy(alpha = if (colors.isDark) 0.12f else 0.08f),
                topLeft = Offset(0f, topSafeY),
                size = androidx.compose.ui.geometry.Size(width, bottomSafeY - topSafeY)
            )

            // Dotted upper safe bound (8°C)
            drawLine(
                color = colors.primary.copy(alpha = 0.35f),
                start = Offset(0f, topSafeY),
                end = Offset(width, topSafeY),
                strokeWidth = 1.dp.toPx()
            )

            // Dotted lower safe bound (2°C)
            drawLine(
                color = colors.primary.copy(alpha = 0.35f),
                start = Offset(0f, bottomSafeY),
                end = Offset(width, bottomSafeY),
                strokeWidth = 1.dp.toPx()
            )

            // ── Calculate Plot Points ──
            val stepX = width / (temperatures.size - 1)
            val points = temperatures.mapIndexed { index, temp ->
                val x = index * stepX
                val y = height * (1f - (temp - minTemp) / tempRange)
                Offset(x, y)
            }

            // ── Draw Smooth Cubic Bezier Line (Reusing path object to avoid allocation) ──
            curvePath.reset()
            if (points.isNotEmpty()) {
                curvePath.moveTo(points[0].x, points[0].y)
                for (i in 0 until points.size - 1) {
                    val current = points[i]
                    val next = points[i + 1]
                    val control1 = Offset(current.x + (next.x - current.x) / 2f, current.y)
                    val control2 = Offset(current.x + (next.x - current.x) / 2f, next.y)
                    curvePath.cubicTo(control1.x, control1.y, control2.x, control2.y, next.x, next.y)
                }
            }

            // Draw Area Fill Under Curve (Reusing fill path)
            fillAreaPath.reset()
            fillAreaPath.addPath(curvePath)
            fillAreaPath.lineTo(width, height)
            fillAreaPath.lineTo(0f, height)
            fillAreaPath.close()

            drawPath(
                path = fillAreaPath,
                brush = Brush.verticalGradient(
                    colors = fillColors,
                    startY = 0f,
                    endY = height
                )
            )

            // Draw Stroke Curve
            drawPath(
                path = curvePath,
                brush = Brush.horizontalGradient(curveColors),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Data Points (Nodes)
            points.forEach { point ->
                drawCircle(
                    color = colors.surface,
                    radius = 4.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = colors.primary,
                    radius = 2.5.dp.toPx(),
                    center = point
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time Axis Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}
