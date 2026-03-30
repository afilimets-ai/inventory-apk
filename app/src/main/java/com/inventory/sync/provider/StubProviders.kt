package com.inventory.sync.provider

import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProvider
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings

/** OneDrive — потребує Microsoft Graph SDK (TODO: Phase 3) */
class OneDriveProvider(private val settings: SyncSettings) : SyncProvider {
    override val type = SyncProviderType.ONEDRIVE
    override val supportsExport = true
    override val supportsImport = true
    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String) =
        SyncResult.Failure("OneDrive: інтеграція ще не реалізована")
    override suspend fun import(format: SyncFormat, fileName: String) =
        SyncImportResult.Failure("OneDrive: інтеграція ще не реалізована")
}

/** Google Drive — потребує Google Drive API SDK (TODO: Phase 3) */
class GoogleDriveProvider(private val settings: SyncSettings) : SyncProvider {
    override val type = SyncProviderType.GOOGLE_DRIVE
    override val supportsExport = true
    override val supportsImport = true
    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String) =
        SyncResult.Failure("Google Drive: інтеграція ще не реалізована")
    override suspend fun import(format: SyncFormat, fileName: String) =
        SyncImportResult.Failure("Google Drive: інтеграція ще не реалізована")
}

/** Email — відправка через SMTP (TODO: Phase 3, потребує JavaMail) */
class EmailProvider(private val settings: SyncSettings) : SyncProvider {
    override val type = SyncProviderType.EMAIL
    override val supportsExport = true
    override val supportsImport = false
    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String) =
        SyncResult.Failure("Email: інтеграція ще не реалізована")
    override suspend fun import(format: SyncFormat, fileName: String) =
        SyncImportResult.Failure("Email не підтримує імпорт")
}

/** Telegram Bot — відправка файлу через Bot API (TODO: Phase 3) */
class TelegramProvider(private val settings: SyncSettings) : SyncProvider {
    override val type = SyncProviderType.TELEGRAM
    override val supportsExport = true
    override val supportsImport = false
    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String) =
        SyncResult.Failure("Telegram: інтеграція ще не реалізована")
    override suspend fun import(format: SyncFormat, fileName: String) =
        SyncImportResult.Failure("Telegram не підтримує імпорт")
}

/** 1C HTTP — обмін через REST-сервіс 1С (TODO: Phase 3) */
class OneCProvider(private val settings: SyncSettings) : SyncProvider {
    override val type = SyncProviderType.ONE_C
    override val supportsExport = true
    override val supportsImport = true
    override suspend fun export(data: ByteArray, format: SyncFormat, fileName: String) =
        SyncResult.Failure("1C HTTP: інтеграція ще не реалізована")
    override suspend fun import(format: SyncFormat, fileName: String) =
        SyncImportResult.Failure("1C HTTP: інтеграція ще не реалізована")
}
