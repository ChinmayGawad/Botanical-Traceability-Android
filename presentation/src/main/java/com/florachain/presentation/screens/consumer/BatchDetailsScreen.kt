package com.florachain.presentation.screens.consumer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.designsystem.components.TrustSealBadge
import com.florachain.designsystem.components.TrustState
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.model.VerificationState

@Composable
fun BatchDetailsScreen(
    product: BotanicalProduct?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = "Botanical Provenance",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = DeepSlate
    ) { paddingValues ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Product details unavailable", color = TextSecondary)
            }
            return@Scaffold
        }

        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Product Identity Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = product.botanicalName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = MintAccent
                        )
                        Text(
                            text = "Batch ID: ${product.batchId}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    val isRejected = product.verificationState == VerificationState.REJECTED || product.verificationState == VerificationState.COMPROMISED
                    val trustState = when {
                        product.isLocalPending -> TrustState.PENDING_SYNC
                        product.verificationState == VerificationState.VERIFIED -> TrustState.VERIFIED_ON_CHAIN
                        isRejected -> TrustState.COMPROMISED
                        else -> TrustState.OFFLINE_RECORDED
                    }
                    val trustScore = when {
                        isRejected -> 15
                        product.verificationState == VerificationState.VERIFIED -> 98
                        else -> 75
                    }
                    TrustSealBadge(state = trustState, trustScore = trustScore)
                }

                HorizontalDivider(
                    color = GlassBorder,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // Quick Specs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpecColumn(label = "Category", value = product.category.name)
                    SpecColumn(label = "Method", value = product.cultivationMethod.name)
                    SpecColumn(label = "Quantity", value = "${product.quantityKg} kg")
                }
            }

            // Supply Chain Provenance Timeline
            Text(
                text = "Cryptographic Provenance Stages",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )

            // Stage 1: Agricultural Harvest
            TimelineNode(
                icon = Icons.Default.Agriculture,
                stageTitle = "Stage 1: Agricultural Harvest",
                subtitle = "${product.farmerName} • ${product.farmLocation}",
                timestamp = product.harvestDate,
                isCompleted = true,
                content = {
                    product.gpsCoordinates?.let { gps ->
                        Text(
                            text = "Verified Farm GPS: ${gps.latitude}, ${gps.longitude}",
                            fontSize = 12.sp,
                            color = BotanicalEmerald,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            )

            // Stage 2: Quality & Lab Assay
            val lab = product.labReport
            TimelineNode(
                icon = Icons.Default.Science,
                stageTitle = "Stage 2: Laboratory Assay & Purity",
                subtitle = lab?.let { "${it.labName} • Result: ${it.overallResult}" } ?: "Pending Lab Analysis",
                timestamp = lab?.testDate ?: "—",
                isCompleted = lab != null,
                content = {
                    if (lab != null) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(
                                text = "Purity: ${lab.purityPercentage}% • Moisture: ${lab.moisturePercentage}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrustGreen
                            )
                            Text(
                                text = "Heavy Metals: ${lab.heavyMetalsStatus.name} • Pesticides: ${lab.pesticideResidueStatus.name}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            )

            // Stage 3: Processing & Extraction
            val proc = product.processingDetails
            TimelineNode(
                icon = Icons.Default.PrecisionManufacturing,
                stageTitle = "Stage 3: Botanical Extraction",
                subtitle = proc?.let { "${it.processorName} • ${it.method}" } ?: "Awaiting Extraction",
                timestamp = proc?.processingDate ?: "—",
                isCompleted = proc != null,
                content = {
                    if (proc != null) {
                        Text(
                            text = "Output Yield: ${proc.processedQuantityKg} kg (${proc.yieldLossPercentage?.toInt() ?: 0}% loss)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            )

            // Stage 4: Cold-Chain Logistics
            val ship = product.shipmentDetails
            TimelineNode(
                icon = Icons.Default.LocalShipping,
                stageTitle = "Stage 4: Cold-Chain Logistics",
                subtitle = ship?.let { "${it.distributorName} • Vehicle: ${it.vehicleNumber}" } ?: "Awaiting Dispatch",
                timestamp = ship?.dispatchDate ?: "—",
                isCompleted = ship != null,
                content = {
                    if (ship != null) {
                        Text(
                            text = "Temp Range: ${ship.temperatureRange} • Status: ${ship.status}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            )

            // Stage 5: Retail Verification
            val ret = product.retailDetails
            TimelineNode(
                icon = Icons.Default.Storefront,
                stageTitle = "Stage 5: Retail Intake & Consumer Seal",
                subtitle = ret?.let { "${it.retailerName} • ${it.storeLocation}" } ?: "In Transit to Retailer",
                timestamp = ret?.receivedDate ?: "—",
                isCompleted = ret != null,
                content = {
                    if (ret != null) {
                        Text(
                            text = "Retail Shelf Price: ₹${ret.unitPrice} / unit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintAccent
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun SpecColumn(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

@Composable
private fun TimelineNode(
    icon: ImageVector,
    stageTitle: String,
    subtitle: String,
    timestamp: String,
    isCompleted: Boolean,
    content: @Composable () -> Unit = {}
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isCompleted) GlassBorder else Color(0x1A94A3B8)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isCompleted) BotanicalDark else SurfaceDarkElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCompleted) BotanicalEmerald else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stageTitle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isCompleted) TextPrimary else TextMuted
                    )
                    Text(
                        text = timestamp,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                content()
            }
        }
    }
}
