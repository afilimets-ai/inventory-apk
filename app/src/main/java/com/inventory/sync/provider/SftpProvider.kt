package com.inventory.sync.provider

import com.inventory.sftp.SftpBridge
import com.inventory.sftp.SftpClientI
import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProvider
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * SFTP sync provider based on libSFTP.so from DCAPP.
 * Uses temp files for data transfer between JVM and native code
 * (native API works with file paths, not byte streams).
 *
 * @param sftpClient injected for tests; in production — SftpBridge
 * @param cacheDir   temp directory for temp files (Context.getCacheDir())
 */
class SftpProvider(
    private val settings: SyncSettings,
    private val sftpClient: SftpClientI = SftpBridge,
    private val cacheDir: File
) : SyncProvider {

    override val type = SyncProviderType.SFTP
    override val supportsExport = true
    override val supportsImport = true

    // Serializes export and import — libSFTP.so holds one global SSH session
    private val mutex = Mutex()

    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String): SyncResult =
        mutex.withLock { withContext(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                if (!sftpClient.init()) {
                    return@withContext SyncResult.Failure("SFTP init failed: ${sftpClient.lastError()}")
                }
                val port = if (settings.port > 0) settings.port else 22
                if (!sftpClient.connect(settings.host, port, settings.username, settings.password)) {
                    sftpClient.free()
                    return@withContext SyncResult.Failure(
                        "SFTP connect failed (${settings.host}:$port): ${sftpClient.lastError()}"
                    )
                }
                tempFile = File(cacheDir, "sftp_export_${System.currentTimeMillis()}.tmp")
                tempFile.writeBytes(data)

                val remotePath = buildRemotePath(settings.path, "$fileName.${format.extension}")
                if (!sftpClient.putFile(tempFile.absolutePath, remotePath, true)) {
                    return@withContext SyncResult.Failure(
                        "SFTP putFile failed for $remotePath: ${sftpClient.lastError()}"
                    )
                }
                SyncResult.Success
            } catch (e: Exception) {
                SyncResult.Failure("SFTP export error: ${e.message}", e)
            } finally {
                tempFile?.delete()
                runCatching { sftpClient.disconnect() }
                runCatching { sftpClient.free() }
            }
        } }

    override suspend fun import(format: SyncFormat, fileName: String): SyncImportResult =
        mutex.withLock { withContext(Dispatchers.IO) {
            var tempFile: File? = null
            try {
                if (!sftpClient.init()) {
                    return@withContext SyncImportResult.Failure("SFTP init failed: ${sftpClient.lastError()}")
                }
                val port = if (settings.port > 0) settings.port else 22
                if (!sftpClient.connect(settings.host, port, settings.username, settings.password)) {
                    sftpClient.free()
                    return@withContext SyncImportResult.Failure(
                        "SFTP connect failed (${settings.host}:$port): ${sftpClient.lastError()}"
                    )
                }
                tempFile = File(cacheDir, "sftp_import_${System.currentTimeMillis()}.tmp")
                val remotePath = buildRemotePath(settings.path, "$fileName.${format.extension}")

                if (!sftpClient.getFile(remotePath, tempFile.absolutePath, true)) {
                    return@withContext SyncImportResult.Failure(
                        "SFTP getFile failed for $remotePath: ${sftpClient.lastError()}"
                    )
                }
                SyncImportResult.Success(tempFile.readBytes())
            } catch (e: Exception) {
                SyncImportResult.Failure("SFTP import error: ${e.message}", e)
            } finally {
                tempFile?.delete()
                runCatching { sftpClient.disconnect() }
                runCatching { sftpClient.free() }
            }
        } }

    private fun buildRemotePath(dir: String, file: String): String {
        val cleanDir = dir.trimEnd('/')
        return if (cleanDir.isEmpty()) file else "$cleanDir/$file"
    }
}
