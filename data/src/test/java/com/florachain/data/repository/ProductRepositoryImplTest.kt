package com.florachain.data.repository

import com.florachain.data.local.dao.ProductDao
import com.florachain.data.local.dao.SyncQueueDao
import com.florachain.data.local.entity.ProductEntity
import com.florachain.data.local.entity.SyncQueueEntity
import com.florachain.data.remote.FloraChainApi
import com.florachain.domain.model.BotanicalProduct
import com.florachain.domain.model.CultivationMethod
import com.florachain.domain.model.ProductCategory
import com.florachain.domain.model.ProductStatus
import com.florachain.domain.model.VerificationState
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ProductRepositoryImplTest {

    private val api = mockk<FloraChainApi>()
    private val productDao = mockk<ProductDao>(relaxed = true)
    private val syncQueueDao = mockk<SyncQueueDao>(relaxed = true)

    private lateinit var repository: ProductRepositoryImpl

    private val dummyEntity = ProductEntity(
        id = "PROD-001",
        batchId = "BOT-2024-8901",
        name = "Organic Ashwagandha",
        botanicalName = "Withania somnifera",
        category = "HERB",
        cultivationMethod = "ORGANIC",
        quantityKg = 250.0,
        harvestDate = "2026-03-10",
        farmLocation = "Mandsaur, MP",
        latitude = 24.0,
        longitude = 75.0,
        farmerId = "FARMER-101",
        farmerName = "Ramesh Patel",
        farmerOrg = null,
        status = "APPROVED",
        verificationState = "VERIFIED",
        qrCodeValue = null,
        description = null,
        imageUrl = null,
        createdTimestamp = "2026-03-10T08:30:00Z",
        activeCompounds = "Withanolides",
        isLocalPending = false
    )

    private val dummyProduct = BotanicalProduct(
        id = "PROD-002",
        batchId = "BOT-2024-9999",
        name = "Organic Tulsi",
        botanicalName = "Ocimum sanctum",
        category = ProductCategory.HERB,
        cultivationMethod = CultivationMethod.ORGANIC,
        quantityKg = 100.0,
        harvestDate = "2026-03-12",
        farmLocation = "Varanasi, UP",
        gpsCoordinates = null,
        farmerId = "FARMER-102",
        farmerName = "Suresh Kumar",
        farmerOrg = null,
        status = ProductStatus.HARVESTED,
        verificationState = VerificationState.PENDING,
        qrCodeValue = null,
        description = null,
        imageUrl = null,
        createdTimestamp = null
    )

    @Before
    fun setUp() {
        repository = ProductRepositoryImpl(
            api = api,
            productDao = productDao,
            syncQueueDao = syncQueueDao
        )
    }

    @Test
    fun `getProducts returns local cached list when remote api throws exception`() = runTest {
        coEvery { api.getProducts(any(), any(), any()) } throws IOException("Network unreachable")
        coEvery { api.getSyncProducts(any()) } throws IOException("Network unreachable")
        coEvery { productDao.getAllProducts() } returns listOf(dummyEntity)

        val result = repository.getProducts()

        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(1, list.size)
        assertEquals("BOT-2024-8901", list[0].batchId)
        coVerify(exactly = 1) { productDao.getAllProducts() }
    }

    @Test
    fun `registerHarvest queues offline action when remote api fails with IOException`() = runTest {
        coEvery { api.registerProduct(any()) } throws IOException("Offline connection error")
        coEvery { syncQueueDao.insertAction(any()) } returns 1L
        coEvery { productDao.insertOrUpdate(any()) } just Runs

        val result = repository.registerHarvest(dummyProduct)

        assertTrue(result.isSuccess)
        val registered = result.getOrThrow()
        assertTrue(registered.isLocalPending)

        // Verify inserted into sync queue and local room dao
        coVerify(exactly = 1) { syncQueueDao.insertAction(match { it.actionType == "REGISTER_HARVEST" }) }
        coVerify(exactly = 1) { productDao.insertOrUpdate(match { it.isLocalPending }) }
    }

    @Test
    fun `getProductByIdOrBatch returns local product when remote api fails`() = runTest {
        coEvery { api.getProductById("BOT-2024-8901") } throws IOException("No connection")
        coEvery { productDao.getProductByIdOrBatch("BOT-2024-8901") } returns dummyEntity

        val result = repository.getProductByIdOrBatch("BOT-2024-8901")

        assertTrue(result.isSuccess)
        val product = result.getOrThrow()
        assertEquals("BOT-2024-8901", product.batchId)
        assertEquals("Organic Ashwagandha", product.name)
    }
}

