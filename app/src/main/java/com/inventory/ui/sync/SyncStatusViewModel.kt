package com.inventory.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.data.repository.InventoryRepository
import com.inventory.network.NetworkMonitor
import com.inventory.sync.SyncEngine
import com.inventory.sync.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    networkMonitor: NetworkMonitor,
    syncEngine: SyncEngine,
    repository: InventoryRepository
) : ViewModel() {

    val syncStatus: StateFlow<SyncStatusState> = combine(
        networkMonitor.isOnline,
        syncEngine.state,
        repository.getPendingOutboxCount()
    ) { online, syncState, pendingCount ->
        SyncStatusState(
            isOnline = online,
            isSyncing = syncState is SyncState.Running,
            pendingCount = pendingCount,
            lastSyncTimestamp = (syncState as? SyncState.Success)?.timestamp,
            errorMessage = (syncState as? SyncState.Error)?.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SyncStatusState()
    )
}
