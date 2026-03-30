package com.inventory.sync.provider

import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProvider
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class WebDavProvider(private val settings: SyncSettings) : SyncProvider {

    override val type = SyncProviderType.WEBDAV
    override val supportsExport = true
    override val supportsImport = true

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                val url = buildUrl("$fileName.${format.extension}")
                val body = data.toRequestBody("application/octet-stream".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", credentials())
                    .put(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful || response.code == 201) SyncResult.Success
                    else SyncResult.Failure("WebDAV PUT помилка ${response.code}")
                }
            } catch (e: Exception) {
                SyncResult.Failure("WebDAV помилка експорту: ${e.message}", e)
            }
        }

    override suspend fun import(format: SyncFormat, fileName: String): SyncImportResult =
        withContext(Dispatchers.IO) {
            try {
                val url = buildUrl("$fileName.${format.extension}")
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", credentials())
                    .get()
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        SyncImportResult.Success(response.body?.bytes() ?: ByteArray(0))
                    } else {
                        SyncImportResult.Failure("WebDAV GET помилка ${response.code}")
                    }
                }
            } catch (e: Exception) {
                SyncImportResult.Failure("WebDAV помилка імпорту: ${e.message}", e)
            }
        }

    private fun buildUrl(file: String): String {
        val base = settings.webDavUrl.trimEnd('/')
        val path = settings.path.trim('/')
        return if (path.isEmpty()) "$base/$file" else "$base/$path/$file"
    }

    private fun credentials() = Credentials.basic(settings.username, settings.password)
}
