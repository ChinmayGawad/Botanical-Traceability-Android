package com.florachain.presentation.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.domain.model.CultivationMethod
import com.florachain.domain.model.ProductCategory
import com.florachain.presentation.viewmodel.FarmerViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterHarvestScreen(
    viewModel: FarmerViewModel,
    farmerId: String,
    farmerName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("Organic Ashwagandha Root") }
    var botanicalName by remember { mutableStateOf("Withania somnifera") }
    var batchId by remember { mutableStateOf("ASHWA-" + UUID.randomUUID().toString().take(6).uppercase()) }
    var category by remember { mutableStateOf(ProductCategory.ROOT_POWDER) }
    var method by remember { mutableStateOf(CultivationMethod.ORGANIC) }
    var quantityKgText by remember { mutableStateOf("250.0") }
    var farmLocation by remember { mutableStateOf("Neemuch, Madhya Pradesh, India") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var methodExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            viewModel.clearRegistrationStatus()
            onNavigateBack()
        }
    }

    val colors = FloraChainTheme.colors

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Log New Harvest",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                showThemeToggle = true
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
            // Batch & Crop Spec
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Botanical Specifications",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = batchId,
                    onValueChange = { batchId = it },
                    label = { Text("Unique Batch ID") },
                    trailingIcon = {
                        IconButton(onClick = {
                            batchId = "ASHWA-" + UUID.randomUUID().toString().take(6).uppercase()
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = colors.primary)
                        }
                    },
                    colors = floraChainTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Common Crop Name") },
                    colors = floraChainTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = botanicalName,
                    onValueChange = { botanicalName = it },
                    label = { Text("Botanical Taxon / Species") },
                    colors = floraChainTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Cultivation Method Dropdown
                ExposedDropdownMenuBox(
                    expanded = methodExpanded,
                    onExpandedChange = { methodExpanded = it }
                ) {
                    OutlinedTextField(
                        value = method.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cultivation Practice") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = methodExpanded) },
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = methodExpanded,
                        onDismissRequest = { methodExpanded = false }
                    ) {
                        CultivationMethod.entries.forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.name) },
                                onClick = {
                                    method = m
                                    methodExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = quantityKgText,
                    onValueChange = { quantityKgText = it },
                    label = { Text("Harvest Quantity (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = floraChainTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Farm Location & Direct GPS Geotagging Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Agricultural Origin & Geotagging",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = farmLocation,
                    onValueChange = { farmLocation = it },
                    label = { Text("Farm Address / Region") },
                    colors = floraChainTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GPS Capture Section
                val coords = uiState.capturedCoordinates
                if (coords != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.primaryContainer.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Geotagged Coordinates:",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                            Text(
                                text = "${coords.latitude}, ${coords.longitude} (±${coords.accuracyMeters.toInt()}m)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = colors.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Button(
                    onClick = { viewModel.captureCurrentGps() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceElevated,
                        contentColor = colors.primary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLocationCapturing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = colors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reading Satellite Coordinates...", color = colors.primary)
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null, tint = colors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (coords != null) "Re-capture GPS Location" else "Capture Farm GPS Location", color = colors.primary)
                    }
                }
            }

            // Offline Resilience Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = TrustGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Offline-first enabled: saves locally if out of cell range",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }

            uiState.error?.let { err ->
                Text(
                    text = err,
                    color = ErrorRose,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // Submit Button
            Button(
                onClick = {
                    val qty = quantityKgText.toDoubleOrNull() ?: 1.0
                    viewModel.registerHarvest(
                        name = name,
                        botanicalName = botanicalName,
                        batchId = batchId,
                        category = category,
                        method = method,
                        quantityKg = qty,
                        farmLocation = farmLocation,
                        farmerId = farmerId.ifBlank { "FARMER-001" },
                        farmerName = farmerName.ifBlank { "Ramesh Patel" },
                        farmerOrg = "Vedic Herbs Cooperative"
                    )
                },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(22.dp))
                } else {
                    Text("Commit Harvest & Seal Origin", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}
