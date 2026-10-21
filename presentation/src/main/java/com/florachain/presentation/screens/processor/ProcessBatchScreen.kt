package com.florachain.presentation.screens.processor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.presentation.viewmodel.ProcessorViewModel

@Composable
fun ProcessBatchScreen(
    viewModel: ProcessorViewModel,
    processorId: String,
    processorName: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var method by remember { mutableStateOf("Hydro-Ethanolic Extraction (10:1)") }
    var facilityLocation by remember { mutableStateOf("FloraChain Processing Plant A, Indore") }
    var initialQtyText by remember { mutableStateOf("250.0") }
    var processedQtyText by remember { mutableStateOf("25.0") }
    var notes by remember { mutableStateOf("Extracted with standard GMP protocol") }

    val initialQty = initialQtyText.toDoubleOrNull() ?: 0.0
    val processedQty = processedQtyText.toDoubleOrNull() ?: 0.0
    val yieldLoss = if (initialQty > 0) ((initialQty - processedQty) / initialQty) * 100 else 0.0

    val colors = FloraChainTheme.colors

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Processor Extraction Portal",
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
            // Selected Batch Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Botanical Batch to Process",
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
                        Text(prod.botanicalName, color = colors.primary, fontSize = 13.sp)
                        Text("Batch ID: ${prod.batchId} • Raw: ${prod.quantityKg} kg", fontSize = 12.sp, color = colors.textSecondary)
                    }
                } else {
                    Text("Select a batch from available inventory:", fontSize = 13.sp, color = colors.textMuted)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mini horizontal or dropdown list of products
                uiState.products.take(4).forEach { p ->
                    val isSelected = uiState.selectedProduct?.id == p.id
                    Button(
                        onClick = {
                            viewModel.selectProduct(p)
                            initialQtyText = p.quantityKg.toString()
                            processedQtyText = (p.quantityKg * 0.1).toString()
                        },
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

            // Extraction Specifications
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Extraction Parameters",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = method,
                    onValueChange = { method = it },
                    label = { Text("Extraction Formula / Method") },
                    colors = floraChainTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = facilityLocation,
                    onValueChange = { facilityLocation = it },
                    label = { Text("Processing Facility Location") },
                    colors = floraChainTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = initialQtyText,
                        onValueChange = { initialQtyText = it },
                        label = { Text("Raw Input (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = processedQtyText,
                        onValueChange = { processedQtyText = it },
                        label = { Text("Output Extract (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calculated Yield Loss
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceElevated, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calculated Yield Loss:", fontSize = 13.sp, color = colors.textMuted)
                    Text(
                        text = "${String.format("%.1f", yieldLoss)}%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (yieldLoss < 95.0) colors.primary else WarningAmber
                    )
                }
            }

            if (uiState.isSuccess) {
                Text(
                    text = "✓ Extraction logged and submitted to blockchain relayer!",
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
                    viewModel.submitProcessing(
                        processorId = processorId.ifBlank { "PROC-001" },
                        processorName = processorName.ifBlank { "BioExtracts Ltd" },
                        method = method,
                        facilityLocation = facilityLocation,
                        initialQty = initialQty,
                        processedQty = processedQty,
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
                    Text("Anchor Processing Stage", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
