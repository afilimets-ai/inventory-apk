package com.inventory.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.inventory.app.R
import com.inventory.app.data.local.AppDatabase
import com.inventory.app.sync.NetworkMonitor
import com.inventory.app.sync.SyncStatusRepository
import com.inventory.app.sync.SyncWorker
import com.inventory.app.ui.components.SyncStatusBar
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Main activity that displays the sync status bar and initializes background sync.
 *
 * Responsibilities:
 * - Observes [SyncStatusRepository.syncState] and updates the [SyncStatusBar] UI
 * - Initializes periodic [SyncWorker] via WorkManager for background sync operations
 * - Wires together the database, network monitoring, and UI components
 *
 * The activity demonstrates the offline-first architecture:
 * - Displays real-time sync status (Offline/Syncing/Online)
 * - Shows count of pending operations when offline
 * - Schedules background sync when network becomes available
 */
class MainActivity : AppCompatActivity() {

    private lateinit var syncStatusBar: SyncStatusBar
    private lateinit var syncStatusRepository: SyncStatusRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        syncStatusBar = findViewById(R.id.syncStatusBar)

        // Initialize dependencies
        val database = AppDatabase.getDatabase(applicationContext)
        val networkMonitor = NetworkMonitor(applicationContext)
        syncStatusRepository = SyncStatusRepository(
            networkMonitor = networkMonitor,
            outboxDao = database.outboxDao()
        )

        // Observe sync state and update UI
        observeSyncState()

        // Schedule periodic sync worker
        schedulePeriodicSync()
    }

    /**
     * Observes the sync state Flow and updates the SyncStatusBar UI reactively.
     *
     * Uses lifecycleScope to automatically cancel observation when activity is destroyed.
     * The Flow will emit updates whenever network state or pending operations count changes.
     */
    private fun observeSyncState() {
        lifecycleScope.launch {
            syncStatusRepository.syncState.collect { state ->
                syncStatusBar.updateState(state)
            }
        }
    }

    /**
     * Schedules periodic background sync using WorkManager.
     *
     * Configuration:
     * - Runs every 15 minutes when network is available
     * - Requires network connectivity constraint
     * - Uses KEEP policy to avoid duplicate work requests
     * - Work is identified by unique name "sync_work" for management
     *
     * The SyncWorker will process pending operations from the outbox
     * and attempt to sync them to the server when network is available.
     */
    private fun schedulePeriodicSync() {
        // Define constraints: require network connectivity
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Create periodic work request (runs every 15 minutes)
        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        // Enqueue the work request with KEEP policy to avoid duplicates
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "sync_work",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
}
