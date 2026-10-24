package com.florachain.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Verification : Screen("verification")
    data object BatchDetails : Screen("batch_details/{batchId}") {
        fun createRoute(batchId: String) = "batch_details/$batchId"
    }
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object FarmerDashboard : Screen("farmer_dashboard")
    data object RegisterHarvest : Screen("register_harvest")
    data object ProcessorDashboard : Screen("processor_dashboard")
    data object LabDashboard : Screen("lab_dashboard")
    data object DistributorDashboard : Screen("distributor_dashboard")
    data object RetailerDashboard : Screen("retailer_dashboard")
    data object AdminDashboard : Screen("admin_dashboard")
    data object UserApprovals : Screen("user_approvals")
    data object SuspiciousReports : Screen("suspicious_reports")
    data object BlockchainExplorer : Screen("blockchain_explorer")
    data object GenerateQr : Screen("generate_qr")
}

