package com.inventory.sync

import java.io.Serializable

/**
 * Налаштування для кожного провайдера синхронізації.
 * Зберігаються в SharedPreferences у форматі JSON.
 */
data class SyncSettings(
    val providerType: SyncProviderType,
    val isImportEnabled: Boolean = false,
    val isExportEnabled: Boolean = false,
    val format: SyncFormat = SyncFormat.CSV,
    // Спільні поля
    val host: String = "",
    val port: Int = 0,
    val username: String = "",
    val password: String = "",
    val path: String = "",
    // HTTP API
    val apiUrl: String = "",
    val apiToken: String = "",
    // Email
    val smtpHost: String = "",
    val smtpPort: Int = 587,
    val emailFrom: String = "",
    val emailTo: String = "",
    // Telegram
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    // 1C
    val oneCUrl: String = "",
    val oneCLogin: String = "",
    val oneCPassword: String = "",
    // OneDrive / Google Drive — OAuth токени зберігаються окремо через AccountManager
    val cloudFolderPath: String = "",
    // WebDAV
    val webDavUrl: String = "",
    // Ім'я файлу (без розширення) — генерується обліковою системою користувача
    val exportFileName: String = "",
    val importFileName: String = "",
) : Serializable
