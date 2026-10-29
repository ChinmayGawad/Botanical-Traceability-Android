package com.florachain.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class FleetWaypoint(
    val title: String,
    val location: String,
    val x: Float, // Normalized 0..1
    val y: Float, // Normalized 0..1
    val checkpointName: String,
    val stageStatus: String
)

data class FleetBatch(
    val batchId: String,
    val productName: String,
    val botanicalName: String,
    val origin: String,
    val destination: String,
    val truckPlate: String,
    val driverName: String,
    val driverContact: String,
    val cargoWeightKg: Double,
    val targetTempRange: String,
    val baseTemp: Double,
    val baseHumidity: Int,
    val totalDistanceKm: Int,
    val waypoints: List<FleetWaypoint>
)

data class FleetCommandUiState(
    val batches: List<FleetBatch> = emptyList(),
    val selectedBatch: FleetBatch,
    val scrubberProgress: Float = 0.58f, // 0.0 to 1.0
    val isPlaying: Boolean = true,
    val simulationSpeed: Float = 1.0f, // 1x, 2x, 5x
    val currentTemp: Double = 4.2,
    val currentHumidity: Int = 52,
    val currentSpeedKmh: Int = 62,
    val currentGForce: Double = 0.08,
    val truckX: Float = 0.5f,
    val truckY: Float = 0.5f,
    val truckHeadingDegrees: Float = 45f,
    val batteryPercent: Int = 94,
    val activeSatelliteCount: Int = 12,
    val blockAnchorTx: String = "0x8f2d...41a9"
)

@HiltViewModel
class FleetCommandViewModel @Inject constructor() : ViewModel() {

    private val availableBatches = listOf(
        FleetBatch(
            batchId = "BOT-2024-8901",
            productName = "Organic Ashwagandha Extract",
            botanicalName = "Withania somnifera",
            origin = "Neemuch, Madhya Pradesh",
            destination = "AYUSH Pharma Terminal, New Delhi",
            truckPlate = "MP-09-CX-4521",
            driverName = "Rajesh Gurjar (Verified CDL)",
            driverContact = "+91 98260 11492",
            cargoWeightKg = 480.0,
            targetTempRange = "2°C - 8°C (Cold-Chain)",
            baseTemp = 4.2,
            baseHumidity = 52,
            totalDistanceKm = 640,
            waypoints = listOf(
                FleetWaypoint("Origin Farm", "Neemuch, MP", 0.15f, 0.75f, "Malwa Organic Cluster", "DEPARTED"),
                FleetWaypoint("Processing Hub", "Indore, MP", 0.35f, 0.65f, "BioExtracts GMP Plant", "COMPLETED"),
                FleetWaypoint("Highway Waypoint", "Gwalior Bypass, MP", 0.60f, 0.45f, "NH-44 Checkpost", "IN_TRANSIT"),
                FleetWaypoint("Destination Hub", "New Delhi AYUSH Terminal", 0.85f, 0.20f, "Central Pharma Depot", "ETA 4h 20m")
            )
        ),
        FleetBatch(
            batchId = "BOT-2024-4412",
            productName = "High-Curcumin Turmeric Rhizomes",
            botanicalName = "Curcuma longa (95% Active)",
            origin = "Erode, Tamil Nadu",
            destination = "JNPT Sea Port, Mumbai",
            truckPlate = "TN-33-AA-9810",
            driverName = "S. Murugan (Master Reefer)",
            driverContact = "+91 94432 88201",
            cargoWeightKg = 1250.0,
            targetTempRange = "15°C - 22°C (Dry Ambient)",
            baseTemp = 18.5,
            baseHumidity = 48,
            totalDistanceKm = 1120,
            waypoints = listOf(
                FleetWaypoint("Farm Origin", "Erode Agricultural Coop", 0.20f, 0.85f, "Kalingarayan Basin", "DEPARTED"),
                FleetWaypoint("Quality QA Lab", "Salem Testing Facility", 0.40f, 0.65f, "AYUSH Phytochem Lab", "COMPLETED"),
                FleetWaypoint("Transit Check", "Pune Express Corridor", 0.65f, 0.40f, "Maharashtra Toll Gate 4", "IN_TRANSIT"),
                FleetWaypoint("Export Terminal", "JNPT Port Container Yard", 0.82f, 0.22f, "Customs Bonded Area", "ETA 8h 15m")
            )
        ),
        FleetBatch(
            batchId = "BOT-2024-1109",
            productName = "Krishna Holy Tulsi Leaves",
            botanicalName = "Ocimum tenuiflorum",
            origin = "Haridwar Foothills, Uttarakhand",
            destination = "Electronic City AYUSH Hub, Bengaluru",
            truckPlate = "UK-07-TR-6543",
            driverName = "Devendra Negi (Lead Logistics)",
            driverContact = "+91 97580 33491",
            cargoWeightKg = 320.0,
            targetTempRange = "4°C - 10°C (Cold-Chain)",
            baseTemp = 5.8,
            baseHumidity = 55,
            totalDistanceKm = 2150,
            waypoints = listOf(
                FleetWaypoint("Foothill Harvest", "Haridwar Himalayan Base", 0.25f, 0.20f, "Ganga Basin Organic", "DEPARTED"),
                FleetWaypoint("Extraction Unit", "Roorkee Phytocare", 0.42f, 0.40f, "Solvent Extraction Lab", "COMPLETED"),
                FleetWaypoint("Central Transit", "Nagpur Cold Hub", 0.60f, 0.60f, "Central Cold-Chain Grid", "IN_TRANSIT"),
                FleetWaypoint("Delivery Depot", "Bengaluru Tech Park Terminal", 0.78f, 0.85f, "Retail Distribution", "ETA 14h")
            )
        )
    )

