package com.florachain.presentation.viewmodel

import app.cash.turbine.test
import com.florachain.domain.model.*
import com.florachain.domain.repository.ProductRepository
import com.florachain.domain.usecase.SubmitSuspiciousReportUseCase
import com.florachain.domain.usecase.VerifyProductUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VerifyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val productRepository = mockk<ProductRepository>()
    private val submitSuspiciousReportUseCase = mockk<SubmitSuspiciousReportUseCase>(relaxed = true)
    private lateinit var verifyProductUseCase: VerifyProductUseCase

    private val dummyVerificationResult = VerificationResult(
        queryIdentifier = "BOT-2024-8901",
        verificationState = VerificationState.VERIFIED,
        trustScore = 98,
        onChainVerified = true,
        verifiedContractAddress = "0x2B04B82dD07F1938E74b0CDCA9Fa5693Df147a87",
        rootOriginTxHash = "0x7a39d891b24e60fc9019b889104fa218c57e84b901a89c",
        product = BotanicalProduct(
            id = "PROD-001",
            batchId = "BOT-2024-8901",
            name = "Organic Ashwagandha",
            botanicalName = "Withania somnifera",
            category = ProductCategory.HERB,
            cultivationMethod = CultivationMethod.ORGANIC,
            quantityKg = 500.0,
            harvestDate = "2026-03-10",
            farmLocation = "Mandsaur, MP",
            gpsCoordinates = GpsCoordinates(24.07, 75.07),
            farmerId = "FARMER-101",
            farmerName = "Ramesh Patel",
            farmerOrg = null,
            status = ProductStatus.APPROVED,
            verificationState = VerificationState.VERIFIED,
            qrCodeValue = null,
            description = null,
            imageUrl = null,
            createdTimestamp = null
        ),
        stageIntegrity = emptyList(),
        verificationSummary = "Cryptographically authentic."
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        verifyProductUseCase = VerifyProductUseCase(productRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onQrCodeScanned extracts batch and updates state on success`() = runTest {
        coEvery { productRepository.verifyProduct("BOT-2024-8901") } returns Result.success(dummyVerificationResult)

        val viewModel = VerifyViewModel(verifyProductUseCase, submitSuspiciousReportUseCase)

        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isScanning)
            assertFalse(initial.isLoading)

            // Trigger QR scan with deep link format
            viewModel.onQrCodeScanned("florachain://verify?batchId=BOT-2024-8901")

            val scanState = awaitItem()
            assertFalse(scanState.isScanning)

            testDispatcher.scheduler.advanceUntilIdle()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertNotNull(successState.result)
            assertEquals("BOT-2024-8901", successState.navigationEvent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `verifyBatch updates error state on verification failure`() = runTest {
        coEvery { productRepository.verifyProduct("FAKE-BATCH") } returns Result.failure(
            NoSuchElementException("Batch does not exist on blockchain")
        )

        val viewModel = VerifyViewModel(verifyProductUseCase, submitSuspiciousReportUseCase)

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.verifyBatch("FAKE-BATCH")

            testDispatcher.scheduler.advanceUntilIdle()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertNull(errorState.result)
            assertEquals("Batch does not exist on blockchain", errorState.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `resetScanner restores scanning state`() = runTest {
        coEvery { productRepository.verifyProduct(any()) } returns Result.success(dummyVerificationResult)

        val viewModel = VerifyViewModel(verifyProductUseCase, submitSuspiciousReportUseCase)

        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onQrCodeScanned("BATCH-123")
            awaitItem() // scanState

            viewModel.resetScanner()
            val resetState = awaitItem()
            assertTrue(resetState.isScanning)
            assertFalse(resetState.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

