package com.florachain.domain.usecase

import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.model.CultivationMethod
import com.florachain.domain.model.ProductCategory
import com.florachain.domain.model.ProductStatus
import com.florachain.domain.model.VerificationState
import com.florachain.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RegisterHarvestUseCaseTest {

    private val mockRepository = mockk<ProductRepository>()
    private val useCase = RegisterHarvestUseCase(mockRepository)

    @Test
    fun `harvest with empty product name should fail with IllegalArgumentException`() = runTest {
        val invalidProduct = BotanicalProduct(
            id = "1",
            batchId = "BATCH-001",
            name = "", // Blank name
            botanicalName = "Withania somnifera",
            category = ProductCategory.ROOT_POWDER,
            cultivationMethod = CultivationMethod.ORGANIC,
            quantityKg = 100.0,
            harvestDate = "2026-10-17",
            farmLocation = "Neemuch, MP",
            gpsCoordinates = null,
            farmerId = "F-01",
            farmerName = "Ramesh",
            farmerOrg = null,
            status = ProductStatus.HARVESTED,
            verificationState = VerificationState.PENDING,
            qrCodeValue = null,
            description = null,
            imageUrl = null,
            createdTimestamp = null
        )

        val result = useCase(invalidProduct)
        assertTrue(result.isFailure)
        assertEquals("Product name cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun `harvest with zero or negative quantity should fail`() = runTest {
        val invalidProduct = BotanicalProduct(
            id = "1",
            batchId = "BATCH-001",
            name = "Ashwagandha",
            botanicalName = "Withania somnifera",
            category = ProductCategory.ROOT_POWDER,
            cultivationMethod = CultivationMethod.ORGANIC,
            quantityKg = 0.0, // Zero quantity
            harvestDate = "2026-10-17",
            farmLocation = "Neemuch, MP",
            gpsCoordinates = null,
            farmerId = "F-01",
            farmerName = "Ramesh",
            farmerOrg = null,
            status = ProductStatus.HARVESTED,
            verificationState = VerificationState.PENDING,
            qrCodeValue = null,
            description = null,
            imageUrl = null,
            createdTimestamp = null
        )

        val result = useCase(invalidProduct)
        assertTrue(result.isFailure)
        assertEquals("Quantity must be greater than 0 kg", result.exceptionOrNull()?.message)
    }

    @Test
    fun `valid product should be delegated to repository and succeed`() = runTest {
        val validProduct = BotanicalProduct(
            id = "1",
            batchId = "BATCH-001",
            name = "Organic Ashwagandha",
            botanicalName = "Withania somnifera",
            category = ProductCategory.ROOT_POWDER,
            cultivationMethod = CultivationMethod.ORGANIC,
            quantityKg = 150.0,
            harvestDate = "2026-10-17",
            farmLocation = "Neemuch, MP",
            gpsCoordinates = null,
            farmerId = "F-01",
            farmerName = "Ramesh",
            farmerOrg = null,
            status = ProductStatus.HARVESTED,
            verificationState = VerificationState.PENDING,
            qrCodeValue = null,
            description = null,
            imageUrl = null,
            createdTimestamp = null
        )

        coEvery { mockRepository.registerHarvest(validProduct) } returns Result.success(validProduct)

        val result = useCase(validProduct)
        assertTrue(result.isSuccess)
        assertEquals("BATCH-001", result.getOrNull()?.batchId)
    }
}
