package com.florachain.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.FloraChainTheme

data class JourneyWaypoint(
    val stageNumber: Int,
    val title: String,
    val facilityName: String,
    val location: String,
    val icon: ImageVector,
    val status: String,
    val timestamp: String,
    val coordinates: Pair<Float, Float>, // Normalized 0..1 for canvas
    val details: String
)

@Composable
fun JourneyMapCanvas(
    batchId: String,
    productName: String,
    currentTemperature: Double = 18.2,
    currentHumidity: Int = 52,
    modifier: Modifier = Modifier
) {
    val colors = FloraChainTheme.colors
    var isSimulating by remember { mutableStateOf(true) }
    var selectedWaypointIndex by remember { mutableStateOf(3) } // Default selected: Transit

    val waypoints = remember {
        listOf(
            JourneyWaypoint(
                stageNumber = 1,
                title = "Agricultural Harvest",
                facilityName = "Malwa Organic Farms",
                location = "Neemuch, MP (24.46°N, 74.86°E)",
                icon = Icons.Default.Agriculture,
                status = "COMPLETED",
                timestamp = "10 Mar • 08:30 AM",
                coordinates = Pair(0.12f, 0.20f),
                details = "Harvested 500kg organic root stock with GPS geotagging."
            ),
            JourneyWaypoint(
                stageNumber = 2,
                title = "GMP Processing",
                facilityName = "BioExtracts GMP Ltd",
                location = "Indore, MP (22.71°N, 75.85°E)",
                icon = Icons.Default.PrecisionManufacturing,
                status = "COMPLETED",
                timestamp = "12 Mar • 11:00 AM",
                coordinates = Pair(0.32f, 0.42f),
                details = "Milled and cold-extracted. Yield 479kg. Moisture <8%."
            ),
            JourneyWaypoint(
                stageNumber = 3,
                title = "HPLC Lab Assay",
                facilityName = "National Testing Lab",
                location = "Mumbai, MH (19.07°N, 72.87°E)",
                icon = Icons.Default.Biotech,
                status = "COMPLETED",
                timestamp = "13 Mar • 04:15 PM",
                coordinates = Pair(0.52f, 0.65f),
                details = "Purity: 5.4% HPLC Active withanolides. Heavy metals: Passed."
            ),
            JourneyWaypoint(
                stageNumber = 4,
                title = "Cold-Chain Logistics",
                facilityName = "FloraLogistics Reefer",
                location = "Pune-BLR Transit (18.52°N, 73.85°E)",
                icon = Icons.Default.LocalShipping,
                status = "IN_TRANSIT",
                timestamp = "Live IoT Telematics",
                coordinates = Pair(0.72f, 0.82f),
                details = "Reefer Truck MH-12-QZ-4921. Temp stable 18.2°C (Limit: 15-22°C)."
            ),
            JourneyWaypoint(
                stageNumber = 5,
                title = "Retail Verification",
                facilityName = "AyurAushadhi Store",
                location = "Bangalore, KA (12.97°N, 77.59°E)",
                icon = Icons.Default.Storefront,
                status = "READY",
                timestamp = "ETA 16 Mar",
                coordinates = Pair(0.90f, 0.90f),
                details = "Destined for shelf display with authenticated packaging QR."
            )
        )
    }

    // Vehicle progress animation
    val infiniteTransition = rememberInfiniteTransition(label = "TruckTransition")
    val vehicleProgress by infiniteTransition.animateFloat(
        initialValue = 0.52f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "VehicleProgress"
    )

    CyberGlassCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header with status & simulation toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.primary,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Text(
                            text = "LIVE GEOSPATIAL PROVENANCE ROUTE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        text = "5-Node Supply Chain Map",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (colors.isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.glassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { isSimulating = !isSimulating }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Simulate",
                            tint = colors.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isSimulating) "Live Tracking" else "Paused",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // Visual Vector Journey Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (colors.isDark) Color(0xFF070B14) else Color(0xFFE8F5E9).copy(alpha = 0.45f))
                    .border(1.dp, colors.glassBorder, RoundedCornerShape(14.dp))
            ) {
                val lineColor = colors.primary.copy(alpha = 0.65f)
                val activeLineColor = colors.primary
                val gridColor = colors.glassBorder.copy(alpha = 0.25f)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Draw subtle coordinate grid
                    val gridCols = 8
                    val gridRows = 5
                    for (i in 0..gridCols) {
                        val x = i * (w / gridCols)
                        drawLine(gridColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
                    }
                    for (j in 0..gridRows) {
                        val y = j * (h / gridRows)
                        drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
                    }

                    // 2. Compute waypoint pixel positions
                    val points = waypoints.map { wp ->
                        Offset(wp.coordinates.first * w, wp.coordinates.second * h)
                    }

                    // 3. Draw route bezier path
                    val path = Path()
                    path.moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        val prev = points[i - 1]
                        val curr = points[i]
                        val midX = (prev.x + curr.x) / 2
                        path.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }

                    // Draw connecting path line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. Draw Waypoint Circles
                    points.forEachIndexed { index, offset ->
                        val isSelected = index == selectedWaypointIndex
                        val isCompleted = index < 3
                        val isCurrent = index == 3

                        // Outer pulse ring for current node
                        if (isCurrent) {
                            drawCircle(
                                color = activeLineColor.copy(alpha = 0.25f),
                                radius = 22.dp.toPx(),
                                center = offset
                            )
                        }

                        // Outer ring
                        drawCircle(
                            color = if (isSelected) Color(0xFFF59E0B) else if (isCompleted) activeLineColor else Color.Gray,
                            radius = if (isSelected) 14.dp.toPx() else 10.dp.toPx(),
                            center = offset
                        )

                        // Center white pip
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = offset
                        )
                    }

                    // 5. Animated Vehicle Marker (between node 3 and 4)
                    if (isSimulating) {
                        val p3 = points[2]
                        val p4 = points[3]
                        val fraction = (vehicleProgress - 0.52f) / (0.72f - 0.52f)
                        val vehicleX = p3.x + (p4.x - p3.x) * fraction
                        val vehicleY = p3.y + (p4.y - p3.y) * fraction

                        // Vehicle glow
                        drawCircle(
                            color = Color(0xFF38BDF8).copy(alpha = 0.35f),
                            radius = 16.dp.toPx(),
                            center = Offset(vehicleX, vehicleY)
                        )
                        drawCircle(
                            color = Color(0xFF0284C7),
                            radius = 7.dp.toPx(),
                            center = Offset(vehicleX, vehicleY)
                        )
                    }
                }

                // Interactive tap overlay buttons for waypoints
                waypoints.forEachIndexed { index, wp ->
                    Box(
                        modifier = Modifier
                            .offset(
                                x = (wp.coordinates.first * 320).dp - 20.dp,
                                y = (wp.coordinates.second * 170).dp - 20.dp
                            )
                            .size(40.dp)
                            .clickable { selectedWaypointIndex = index }
                    )
                }

                // Stage labels chip strip at top of map
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    waypoints.forEachIndexed { index, wp ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedWaypointIndex == index) colors.primary else colors.surface.copy(alpha = 0.75f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.glassBorder),
                            modifier = Modifier.clickable { selectedWaypointIndex = index }
                        ) {
                            Text(
                                text = "S${wp.stageNumber}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedWaypointIndex == index) Color.White else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Waypoint Inspector Card
            val selected = waypoints[selectedWaypointIndex]
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.glassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = selected.icon,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Stage ${selected.stageNumber}: ${selected.title}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = selected.timestamp,
                                fontSize = 10.sp,
                                color = colors.textMuted
                            )
                        }
                        Text(
                            text = "${selected.facilityName} • ${selected.location}",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            maxLines = 1
                        )
                        Text(
                            text = selected.details,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Real-Time Cold-Chain IoT Telematics Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (colors.isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.glassBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TelematicsItem(
                        icon = Icons.Default.Thermostat,
                        label = "REEFER TEMP",
                        value = "${currentTemperature}°C",
                        color = if (currentTemperature in 15.0..22.0) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                    TelematicsItem(
                        icon = Icons.Default.WaterDrop,
                        label = "HUMIDITY",
                        value = "${currentHumidity}%",
                        color = Color(0xFF38BDF8)
                    )
                    TelematicsItem(
                        icon = Icons.Default.Speed,
                        label = "FLEET SPEED",
                        value = "64 km/h",
                        color = colors.textPrimary
                    )
                    TelematicsItem(
                        icon = Icons.Default.GpsFixed,
                        label = "GEO-FENCE",
                        value = "LOCKED",
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelematicsItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    val colors = FloraChainTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
        }
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = color)
    }
}