    private val _uiState = MutableStateFlow(
        FleetCommandUiState(
            batches = availableBatches,
            selectedBatch = availableBatches[0]
        )
    )
    val uiState: StateFlow<FleetCommandUiState> = _uiState.asStateFlow()

    private var simulationJob: Job? = null

    init {
        updatePositionAndTelemetry(_uiState.value.scrubberProgress)
        startSimulation()
    }

    fun selectBatch(batchId: String) {
        val found = availableBatches.find { it.batchId == batchId } ?: return
        _uiState.update { it.copy(selectedBatch = found) }
        updatePositionAndTelemetry(_uiState.value.scrubberProgress)
    }

    fun setScrubberProgress(progress: Float) {
        val clamped = progress.coerceIn(0f, 1f)
        _uiState.update { it.copy(scrubberProgress = clamped) }
        updatePositionAndTelemetry(clamped)
    }

    fun togglePlayPause() {
        val nextState = !_uiState.value.isPlaying
        _uiState.update { it.copy(isPlaying = nextState) }
        if (nextState) {
            startSimulation()
        } else {
            simulationJob?.cancel()
        }
    }

    fun setSimulationSpeed(speed: Float) {
        _uiState.update { it.copy(simulationSpeed = speed) }
    }

    fun resetToStart() {
        setScrubberProgress(0f)
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(60L)
                if (_uiState.value.isPlaying) {
                    val current = _uiState.value.scrubberProgress
                    val speed = _uiState.value.simulationSpeed
                    val delta = 0.0018f * speed
                    var next = current + delta
                    if (next > 1.0f) {
                        next = 0.0f
                    }
                    _uiState.update { it.copy(scrubberProgress = next) }
                    updatePositionAndTelemetry(next)
                }
            }
        }
    }

    private fun updatePositionAndTelemetry(progress: Float) {
        val batch = _uiState.value.selectedBatch
        val waypoints = batch.waypoints
        if (waypoints.size < 2) return

        // Segment calculation
        val totalSegments = waypoints.size - 1
        val scaledProgress = progress * totalSegments
        val segmentIndex = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
        val segmentFraction = (scaledProgress - segmentIndex).coerceIn(0f, 1f)

        val p1 = waypoints[segmentIndex]
        val p2 = waypoints[segmentIndex + 1]

        val curX = p1.x + (p2.x - p1.x) * segmentFraction
        val curY = p1.y + (p2.y - p1.y) * segmentFraction

        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        val angleRad = atan2(dy, dx)
        val headingDegrees = Math.toDegrees(angleRad.toDouble()).toFloat()

        // Slight micro-variations for realistic live sensor readout
        val tempNoise = sin(progress * 15.0) * 0.35
        val humNoise = (cos(progress * 12.0) * 3).toInt()
        val speedCalc = (55 + sin(progress * 20.0) * 8).toInt()
        val gForceCalc = (0.07 + (sin(progress * 35.0) * 0.03)).coerceAtLeast(0.04)

        _uiState.update {
            it.copy(
                truckX = curX,
                truckY = curY,
                truckHeadingDegrees = headingDegrees,
                currentTemp = String.format(java.util.Locale.US, "%.1f", batch.baseTemp + tempNoise).toDouble(),
                currentHumidity = batch.baseHumidity + humNoise,
                currentSpeedKmh = speedCalc,
                currentGForce = String.format(java.util.Locale.US, "%.2f", gForceCalc).toDouble()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
