package com.florachain.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.florachain.domain.model.*
import com.florachain.domain.repository.ProductRepository
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
class BatchDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val productRepository = mockk<ProductRepository>()

    private val dummyProduct = BotanicalProduct(
        id = "PROD-001",
        batchId = "BOT-2024-8901",
        name = "Organic Ashwagandha",
        botanicalName = "Withania somnifera",
        category = ProductCategory.HERB,
        cultivationMethod = CultivationMethod.ORGANIC,
        quantityKg = 500.0,
        harvestDate = "2026-03-10",
        farmLocation = "Mandsaur, Madhya Pradesh",
        gpsCoordinates = GpsCoordinates(24.07, 75.07),
        farmerId = "FARMER-101",
        farmerName = "Ramesh Patel",
        farmerOrg = "Malwa Organic Farmers Guild",
        status = ProductStatus.APPROVED,
        verificationState = VerificationState.VERIFIED,
        qrCodeValue = "florachain://verify?batchId=BOT-2024-8901",
        description = "High-withanolide root harvest",
        imageUrl = null,
        createdTimestamp = "2026-03-10T08:30:00Z"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProduct succeeds when repository returns product`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("batchId" to "BOT-2024-8901"))
        coEvery { productRepository.getProductByIdOrBatch("BOT-2024-8901") } returns Result.success(dummyProduct)

        val viewModel = BatchDetailsViewModel(productRepository, savedStateHandle)

        viewModel.uiState.test {
            // Initial state is Loading
            assertEquals(BatchDetailsUiState.Loading, awaitItem())

            // Advance dispatcher to execute coroutine
            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertTrue(successState is BatchDetailsUiState.Success)
            val product = (successState as BatchDetailsUiState.Success).product
            assertEquals("BOT-2024-8901", product.batchId)
            assertEquals("Organic Ashwagandha", product.name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadProduct sets Error state when repository returns failure`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("batchId" to "INVALID-BATCH"))
        coEvery { productRepository.getProductByIdOrBatch("INVALID-BATCH") } returns Result.failure(
            NoSuchElementException("Batch not found")
        )

        val viewModel = BatchDetailsViewModel(productRepository, savedStateHandle)

        viewModel.uiState.test {
            assertEquals(BatchDetailsUiState.Loading, awaitItem())

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is BatchDetailsUiState.Error)
            assertEquals("Batch not found", (errorState as BatchDetailsUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadProduct sets Error state when batchId is null or blank`() = runTest {
        val savedStateHandle = SavedStateHandle()

        val viewModel = BatchDetailsViewModel(productRepository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is BatchDetailsUiState.Error)
            assertEquals("Invalid or missing Batch ID", (state as BatchDetailsUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

