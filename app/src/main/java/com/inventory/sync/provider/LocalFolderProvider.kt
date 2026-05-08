package com.inventory.sync.provider

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProvider
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings
import java.io.File

/**
 * Локальний провайдер синхронізації через SAF або прямий шлях до папки.
 * Прямий шлях потрібен для промислових Android-пристроїв, де SAF picker недоступний або нестабільний.
 */
class LocalFolderProvider(
    private val settings: SyncSettings,
    private val context: Context
) : SyncProvider {

    override val type = SyncProviderType.LOCAL_FOLDER
    override val supportsExport = true
    override val supportsImport = true

    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String): SyncResult {
        if (settings.path.isBlank()) {
            return SyncResult.Failure("Папку не вибрано. Вкажіть папку у налаштуваннях провайдера.")
        }

        return try {
            val fullName = "$fileName.${format.extension}"
            if (settings.path.isFilePath()) {
                val dir = settings.path.toDirectory()
                    ?: return SyncResult.Failure("Папка недоступна або не існує: ${settings.path}")
                val target = File(dir, fullName)
                target.outputStream().use { it.write(data) }
                SyncResult.Success
            } else {
                val dir = settings.path.toDocumentDirectory()
                    ?: return SyncResult.Failure("Не вдалося відкрити папку: ${settings.path}. Виберіть папку ще раз.")
                val existing = dir.findFile(fullName)
                val docFile = existing ?: dir.createFile(
                    mimeForFormat(format), fullName
                ) ?: return SyncResult.Failure("Не вдалося створити файл: $fullName")

                context.contentResolver.openOutputStream(docFile.uri, "wt")?.use { out ->
                    out.write(data)
                } ?: return SyncResult.Failure("Не вдалося записати у файл: $fullName")

                SyncResult.Success
            }
        } catch (e: Exception) {
            SyncResult.Failure("Помилка запису у локальну папку: ${e.message}", e)
        }
    }

    override suspend fun import(format: SyncFormat, fileName: String): SyncImportResult {
        if (settings.path.isBlank()) {
            return SyncImportResult.Failure("Папку не вибрано. Вкажіть папку у налаштуваннях провайдера.")
        }

        return try {
            val fullName = "$fileName.${format.extension}"
            if (settings.path.isFilePath()) {
                val dir = settings.path.toDirectory()
                    ?: return SyncImportResult.Failure("Папка недоступна або не існує: ${settings.path}")
                val file = File(dir, fullName)
                if (!file.isFile) return SyncImportResult.Failure("Файл не знайдено: ${file.absolutePath}")
                SyncImportResult.Success(file.readBytes())
            } else {
                val dir = settings.path.toDocumentDirectory()
                    ?: return SyncImportResult.Failure("Не вдалося відкрити папку: ${settings.path}. Виберіть папку ще раз.")
                val docFile = dir.findFile(fullName)
                    ?: return SyncImportResult.Failure("Файл не знайдено: $fullName")

                val bytes = context.contentResolver.openInputStream(docFile.uri)?.use { it.readBytes() }
                    ?: return SyncImportResult.Failure("Не вдалося прочитати файл: $fullName")

                SyncImportResult.Success(bytes)
            }
        } catch (e: Exception) {
            SyncImportResult.Failure("Помилка читання з локальної папки: ${e.message}", e)
        }
    }

    override suspend fun discoverImportFiles(format: SyncFormat): List<String> {
        if (settings.path.isBlank()) return emptyList()

        return try {
            val ext = ".${format.extension}"
            if (settings.path.isFilePath()) {
                val dir = settings.path.toDirectory() ?: return emptyList()
                dir.listFiles()
                    ?.filter { it.isFile && it.name.endsWith(ext, ignoreCase = true) }
                    ?.sortedByDescending { it.lastModified() }
                    ?.map { it.name.removeExtension(ext) }
                    ?: emptyList()
            } else {
                val dir = settings.path.toDocumentDirectory() ?: return emptyList()
                dir.listFiles()
                    .filter { it.isFile && (it.name ?: "").endsWith(ext, ignoreCase = true) }
                    .sortedByDescending { it.lastModified() }
                    .mapNotNull { it.name?.removeExtension(ext) }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun mimeForFormat(format: SyncFormat): String = when (format) {
        SyncFormat.CSV -> "text/csv"
        SyncFormat.JSON -> "application/json"
        SyncFormat.EXCEL -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    }

    private fun String.isFilePath(): Boolean =
        startsWith("/") || startsWith("file://")

    private fun String.toDirectory(): File? {
        val rawPath = if (startsWith("file://")) {
            Uri.parse(this).path ?: removePrefix("file://")
        } else {
            this
        }
        val candidates = buildList {
            add(File(rawPath))
            if (rawPath.startsWith("/sdcard/")) {
                add(File(Environment.getExternalStorageDirectory(), rawPath.removePrefix("/sdcard/")))
            }
        }

        return candidates.firstOrNull { it.isDirectory && it.canRead() }
    }

    private fun String.toDocumentDirectory(): DocumentFile? =
        DocumentFile.fromTreeUri(context, Uri.parse(this))

    private fun String.removeExtension(extension: String): String =
        if (endsWith(extension, ignoreCase = true)) dropLast(extension.length) else this
}
