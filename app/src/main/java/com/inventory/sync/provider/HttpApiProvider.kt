package com.inventory.sync.provider

import com.inventory.data.entity.OutboxEntry
import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProvider
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class HttpApiProvider(
    private val settings: SyncSettings,
    private val client: OkHttpClient
) : SyncProvider {

    override val type = SyncProviderType.HTTP_API
    override val supportsExport = true
    override val supportsImport = true
    override val supportsOutbox = true

    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                val url = buildUrl(settings.apiUrl, "export/$fileName.${format.extension}")
                val body = data.toRequestBody(format.toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .addAuthHeader(settings.apiToken)
                    .put(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) SyncResult.Success
                    else SyncResult.Failure("HTTP помилка ${response.code}: ${response.message}")
                }
            } catch (e: Exception) {
                SyncResult.Failure("HTTP помилка експорту: ${e.message}", e)
            }
        }

    override suspend fun sendOutbox(entry: OutboxEntry): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                val url = buildUrl(settings.apiUrl, "operations")
                val body = entry.payload.toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(url)
                    .addAuthHeader(settings.apiToken)
                    .header("Idempotency-Key", entry.idempotencyKey)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) SyncResult.Success
                    else SyncResult.Failure("HTTP outbox помилка ${response.code}: ${response.message}")
                }
            } catch (e: Exception) {
                SyncResult.Failure("HTTP помилка outbox: ${e.message}", e)
            }
        }

    override suspend fun import(format: SyncFormat, fileName: String): SyncImportResult =
        withContext(Dispatchers.IO) {
            try {
                val url = buildUrl(settings.apiUrl, "import/$fileName.${format.extension}")
                val request = Request.Builder()
                    .url(url)
                    .addAuthHeader(settings.apiToken)
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bytes = response.body?.bytes() ?: ByteArray(0)
                        SyncImportResult.Success(bytes)
                    } else {
                        SyncImportResult.Failure("HTTP помилка ${response.code}: ${response.message}")
                    }
                }
            } catch (e: Exception) {
                SyncImportResult.Failure("HTTP помилка імпорту: ${e.message}", e)
            }
        }

    private fun buildUrl(base: String, path: String): String {
        val cleanBase = base.trimEnd('/')
        return "$cleanBase/$path"
    }

    private fun Request.Builder.addAuthHeader(token: String): Request.Builder {
        return if (token.isNotBlank()) addHeader("Authorization", "Bearer $token") else this
    }

    private fun SyncFormat.toMediaType() = when (this) {
        SyncFormat.CSV -> "text/csv".toMediaType()
        SyncFormat.JSON -> "application/json".toMediaType()
        SyncFormat.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaType()
    }
}
