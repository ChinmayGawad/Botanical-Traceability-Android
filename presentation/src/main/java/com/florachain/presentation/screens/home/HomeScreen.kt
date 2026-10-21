package com.florachain.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.*
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
    val colors = FloraChainTheme.colors
    val haptic = LocalHapticFeedback.current

    var searchInput by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var isBatchesExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val categoryFilters = listOf("All", "Ashwagandha", "Tulsi", "Turmeric", "Brahmi")

    Scaffold(
        topBar = {
            AppTopBar(
                title = "FloraChain",
                showNetworkPill = true,
                showThemeToggle = true,
                actions = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.refresh()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = colors.primary
                        )
                    }
                }
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Hero Section with Ambient Glow ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (colors.isDark) HeroGlowDark else HeroGlowLight
                    )
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DECENTRALIZED BOTANICAL INTEGRITY",
                                color = colors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                        }
                    }

                    Text(
                        text = "Soil-to-Shelf\nTraceability Platform",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        lineHeight = 34.sp
                    )

                    Text(
                        text = "Verify botanical harvest authenticity, active compound HPLC purity assays, and cold-chain IoT telemetry immutably recorded on blockchain.",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        lineHeight = 19.sp
                    )
                }
            }

            // ── Search & Scan Bar (CyberGlassCard) ──
            CyberGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        placeholder = {
                            Text(
                                "Search Batch (e.g. BOT-2024-8901)",
                                fontSize = 13.sp,
                                color = colors.textMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = colors.primary
                            )
                        },
                        trailingIcon = {
                            if (searchInput.isNotBlank()) {
                                IconButton(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onNavigateToDetails(searchInput.trim())
                                }) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = "Search",
                                        tint = colors.primary
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.glassBorder,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (searchInput.isNotBlank()) {
                                    onNavigateToDetails(searchInput.trim())
                                } else {
                                    onNavigateToDetails("BOT-2024-8901")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.4f)
                                .height(46.dp)
                        ) {
                            Icon(
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Verify Batch",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToScan()
                            },
                            border = BorderStroke(1.dp, colors.primary),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Scan QR",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ── Live 5-Node Blockchain Consensus Strip ──
            ConsensusNodeStrip()

            // ── Live Provenance Featured Card ──
            val featured = uiState.featuredProduct
            if (featured != null) {
                CyberGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateToDetails(featured.id)
                    }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.12f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(colors.primary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "LIVE PROVENANCE",
                                        color = colors.primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "#" + featured.batchId,
                                color = colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = featured.name,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = featured.botanicalName + " (${featured.category.name})",
                                fontSize = 12.sp,
                                color = colors.textMuted,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        // Data tiles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = colors.surfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.glassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        "ACTIVE HPLC PURITY",
                                        fontSize = 9.sp,
                                        color = colors.textSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "99.5% Withanolides",
                                        fontSize = 12.sp,
                                        color = colors.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                color = colors.surfaceElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.glassBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        "FARM ORIGIN",
                                        fontSize = 9.sp,
                                        color = colors.textSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        featured.farmLocation.take(18),
                                        fontSize = 12.sp,
                                        color = colors.infoBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToDetails(featured.id)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.primary,
                                contentColor = colors.onPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                "Inspect Full Provenance Journey",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ── Ledger Protocol Metrics Strip ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "NETWORK PROTOCOL METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
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
                    color = colors.textMuted,
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
                        color = colors.glassSurface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.glassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.12f),
                                        RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    stage.icon,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        stage.step + " • ",
                                        color = colors.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        stage.title,
                                        color = colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    stage.desc,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Verified Botanical Batches with Category Filter Chips ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            color = colors.textMuted,
                            letterSpacing = 1.sp
                        )

                        Surface(
                            color = colors.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${uiState.products.size} on-chain",
                                fontSize = 10.sp,
                                color = colors.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (uiState.products.size > 3) {
                        Surface(
                            color = colors.surfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colors.glassBorder),
                            modifier = Modifier.clickable {
                                isBatchesExpanded = !isBatchesExpanded
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isBatchesExpanded) "Show Less" else "View All (${uiState.products.size})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.primary
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = if (isBatchesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoryFilters) { filter ->
                        val isSelected = selectedCategoryFilter == filter
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) colors.primary else colors.glassSurface,
                            label = "ChipBg"
                        )
                        val chipText by animateColorAsState(
                            targetValue = if (isSelected) colors.onPrimary else colors.textSecondary,
                            label = "ChipText"
                        )

                        Surface(
                            color = chipBg,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.primary else colors.glassBorder
                            ),
                            modifier = Modifier.clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedCategoryFilter = filter
                            }
                        ) {
                            Text(
                                text = filter,
                                color = chipText,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                val filteredProducts = uiState.products.filter { prod ->
                    if (selectedCategoryFilter == "All") true
                    else prod.name.contains(selectedCategoryFilter, ignoreCase = true) ||
                            prod.botanicalName.contains(selectedCategoryFilter, ignoreCase = true)
                }

                val displayedBatches = if (isBatchesExpanded) filteredProducts else filteredProducts.take(3)

                if (displayedBatches.isEmpty()) {
                    Surface(
                        color = colors.glassSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, colors.glassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No botanical batches found for this category.",
                            fontSize = 12.sp,
                            color = colors.textMuted,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    displayedBatches.forEach { prod ->
                        CompactBatchCard(
                            product = prod,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToDetails(prod.id)
                            }
                        )
                    }
                }
            }

            // ── Stakeholder Field Portal Callout ──
            CyberGlassCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToPortal()
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.12f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "AYUSH Stakeholder Portal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            "Sign in as Farmer, Processor, QA Lab, Logistics, or Retailer",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
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
    val colors = FloraChainTheme.colors

    Surface(
        color = colors.glassSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, colors.glassBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.12f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    label,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
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
    val colors = FloraChainTheme.colors
    val isRejected = product.verificationState == VerificationState.REJECTED

    Surface(
        color = colors.glassSurface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isRejected) colors.errorRose.copy(alpha = 0.5f) else colors.glassBorder
        ),
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
                        if (isRejected) colors.errorRose.copy(alpha = 0.15f)
                        else colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.12f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRejected) Icons.Default.Warning else Icons.Default.Eco,
                    contentDescription = null,
                    tint = if (isRejected) colors.errorRose else colors.primary,
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
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Surface(
                        color = if (isRejected) colors.errorRose.copy(alpha = 0.15f)
                        else colors.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (isRejected) "FAILED QA" else "VERIFIED 98%",
                            color = if (isRejected) colors.errorRose else colors.primary,
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
                        color = colors.primary
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = product.botanicalName,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = "${product.quantityKg.toInt()} kg",
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
