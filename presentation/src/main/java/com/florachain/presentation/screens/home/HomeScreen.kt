package com.florachain.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.model.VerificationState
import com.florachain.presentation.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToPortal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchInput by remember { mutableStateOf("") }
    var isBatchesExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "FloraChain",
                actions = {
                    Surface(
                        color = BotanicalEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, BotanicalEmerald.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(BotanicalEmerald, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Polygon Amoy",
                                color = BotanicalEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = BotanicalEmerald
                        )
                    }
                }
            )
        },
        containerColor = DeepSlate
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Hero Section ──
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = BotanicalDark.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BotanicalEmerald.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "DECENTRALIZED BOTANICAL INTEGRITY",
                        color = MintAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Soil-to-Shelf\nTraceability Platform",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    lineHeight = 32.sp
                )

                Text(
                    text = "Verify botanical harvest authenticity, active compound HPLC purity assays, and cold-chain IoT telemetry immutably recorded on blockchain.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }

            // ── Search & Scan Bar ──
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        placeholder = { Text("Search Batch (e.g. BOT-2024-8901)", fontSize = 13.sp, color = TextMuted) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = BotanicalEmerald)
                        },
                        trailingIcon = {
                            if (searchInput.isNotBlank()) {
                                IconButton(onClick = {
                                    onNavigateToDetails(searchInput.trim())
                                }) {
                                    Icon(Icons.Default.ArrowForward, contentDescription = "Search", tint = BotanicalEmerald)
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = BotanicalEmerald,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (searchInput.isNotBlank()) {
                                    onNavigateToDetails(searchInput.trim())
                                } else {
                                    onNavigateToDetails("BOT-2024-8901")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BotanicalEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = DeepSlate, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verify Batch", color = DeepSlate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onNavigateToScan,
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(MintAccent)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MintAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan QR", color = MintAccent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Live Provenance Featured Card ──
            val featured = uiState.featuredProduct
            if (featured != null) {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDetails(featured.id) },
                    borderColor = BotanicalEmerald.copy(alpha = 0.5f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = BotanicalDark,
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, BotanicalEmerald.copy(alpha = 0.6f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(BotanicalEmerald, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE PROVENANCE",
                                        color = BotanicalEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "#" + featured.batchId,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = featured.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = featured.botanicalName + " (${featured.category.name})",
                                fontSize = 12.sp,
                                color = TextMuted,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        // Data tiles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = SurfaceDark,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("ACTIVE PURITY", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                    Text("99.5% Withanolides", fontSize = 12.sp, color = BotanicalEmerald, fontWeight = FontWeight.Bold)
                                }
                            }

                            Surface(
                                color = SurfaceDark,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("FARM LOCATION", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                    Text(featured.farmLocation.take(16), fontSize = 12.sp, color = InfoBlue, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // 5-Node Consensus Strip
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("5-NODE BLOCKCHAIN CONSENSUS", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Farm", "Mill", "Lab", "Cold", "Retail").forEach { node ->
                                    Surface(
                                        color = BotanicalDark.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, BotanicalEmerald.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BotanicalEmerald, modifier = Modifier.size(12.dp))
                                            Text(node, fontSize = 9.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { onNavigateToDetails(featured.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = BotanicalEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Text("Inspect Full Provenance Journey", color = DeepSlate, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = DeepSlate, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Ledger Blockchain Metrics Strip ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NETWORK PROTOCOL METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCardItem(
                        icon = Icons.Default.Eco,
                        title = "${uiState.products.size}+ Batches",
                        label = "Synchronized",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCardItem(
                        icon = Icons.Default.Shield,
                        title = "100%",
                        label = "Tamper-Proof",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCardItem(
                        icon = Icons.Default.Hub,
                        title = "5/5 Nodes",
                        label = "Active Consensus",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCardItem(
                        icon = Icons.Default.Lock,
                        title = "#1,048,291",
                        label = "Block Height",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── 5 Supply Chain Traceability Pipeline Stages ──
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "CRYPTOGRAPHIC SUPPLY CHAIN PIPELINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )

                val stages = listOf(
                    StageInfo("01", "Farmer Genesis", "GPS Geotagging, Harvest Timestamps & NPOP Organic CID", Icons.Default.Agriculture),
                    StageInfo("02", "Processor Milling", "Milling <45°C, Mass Balance Delta & GMP Audit Hash", Icons.Default.PrecisionManufacturing),
                    StageInfo("03", "Laboratory Assay", "HPLC Active Purity, Heavy Metal ICP-MS & ISO/IEC 17025", Icons.Default.Biotech),
                    StageInfo("04", "Distributor IoT", "Cold-Chain Live Telematics (2-8°C) & Tamper Seal ID", Icons.Default.LocalShipping),
                    StageInfo("05", "Retailer Monograph", "Digital Authenticity Monograph & Consumer Trust Scan", Icons.Default.Storefront)
                )

                stages.forEach { stage ->
                    Surface(
                        color = SurfaceDark.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(BotanicalDark, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(stage.icon, contentDescription = null, tint = BotanicalEmerald, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(stage.step + " • ", color = MintAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(stage.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(stage.desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }

            // ── Recent Botanical Batches (Compact View) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "VERIFIED BOTANICAL BATCHES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 1.sp
                        )

                        Surface(
                            color = BotanicalEmerald.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${uiState.products.size} on-chain",
                                fontSize = 10.sp,
                                color = BotanicalEmerald,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (uiState.products.size > 3) {
                        Surface(
                            color = SurfaceDarkElevated.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier.clickable { isBatchesExpanded = !isBatchesExpanded }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isBatchesExpanded) "Show Less" else "View All (${uiState.products.size})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MintAccent
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = if (isBatchesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MintAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                val displayedBatches = if (isBatchesExpanded) uiState.products else uiState.products.take(3)

                if (displayedBatches.isEmpty()) {
                    Surface(
                        color = SurfaceDark.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No botanical batches recorded yet.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    displayedBatches.forEach { prod ->
                        CompactBatchCard(
                            product = prod,
                            onClick = { onNavigateToDetails(prod.id) }
                        )
                    }
                }
            }

            // ── Stakeholder Field Portal Callout ──
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToPortal() }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(BotanicalDark, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = null, tint = BotanicalEmerald, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AYUSH Stakeholder Portal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Sign in as Farmer, Processor, QA Lab, Logistics, or Retailer", fontSize = 11.sp, color = TextSecondary)
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BotanicalEmerald, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class StageInfo(val step: String, val title: String, val desc: String, val icon: ImageVector)

@Composable
private fun MetricCardItem(
    icon: ImageVector,
    title: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDark.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(BotanicalDark, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = BotanicalEmerald, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(label, fontSize = 10.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun CompactBatchCard(
    product: BotanicalProduct,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRejected = product.verificationState == VerificationState.REJECTED
    Surface(
        color = SurfaceDark.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isRejected) ErrorRose.copy(alpha = 0.5f) else GlassBorder),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isRejected) ErrorRose.copy(alpha = 0.15f) else BotanicalDark,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRejected) Icons.Default.Warning else Icons.Default.Eco,
                    contentDescription = null,
                    tint = if (isRejected) ErrorRose else BotanicalEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = product.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Surface(
                        color = if (isRejected) ErrorRose.copy(alpha = 0.15f) else BotanicalEmerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isRejected) "FAILED QA" else "VERIFIED 98%",
                            color = if (isRejected) ErrorRose else BotanicalEmerald,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = product.batchId,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MintAccent
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = product.botanicalName,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "${product.quantityKg.toInt()} kg",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
