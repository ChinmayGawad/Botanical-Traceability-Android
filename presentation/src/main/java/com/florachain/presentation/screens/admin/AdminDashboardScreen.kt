package com.florachain.presentation.screens.admin

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.FloraChainTheme
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.CyberGlassCard
import com.florachain.presentation.viewmodel.AdminViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateToApprovals: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToExplorer: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FloraChainTheme.colors
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Consortium Admin",
                showNetworkPill = true,
                showThemeToggle = true,
                actions = {
                    IconButton(onClick = { viewModel.loadData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = colors.primary)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color(0xFFEF4444))
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Portal Header Banner
            CyberGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = colors.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = colors.primary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CONSORTIUM GOVERNANCE DECK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                        }
                    }
                    Text("Consortium Governance Admin Portal", fontSize = 20.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)
                    Text(
                        "Network monitoring, smart contract chaincode verification, stakeholder authorization, and fraud resolution.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )
                }
            }

            // Metrics Grid
            val stats = uiState.networkStats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricTile(
                    title = "BLOCK HEIGHT",
                    value = stats?.let { "#${it.blockHeight}" } ?: "#4,921,084",
                    icon = Icons.Default.ViewInAr,
                    color = colors.primary,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricTile(
                    title = "ACTIVE NODES",
                    value = stats?.let { "${it.activeNodes} Nodes" } ?: "28 Nodes",
                    icon = Icons.Default.Hub,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricTile(
                    title = "PENDING APPROVALS",
                    value = "${uiState.pendingUsers.size} Users",
                    icon = Icons.Default.HowToReg,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToApprovals
                )
                AdminMetricTile(
                    title = "FRAUD REPORTS",
                    value = "${uiState.reports.size} Reports",
                    icon = Icons.Default.Warning,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToReports
                )
            }

            // Quick Governance Action Buttons
            Text(
                text = "Consortium Actions",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            // 1. Approvals Action Card
            CyberGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToApprovals() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, tint = Color(0xFFF59E0B))
                        }
                        Column {
                            Text("Stakeholder Approvals", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("Authorize Farmer, Processor & Lab Nodes (${uiState.pendingUsers.size} waiting)", fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textMuted)
                }
            }

            // 2. Incident Reports Desk Action Card
            CyberGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToReports() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFEF4444))
                        }
                        Column {
                            Text("Incident Triage & Fraud Desk", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("Resolve consumer counterfeit & seal breach alerts", fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textMuted)
                }
            }

            // 3. Ledger Explorer Action Card
            CyberGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToExplorer() }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = colors.primary)
                        }
                        Column {
                            Text("Blockchain Ledger Explorer", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text("Audit immutable smart contract events & gas receipts", fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.textMuted)
                }
            }
        }
    }
}

@Composable
private fun AdminMetricTile(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = FloraChainTheme.colors
    CyberGlassCard(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(text = title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
            }
            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = colors.textPrimary
            )
        }
    }
}
