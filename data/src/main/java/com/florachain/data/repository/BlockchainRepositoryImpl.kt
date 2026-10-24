package com.florachain.data.repository

import com.florachain.data.mapper.toDomain
import com.florachain.data.remote.FloraChainApi
import com.florachain.domain.model.BlockchainTx
import com.florachain.domain.model.NetworkStats
import com.florachain.domain.repository.BlockchainRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BlockchainRepositoryImpl(
    private val api: FloraChainApi
) : BlockchainRepository {

    private val _networkStats = MutableStateFlow<NetworkStats>(getInitialNetworkStats())
    private val _transactions = MutableStateFlow<List<BlockchainTx>>(getInitialTransactions())

    override fun observeNetworkStats(): Flow<NetworkStats> = _networkStats.asStateFlow()

    override suspend fun getNetworkStats(): Result<NetworkStats> {
        return runCatching {
            try {
                val response = api.getNetworkStats()
                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!.toDomain()
                    _networkStats.value = stats
                    stats
                } else {
                    _networkStats.value
                }
            } catch (e: Exception) {
                _networkStats.value
            }
        }
    }

    override suspend fun getAllTransactions(stage: String?): Result<List<BlockchainTx>> {
        return runCatching {
            try {
                val response = api.getAllTransactions()
                if (response.isSuccessful && response.body() != null) {
                    val txs = response.body()!!.map { it.toDomain() }
                    _transactions.value = txs
                    if (!stage.isNullOrBlank() && stage != "ALL") {
                        txs.filter { it.stage.equals(stage, ignoreCase = true) }
                    } else {
                        txs
                    }
                } else {
                    filterTransactionsByStage(_transactions.value, stage)
                }
            } catch (e: Exception) {
                filterTransactionsByStage(_transactions.value, stage)
            }
        }
    }

    override suspend fun getTransactionsByProduct(productId: String): Result<List<BlockchainTx>> {
        return runCatching {
            try {
                val response = api.getTransactionsByProduct(productId)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.map { it.toDomain() }
                } else {
                    _transactions.value.filter {
                        it.rawPayload?.contains(productId, ignoreCase = true) == true ||
                        it.action.contains(productId, ignoreCase = true)
                    }
                }
            } catch (e: Exception) {
                _transactions.value.filter {
                    it.rawPayload?.contains(productId, ignoreCase = true) == true ||
                    it.action.contains(productId, ignoreCase = true)
                }
            }
        }
    }

    private fun filterTransactionsByStage(list: List<BlockchainTx>, stage: String?): List<BlockchainTx> {
        return if (!stage.isNullOrBlank() && stage != "ALL") {
            list.filter { it.stage.equals(stage, ignoreCase = true) }
        } else {
            list
        }
    }

    private fun getInitialNetworkStats(): NetworkStats {
        return NetworkStats(
            blockHeight = 4921084L,
            totalTransactions = 18420L,
            activeNodes = 28,
            gasUsed = 21458900L,
            contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3",
            networkName = "Polygon Amoy Testnet (Chain ID 80002)"
        )
    }

    private fun getInitialTransactions(): List<BlockchainTx> {
        return listOf(
            BlockchainTx(
                txId = "0x7a39d891b24e60fc9019b889104fa218c57e84b901a89c4412ef10",
                blockNumber = 4921084L,
                from = "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC",
                to = "0x5FbDB2315678afecb367f032d93F642f64180aa3",
                stage = "RETAIL",
                action = "INVENTORY_RECEIVE",
                actor = "AyurAushadhi Pharmacy",
                timestamp = "2026-03-15T10:45:00Z",
                status = "CONFIRMED",
                gasUsed = 42100L,
                rawPayload = """{"batchId":"BOT-2024-8901","unitPrice":450.0,"shelfVerified":true}"""
            ),
            BlockchainTx(
                txId = "0x4e12c8901bfa899124e123fc891230198421bcde01a89c3310ff91",
                blockNumber = 4921062L,
                from = "0x90F79bf6EB2c4f870365E785982E1f101E93b906",
                to = "0x5FbDB2315678afecb367f032d93F642f64180aa3",
                stage = "TRANSIT",
                action = "COLD_CHAIN_DISPATCH",
                actor = "FloraLogistics Express",
                timestamp = "2026-03-14T14:20:00Z",
                status = "CONFIRMED",
                gasUsed = 51200L,
                rawPayload = """{"batchId":"BOT-2024-8901","vehicle":"MH-12-QZ-4921","tempRange":"15-22C"}"""
            ),
            BlockchainTx(
                txId = "0x9812ba441cde01a89c8910fa218c57e84b901a89c7a39d891b24e",
                blockNumber = 4921010L,
                from = "0x15d34AAf54267DB7D7c367839AAf71A00a2C6A65",
                to = "0x5FbDB2315678afecb367f032d93F642f64180aa3",
                stage = "LAB",
                action = "HPLC_ASSAY_APPROVAL",
                actor = "National Botanical Testing Lab",
                timestamp = "2026-03-13T16:15:00Z",
                status = "CONFIRMED",
                gasUsed = 68400L,
                rawPayload = """{"batchId":"BOT-2024-8901","purity":5.4,"moisture":7.8,"iso17025":true}"""
            ),
            BlockchainTx(
                txId = "0x3344a19c8910fa218c57e84b901a89c7a39d891b24e60fc9019b8",
                blockNumber = 4920950L,
                from = "0x9965507D1a55bcC2695C58ba16FB37d819B0A4df",
                to = "0x5FbDB2315678afecb367f032d93F642f64180aa3",
                stage = "PROCESS",
                action = "EXTRACTION_BATCH_PROCESSED",
                actor = "BioExtracts GMP Processing Ltd",
                timestamp = "2026-03-12T11:00:00Z",
                status = "CONFIRMED",
                gasUsed = 54100L,
                rawPayload = """{"batchId":"BOT-2024-8901","yieldLoss":4.2,"outputKg":479.0}"""
            ),
            BlockchainTx(
                txId = "0x1122fc9019b889104fa218c57e84b901a89c7a39d891b24e608899",
                blockNumber = 4920800L,
                from = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                to = "0x5FbDB2315678afecb367f032d93F642f64180aa3",
                stage = "HARVEST",
                action = "REGISTER_CROP_ORIGIN",
                actor = "Ramesh Patel",
                timestamp = "2026-03-10T08:30:00Z",
                status = "CONFIRMED",
                gasUsed = 78900L,
                rawPayload = """{"batchId":"BOT-2024-8901","gpsLat":24.4639,"gpsLng":74.8647,"cultivation":"ORGANIC"}"""
            )
        )
    }
}
