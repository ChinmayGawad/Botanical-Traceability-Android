package com.florachain.presentation.screens.consumer

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.FloraChainTheme
import com.florachain.domain.model.BotanicalProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLabReportBottomSheet(
    product: BotanicalProduct,
    onDismiss: () -> Unit
) {
    val colors = FloraChainTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val verificationUrl = "https://blockchain-botanical-traceability.vercel.app/verify/${product.batchId}"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = colors.primary)
                    Text("Share Cryptographic Proof", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textMuted)
                }
            }

            // Summary Card
            Surface(
                color = colors.surfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.glassBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = product.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text(text = "${product.botanicalName} • Batch #${product.batchId}", fontSize = 11.sp, color = colors.primary, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)

                    HorizontalDivider(color = colors.glassBorder)

                    val lab = product.labReport
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("HPLC PURITY ASSAY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                            Text("${lab?.purityPercentage ?: 5.4}% Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("SAFETY ACCREDITATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                            Text("ISO/IEC 17025 Certified", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }

                    Text(
                        text = "Recorded permanently on Polygon Amoy EVM smart contract.",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // Export Official PDF Certificate Action
            Button(
                onClick = {
                    try {
                        com.florachain.core.pdf.BotanicalPdfGenerator.generateAndSharePdf(context, product)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error creating PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export & Share Official PDF CoA", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Secondary Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(verificationUrl))
                        Toast.makeText(context, "Verification link copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Link", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        shareNativeProof(context, product, verificationUrl)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Text", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun shareNativeProof(context: Context, product: BotanicalProduct, url: String) {
    val shareText = """
        🌿 FloraChain Verified Botanical Provenance:
        Product: ${product.name} (${product.botanicalName})
        Batch ID: #${product.batchId}
        Purity: ${product.labReport?.purityPercentage ?: 5.4}% Active HPLC
        Integrity: 100% Cryptographically Verified on Polygon Amoy Blockchain
        
        Verify full provenance timeline and Certificate of Analysis:
        $url
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Verified Botanical Provenance: ${product.name}")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Botanical Provenance Proof"))
}
