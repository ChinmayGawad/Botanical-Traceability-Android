package com.florachain.data.blockchain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class Web3jClient(
    private val rpcUrl: String = "https://polygon-amoy-bor-rpc.publicnode.com"
) {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getLatestBlockNumber(): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("method", "eth_blockNumber")
                put("params", org.json.JSONArray())
                put("id", 1)
            }

            val request = Request.Builder()
                .url(rpcUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: throw IllegalStateException("Empty RPC response")
                val json = JSONObject(body)
                val hexBlock = json.getString("result")
                java.lang.Long.decode(hexBlock)
            }
        }
    }

    suspend fun verifyTransaction(txHash: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            if (txHash.isBlank() || txHash.startsWith("0x0000000000000000")) return@runCatching false

            val payload = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("method", "eth_getTransactionReceipt")
                val params = org.json.JSONArray().apply { put(txHash) }
                put("params", params)
                put("id", 1)
            }

            val request = Request.Builder()
                .url(rpcUrl)
                .post(payload.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@runCatching false
                val json = JSONObject(body)
                if (json.has("result") && !json.isNull("result")) {
                    val receipt = json.getJSONObject("result")
                    val status = receipt.optString("status", "0x0")
                    status == "0x1"
                } else {
                    false
                }
            }
        }
    }
}
