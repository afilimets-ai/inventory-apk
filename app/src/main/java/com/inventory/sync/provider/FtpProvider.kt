package com.inventory.sync.provider

import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProvider
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class FtpProvider(private val settings: SyncSettings) : SyncProvider {

    override val type = SyncProviderType.FTP
    override val supportsExport = true
    override val supportsImport = true

    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String): SyncResult =
        withContext(Dispatchers.IO) {
            val ftp = FTPClient()
            try {
                ftp.connect(settings.host, if (settings.port > 0) settings.port else 21)
                ftp.login(settings.username, settings.password)
                ftp.enterLocalPassiveMode()
                ftp.setFileType(FTP.BINARY_FILE_TYPE)

                val remotePath = buildRemotePath(settings.path, "$fileName.${format.extension}")
                val success = ftp.storeFile(remotePath, ByteArrayInputStream(data))
                if (success) SyncResult.Success
                else SyncResult.Failure("FTP storeFile повернув false для $remotePath")
            } catch (e: Exception) {
                SyncResult.Failure("FTP помилка експорту: ${e.message}", e)
            } finally {
                runCatching { if (ftp.isConnected) { ftp.logout(); ftp.disconnect() } }
            }
        }

    override suspend fun import(format: SyncFormat, fileName: String): SyncImportResult =
        withContext(Dispatchers.IO) {
            val ftp = FTPClient()
            try {
                ftp.connect(settings.host, if (settings.port > 0) settings.port else 21)
                ftp.login(settings.username, settings.password)
                ftp.enterLocalPassiveMode()
                ftp.setFileType(FTP.BINARY_FILE_TYPE)

                val remotePath = buildRemotePath(settings.path, "$fileName.${format.extension}")
                val out = ByteArrayOutputStream()
                val success = ftp.retrieveFile(remotePath, out)
                if (success) SyncImportResult.Success(out.toByteArray())
                else SyncImportResult.Failure("FTP retrieveFile повернув false для $remotePath")
            } catch (e: Exception) {
                SyncImportResult.Failure("FTP помилка імпорту: ${e.message}", e)
            } finally {
                runCatching { if (ftp.isConnected) { ftp.logout(); ftp.disconnect() } }
            }
        }

    private fun buildRemotePath(dir: String, file: String): String {
        val cleanDir = dir.trimEnd('/')
        return if (cleanDir.isEmpty()) file else "$cleanDir/$file"
    }
}
