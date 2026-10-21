package com.florachain.presentation.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.florachain.designsystem.components.TrustSealBadge
import com.florachain.designsystem.components.TrustState
import com.florachain.domain.model.BotanicalProduct
import com.florachain.presentation.viewmodel.FarmerViewModel

@Composable
fun FarmerDashboardScreen(
    viewModel: FarmerViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FloraChainTheme.colors

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Farmer Field Portal",
                showThemeToggle = true,
                actions = {
                    IconButton(onClick = { viewModel.refreshProducts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.primary)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = colors.errorRose)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToRegister,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Harvest", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Batches Card
                GlassmorphicCard(modifier = Modifier.weight(1f)) {
                    Text("Total Batches", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = "${uiState.products.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Offline Sync Queue Card
                val hasPending = uiState.pendingSyncCount > 0
                GlassmorphicCard(
                    modifier = Modifier.weight(1f),
                    borderColor = if (hasPending) WarningAmber.copy(alpha = 0.5f) else GlassBorder
                ) {
                    Text("Offline Pending", fontSize = 12.sp, color = if (hasPending) WarningAmber else TextMuted)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${uiState.pendingSyncCount}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasPending) WarningAmber else TrustGreen
                        )
                        if (hasPending) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.SyncProblem, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Harvested Botanical Batches",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BotanicalEmerald)
                }
            }

            // Batches List
            if (uiState.products.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Spa, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No crops registered yet", color = TextSecondary)
                        Text("Tap 'Log Harvest' to record your first crop", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.products) { product ->
                        ProductBatchCard(
                            product = product,
                            onClick = { onNavigateToDetails(product.batchId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductBatchCard(
    product: BotanicalProduct,
    onClick: () -> Unit
) {
    val colors = FloraChainTheme.colors
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = product.botanicalName,
                    fontSize = 13.sp,
                    color = colors.primary
                )
                Text(
                    text = "ID: ${product.batchId} • ${product.quantityKg} kg",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = "Location: ${product.farmLocation}",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }

            if (product.isLocalPending) {
                TrustSealBadge(state = TrustState.PENDING_SYNC)
            } else {
                TrustSealBadge(state = TrustState.VERIFIED_ON_CHAIN, trustScore = 98)
            }
        }
    }
}
