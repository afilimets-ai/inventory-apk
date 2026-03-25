package com.inventory.app.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.inventory.app.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker that drains the outbox when network is available.
 * Uses WorkManager for reliable background execution with network constraints.
 *
 * Processes pending operations from the outbox and syncs them to the server.
 * This worker should be scheduled with network connectivity constraints.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    /**
     * Executes the sync operation.
     * Fetches all pending operations from the outbox and attempts to sync them.
     *
     * @return Result.success() if sync completed successfully,
     *         Result.retry() if a temporary failure occurred,
     *         Result.failure() if a permanent failure occurred
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(applicationContext)
            val outboxDao = database.outboxDao()

            // Fetch all pending operations
            val pendingOperations = outboxDao.getPendingOperations()

            if (pendingOperations.isEmpty()) {
                // No operations to sync
                return@withContext Result.success()
            }

            // Process each pending operation
            var failureCount = 0
            for (operation in pendingOperations) {
                try {
                    // Update status to SYNCING
                    outboxDao.updateSyncStatus(operation.id, "SYNCING")

                    // TODO: Perform actual sync operation to server
                    // For now, we'll simulate a successful sync
                    // In production, this would make HTTP requests based on operationType
                    // Example:
                    // when (operation.operationType) {
                    //     "CREATE_ITEM" -> apiClient.createItem(operation.payload)
                    //     "UPDATE_ITEM" -> apiClient.updateItem(operation.payload)
                    //     "DELETE_ITEM" -> apiClient.deleteItem(operation.payload)
                    // }

                    // If sync successful, delete the operation from outbox
                    outboxDao.deleteById(operation.id)

                } catch (e: Exception) {
                    // Mark operation as failed but continue processing others
                    outboxDao.updateSyncStatus(operation.id, "FAILED")
                    failureCount++

                    // Log error for debugging
                    // In production, use proper logging framework (Timber, etc.)
                    e.printStackTrace()
                }
            }

            // Determine result based on success/failure ratio
            when {
                failureCount == 0 -> Result.success()
                failureCount < pendingOperations.size -> Result.success() // Partial success
                else -> Result.retry() // All operations failed, retry later
            }

        } catch (e: Exception) {
            // Unexpected error during sync
            e.printStackTrace()
            Result.retry()
        }
    }
}
