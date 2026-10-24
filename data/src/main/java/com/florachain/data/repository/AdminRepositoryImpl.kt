package com.florachain.data.repository

import com.florachain.data.mapper.toCreateRequestDto
import com.florachain.data.mapper.toDomain
import com.florachain.data.remote.FloraChainApi
import com.florachain.data.remote.dto.UpdateReportStatusRequestDto
import com.florachain.domain.model.AuthUser
import com.florachain.domain.model.ReportStatus
import com.florachain.domain.model.SuspiciousReport
import com.florachain.domain.model.UserRole
import com.florachain.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AdminRepositoryImpl(
    private val api: FloraChainApi
) : AdminRepository {

    // In-memory cache for live simulation & offline resilience
    private val _pendingUsers = MutableStateFlow<List<AuthUser>>(getInitialPendingUsers())
    private val _reports = MutableStateFlow<List<SuspiciousReport>>(getInitialReports())

    override fun observePendingUsers(): Flow<List<AuthUser>> = _pendingUsers.asStateFlow()

    override suspend fun getPendingUsers(): Result<List<AuthUser>> {
        return runCatching {
            try {
                val response = api.getPendingUsers()
                if (response.isSuccessful && response.body() != null) {
                    val users = response.body()!!.map { it.toDomain() }
                    _pendingUsers.value = users
                    users
                } else {
                    _pendingUsers.value
                }
            } catch (e: Exception) {
                _pendingUsers.value
            }
        }
    }

    override suspend fun approveUser(userId: String): Result<AuthUser> {
        return runCatching {
            try {
                val response = api.approveUser(userId)
                if (response.isSuccessful && response.body() != null) {
                    val approved = response.body()!!.toDomain()
                    _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
                    approved
                } else {
                    val user = _pendingUsers.value.find { it.id == userId }
                        ?: throw NoSuchElementException("User $userId not found")
                    val updated = user.copy(status = "APPROVED")
                    _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
                    updated
                }
            } catch (e: Exception) {
                val user = _pendingUsers.value.find { it.id == userId }
                    ?: throw NoSuchElementException("User $userId not found")
                val updated = user.copy(status = "APPROVED")
                _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
                updated
            }
        }
    }

    override suspend fun rejectUser(userId: String): Result<Unit> {
        return runCatching {
            try {
                val response = api.rejectUser(userId)
                if (response.isSuccessful) {
                    _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
                } else {
                    _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
                }
            } catch (e: Exception) {
                _pendingUsers.value = _pendingUsers.value.filter { it.id != userId }
            }
        }
    }

    override fun observeSuspiciousReports(): Flow<List<SuspiciousReport>> = _reports.asStateFlow()

    override suspend fun getSuspiciousReports(status: ReportStatus?): Result<List<SuspiciousReport>> {
        return runCatching {
            try {
                val response = api.getReports(status?.name)
                if (response.isSuccessful && response.body() != null) {
                    val fetched = response.body()!!.map { it.toDomain() }
                    _reports.value = fetched
                    if (status != null) fetched.filter { it.status == status } else fetched
                } else {
                    if (status != null) _reports.value.filter { it.status == status } else _reports.value
                }
            } catch (e: Exception) {
                if (status != null) _reports.value.filter { it.status == status } else _reports.value
            }
        }
    }

    override suspend fun updateReportStatus(
        reportId: String,
        status: ReportStatus,
        notes: String
    ): Result<SuspiciousReport> {
        return runCatching {
            try {
                val response = api.updateReportStatus(
                    reportId,
                    UpdateReportStatusRequestDto(status = status.name, adminNotes = notes)
                )
                if (response.isSuccessful && response.body() != null) {
                    val updated = response.body()!!.toDomain()
                    _reports.value = _reports.value.map { if (it.id == reportId) updated else it }
                    updated
                } else {
                    val existing = _reports.value.find { it.id == reportId }
                        ?: throw NoSuchElementException("Report $reportId not found")
                    val updated = existing.copy(status = status, adminNotes = notes)
                    _reports.value = _reports.value.map { if (it.id == reportId) updated else it }
                    updated
                }
            } catch (e: Exception) {
                val existing = _reports.value.find { it.id == reportId }
                    ?: throw NoSuchElementException("Report $reportId not found")
                val updated = existing.copy(status = status, adminNotes = notes)
                _reports.value = _reports.value.map { if (it.id == reportId) updated else it }
                updated
            }
        }
    }

    override suspend fun submitSuspiciousReport(report: SuspiciousReport): Result<SuspiciousReport> {
        return runCatching {
            try {
                val response = api.submitReport(report.toCreateRequestDto())
                if (response.isSuccessful && response.body() != null) {
                    val created = response.body()!!.toDomain()
                    _reports.value = listOf(created) + _reports.value
                    created
                } else {
                    val localReport = report.copy(
                        id = if (report.id.isBlank()) "REP-" + UUID.randomUUID().toString().take(8).uppercase() else report.id
                    )
                    _reports.value = listOf(localReport) + _reports.value
                    localReport
                }
            } catch (e: Exception) {
                val localReport = report.copy(
                    id = if (report.id.isBlank()) "REP-" + UUID.randomUUID().toString().take(8).uppercase() else report.id
                )
                _reports.value = listOf(localReport) + _reports.value
                localReport
            }
        }
    }

    private fun getInitialPendingUsers(): List<AuthUser> {
        return listOf(
            AuthUser(
                id = "USER-PENDING-001",
                name = "Devendra Sharma",
                email = "devendra.organic@krishi.in",
                role = UserRole.FARMER,
                organization = "Malwa Organic Herb Growers Collective",
                location = "Neemuch, Madhya Pradesh",
                status = "PENDING_APPROVAL",
                joinedDate = "2026-03-12",
                certifications = listOf("NPOP India Organic", "Jaivik Bharat"),
                avatarUrl = null,
                walletAddress = "0x892a...c4b1",
                aadhaarNumber = "XXXXXXXX4821"
            ),
            AuthUser(
                id = "USER-PENDING-002",
                name = "Dr. Sunita Deshmukh",
                email = "sunita.qa@herbalassays.com",
                role = UserRole.LABORATORY,
                organization = "BioAnalytical HPLC Research Center",
                location = "Navi Mumbai, Maharashtra",
                status = "PENDING_APPROVAL",
                joinedDate = "2026-03-14",
                certifications = listOf("NABL ISO/IEC 17025:2017", "AYUSH Approved"),
                avatarUrl = null,
                walletAddress = "0x44f1...908e",
                aadhaarNumber = "XXXXXXXX9912"
            ),
            AuthUser(
                id = "USER-PENDING-003",
                name = "Vikram Transport Logistics",
                email = "coldchain@vikramlogistics.in",
                role = UserRole.DISTRIBUTOR,
                organization = "Sahyadri Cold Chain Logistics Pvt Ltd",
                location = "Pune, Maharashtra",
                status = "PENDING_APPROVAL",
                joinedDate = "2026-03-15",
                certifications = listOf("FSSAI Cold Chain Certified", "GPS Telematics Enabled"),
                avatarUrl = null,
                walletAddress = "0x12b8...a76c",
                aadhaarNumber = "XXXXXXXX3381"
            )
        )
    }

    private fun getInitialReports(): List<SuspiciousReport> {
        return listOf(
            SuspiciousReport(
                id = "REP-2026-0041",
                batchId = "BOT-2024-8901",
                reporterName = "Aakash Mehta",
                reporterContact = "+91 98201 44810",
                issueType = "Broken Packaging Security Seal",
                description = "Jar tamper band was partially ruptured on arrival. Seal hologram does not reflect green under flashlight.",
                evidencePhotoUrl = null,
                status = ReportStatus.INVESTIGATING,
                adminNotes = "Contacted distributor cold-chain manager. Retaining packaging for physical lab assay.",
                createdAt = "2026-03-15T11:30:00Z"
            ),
            SuspiciousReport(
                id = "REP-2026-0039",
                batchId = "TUR-2024-102",
                reporterName = "Dr. Neha Rao",
                reporterContact = "dr.neha@ayurveda-clinic.org",
                issueType = "Mismatched Curcumin Assay",
                description = "In-clinic thin-layer test indicated significantly lower curcuminoid density than the 5.2% stated on the label.",
                evidencePhotoUrl = null,
                status = ReportStatus.PENDING_REVIEW,
                adminNotes = null,
                createdAt = "2026-03-14T09:15:00Z"
            ),
            SuspiciousReport(
                id = "REP-2026-0028",
                batchId = "TUL-2024-033",
                reporterName = "Pooja Kadam",
                reporterContact = "pooja.kadam@gmail.com",
                issueType = "Counterfeit QR Code Redirection",
                description = "QR code on secondary box was pointing to an unverified third-party URL rather than florachain domain.",
                evidencePhotoUrl = null,
                status = ReportStatus.RESOLVED_VALID,
                adminNotes = "Confirmed counterfeit outer sleeve packaging. Retailer notified and shelf batch quarantined immediately.",
                createdAt = "2026-03-10T16:45:00Z"
            )
        )
    }
}
