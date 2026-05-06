package com.inventory.sync

/**
 * Типи провайдерів синхронізації.
 * Кожен провайдер може бути вибраний для імпорту та/або експорту незалежно.
 */
enum class SyncProviderType(val displayName: String) {
    LOCAL_FOLDER("Локальна папка"),
    HTTP_API("HTTP API"),
    FTP("FTP"),
    WEBDAV("WebDAV"),
    ONEDRIVE("OneDrive"),
    GOOGLE_DRIVE("Google Drive"),
    EMAIL("Електронна пошта"),
    TELEGRAM("Telegram Bot"),
    ONE_C("1C HTTP"),
    SFTP("SFTP")
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

    /**
     * Експортує дані у вигляді байтів (вже серіалізовано).
     * @param data байти файлу (CSV/JSON/Excel)
     * @param format формат файлу
     * @param fileName ім'я файлу без розширення
     */
    suspend fun export(data: ByteArray, format: SyncFormat, fileName: String): SyncResult

    /**
     * Імпортує дані, повертає байти файлу для десеріалізації.
     * @param format очікуваний формат
     * @param fileName ім'я файлу без розширення (якщо потрібно)
     */
    suspend fun import(format: SyncFormat, fileName: String): SyncImportResult
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

/** Стан фонової синхронізації для UI */
sealed class SyncState {
    object Idle : SyncState()
    object Running : SyncState()
    data class Success(val timestamp: Long) : SyncState()
    data class Error(val message: String) : SyncState()
}
