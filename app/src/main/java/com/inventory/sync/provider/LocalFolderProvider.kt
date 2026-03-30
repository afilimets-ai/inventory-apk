package com.inventory.sync.provider

import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProvider
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings
import java.io.File

class LocalFolderProvider(private val settings: SyncSettings) : SyncProvider {

    override val type = SyncProviderType.LOCAL_FOLDER
    override val supportsExport = true
    override val supportsImport = true

    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String): SyncResult {
        return try {
            val dir = File(settings.path)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "$fileName.${format.extension}")
            file.writeBytes(data)
            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Failure("Помилка запису у локальну папку: ${e.message}", e)
        }
    }

    override suspend fun import(format: SyncFormat, fileName: String): SyncImportResult {
        return try {
            val file = File(settings.path, "$fileName.${format.extension}")
            if (!file.exists()) {
                return SyncImportResult.Failure("Файл не знайдено: ${file.absolutePath}")
            }
            SyncImportResult.Success(file.readBytes())
        } catch (e: Exception) {
            SyncImportResult.Failure("Помилка читання з локальної папки: ${e.message}", e)
        }
    }
}
