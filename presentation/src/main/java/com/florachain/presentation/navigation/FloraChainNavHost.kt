package com.florachain.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.florachain.domain.model.UserRole
import com.florachain.presentation.screens.auth.LoginScreen
import com.florachain.presentation.screens.auth.RegisterScreen
import com.florachain.presentation.screens.consumer.BatchDetailsScreen
import com.florachain.presentation.screens.consumer.PublicVerificationScreen
import com.florachain.presentation.screens.distributor.DispatchShipmentScreen
import com.florachain.presentation.screens.farmer.FarmerDashboardScreen
import com.florachain.presentation.screens.farmer.RegisterHarvestScreen
import com.florachain.presentation.screens.home.HomeScreen
import com.florachain.presentation.screens.lab.RecordAssayScreen
import com.florachain.presentation.screens.processor.ProcessBatchScreen
import com.florachain.presentation.screens.retailer.StockIntakeScreen
import com.florachain.presentation.screens.retailer.GenerateQrScreen
import com.florachain.presentation.screens.admin.AdminDashboardScreen
import com.florachain.presentation.screens.admin.UserApprovalsScreen
import com.florachain.presentation.screens.admin.SuspiciousReportsScreen
import com.florachain.presentation.screens.admin.BlockchainExplorerScreen
import com.florachain.presentation.screens.fleet.FleetCommandScreen
import com.florachain.presentation.viewmodel.*

sealed class BottomNavItem(val title: String, val route: String, val icon: ImageVector) {
    data object Home : BottomNavItem("Home", Screen.Home.route, Icons.Default.Home)
    data object Verify : BottomNavItem("Verify QR", Screen.Verification.route, Icons.Default.QrCodeScanner)
    data object Portal : BottomNavItem("Portal", "portal_section", Icons.Default.Dashboard)
    data object Login : BottomNavItem("Account", Screen.Login.route, Icons.Default.Person)
}

