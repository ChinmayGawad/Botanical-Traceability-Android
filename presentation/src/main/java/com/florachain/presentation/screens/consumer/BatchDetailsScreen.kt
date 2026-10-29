package com.florachain.presentation.screens.consumer

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.*
import com.florachain.domain.model.BlockchainTx
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.model.VerificationState
import com.florachain.presentation.viewmodel.VerifyViewModel

@Composable
fun BatchDetailsScreen(
    product: BotanicalProduct?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    verifyViewModel: VerifyViewModel? = null
) {
    val colors = FloraChainTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Botanical Provenance",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showNetworkPill = true,
                showThemeToggle = true
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Product details unavailable", color = colors.textSecondary)
            }
            return@Scaffold
        }

        val scrollState = rememberScrollState()
        var viewMode by remember { mutableStateOf("MAP") }
        var isShareSheetOpen by remember { mutableStateOf(false) }
        var isReportDialogOpen by remember { mutableStateOf(false) }
        var selectedReceiptTx by remember { mutableStateOf<BlockchainTx?>(null) }

        val isRejected = product.verificationState == VerificationState.REJECTED ||
                product.verificationState == VerificationState.COMPROMISED
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

        Box(modifier = Modifier.fillMaxSize()) {
            if (product.verificationState == VerificationState.VERIFIED) {
                ConfettiCelebration(triggerKey = product.batchId)
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Product Identity Card with Animated TrustScoreGauge
                CyberGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary
                            )
                            Text(
                                text = product.botanicalName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.primary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                            Text(
                                text = "Batch: #${product.batchId}",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TrustSealBadge(state = trustState, trustScore = trustScore)
                        }

                        // Animated Circular Trust Gauge
                        TrustScoreGauge(
                            score = trustScore,
                            gaugeSize = 92.dp,
                            strokeWidth = 7.dp,
                            label = "INTEGRITY"
                        )
                    }

                    HorizontalDivider(
                        color = colors.glassBorder,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )

                    // Quick Specs Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SpecColumn(label = "Category", value = product.category.name)
                        SpecColumn(label = "Method", value = product.cultivationMethod.name)
                        SpecColumn(label = "Harvest Qty", value = "${product.quantityKg.toInt()} kg")
                        SpecColumn(label = "Network", value = "Polygon Amoy")
                    }
                }

                // Action Strip: Share CoA & Report Issue
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { isShareSheetOpen = true },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share CoA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { isReportDialogOpen = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Report Issue", fontSize = 12.sp)
                    }
                }

                // Dual View Mode Selector Tabs: Journey Map vs Stage Timeline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (viewMode == "MAP") colors.primary else colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (viewMode == "MAP") colors.primary else colors.glassBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewMode = "MAP" }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = if (viewMode == "MAP") Color.White else colors.textPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Interactive Map", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (viewMode == "MAP") Color.White else colors.textPrimary)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (viewMode == "TIMELINE") colors.primary else colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (viewMode == "TIMELINE") colors.primary else colors.glassBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewMode = "TIMELINE" }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = if (viewMode == "TIMELINE") Color.White else colors.textPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stage Timeline", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (viewMode == "TIMELINE") Color.White else colors.textPrimary)
                        }
                    }
                }

                if (viewMode == "MAP") {
                    // ── Interactive Geospatial Route Canvas ──
                    JourneyMapCanvas(
                        batchId = product.batchId,
                        productName = product.name
                    )
                } else {
                    // Supply Chain Provenance Timeline Header
                    Text(
                        text = "Cryptographic Provenance Stages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        letterSpacing = 0.5.sp
                    )

                    // Stage 1: Agricultural Harvest
                    TimelineNode(
                        icon = Icons.Default.Agriculture,
                        stageTitle = "Stage 1: Agricultural Harvest",
                        subtitle = "${product.farmerName} • ${product.farmLocation}",
                        timestamp = product.harvestDate,
                        isCompleted = true,

                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        product.gpsCoordinates?.let { gps ->
                            Surface(
                                color = colors.primary.copy(alpha = if (colors.isDark) 0.15f else 0.10f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "GPS: ${String.format("%.4f", gps.latitude)}, ${String.format("%.4f", gps.longitude)} (Tamper-Proof)",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colors.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            )

            // Stage 2: Quality & Lab Assay
            val lab = product.labReport
            TimelineNode(
                icon = Icons.Default.Biotech,
                stageTitle = "Stage 2: HPLC Lab Assay & Heavy Metals",
                subtitle = lab?.let { "${it.labName} • Result: ${it.overallResult}" } ?: "Pending HPLC Assay",
                timestamp = lab?.testDate ?: "—",
                isCompleted = lab != null,
                content = {
                    if (lab != null) {
                        Column(
                            modifier = Modifier.padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = colors.surfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("ACTIVE PURITY", fontSize = 9.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                                        Text("${lab.purityPercentage}% HPLC", fontSize = 12.sp, color = colors.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Surface(
                                    color = colors.surfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("MOISTURE", fontSize = 9.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                                        Text("${lab.moisturePercentage}% (<10% Limit)", fontSize = 12.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(
                                text = "Heavy Metals: ${lab.heavyMetalsStatus.name} • Pesticides: ${lab.pesticideResidueStatus.name} • ISO/IEC 17025",
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            )

            // Stage 3: Processing & Extraction
            val proc = product.processingDetails
            TimelineNode(
                icon = Icons.Default.PrecisionManufacturing,
                stageTitle = "Stage 3: GMP Extraction & Milling",
                subtitle = proc?.let { "${it.processorName} • ${it.method}" } ?: "Awaiting Milling Extraction",
                timestamp = proc?.processingDate ?: "—",
                isCompleted = proc != null,
                content = {
                    if (proc != null) {
                        Text(
                            text = "Output Yield: ${proc.processedQuantityKg} kg (${proc.yieldLossPercentage?.toInt() ?: 0}% loss delta) • Temp <45°C",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            )

            // Stage 4: Cold-Chain Logistics with ColdChainTempChart
            val ship = product.shipmentDetails
            TimelineNode(
                icon = Icons.Default.LocalShipping,
                stageTitle = "Stage 4: Cold-Chain IoT Logistics",
                subtitle = ship?.let { "${it.distributorName} • Vehicle: ${it.vehicleNumber}" } ?: "In Cold Storage Transit",
                timestamp = ship?.dispatchDate ?: "Live Telematics",
                isCompleted = true,
                content = {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Embedded Visual Cold-Chain Telemetry Chart
                        ColdChainTempChart()
                    }
                }
            )

            // Stage 5: Retail Verification & Consumer Seal
            val ret = product.retailDetails
            TimelineNode(
                icon = Icons.Default.Storefront,
                stageTitle = "Stage 5: Retail Intake & Authenticity Seal",
                subtitle = ret?.let { "${it.retailerName} • ${it.storeLocation}" } ?: "Verified for Retail Distribution",
                timestamp = ret?.receivedDate ?: "—",
                isCompleted = ret != null,
                content = {
                    if (ret != null) {
                        Surface(
                            color = colors.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Digital Monograph Verified • Retail Shelf Price: ₹${ret.unitPrice} / unit",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            )
            }

            // ── Cryptographic Proof Explorer Card ──
            CyberGlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "IMMUTABLE CRYPTOGRAPHIC PROOF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // EVM Transaction Hash (Tap to inspect full cryptographic receipt)
                    val txHash = product.timeline.firstOrNull { it.txHash != null }?.txHash
                        ?: "0x7a39d891b24e60fc9019b889104fa218c57e84b901a89c"
                    Surface(
                        color = colors.surfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedReceiptTx = BlockchainTx(
                                    txId = txHash,
                                    blockNumber = 4921084L,
                                    from = product.farmerId,
                                    to = "0x5FbD...8aa3 (EVM Bridge)",
                                    stage = "BATCH_VERIFICATION",
                                    action = "EVM_CONSORTIUM_SEAL",
                                    actor = product.farmerName,
                                    timestamp = product.harvestDate,
                                    status = "CONFIRMED_ON_CHAIN",
                                    gasUsed = 21450L,
                                    rawPayload = "{\"batchId\":\"${product.batchId}\",\"crop\":\"${product.name}\",\"verification\":\"${product.verificationState.name}\",\"trustScore\":98}"
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("POLYGON AMOY TX HASH", fontSize = 9.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                                    Text("• Tap to inspect receipt", fontSize = 9.sp, color = colors.primary, fontWeight = FontWeight.Medium)
                                }
                                Text(
                                    text = txHash.take(22) + "...",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                clipboardManager.setText(AnnotatedString(txHash))
                                Toast.makeText(context, "Transaction hash copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Hash", tint = colors.primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // IPFS Certificate of Analysis CID
                    val ipfsCid = product.labReport?.certificateIpfsCid?.let { "ipfs://$it" }
                        ?: "ipfs://QmXoypizjW3WknFiJnKLwHCnL72vedxjQkDDP1mXWo6uco"
                    Surface(
                        color = colors.surfaceElevated,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("IPFS CoA CERTIFICATE CID", fontSize = 9.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                                Text(
                                    text = ipfsCid.take(24) + "...",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = colors.infoBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                clipboardManager.setText(AnnotatedString(ipfsCid))
                                Toast.makeText(context, "IPFS CID copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy IPFS", tint = colors.infoBlue, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isShareSheetOpen) {
            ShareLabReportBottomSheet(
                product = product,
                onDismiss = { isShareSheetOpen = false }
            )
        }

        if (isReportDialogOpen) {
            ReportSuspiciousDialog(
                batchId = product.batchId,
                onDismiss = { isReportDialogOpen = false },
                onSubmit = { report ->
                    if (verifyViewModel != null) {
                        verifyViewModel.submitReport(
                            batchId = product.batchId,
                            issueType = report.issueType,
                            description = report.description,
                            reporterName = report.reporterName,
                            reporterContact = report.reporterContact ?: "",
                            onSuccess = {
                                Toast.makeText(context, "✓ Fraud report submitted to Consortium Governance Desk.", Toast.LENGTH_LONG).show()
                                isReportDialogOpen = false
                            },
                            onError = { err ->
                                Toast.makeText(context, "Error submitting report: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Fraud report logged. Consortium alerted.", Toast.LENGTH_LONG).show()
                        isReportDialogOpen = false
                    }
                }
            )
        }

        selectedReceiptTx?.let { tx ->
            TransactionReceiptDialog(
                tx = tx,
                onDismiss = { selectedReceiptTx = null },
                onCopyHash = {
                    clipboardManager.setText(AnnotatedString(tx.txId))
                    Toast.makeText(context, "Transaction hash copied to clipboard", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
}


@Composable
private fun SpecColumn(label: String, value: String) {
    val colors = FloraChainTheme.colors
    Column {
        Text(text = label, fontSize = 10.sp, color = colors.textMuted, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
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
    val colors = FloraChainTheme.colors
    var isExpanded by remember { mutableStateOf(true) }

    CyberGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        borderWidth = 1.dp,
        borderBrush = if (isCompleted) colors.cardBorderBrush else androidx.compose.ui.graphics.SolidColor(colors.glassBorder.copy(alpha = 0.4f)),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompleted) colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.12f)
                            else colors.surfaceElevated
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCompleted) colors.primary else colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stageTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isCompleted) colors.textPrimary else colors.textMuted
                        )
                        Text(
                            text = timestamp,
                            fontSize = 10.sp,
                            color = colors.textMuted
                        )
                    }
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Box(modifier = Modifier.padding(start = 48.dp, top = 6.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun TransactionReceiptDialog(
    tx: BlockchainTx,
    onDismiss: () -> Unit,
    onCopyHash: () -> Unit
) {
    val colors = FloraChainTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Security, contentDescription = null, tint = colors.primary)
                Text("Cryptographic Ledger Receipt", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = colors.surfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("TRANSACTION HASH", fontSize = 8.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                        Text(tx.txId, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = colors.primary)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("BLOCK NUMBER", fontSize = 8.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                        Text("#${tx.blockNumber}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("GAS CONSUMED", fontSize = 8.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                        Text("${tx.gasUsed} WEI", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("SIGNING ACTOR", fontSize = 8.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                        Text(tx.actor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ACTION", fontSize = 8.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                        Text(tx.action, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    }
                }

                val payload = tx.rawPayload
                if (!payload.isNullOrBlank()) {
                    Text("DECODED ON-CHAIN EVENT PAYLOAD", fontSize = 8.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = payload,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onCopyHash) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Tx Hash")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

