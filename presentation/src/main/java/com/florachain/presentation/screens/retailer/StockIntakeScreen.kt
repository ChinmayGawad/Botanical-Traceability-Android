package com.florachain.presentation.screens.retailer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.presentation.viewmodel.RetailerViewModel

@Composable
fun StockIntakeScreen(
    viewModel: RetailerViewModel,
    retailerId: String,
    retailerName: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var storeLocation by remember { mutableStateOf("Connaught Place Store #14, New Delhi") }
    var unitPriceText by remember { mutableStateOf("499.0") }
    var shelfBatchId by remember { mutableStateOf("SHELF-ND-01") }
    var notes by remember { mutableStateOf("Verified tamper-evident seal and temperature log") }

    val unitPrice = unitPriceText.toDoubleOrNull() ?: 499.0

    val colors = FloraChainTheme.colors

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Retail Stock Intake",
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
                    text = "Select Delivered Batch for Shelf Intake",
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
                            .background(BotanicalDark.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(prod.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Batch: ${prod.batchId} • Botanical: ${prod.botanicalName}", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                uiState.products.take(3).forEach { p ->
                    val isSelected = uiState.selectedProduct?.id == p.id
                    Button(
                        onClick = { viewModel.selectProduct(p) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) BotanicalEmerald else SurfaceDarkElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "${p.name} (${p.batchId})",
                            color = if (isSelected) DeepSlate else TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Retail & Shelf Configuration
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Store Placement & Shelf Price",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = storeLocation,
                    onValueChange = { storeLocation = it },
                    label = { Text("Store Location") },
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = shelfBatchId,
                    onValueChange = { shelfBatchId = it },
                    label = { Text("Shelf / Aisle Code") },
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it },
                    label = { Text("Consumer Retail Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
            }

            if (uiState.isSuccess) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BotanicalDark.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, tint = BotanicalEmerald, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("✓ Product Intake Complete & Consumer QR Active!", color = TrustGreen, fontWeight = FontWeight.Bold)
                    Text("Customers can scan QR to view complete cryptographic provenance", color = TextSecondary, fontSize = 12.sp)
                }
            }

            uiState.error?.let { err ->
                Text(text = err, color = ErrorRose, fontSize = 13.sp)
            }

            // Submit Button
            Button(
                onClick = {
                    viewModel.intakeStock(
                        retailerId = retailerId.ifBlank { "RET-001" },
                        retailerName = retailerName.ifBlank { "AyurAushadhi Wellness" },
                        storeLocation = storeLocation,
                        shelfBatchId = shelfBatchId,
                        unitPrice = unitPrice,
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
                    Text("Complete Stock Intake & Issue QR", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
