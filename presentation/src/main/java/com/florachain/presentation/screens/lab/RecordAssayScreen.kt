package com.florachain.presentation.screens.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.domain.model.TestStatus
import com.florachain.presentation.viewmodel.LabViewModel

@Composable
fun RecordAssayScreen(
    viewModel: LabViewModel,
    labId: String,
    labName: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var purityText by remember { mutableStateOf("98.4") }
    var moistureText by remember { mutableStateOf("5.2") }
    var heavyMetals by remember { mutableStateOf(TestStatus.PASSED) }
    var microbial by remember { mutableStateOf(TestStatus.PASSED) }
    var pesticide by remember { mutableStateOf(TestStatus.PASSED) }
    var isApproved by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("Meets AYUSH Pharmacopoeia quality thresholds") }

    val purity = purityText.toDoubleOrNull() ?: 98.4
    val moisture = moistureText.toDoubleOrNull() ?: 5.2

    val colors = FloraChainTheme.colors

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Laboratory QA Assay",
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
            // Batch Selection Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Batch for QA Testing",
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
                        Text("Batch: ${prod.batchId} • Origin: ${prod.farmLocation}", fontSize = 12.sp, color = colors.textSecondary)
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

            // Chemical Assay Metrics
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Analytical Chemistry Metrics",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = purityText,
                        onValueChange = { purityText = it },
                        label = { Text("Purity % (HPLC)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = moistureText,
                        onValueChange = { moistureText = it },
                        label = { Text("Moisture %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Safety Status Selectors
                StatusRow(label = "Heavy Metals (Pb, Cd, As, Hg)", status = heavyMetals) { heavyMetals = it }
                Spacer(modifier = Modifier.height(8.dp))
                StatusRow(label = "Microbial Bioburden", status = microbial) { microbial = it }
                Spacer(modifier = Modifier.height(8.dp))
                StatusRow(label = "Pesticide Residue Screen", status = pesticide) { pesticide = it }
            }

            // Overall Approval Switch
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Quality Assurance Verdict", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text(
                            text = if (isApproved) "Batch meets all pharmacopoeial standards" else "Rejected due to safety non-compliance",
                            fontSize = 12.sp,
                            color = if (isApproved) TrustGreen else ErrorRose
                        )
                    }
                    Switch(
                        checked = isApproved,
                        onCheckedChange = { isApproved = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.primary,
                            checkedTrackColor = colors.primary.copy(alpha = 0.35f)
                        )
                    )
                }
            }

            if (uiState.isSuccess) {
                Text(
                    text = "✓ Certificate of Analysis (CoA) immutably sealed on-chain!",
                    color = TrustGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            uiState.error?.let { err ->
                Text(text = err, color = ErrorRose, fontSize = 13.sp)
            }

            // Submit Button
            Button(
                onClick = {
                    viewModel.submitAssay(
                        labId = labId.ifBlank { "LAB-AYUSH-01" },
                        labName = labName.ifBlank { "National Botanical Analytical Lab" },
                        testedBy = "Dr. Ananya Sharma",
                        purity = purity,
                        moisture = moisture,
                        heavyMetals = heavyMetals,
                        microbial = microbial,
                        pesticide = pesticide,
                        isApproved = isApproved,
                        notes = notes
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
                    Text("Certify Batch & Issue CoA", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    status: TestStatus,
    onStatusChange: (TestStatus) -> Unit
) {
    val colors = FloraChainTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = colors.textSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
                selected = status == TestStatus.PASSED,
                onClick = { onStatusChange(TestStatus.PASSED) },
                label = { Text("PASS", fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TrustGreen,
                    selectedLabelColor = Color.White,
                    containerColor = colors.surfaceElevated,
                    labelColor = colors.textSecondary
                )
            )
            FilterChip(
                selected = status == TestStatus.FAILED,
                onClick = { onStatusChange(TestStatus.FAILED) },
                label = { Text("FAIL", fontSize = 10.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ErrorRose,
                    selectedLabelColor = Color.White,
                    containerColor = colors.surfaceElevated,
                    labelColor = colors.textSecondary
                )
            )
        }
    }
}
