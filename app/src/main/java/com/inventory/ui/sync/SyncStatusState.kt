package com.inventory.ui.sync

/**
 * UI стан індикатора синхронізації.
 *
 * @param isOnline true якщо пристрій має мережу з доступом до інтернету
 * @param isSyncing true якщо SyncEngine зараз виконує export/import
 * @param pendingCount кількість операцій в outbox (PENDING + FAILED)
 * @param lastSyncTimestamp timestamp останньої успішної синхронізації (null якщо ще не було)
 * @param errorMessage повідомлення про помилку синхронізації (null якщо немає)
 */
data class SyncStatusState(
    val isOnline: Boolean = false,
    val isSyncing: Boolean = false,
    val pendingCount: Int = 0,
    val lastSyncTimestamp: Long? = null,
    val errorMessage: String? = null
) {
    val displayMode: SyncDisplayMode
        get() = when {
            errorMessage != null -> SyncDisplayMode.ERROR
            !isOnline && pendingCount > 0 -> SyncDisplayMode.OFFLINE_PENDING
            !isOnline -> SyncDisplayMode.OFFLINE
            isSyncing -> SyncDisplayMode.SYNCING
            pendingCount > 0 -> SyncDisplayMode.ONLINE_PENDING
            else -> SyncDisplayMode.SYNCED
        }
}

enum class SyncDisplayMode {
    /** Офлайн, є незсинхронізовані операції */
    OFFLINE_PENDING,
    /** Офлайн, все синхронізовано */
    OFFLINE,
    /** Онлайн, синхронізація в процесі */
    SYNCING,
    /** Онлайн, є незсинхронізовані операції */
    ONLINE_PENDING,
    /** Онлайн, все синхронізовано */
    SYNCED,
    /** Помилка синхронізації */
    ERROR
}
