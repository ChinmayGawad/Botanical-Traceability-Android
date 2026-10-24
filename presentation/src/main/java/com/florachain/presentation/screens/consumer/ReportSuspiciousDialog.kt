package com.florachain.presentation.screens.consumer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.domain.model.ReportStatus
import com.florachain.domain.model.SuspiciousReport
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportSuspiciousDialog(
    batchId: String,
    onDismiss: () -> Unit,
    onSubmit: (SuspiciousReport) -> Unit
) {
    var reporterName by remember { mutableStateOf("") }
    var reporterContact by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIssueType by remember { mutableStateOf("Broken Packaging Security Seal") }
    var isIssueMenuExpanded by remember { mutableStateOf(false) }

    val issueTypes = remember {
        listOf(
            "Broken Packaging Security Seal",
            "Mismatched Batch Identifier",
            "Suspicious Color, Aroma or Texture",
            "Counterfeit QR Redirection",
            "Damaged Cold-Chain Indicator",
            "Other Product Defect"
        )
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFEF4444))
                Text("Report Suspicious Batch", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Submitting a fraud report for Batch #${batchId}. This incident will be routed directly to the Consortium Governance Admin Desk.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )

                // Issue Type Dropdown
                Text("Issue Type", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isIssueMenuExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedIssueType, fontSize = 11.sp, maxLines = 1)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = isIssueMenuExpanded,
                        onDismissRequest = { isIssueMenuExpanded = false }
                    ) {
                        issueTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, fontSize = 12.sp) },
                                onClick = {
                                    selectedIssueType = type
                                    isIssueMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Reporter Name
                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = { Text("Your Name / Organization", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Priya Sharma", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Contact
                OutlinedTextField(
                    value = reporterContact,
                    onValueChange = { reporterContact = it },
                    label = { Text("Contact Phone / Email", fontSize = 12.sp) },
                    placeholder = { Text("+91 98765 43210", fontSize = 11.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Incident Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Incident Description", fontSize = 12.sp) },
                    placeholder = { Text("Describe observed irregularities or tampering...", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val report = SuspiciousReport(
                        id = "",
                        batchId = batchId,
                        reporterName = if (reporterName.isBlank()) "Anonymous Consumer" else reporterName.trim(),
                        reporterContact = reporterContact.trim().ifBlank { null },
                        issueType = selectedIssueType,
                        description = if (description.isBlank()) "Reported via mobile application." else description.trim(),
                        evidencePhotoUrl = null,
                        status = ReportStatus.PENDING_REVIEW,
                        adminNotes = null,
                        createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
                    )
                    onSubmit(report)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Submit Incident", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Cancel", fontSize = 12.sp)
            }
        }
    )
}
