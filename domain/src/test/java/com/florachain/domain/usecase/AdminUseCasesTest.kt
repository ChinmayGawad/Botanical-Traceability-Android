package com.florachain.domain.usecase

import com.florachain.domain.model.*
import com.florachain.domain.repository.AdminRepository
import com.florachain.domain.repository.BlockchainRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminUseCasesTest {

    private val mockAdminRepo = mockk<AdminRepository>()
    private val mockBlockchainRepo = mockk<BlockchainRepository>()

    private val approveUserUseCase = ApproveUserUseCase(mockAdminRepo)
    private val rejectUserUseCase = RejectUserUseCase(mockAdminRepo)
    private val submitSuspiciousReportUseCase = SubmitSuspiciousReportUseCase(mockAdminRepo)
    private val getBlockchainStatsUseCase = GetBlockchainStatsUseCase(mockBlockchainRepo)

    @Test
    fun `approveUser with blank id should fail`() = runTest {
        val result = approveUserUseCase("   ")
        assertTrue(result.isFailure)
        assertEquals("User ID cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun `approveUser with valid id should return approved AuthUser`() = runTest {
        val approvedUser = AuthUser(
            id = "USER-1",
            name = "Ramesh",
            email = "ramesh@farm.org",
            role = UserRole.FARMER,
            organization = "Malwa Collective",
            location = "Neemuch",
            status = "APPROVED",
            joinedDate = "2026-03-10",
            certifications = emptyList(),
            avatarUrl = null,
            walletAddress = null,
            aadhaarNumber = null
        )
        coEvery { mockAdminRepo.approveUser("USER-1") } returns Result.success(approvedUser)

        val result = approveUserUseCase("USER-1")
        assertTrue(result.isSuccess)
        assertEquals("APPROVED", result.getOrNull()?.status)
    }

    @Test
    fun `rejectUser with blank id should fail`() = runTest {
        val result = rejectUserUseCase("")
        assertTrue(result.isFailure)
    }

    @Test
    fun `submitSuspiciousReport with blank batchId should fail`() = runTest {
        val invalidReport = SuspiciousReport(
            id = "",
            batchId = "",
            reporterName = "John",
            reporterContact = null,
            issueType = "Seal Rupture",
            description = "Tampered band",
            evidencePhotoUrl = null,
            status = ReportStatus.PENDING_REVIEW,
            adminNotes = null,
            createdAt = "2026-03-15"
        )
        val result = submitSuspiciousReportUseCase(invalidReport)
        assertTrue(result.isFailure)
        assertEquals("Batch ID cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun `submitSuspiciousReport with valid payload should succeed`() = runTest {
        val validReport = SuspiciousReport(
            id = "REP-01",
            batchId = "BOT-2024-8901",
            reporterName = "Aakash",
            reporterContact = "+919876543210",
            issueType = "Seal Rupture",
            description = "Tampered band observed",
            evidencePhotoUrl = null,
            status = ReportStatus.PENDING_REVIEW,
            adminNotes = null,
            createdAt = "2026-03-15"
        )
        coEvery { mockAdminRepo.submitSuspiciousReport(validReport) } returns Result.success(validReport)

        val result = submitSuspiciousReportUseCase(validReport)
        assertTrue(result.isSuccess)
        assertEquals("REP-01", result.getOrNull()?.id)
    }

    @Test
    fun `getBlockchainStats should return block height and active nodes`() = runTest {
        val stats = NetworkStats(
            blockHeight = 4921084L,
            totalTransactions = 18420L,
            activeNodes = 28,
            gasUsed = 21458900L,
            contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3"
        )
        coEvery { mockBlockchainRepo.getNetworkStats() } returns Result.success(stats)

        val result = getBlockchainStatsUseCase()
        assertTrue(result.isSuccess)
        assertEquals(4921084L, result.getOrNull()?.blockHeight)
        assertEquals(28, result.getOrNull()?.activeNodes)
    }
}
