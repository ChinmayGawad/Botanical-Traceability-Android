package com.florachain.presentation.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.FloraChainTheme
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.CyberGlassCard
import com.florachain.domain.model.BlockchainTx
import com.florachain.presentation.viewmodel.BlockchainExplorerViewModel

@Composable
fun BlockchainExplorerScreen(
    viewModel: BlockchainExplorerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FloraChainTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val stageFilters = remember {
        listOf("ALL", "HARVEST", "PROCESS", "LAB", "TRANSIT", "RETAIL")
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Ledger Explorer",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showNetworkPill = true,
                showThemeToggle = true,
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.primary)
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
        ) {
            // Network Stats Banner
            val stats = uiState.stats
            CyberGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("CURRENT BLOCK HEIGHT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                            Text(
                                text = stats?.let { "#${it.blockHeight}" } ?: "#4,921,084",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ACTIVE EVM CONTRACT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                            Text(
                                text = "0x5FbD...8aa3",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Network: ${stats?.networkName ?: "Polygon Amoy"}",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "Cumulative Gas: ${stats?.gasUsed ?: 21458900L}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search by Hash, Actor or Block...", fontSize = 12.sp, color = colors.textMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colors.primary) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.glassBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Stage Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(stageFilters) { stage ->
                    val isSelected = uiState.selectedStage == stage
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onStageFilterSelected(stage) },
                        label = { Text(stage, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Transactions Table List
            if (uiState.filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions matching search criteria", color = colors.textSecondary, fontSize = 13.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredTransactions, key = { it.txId }) { tx ->
                        TransactionRowCard(
                            tx = tx,
                            onClick = { viewModel.selectTransaction(tx) },
                            onCopy = {
                                clipboardManager.setText(AnnotatedString(tx.txId))
                                Toast.makeText(context, "Tx Hash copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Modal Sheet: Raw Cryptographic Receipt & Event Payload
    uiState.selectedTransaction?.let { tx ->
        TransactionReceiptDialog(
            tx = tx,
            onDismiss = { viewModel.selectTransaction(null) },
            onCopyHash = {
                clipboardManager.setText(AnnotatedString(tx.txId))
                Toast.makeText(context, "Transaction hash copied", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun TransactionRowCard(
    tx: BlockchainTx,
    onClick: () -> Unit,
    onCopy: () -> Unit
) {
    val colors = FloraChainTheme.colors
    CyberGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = tx.stage,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "Block #${tx.blockNumber}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tx.status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = tx.action.replace('_', ' '),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tx.txId.take(14)}...${tx.txId.takeLast(6)}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.primary
                )
                Text(
                    text = "${tx.actor} • ${tx.timestamp.take(10)}",
                    fontSize = 10.sp,
                    color = colors.textMuted
                )
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
                Text("Cryptographic Receipt", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
