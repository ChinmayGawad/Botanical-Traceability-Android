package com.florachain.presentation.screens.distributor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.domain.model.TransportType
import com.florachain.presentation.viewmodel.DistributorViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatchShipmentScreen(
    viewModel: DistributorViewModel,
    distributorId: String,
    distributorName: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var vehicleNumber by remember { mutableStateOf("MP-09-CX-4521") }
    var trackingNumber by remember { mutableStateOf("TRK-" + UUID.randomUUID().toString().take(8).uppercase()) }
    var sourceLocation by remember { mutableStateOf("Indore Central Cold Warehouse") }
    var destinationLocation by remember { mutableStateOf("New Delhi AYUSH Pharma Hub") }
    var temperatureRange by remember { mutableStateOf("2°C - 8°C (Strict Cold-Chain)") }
    var transportType by remember { mutableStateOf(TransportType.REFRIGERATED_TRUCK) }
    var transportExpanded by remember { mutableStateOf(false) }

    val colors = FloraChainTheme.colors

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Distributor Logistics Command",
                showThemeToggle = true,
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = colors.errorRose)
                    }
                }
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = colors.surfaceElevated,
                contentColor = colors.primary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Dispatch Consignment", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Active Fleet Transit", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Route, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (selectedTabIndex == 0) {
                // Batch Selection Card
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select Botanical Batch for Transit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (uiState.selectedProduct != null) {
                        val prod = uiState.selectedProduct!!
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(prod.name, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("Batch: ${prod.batchId} • Weight: ${prod.quantityKg} kg", fontSize = 12.sp, color = colors.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    uiState.products.take(3).forEach { p ->
                        val isSelected = uiState.selectedProduct?.id == p.id
                        Button(
                            onClick = { viewModel.selectProduct(p) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) colors.primary else colors.surfaceElevated,
                                contentColor = if (isSelected) colors.onPrimary else colors.textPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "${p.name} (${p.batchId})",
                                color = if (isSelected) colors.onPrimary else colors.textPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Waybill & Telemetry Card
                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Cold-Chain Transit Telemetry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("Vehicle License Number") },
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = trackingNumber,
                        onValueChange = { trackingNumber = it },
                        label = { Text("Waybill / Airway Bill Tracking Number") },
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = temperatureRange,
                        onValueChange = { temperatureRange = it },
                        label = { Text("Temperature Threshold Telemetry") },
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = sourceLocation,
                        onValueChange = { sourceLocation = it },
                        label = { Text("Source Dispatch Warehouse") },
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = destinationLocation,
                        onValueChange = { destinationLocation = it },
                        label = { Text("Destination Delivery Point") },
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (uiState.isSuccess) {
                    Text(
                        text = "✓ Waybill generated and shipment committed to blockchain!",
                        color = TrustGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                uiState.error?.let { err ->
                    Text(text = err, color = ErrorRose, fontSize = 13.sp)
                }

                // Dispatch Button
                Button(
                    onClick = {
                        viewModel.dispatchShipment(
                            distributorId = distributorId.ifBlank { "DIST-001" },
                            distributorName = distributorName.ifBlank { "FloraLogistics Express" },
                            sourceLocation = sourceLocation,
                            destinationLocation = destinationLocation,
                            vehicleNumber = vehicleNumber,
                            transportType = transportType,
                            temperatureRange = temperatureRange,
                            trackingNumber = trackingNumber.ifBlank { null }
                        )
                    },
                    enabled = uiState.selectedProduct != null && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(22.dp))
                    } else {
                        Text("Dispatch Cold-Chain Shipment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            } else {
                // Active Transit Shipments Tab
                val allShipments = uiState.products.mapNotNull { prod ->
                    prod.shipmentDetails?.let { Pair(prod, it) }
                }

                if (allShipments.isEmpty()) {
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No active consignments logged in system yet.",
                            color = colors.textMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    allShipments.forEach { (prod, shipment) ->
                        val isDelivered = shipment.status.equals("DELIVERED", ignoreCase = true)
                        GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Batch: ${prod.batchId}",
                                        fontSize = 12.sp,
                                        color = colors.primary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Surface(
                                    color = if (isDelivered) TrustGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = shipment.status,
                                        color = if (isDelivered) TrustGreen else WarningAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Waybill: ${shipment.trackingNumber ?: shipment.shipmentId}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "Route: ${shipment.sourceLocation} → ${shipment.destinationLocation}",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                            Text(
                                text = "Vehicle: ${shipment.vehicleNumber} • Temp: ${shipment.temperatureRange}",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )

                            if (!isDelivered) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.markDelivered(shipment.shipmentId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TrustGreen,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Confirm Delivery & Sign Ledger (DELIVERED)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
