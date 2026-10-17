package com.florachain.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.florachain.designsystem.*
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.domain.model.UserRole
import com.florachain.presentation.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("farmer@ayush.gov.in") }
    var password by remember { mutableStateOf("Farmer@123") }
    var selectedDemoRole by remember { mutableStateOf(UserRole.FARMER) }

    LaunchedEffect(uiState.isSuccess, uiState.currentUser) {
        if (uiState.isSuccess && uiState.currentUser != null) {
            onLoginSuccess(uiState.currentUser!!.role)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSlate),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Branding Icon & Title
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(BotanicalDark, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = null,
                    tint = BotanicalEmerald,
                    modifier = Modifier.size(36.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "FloraChain",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Native Botanical Traceability & Blockchain Sync",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            // Quick Demo Role Pills
            Text(
                text = "Fast Demo Role Switcher",
                fontSize = 12.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val demoRoles = listOf(
                    Triple(UserRole.FARMER, "farmer@ayush.gov.in", "Farmer@123"),
                    Triple(UserRole.PROCESSOR, "processor@ayush.gov.in", "Processor@123"),
                    Triple(UserRole.LABORATORY, "lab@ayush.gov.in", "Lab@123"),
                    Triple(UserRole.DISTRIBUTOR, "distributor@ayush.gov.in", "Distributor@123"),
                    Triple(UserRole.RETAILER, "retailer@ayush.gov.in", "Retailer@123")
                )

                items(demoRoles) { (role, roleEmail, rolePass) ->
                    val isSelected = selectedDemoRole == role
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedDemoRole = role
                            email = roleEmail
                            password = rolePass
                        },
                        label = { Text(role.name, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BotanicalEmerald,
                            selectedLabelColor = Color.Black,
                            containerColor = SurfaceDark,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            // Login Input Card
            GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = BotanicalEmerald) },
                    singleLine = true,
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

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BotanicalEmerald) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
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

                uiState.error?.let { err ->
                    Text(
                        text = err,
                        color = ErrorRose,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.login(email, password) },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = BotanicalEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = DeepSlate, modifier = Modifier.size(22.dp))
                    } else {
                        Text("Sign In to FloraChain", color = DeepSlate, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        // Demo quick biometric unlock with current credentials
                        viewModel.login(email, password)
                    },
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(MintAccent)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MintAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Biometric Fast Unlock", color = MintAccent, fontWeight = FontWeight.SemiBold)
                }
            }

            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "New Stakeholder? Register with Aadhaar",
                    color = MintAccent,
                    fontSize = 13.sp
                )
            }
        }
    }
}
