package com.florachain.presentation.screens.fleet

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.presentation.viewmodel.FleetBatch
import com.florachain.presentation.viewmodel.FleetCommandViewModel

@Composable
fun FleetCommandScreen(
    viewModel: FleetCommandViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FloraChainTheme.colors
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Fleet Command Deck",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showThemeToggle = true,
                actions = {
                    Surface(
                        color = TrustGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(TrustGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE TELEMATICS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrustGreen
                            )
                        }
                    }
                }
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Batch Switcher Strip
            Text(
                text = "ACTIVE IN-TRANSIT CONSIGNMENTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                letterSpacing = 1.sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.batches) { batch ->
                    val isSelected = batch.batchId == uiState.selectedBatch.batchId
                    Surface(
                        color = if (isSelected) colors.primaryContainer.copy(alpha = 0.5f) else colors.surfaceElevated,
                        shape = RoundedCornerShape(12.dp),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, colors.primary) else null,
                        modifier = Modifier.clickable { viewModel.selectBatch(batch.batchId) }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = batch.batchId,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) colors.primary else colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = batch.productName,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                maxLines = 1
                            )
                            Text(
                                text = "${batch.origin.substringBefore(",")} → ${batch.destination.substringBefore(",")}",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }

            // Interactive Vector Map Canvas Deck
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = uiState.selectedBatch.productName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Vehicle: ${uiState.selectedBatch.truckPlate} • Driver: ${uiState.selectedBatch.driverName}",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                    Surface(
                        color = colors.surfaceElevated,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${(uiState.scrubberProgress * 100).toInt()}% COMPLETE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas Map Component
                FleetMapCanvas(
                    batch = uiState.selectedBatch,
                    truckX = uiState.truckX,
                    truckY = uiState.truckY,
                    truckHeadingDegrees = uiState.truckHeadingDegrees,
                    progress = uiState.scrubberProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.background)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Time Scrubber & Simulation Deck
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.resetToStart() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Replay,
                            contentDescription = "Restart",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Slider(
                        value = uiState.scrubberProgress,
                        onValueChange = { viewModel.setScrubberProgress(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.surfaceElevated
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Speed pills
                    listOf(1.0f, 2.0f, 5.0f).forEach { spd ->
                        val isSpeedSelected = uiState.simulationSpeed == spd
                        Surface(
                            color = if (isSpeedSelected) colors.primary else colors.surfaceElevated,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clickable { viewModel.setSimulationSpeed(spd) }
                                .padding(horizontal = 2.dp)
                        ) {
                            Text(
                                text = "${spd.toInt()}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSpeedSelected) colors.onPrimary else colors.textMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                val currentKm = (uiState.scrubberProgress * uiState.selectedBatch.totalDistanceKm).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Origin: ${uiState.selectedBatch.origin.substringBefore(",")}",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = "$currentKm km / ${uiState.selectedBatch.totalDistanceKm} km",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary
                    )
                    Text(
                        text = "Destination: ${uiState.selectedBatch.destination.substringBefore(",")}",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            // Live IoT Cargo Telematics Grid
            Text(
                text = "LIVE REEFER IOT TELEMATICS SENSORS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Temperature Gauge Card
                GlassmorphicCard(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Temperature", fontSize = 12.sp, color = colors.textMuted)
                        Icon(Icons.Default.AcUnit, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.currentTemp}°C",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Surface(
                        color = TrustGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "STABLE • ${uiState.selectedBatch.targetTempRange.substringBefore(" ")}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrustGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Humidity Gauge Card
                GlassmorphicCard(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Relative Humidity", fontSize = 12.sp, color = colors.textMuted)
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF00B4D8), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.currentHumidity}% RH",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Surface(
                        color = TrustGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "OPTIMAL (45-60%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrustGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Vehicle Speed Card
                GlassmorphicCard(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Speed & Motion", fontSize = 12.sp, color = colors.textMuted)
                        Icon(Icons.Default.Speed, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.currentSpeedKmh} km/h",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text("Expressway Cruise", fontSize = 11.sp, color = colors.textMuted)
                }

                // Shock / Vibration Card
                GlassmorphicCard(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Shock / G-Force", fontSize = 12.sp, color = colors.textMuted)
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${uiState.currentGForce}g",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text("Zero Impact Threshold", fontSize = 11.sp, color = colors.textMuted)
                }
            }

            // Driver & Reefer Diagnostics Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Hardware Diagnostics & Gateway Seal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Lead Driver:", fontSize = 11.sp, color = colors.textMuted)
                        Text(uiState.selectedBatch.driverName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = colors.textPrimary)
                        Text("Emergency: ${uiState.selectedBatch.driverContact}", fontSize = 11.sp, color = colors.primary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Battery Reserve:", fontSize = 11.sp, color = colors.textMuted)
                        Text("${uiState.batteryPercent}% (Active Solar)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TrustGreen)
                        Text("Lock: ${uiState.activeSatelliteCount} GNSS Satellites", fontSize = 11.sp, color = colors.textMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = colors.surfaceElevated)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("On-Chain Telematics Hash Anchor:", fontSize = 11.sp, color = colors.textMuted)
                    Text(
                        text = uiState.blockAnchorTx,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.primary
                    )
                }
            }

            // Fleet KPI Overview
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("14.8 T", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.textPrimary)
                        Text("Fleet In-Transit", fontSize = 11.sp, color = colors.textMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("99.8%", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TrustGreen)
                        Text("Cold-Chain SLA", fontSize = 11.sp, color = colors.textMuted)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("98.4%", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.primary)
                        Text("Avg Purity Assay", fontSize = 11.sp, color = colors.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun FleetMapCanvas(
    batch: FleetBatch,
    truckX: Float,
    truckY: Float,
    truckHeadingDegrees: Float,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = FloraChainTheme.colors
    val waypoints = batch.waypoints

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Subtle background grid
        val gridStep = 40f
        var gx = 0f
        while (gx < w) {
            drawLine(
                color = colors.surfaceElevated.copy(alpha = 0.5f),
                start = Offset(gx, 0f),
                end = Offset(gx, h),
                strokeWidth = 1f
            )
            gx += gridStep
        }
        var gy = 0f
        while (gy < h) {
            drawLine(
                color = colors.surfaceElevated.copy(alpha = 0.5f),
                start = Offset(0f, gy),
                end = Offset(w, gy),
                strokeWidth = 1f
            )
            gy += gridStep
        }

        if (waypoints.size >= 2) {
            // Draw planned path (dashed)
            val fullPath = Path()
            waypoints.forEachIndexed { i, pt ->
                val px = pt.x * w
                val py = pt.y * h
                if (i == 0) fullPath.moveTo(px, py) else fullPath.lineTo(px, py)
            }
            drawPath(
                path = fullPath,
                color = colors.textMuted.copy(alpha = 0.4f),
                style = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f),
                    cap = StrokeCap.Round
                )
            )

            // Draw passed path (solid primary glow)
            val truckPx = truckX * w
            val truckPy = truckY * h

            val totalSegments = waypoints.size - 1
            val scaledProgress = progress * totalSegments
            val currentSegment = scaledProgress.toInt().coerceIn(0, totalSegments - 1)

            val passedPath = Path()
            passedPath.moveTo(waypoints[0].x * w, waypoints[0].y * h)
            for (i in 1..currentSegment) {
                passedPath.lineTo(waypoints[i].x * w, waypoints[i].y * h)
            }
            passedPath.lineTo(truckPx, truckPy)

            drawPath(
                path = passedPath,
                color = colors.primary,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )

            // Draw waypoints
            waypoints.forEachIndexed { idx, pt ->
                val wx = pt.x * w
                val wy = pt.y * h
                val isReached = idx <= currentSegment

                drawCircle(
                    color = if (isReached) colors.primary else colors.surfaceElevated,
                    radius = 9f,
                    center = Offset(wx, wy)
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(wx, wy)
                )
            }

            // Draw Pulsing Beacon Ring around Truck
            drawCircle(
                color = colors.primary.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = Offset(truckPx, truckPy)
            )

            // Draw moving truck representation (Directional triangle marker)
            rotate(degrees = truckHeadingDegrees, pivot = Offset(truckPx, truckPy)) {
                val arrowPath = Path().apply {
                    moveTo(truckPx + 14f, truckPy)
                    lineTo(truckPx - 10f, truckPy - 9f)
                    lineTo(truckPx - 6f, truckPy)
                    lineTo(truckPx - 10f, truckPy + 9f)
                    close()
                }
                drawPath(
                    path = arrowPath,
                    color = colors.primary
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = Offset(truckPx, truckPy)
                )
            }
        }
    }
}
