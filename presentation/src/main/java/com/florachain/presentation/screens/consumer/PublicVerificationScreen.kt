package com.florachain.presentation.screens.consumer

import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.florachain.core.camera.BarcodeScanningAnalyzer
import com.florachain.designsystem.*
import com.florachain.designsystem.components.AppTopBar
import com.florachain.designsystem.components.GlassmorphicCard
import com.florachain.presentation.viewmodel.VerifyViewModel
import java.util.concurrent.Executors

@Composable
fun PublicVerificationScreen(
    viewModel: VerifyViewModel,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var manualInput by remember { mutableStateOf("") }
    var hasCameraPermission by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.navigationEvent) {
        uiState.navigationEvent?.let { batchId ->
            viewModel.onNavigationHandled()
            onNavigateToDetails(batchId)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Verify Botanical Origin"
            )
        },
        containerColor = DeepSlate
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera Viewfinder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceDark)
                    .border(2.dp, BotanicalEmerald.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission && uiState.isScanning) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }

                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val analyzer = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also { imageAnalysis ->
                                            imageAnalysis.setAnalyzer(
                                                Executors.newSingleThreadExecutor(),
                                                BarcodeScanningAnalyzer { barcode ->
                                                    viewModel.onQrCodeScanned(barcode)
                                                }
                                            )
                                        }

                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        analyzer
                                    )
                                } catch (e: Exception) {
                                    hasCameraPermission = false
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Reticle overlay
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .border(3.dp, BotanicalEmerald, RoundedCornerShape(16.dp))
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MintAccent,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = if (uiState.isLoading) "Verifying cryptographic proof on-chain..." else "Camera scanner inactive",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        if (!uiState.isScanning) {
                            Button(
                                onClick = { viewModel.resetScanner() },
                                colors = ButtonDefaults.buttonColors(containerColor = BotanicalDark)
                            ) {
                                Text("Scan Again", color = BotanicalEmerald)
                            }
                        }
                    }
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = BotanicalEmerald,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // Manual Batch ID Entry Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Or Enter Batch Identifier",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    placeholder = { Text("e.g. ASHWA-2026-001", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (manualInput.isNotBlank()) {
                            viewModel.verifyBatch(manualInput)
                        }
                    }),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (manualInput.isNotBlank()) {
                                viewModel.verifyBatch(manualInput)
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = BotanicalEmerald)
                        }
                    },
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
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
