package com.florachain.data.remote

import com.florachain.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface FloraChainApi {

    // --- Authentication ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/switch-role")
    suspend fun switchRole(@Query("role") role: String): Response<AuthResponse>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<UserDto>

    // --- Products & Harvest ---
    @POST("products")
    suspend fun registerProduct(@Body request: ProductRegistrationRequest): Response<ProductResponse>

    @GET("products")
    suspend fun getProducts(
        @Query("status") status: String? = null,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Response<List<ProductResponse>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): Response<ProductResponse>

    @GET("products/{id}/timeline")
    suspend fun getProductTimeline(@Path("id") id: String): Response<List<TimelineEventDto>>

    // --- Processing ---
    @POST("processing")
    suspend fun submitProcessing(
        @Query("productId") productId: String,
        @Body request: ProcessingRequest
    ): Response<ProcessingResponse>

    // --- Laboratory ---
    @POST("laboratories/test")
    suspend fun submitLabReport(
        @Query("productId") productId: String,
        @Body request: LabTestRequest
    ): Response<LabReportResponse>

    // --- Shipments ---
    @POST("shipments")
    suspend fun createShipment(@Body request: CreateShipmentRequest): Response<ShipmentResponse>

    @PUT("shipments/{id}/status")
    suspend fun updateShipmentStatus(
        @Path("id") id: String,
        @Body request: UpdateShipmentStatusRequest
    ): Response<ShipmentResponse>

    // --- Retailers ---
    @POST("retailers/receive")
    suspend fun receiveRetailStock(
        @Query("productId") productId: String,
        @Body request: RetailReceiveRequest
    ): Response<RetailResponse>

    // --- Verification ---
    @GET("verify/{identifier}")
    suspend fun verifyProduct(@Path("identifier") identifier: String): Response<VerificationResultResponse>

    // --- Web Project Live Sync Feed ---
    @GET
    suspend fun getSyncProducts(@Url url: String): Response<List<ProductResponse>>

    // --- Admin & Stakeholder Governance ---
    @GET("auth/users/pending")
    suspend fun getPendingUsers(): Response<List<UserDto>>

    @PUT("auth/users/{id}/approve")
    suspend fun approveUser(@Path("id") id: String): Response<UserDto>

    @PUT("auth/users/{id}/reject")
    suspend fun rejectUser(@Path("id") id: String): Response<Unit>

    // --- Suspicious Reports & Fraud Resolution ---
    @POST("reports")
    suspend fun submitReport(@Body request: CreateReportRequestDto): Response<ReportResponseDto>

    @GET("reports")
    suspend fun getReports(@Query("status") status: String? = null): Response<List<ReportResponseDto>>

    @PUT("reports/{id}/status")
    suspend fun updateReportStatus(
        @Path("id") id: String,
        @Body request: UpdateReportStatusRequestDto
    ): Response<ReportResponseDto>

    // --- Blockchain Ledger Explorer ---
    @GET("blockchain/stats")
    suspend fun getNetworkStats(): Response<BlockchainStatsResponseDto>

    @GET("blockchain/transactions")
    suspend fun getAllTransactions(): Response<List<TransactionDto>>

    @GET("blockchain/transactions/product/{productId}")
    suspend fun getTransactionsByProduct(@Path("productId") productId: String): Response<List<TransactionDto>>
}

