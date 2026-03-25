package com.inventory.app.sync

/**
 * Sealed class representing the current synchronization state.
 * Used to drive the UI state of the sync status bar.
 */
sealed class SyncState {
    /**
     * Device is offline with pending operations in the outbox.
     * @param pendingCount Number of operations waiting to be synced
     */
    data class Offline(val pendingCount: Int) : SyncState()

    /**
     * Device is online and actively syncing pending operations.
     * @param progress Sync progress as a percentage (0-100), or null if indeterminate
     */
    data class Syncing(val progress: Int? = null) : SyncState()

    /**
     * Device is online and all operations are synced.
     * No pending operations in the outbox.
     */
    data object Online : SyncState()
}
