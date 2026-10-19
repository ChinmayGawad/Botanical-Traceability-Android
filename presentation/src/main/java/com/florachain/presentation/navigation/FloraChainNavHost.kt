package com.florachain.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.florachain.designsystem.BotanicalEmerald
import com.florachain.designsystem.DeepSlate
import com.florachain.designsystem.SurfaceDark
import com.florachain.designsystem.TextMuted
import com.florachain.domain.model.UserRole
import com.florachain.presentation.screens.auth.LoginScreen
import com.florachain.presentation.screens.auth.RegisterScreen
import com.florachain.presentation.screens.consumer.BatchDetailsScreen
import com.florachain.presentation.screens.consumer.PublicVerificationScreen
import com.florachain.presentation.screens.distributor.DispatchShipmentScreen
import com.florachain.presentation.screens.farmer.FarmerDashboardScreen
import com.florachain.presentation.screens.farmer.RegisterHarvestScreen
import com.florachain.presentation.screens.lab.RecordAssayScreen
import com.florachain.presentation.screens.processor.ProcessBatchScreen
import com.florachain.presentation.screens.retailer.StockIntakeScreen
import com.florachain.presentation.viewmodel.*

sealed class BottomNavItem(val title: String, val route: String, val icon: ImageVector) {
    data object Verify : BottomNavItem("Verify QR", Screen.Verification.route, Icons.Default.QrCodeScanner)
    data object Portal : BottomNavItem("Portal", "portal_section", Icons.Default.Dashboard)
    data object Login : BottomNavItem("Account", Screen.Login.route, Icons.Default.Person)
}

@Composable
fun FloraChainNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    verifyViewModel: VerifyViewModel = hiltViewModel(),
    farmerViewModel: FarmerViewModel = hiltViewModel(),
    processorViewModel: ProcessorViewModel = hiltViewModel(),
    labViewModel: LabViewModel = hiltViewModel(),
    distributorViewModel: DistributorViewModel = hiltViewModel(),
    retailerViewModel: RetailerViewModel = hiltViewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem.Verify,
        BottomNavItem.Portal,
        BottomNavItem.Login
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = when (item) {
                        BottomNavItem.Verify -> currentRoute == Screen.Verification.route || currentRoute?.startsWith("batch_details") == true
                        BottomNavItem.Portal -> currentRoute in listOf(
                            Screen.FarmerDashboard.route,
                            Screen.ProcessorDashboard.route,
                            Screen.LabDashboard.route,
                            Screen.DistributorDashboard.route,
                            Screen.RetailerDashboard.route,
                            Screen.RegisterHarvest.route
                        )
                        BottomNavItem.Login -> currentRoute in listOf(
                            Screen.Login.route,
                            Screen.Register.route
                        )
                    }
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (item == BottomNavItem.Portal) {
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
                            } else {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepSlate,
                            selectedTextColor = BotanicalEmerald,
                            indicatorColor = BotanicalEmerald,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Verification.route,
            modifier = Modifier.padding(innerPadding)
        ) {
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
                val verifyState by verifyViewModel.uiState.collectAsState()
                val farmerState by farmerViewModel.uiState.collectAsState()

                // Find batch from verifyViewModel or farmer list matching either id or batchId
                val product = verifyState.result?.product?.takeIf {
                    it.id.equals(batchId, ignoreCase = true) || it.batchId.equals(batchId, ignoreCase = true)
                } ?: verifyState.result?.product
                  ?: farmerState.products.find {
                    it.id.equals(batchId, ignoreCase = true) || it.batchId.equals(batchId, ignoreCase = true)
                }

                BatchDetailsScreen(
                    product = product,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { role ->
                        val target = when (role) {
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
        }
    }
}
