package com.florachain.presentation.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
            val role = uiState.currentUser!!.role
            viewModel.resetSuccess()
            onLoginSuccess(role)
        }
    }

    val colors = FloraChainTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.currentUser != null) {
            val user = uiState.currentUser!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(colors.primaryContainer, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = user.name.ifBlank { "Authenticated Stakeholder" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = colors.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${user.role.name} PORTAL ACTIVE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Email", color = colors.textSecondary, fontSize = 13.sp)
                            Text(user.email, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        user.organization?.takeIf { it.isNotBlank() }?.let { org ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Organization", color = colors.textSecondary, fontSize = 13.sp)
                                Text(org, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        user.location?.takeIf { it.isNotBlank() }?.let { loc ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Location", color = colors.textSecondary, fontSize = 13.sp)
                                Text(loc, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        user.aadhaarNumber?.takeIf { it.isNotBlank() }?.let { aadhaar ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Aadhaar", color = colors.textSecondary, fontSize = 13.sp)
                                Text("•••• •••• " + aadhaar.takeLast(4), color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Button(
                    onClick = { onLoginSuccess(user.role) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = colors.onPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Go to ${user.role.name} Dashboard", color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Text(
                    text = "Switch Stakeholder Demo Role",
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val roles = listOf(
                        UserRole.FARMER,
                        UserRole.PROCESSOR,
                        UserRole.LABORATORY,
                        UserRole.DISTRIBUTOR,
                        UserRole.RETAILER
                    )
                    items(roles) { role ->
                        FilterChip(
                            selected = user.role == role,
                            onClick = { viewModel.switchRole(role) },
                            label = { Text(role.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary,
                                containerColor = colors.surfaceElevated,
                                labelColor = colors.textSecondary
                            )
                        )
                    }
                }

                OutlinedButton(
                    onClick = { viewModel.logout() },
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(ErrorRose)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRose)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out", color = ErrorRose, fontWeight = FontWeight.Bold)
                }
            }
        } else {
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
                        .background(colors.primaryContainer, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FloraChain",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Native Botanical Traceability & Blockchain Sync",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                }

                // Quick Demo Role Pills
                Text(
                    text = "Fast Demo Role Switcher",
                    fontSize = 12.sp,
                    color = colors.textMuted,
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
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary,
                                containerColor = colors.surfaceElevated,
                                labelColor = colors.textSecondary
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
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = colors.primary) },
                        singleLine = true,
                        colors = floraChainTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = colors.primary) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = floraChainTextFieldColors(),
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
                            Text("Sign In to FloraChain", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            // Demo quick biometric unlock with current credentials
                            viewModel.login(email, password)
                        },
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(colors.primary)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = colors.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Biometric Fast Unlock", color = colors.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "New Stakeholder? Register with Aadhaar",
                        color = colors.primary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