@Composable
fun FloraChainNavHost(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    verifyViewModel: VerifyViewModel = hiltViewModel(),
    farmerViewModel: FarmerViewModel = hiltViewModel(),
    processorViewModel: ProcessorViewModel = hiltViewModel(),
    labViewModel: LabViewModel = hiltViewModel(),
    distributorViewModel: DistributorViewModel = hiltViewModel(),
    retailerViewModel: RetailerViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel(),
    blockchainExplorerViewModel: BlockchainExplorerViewModel = hiltViewModel(),
    qrStudioViewModel: QrStudioViewModel = hiltViewModel(),
    fleetCommandViewModel: FleetCommandViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route


    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Verify,
        BottomNavItem.Portal,
        BottomNavItem.Login
    )

    val colors = com.florachain.designsystem.FloraChainTheme.colors
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = when (item) {
                        BottomNavItem.Home -> currentRoute == Screen.Home.route
                        BottomNavItem.Verify -> currentRoute == Screen.Verification.route || currentRoute?.startsWith("batch_details") == true
                        BottomNavItem.Portal -> currentRoute in listOf(
                            Screen.FarmerDashboard.route,
                            Screen.ProcessorDashboard.route,
                            Screen.LabDashboard.route,
                            Screen.DistributorDashboard.route,
                            Screen.RetailerDashboard.route,
                            Screen.RegisterHarvest.route,
                            Screen.AdminDashboard.route,
                            Screen.UserApprovals.route,
                            Screen.SuspiciousReports.route,
                            Screen.BlockchainExplorer.route,
                            Screen.GenerateQr.route
                        )
                        BottomNavItem.Login -> currentRoute in listOf(
                            Screen.Login.route,
                            Screen.Register.route
                        )
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            if (item == BottomNavItem.Portal) {
                                val target = if (authState.currentUser == null || authState.currentUser?.role == UserRole.CONSUMER) {
                                    Screen.Login.route
                                } else {
                                    when (authState.currentUser?.role) {
                                        UserRole.ADMIN -> Screen.AdminDashboard.route
                                        UserRole.FARMER -> Screen.FarmerDashboard.route
                                        UserRole.PROCESSOR -> Screen.ProcessorDashboard.route
                                        UserRole.LABORATORY -> Screen.LabDashboard.route
                                        UserRole.DISTRIBUTOR -> Screen.DistributorDashboard.route
                                        UserRole.RETAILER -> Screen.RetailerDashboard.route
                                        else -> Screen.Login.route
                                    }
                                }
                                navController.navigate(target) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.primary,
                            selectedTextColor = colors.primary,
                            indicatorColor = colors.primary.copy(alpha = if (colors.isDark) 0.22f else 0.15f),
                            unselectedIconColor = colors.textMuted,
                            unselectedTextColor = colors.textMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToDetails = { batchId ->
                        navController.navigate(Screen.BatchDetails.createRoute(batchId))
                    },
                    onNavigateToScan = {
                        navController.navigate(Screen.Verification.route)
                    },
                    onNavigateToPortal = {
                        val target = if (authState.currentUser == null || authState.currentUser?.role == UserRole.CONSUMER) {
                            Screen.Login.route
                        } else {
                            when (authState.currentUser?.role) {
                                UserRole.FARMER -> Screen.FarmerDashboard.route
                                UserRole.PROCESSOR -> Screen.ProcessorDashboard.route
                                UserRole.LABORATORY -> Screen.LabDashboard.route
                                UserRole.DISTRIBUTOR -> Screen.DistributorDashboard.route
                                UserRole.RETAILER -> Screen.RetailerDashboard.route
                                else -> Screen.Login.route
                            }
                        }
                        navController.navigate(target) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToFleetCommand = {
                        navController.navigate(Screen.FleetMap.route)
                    }
                )
            }

            // Consumer Verification Screen
            composable(Screen.Verification.route) {
                PublicVerificationScreen(
                    viewModel = verifyViewModel,
                    onNavigateToDetails = { batchId ->
                        navController.navigate(Screen.BatchDetails.createRoute(batchId))
                    }
                )
            }

            // Batch Details Screen
            composable(
                route = Screen.BatchDetails.route,
                arguments = listOf(navArgument("batchId") { type = NavType.StringType })
            ) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getString("batchId") ?: ""
                val homeState by homeViewModel.uiState.collectAsState()
                val verifyState by verifyViewModel.uiState.collectAsState()
                val farmerState by farmerViewModel.uiState.collectAsState()

                // Find batch from homeViewModel, verifyViewModel, or farmer list matching either id or batchId
                val product = homeState.products.find {
                    it.id.equals(batchId, ignoreCase = true) || it.batchId.equals(batchId, ignoreCase = true)
                } ?: verifyState.result?.product?.takeIf {
                    it.id.equals(batchId, ignoreCase = true) || it.batchId.equals(batchId, ignoreCase = true)
                } ?: farmerState.products.find {
                    it.id.equals(batchId, ignoreCase = true) || it.batchId.equals(batchId, ignoreCase = true)
                } ?: homeState.featuredProduct ?: verifyState.result?.product

                BatchDetailsScreen(
                    product = product,
                    onNavigateBack = { navController.popBackStack() },
                    verifyViewModel = verifyViewModel
                )
            }

            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { role ->
                        val target = when (role) {
                            UserRole.ADMIN -> Screen.AdminDashboard.route
                            UserRole.FARMER -> Screen.FarmerDashboard.route
                            UserRole.PROCESSOR -> Screen.ProcessorDashboard.route
                            UserRole.LABORATORY -> Screen.LabDashboard.route
                            UserRole.DISTRIBUTOR -> Screen.DistributorDashboard.route
                            UserRole.RETAILER -> Screen.RetailerDashboard.route
                            else -> Screen.FarmerDashboard.route
                        }
                        navController.navigate(target) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            // Register Screen
            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = { role ->
                        navController.navigate(Screen.FarmerDashboard.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            // Farmer Dashboard Screen
            composable(Screen.FarmerDashboard.route) {
                if (authState.currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.FarmerDashboard.route) { inclusive = true }
                        }
                    }
                } else {
                    FarmerDashboardScreen(
                        viewModel = farmerViewModel,
                        onNavigateToRegister = { navController.navigate(Screen.RegisterHarvest.route) },
                        onNavigateToDetails = { batchId ->
                            navController.navigate(Screen.BatchDetails.createRoute(batchId))
                        },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // Register Harvest Screen
            composable(Screen.RegisterHarvest.route) {
                if (authState.currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.RegisterHarvest.route) { inclusive = true }
                        }
                    }
                } else {
                    RegisterHarvestScreen(
                        viewModel = farmerViewModel,
                        farmerId = authState.currentUser?.id ?: "FARMER-001",
                        farmerName = authState.currentUser?.name ?: "Ramesh Patel",
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // Processor Dashboard Screen
            composable(Screen.ProcessorDashboard.route) {
                if (authState.currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ProcessorDashboard.route) { inclusive = true }
                        }
                    }
                } else {
                    ProcessBatchScreen(
                        viewModel = processorViewModel,
                        processorId = authState.currentUser?.id ?: "PROC-001",
                        processorName = authState.currentUser?.name ?: "BioExtracts Ltd",
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // Laboratory Dashboard Screen
            composable(Screen.LabDashboard.route) {
                if (authState.currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.LabDashboard.route) { inclusive = true }
                        }
                    }
                } else {
                    RecordAssayScreen(
                        viewModel = labViewModel,
                        labId = authState.currentUser?.id ?: "LAB-001",
                        labName = authState.currentUser?.organization ?: "National Botanical Testing Lab",
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // Distributor Dashboard Screen
            composable(Screen.DistributorDashboard.route) {
                if (authState.currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.DistributorDashboard.route) { inclusive = true }
                        }
                    }
                } else {
                    DispatchShipmentScreen(
                        viewModel = distributorViewModel,
                        distributorId = authState.currentUser?.id ?: "DIST-001",
                        distributorName = authState.currentUser?.organization ?: "FloraLogistics Cold-Chain",
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // Retailer Dashboard Screen
            composable(Screen.RetailerDashboard.route) {
                if (authState.currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.RetailerDashboard.route) { inclusive = true }
                        }
                    }
                } else {
                    StockIntakeScreen(
                        viewModel = retailerViewModel,
                        retailerId = authState.currentUser?.id ?: "RET-001",
                        retailerName = authState.currentUser?.name ?: "AyurAushadhi Pharmacy",
                        onNavigateToGenerateQr = { navController.navigate(Screen.GenerateQr.route) },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // Retailer Generate QR Label Studio
            composable(Screen.GenerateQr.route) {
                GenerateQrScreen(
                    viewModel = qrStudioViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Admin Dashboard Screen
            composable(Screen.AdminDashboard.route) {
                if (authState.currentUser == null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                        }
                    }
                } else {
                    AdminDashboardScreen(
                        viewModel = adminViewModel,
                        onNavigateToApprovals = { navController.navigate(Screen.UserApprovals.route) },
                        onNavigateToReports = { navController.navigate(Screen.SuspiciousReports.route) },
                        onNavigateToExplorer = { navController.navigate(Screen.BlockchainExplorer.route) },
                        onLogout = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            // User Approvals Screen
            composable(Screen.UserApprovals.route) {
                UserApprovalsScreen(
                    viewModel = adminViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Suspicious Reports Desk Screen
            composable(Screen.SuspiciousReports.route) {
                SuspiciousReportsScreen(
                    viewModel = adminViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Blockchain Ledger Explorer Screen
            composable(Screen.BlockchainExplorer.route) {
                BlockchainExplorerScreen(
                    viewModel = blockchainExplorerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Dedicated Fleet Command Center Deck
            composable(Screen.FleetMap.route) {
                FleetCommandScreen(
                    viewModel = fleetCommandViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
