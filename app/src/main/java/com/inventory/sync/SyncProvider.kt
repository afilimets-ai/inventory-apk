package com.inventory.sync

import com.inventory.data.entity.OutboxEntry
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.ImportPreview

/**
 * Типи провайдерів синхронізації.
 * Кожен провайдер може бути вибраний для імпорту та/або експорту незалежно.
 */
enum class SyncProviderType(val displayName: String) {
    LOCAL_FOLDER("Локальна папка"),
    HTTP_API("HTTP API"),
    FTP("FTP"),
    SFTP("SFTP (SSH)"),
    WEBDAV("WebDAV"),
    ONEDRIVE("OneDrive"),
    GOOGLE_DRIVE("Google Drive"),
    EMAIL("Електронна пошта"),
    TELEGRAM("Telegram Bot"),
    ONE_C("1C HTTP")
}

/**
 * Формати файлів для синхронізації.
 */
enum class SyncFormat(val extension: String, val displayName: String) {
    CSV("csv", "CSV"),
    JSON("json", "JSON"),
    EXCEL("xlsx", "Excel")
}

/**
 * Інтерфейс провайдера синхронізації.
 * Кожен провайдер реалізує або експорт, або імпорт (або обидва).
 */
interface SyncProvider {
    val type: SyncProviderType

    /** true якщо провайдер підтримує експорт */
    val supportsExport: Boolean

    /** true якщо провайдер підтримує імпорт */
    val supportsImport: Boolean

    val supportsOutbox: Boolean
        get() = false

    /**
     * Експортує дані у вигляді байтів (вже серіалізовано).
     * @param data байти файлу (CSV/JSON/Excel)
     * @param format формат файлу
     * @param fileName ім'я файлу без розширення
     */
    suspend fun export(data: ByteArray, format: SyncFormat, fileName: String): SyncResult

    suspend fun sendOutbox(entry: OutboxEntry): SyncResult =
        SyncResult.Failure("${type.displayName}: outbox sync is not supported")

    /**
     * Імпортує дані, повертає байти файлу для десеріалізації.
     * @param format очікуваний формат
     * @param fileName ім'я файлу без розширення
     */
    suspend fun import(format: SyncFormat, fileName: String): SyncImportResult

    /**
     * Шукає файли для імпорту за розширенням формату у налаштованому каталозі.
     * Повертає список імен файлів (без розширення), відсортованих від найновішого.
     * Використовується коли ім'я файлу невідоме заздалегідь —
     * облікова система користувача генерує імена самостійно.
     *
     * Провайдери, які не підтримують лістинг (HTTP API, Email, Telegram),
     * повертають порожній список (дефолтна поведінка).
     */
    suspend fun discoverImportFiles(format: SyncFormat): List<String> = emptyList()
}

/** Результат операції синхронізації */
sealed class SyncResult {
    object Success : SyncResult()
    data class Failure(val message: String, val cause: Throwable? = null) : SyncResult()
}

/** Результат операції імпорту */
sealed class SyncImportResult {
    data class Success(val data: ByteArray) : SyncImportResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Success) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int = data.contentHashCode()
    }

    data class Failure(val message: String, val cause: Throwable? = null) : SyncImportResult()
}

data class SyncImportedItem(
    val barcode: String,
    val name: String,
    val quantity: Double,
    val unit: String
)

data class SyncImportSummary(
    val providerName: String,
    val fileName: String,
    val formatName: String,
    val totalRows: Int,
    val items: List<SyncImportedItem>
)

data class PendingImportMapping(
    val settings: SyncSettings,
    val fileName: String,
    val preview: ImportPreview,
    val suggestedMapping: ColumnMapping
)

/** Стан фонової синхронізації для UI */
sealed class SyncState {
    object Idle : SyncState()
    object Running : SyncState()
    data class Success(
        val timestamp: Long,
        val importSummary: SyncImportSummary? = null
    ) : SyncState()
    data class PendingMapping(val pending: PendingImportMapping) : SyncState()
    data class Error(val message: String) : SyncState()
}
