package com.florachain.presentation.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.florachain.designsystem.components.ConfettiCelebration
import com.florachain.designsystem.components.CyberGlassCard
import com.florachain.domain.model.AuthUser
import com.florachain.presentation.viewmodel.AdminViewModel

@Composable
fun UserApprovalsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FloraChainTheme.colors
    var rejectingUser by remember { mutableStateOf<AuthUser?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Stakeholder Approvals",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showThemeToggle = true
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Confetti Celebration Trigger
            uiState.approvedUserId?.let { key ->
                ConfettiCelebration(triggerKey = key)
            }

            if (uiState.pendingUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.primary, modifier = Modifier.size(36.dp))
                        }
                        Text("All Stakeholders Approved", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(
                            "There are no pending node registrations awaiting authorization.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Verify stakeholder credentials before issuing blockchain channel authority.",
                                    fontSize = 11.sp,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }

                    items(uiState.pendingUsers, key = { it.id }) { user ->
                        StakeholderApprovalCard(
                            user = user,
                            onApprove = { viewModel.approveUser(user.id) },
                            onReject = { rejectingUser = user }
                        )
                    }
                }
            }

            // Feedback Snackbar / Banner
            AnimatedVisibility(
                visible = uiState.feedbackMessage != null,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    color = Color(0xFF064E3B),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text(text = uiState.feedbackMessage ?: "", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Rejection
    rejectingUser?.let { user ->
        AlertDialog(
            onDismissRequest = { rejectingUser = null },
            title = { Text("Reject Stakeholder Node?") },
            text = { Text("Are you sure you want to reject ${user.name} (${user.organization ?: user.role.name})? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectUser(user.id)
                        rejectingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Reject Node")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { rejectingUser = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StakeholderApprovalCard(
    user: AuthUser,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val colors = FloraChainTheme.colors
    CyberGlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header Row: Name & Role Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = user.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text(text = user.email, fontSize = 11.sp, color = colors.textSecondary)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.primary.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = user.role.name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = colors.glassBorder)

            // Organization & Location Details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("ORGANIZATION", fontSize = 9.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                    Text(user.organization ?: "Independent Operator", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("LOCATION", fontSize = 9.sp, color = colors.textMuted, fontWeight = FontWeight.Bold)
                    Text(user.location ?: "India", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                }
            }

            // Certifications
            if (user.certifications.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    user.certifications.forEach { cert ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.surfaceElevated
                        ) {
                            Text(
                                text = cert,
                                fontSize = 9.sp,
                                color = colors.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Wallet & Aadhaar Hash
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Aadhaar: ${user.aadhaarNumber ?: "Verified"}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textMuted
                )
                Text(
                    text = "Wallet: ${user.walletAddress ?: "0x...Amoy"}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textMuted
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject", fontSize = 12.sp)
                }

                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve Node", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
