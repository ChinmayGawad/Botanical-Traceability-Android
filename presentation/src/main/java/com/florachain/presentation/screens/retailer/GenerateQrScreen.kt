package com.florachain.presentation.screens.retailer

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.core.qr.QrLabelGenerator
import com.florachain.designsystem.FloraChainTheme
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.CyberGlassCard
import com.florachain.presentation.viewmodel.QrStudioViewModel

@Composable
fun GenerateQrScreen(
    viewModel: QrStudioViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = FloraChainTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var isBatchMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "QR Label Studio",
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Description
            CyberGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Batch QR Code Label Studio", fontSize = 18.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)
                    Text(
                        "Generate high-resolution cryptographic QR packaging labels for retail jars, packaging cartons, and shelf displays.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )
                }
            }

            // Batch Selector Dropdown
            Text("Select Botanical Batch", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
            Box(modifier = Modifier.fillMaxWidth()) {
                CyberGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isBatchMenuExpanded = true },
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = uiState.selectedProduct?.name ?: "Select a batch",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Batch: #${uiState.selectedProduct?.batchId ?: "—"}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = colors.primary
                            )
                        }
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.textPrimary)
                    }
                }

                DropdownMenu(
                    expanded = isBatchMenuExpanded,
                    onDismissRequest = { isBatchMenuExpanded = false }
                ) {
                    uiState.products.forEach { prod ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Batch #${prod.batchId} • ${prod.category.name}", fontSize = 11.sp, color = colors.textMuted)
                                }
                            },
                            onClick = {
                                viewModel.selectProduct(prod.batchId)
                                isBatchMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Label Size Selector Strip
            Text("Packaging Format", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LabelTypeButton(
                    label = "Jar Sticker",
                    sub = "600 × 320",
                    isSelected = uiState.labelType == QrLabelGenerator.LabelType.JAR,
                    onClick = { viewModel.setLabelType(QrLabelGenerator.LabelType.JAR) },
                    modifier = Modifier.weight(1f)
                )
                LabelTypeButton(
                    label = "Carton Box",
                    sub = "800 × 480",
                    isSelected = uiState.labelType == QrLabelGenerator.LabelType.CARTON,
                    onClick = { viewModel.setLabelType(QrLabelGenerator.LabelType.CARTON) },
                    modifier = Modifier.weight(1f)
                )
                LabelTypeButton(
                    label = "Pallet Tag",
                    sub = "1200 × 720",
                    isSelected = uiState.labelType == QrLabelGenerator.LabelType.PALLET,
                    onClick = { viewModel.setLabelType(QrLabelGenerator.LabelType.PALLET) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Live Label Preview
            Text("Printable Label Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(2.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                uiState.generatedBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Generated QR Label",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(if (uiState.labelType == QrLabelGenerator.LabelType.JAR) 600f / 320f else if (uiState.labelType == QrLabelGenerator.LabelType.CARTON) 800f / 480f else 1200f / 720f)
                    )
                } ?: CircularProgressIndicator(color = colors.primary)
            }

            // Action Buttons: Print Label & Copy Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(uiState.verificationUrl))
                        Toast.makeText(context, "Verification URL copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy URL", fontSize = 12.sp)
                }

                Button(
                    onClick = { viewModel.printCurrentLabel(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Print Label", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LabelTypeButton(
    label: String,
    sub: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FloraChainTheme.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) colors.primary else colors.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) colors.primary else colors.glassBorder),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else colors.textPrimary
            )
            Text(
                text = sub,
                fontSize = 9.sp,
                color = if (isSelected) Color.White.copy(alpha = 0.8f) else colors.textMuted
            )
        }
    }
}
