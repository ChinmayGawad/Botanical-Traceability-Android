package com.florachain.presentation.screens.admin

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.FloraChainTheme
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.CyberGlassCard
import com.florachain.domain.model.ReportStatus
import com.florachain.domain.model.SuspiciousReport
import com.florachain.presentation.viewmodel.AdminViewModel

@Composable
fun SuspiciousReportsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FloraChainTheme.colors
    var selectedReportForAction by remember { mutableStateOf<SuspiciousReport?>(null) }

    val filterOptions = remember {
        listOf(
            null to "All Reports",
            ReportStatus.PENDING_REVIEW to "Pending Review",
            ReportStatus.INVESTIGATING to "Investigating",
            ReportStatus.RESOLVED_VALID to "Resolved Valid",
            ReportStatus.DISMISSED to "Dismissed"
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Incident Triage Desk",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showThemeToggle = true
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Status Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { (status, label) ->
                    val isSelected = uiState.selectedReportFilter == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.filterReports(status) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colors.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (uiState.reports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = colors.primary, modifier = Modifier.size(32.dp))
                        }
                        Text("No Flagged Incident Reports", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("No counterfeit batches or security breaches matching this filter.", fontSize = 12.sp, color = colors.textSecondary)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.reports, key = { it.id }) { report ->
                        ReportCard(
                            report = report,
                            onUpdateStatus = { selectedReportForAction = report }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }

    // Modal Sheet for Updating Status & Admin Notes
    selectedReportForAction?.let { report ->
        ReportResolutionDialog(
            report = report,
            onDismiss = { selectedReportForAction = null },
            onSave = { newStatus, notes ->
                viewModel.updateReportStatus(report.id, newStatus, notes)
                selectedReportForAction = null
            }
        )
    }
}

@Composable
private fun ReportCard(
    report: SuspiciousReport,
    onUpdateStatus: () -> Unit
) {
    val colors = FloraChainTheme.colors
    val (statusColor, statusBg) = when (report.status) {
        ReportStatus.PENDING_REVIEW -> Pair(Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.12f))
        ReportStatus.INVESTIGATING -> Pair(Color(0xFF38BDF8), Color(0xFF38BDF8).copy(alpha = 0.12f))
        ReportStatus.RESOLVED_VALID -> Pair(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.12f))
        ReportStatus.DISMISSED -> Pair(Color(0xFF94A3B8), Color(0xFF94A3B8).copy(alpha = 0.12f))
    }

    CyberGlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Report ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                    Text(text = report.id, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBg
                ) {
                    Text(
                        text = report.status.name.replace('_', ' '),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = "Batch: #${report.batchId} • ${report.issueType}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Text(
                text = report.description,
                fontSize = 11.sp,
                color = colors.textSecondary,
                lineHeight = 16.sp
            )

            HorizontalDivider(color = colors.glassBorder)

            // Reporter & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reporter: ${report.reporterName}",
                    fontSize = 10.sp,
                    color = colors.textMuted
                )
                Text(
                    text = report.createdAt.take(10),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textMuted
                )
            }

            // Admin Notes if present
            if (!report.adminNotes.isNullOrBlank()) {
                Surface(
                    color = colors.surfaceElevated,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Admin Resolution: ${report.adminNotes}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = onUpdateStatus,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Update Resolution State", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ReportResolutionDialog(
    report: SuspiciousReport,
    onDismiss: () -> Unit,
    onSave: (ReportStatus, String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(report.status) }
    var notes by remember { mutableStateOf(report.adminNotes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Incident Resolution Desk") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Report #${report.id} (${report.batchId})", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Text("Select Resolution Status:", fontSize = 11.sp)
                ReportStatus.entries.forEach { status ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = status }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(selected = selectedStatus == status, onClick = { selectedStatus = status })
                        Text(status.name.replace('_', ' '), fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Resolution / Audit Notes") },
                    placeholder = { Text("e.g. Quarantined stock at retail location...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedStatus, notes) }) {
                Text("Save Resolution")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
