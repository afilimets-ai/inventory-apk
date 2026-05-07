package com.inventory.sync.provider

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProvider
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings

/**
 * Локальний провайдер синхронізації через SAF (Storage Access Framework).
 * settings.path зберігає URI папки, вибраної через системний файл-менеджер.
 */
class LocalFolderProvider(
    private val settings: SyncSettings,
    private val context: Context
) : SyncProvider {

    override val type = SyncProviderType.LOCAL_FOLDER
    override val supportsExport = true
    override val supportsImport = true

    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String): SyncResult {
        return try {
            val dirUri = Uri.parse(settings.path)
            val dir = DocumentFile.fromTreeUri(context, dirUri)
                ?: return SyncResult.Failure("Не вдалося відкрити папку: ${settings.path}")

            val fullName = "$fileName.${format.extension}"
            // Шукаємо існуючий файл або створюємо новий
            val existing = dir.findFile(fullName)
            val docFile = existing ?: dir.createFile(
                mimeForFormat(format), fullName
            ) ?: return SyncResult.Failure("Не вдалося створити файл: $fullName")

            context.contentResolver.openOutputStream(docFile.uri, "wt")?.use { out ->
                out.write(data)
            } ?: return SyncResult.Failure("Не вдалося записати у файл: $fullName")

            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Failure("Помилка запису у локальну папку: ${e.message}", e)
        }
    }

    override suspend fun import(format: SyncFormat, fileName: String): SyncImportResult {
        return try {
            val dirUri = Uri.parse(settings.path)
            val dir = DocumentFile.fromTreeUri(context, dirUri)
                ?: return SyncImportResult.Failure("Не вдалося відкрити папку: ${settings.path}")

            val fullName = "$fileName.${format.extension}"
            val docFile = dir.findFile(fullName)
                ?: return SyncImportResult.Failure("Файл не знайдено: $fullName")

            val bytes = context.contentResolver.openInputStream(docFile.uri)?.use { it.readBytes() }
                ?: return SyncImportResult.Failure("Не вдалося прочитати файл: $fullName")

            SyncImportResult.Success(bytes)
        } catch (e: Exception) {
            SyncImportResult.Failure("Помилка читання з локальної папки: ${e.message}", e)
        }
    }

    override suspend fun discoverImportFiles(format: SyncFormat): List<String> {
        return try {
            val dirUri = Uri.parse(settings.path)
            val dir = DocumentFile.fromTreeUri(context, dirUri) ?: return emptyList()

            val ext = ".${format.extension}"
            dir.listFiles()
                .filter { it.isFile && (it.name ?: "").endsWith(ext, ignoreCase = true) }
                .sortedByDescending { it.lastModified() }
                .mapNotNull { it.name?.removeSuffix(ext) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun mimeForFormat(format: SyncFormat): String = when (format) {
        SyncFormat.CSV -> "text/csv"
        SyncFormat.JSON -> "application/json"
        SyncFormat.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    }
}
