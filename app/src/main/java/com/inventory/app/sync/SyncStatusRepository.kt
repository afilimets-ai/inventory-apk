package com.inventory.app.sync

import com.inventory.app.data.local.outbox.OutboxDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Repository that combines network connectivity state and outbox pending operations
 * to produce a comprehensive sync status for the UI.
 *
 * Combines two reactive data sources:
 * - NetworkMonitor: Provides network connectivity state (online/offline)
 * - OutboxDao: Provides count of pending operations waiting to sync
 *
 * The resulting Flow emits SyncState that drives the sync status bar UI:
 * - Offline: Network unavailable, shows pending count
 * - Syncing: Network available with pending operations
 * - Online: Network available, all operations synced
 */
class SyncStatusRepository(
    private val networkMonitor: NetworkMonitor,
    private val outboxDao: OutboxDao
) {

    /**
     * Flow that emits the current sync status by combining network and outbox state.
     *
     * State determination logic:
     * - Network offline → Offline(pendingCount)
     * - Network online + pending operations → Syncing(null)
     * - Network online + no pending operations → Online
     *
     * The Flow automatically updates when either network state or pending count changes.
     */
    val syncState: Flow<SyncState> = combine(
        networkMonitor.isOnline,
        outboxDao.getPendingCount()
    ) { isOnline, pendingCount ->
        when {
            !isOnline -> SyncState.Offline(pendingCount)
            pendingCount > 0 -> SyncState.Syncing(progress = null)
            else -> SyncState.Online
        }
    }
}
