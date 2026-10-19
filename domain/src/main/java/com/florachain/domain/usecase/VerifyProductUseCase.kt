package com.florachain.domain.usecase

import com.florachain.domain.model.VerificationResult
import com.florachain.domain.repository.ProductRepository

class VerifyProductUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(identifier: String): Result<VerificationResult> {
        val cleanIdentifier = extractBatchId(identifier)
        if (cleanIdentifier.isBlank()) {
            return Result.failure(IllegalArgumentException("Batch identifier cannot be blank"))
        }
        return repository.verifyProduct(cleanIdentifier)
    }

    fun extractBatchId(raw: String): String {
        var input = raw.trim()
        if (input.isBlank()) return ""

        // Trim trailing slashes or whitespace
        input = input.trimEnd('/')

        // 1. Check for query parameters like ?batchId=... or &batchId=...
        if (input.contains("?")) {
            val queryString = input.substringAfter("?")
            val params = queryString.split("&")
            for (param in params) {
                val parts = param.split("=")
                if (parts.size == 2) {
                    val key = parts[0].trim().lowercase()
                    val value = parts[1].trim()
                    if (key in listOf("batchid", "batch", "id", "identifier") && value.isNotBlank()) {
                        return value
                    }
                }
            }
        }

        // 2. Check for URL path patterns (e.g. /verify/BOT-2024-8901, /batch/..., /products/...)
        val pathWithoutQuery = input.substringBefore("?")
        if (pathWithoutQuery.contains("/verify/")) {
            val candidate = pathWithoutQuery.substringAfterLast("/verify/").trimEnd('/')
            if (candidate.isNotBlank() && !candidate.contains("/")) {
                return candidate
            }
        }
        if (pathWithoutQuery.contains("/batch/")) {
            val candidate = pathWithoutQuery.substringAfterLast("/batch/").trimEnd('/')
            if (candidate.isNotBlank() && !candidate.contains("/")) {
                return candidate
            }
        }
        if (pathWithoutQuery.contains("/products/")) {
            val candidate = pathWithoutQuery.substringAfterLast("/products/").trimEnd('/')
            if (candidate.isNotBlank() && !candidate.contains("/")) {
                return candidate
            }
        }

        // 3. Remove known custom scheme prefixes
        var cleaned = input.removePrefix("florachain:batch:")
            .removePrefix("florachain://verify?batchId=")
            .removePrefix("florachain://")

        // 4. If it still looks like an HTTP/HTTPS URL, take the last path segment
        if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
            cleaned = cleaned.substringBefore("?").trimEnd('/').substringAfterLast("/")
        }

        return cleaned.trim()
    }
}
