package com.inventory.app.data.local.outbox

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for outbox operations.
 * Provides reactive queries for sync status monitoring.
 */
@Dao
interface OutboxDao {
    /**
     * Get count of pending operations (excluding failed).
     * Returns a Flow for reactive observation in the UI.
     */
    @Query("SELECT COUNT(*) FROM outbox WHERE syncStatus = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    /**
     * Get all outbox entries ordered by creation time.
     * Returns a Flow for reactive observation.
     */
    @Query("SELECT * FROM outbox ORDER BY createdAt ASC")
    fun getAll(): Flow<List<OutboxEntity>>

    /**
     * Get all pending entries for sync processing.
     */
    @Query("SELECT * FROM outbox WHERE syncStatus = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<OutboxEntity>

    /**
     * Insert a new operation into the outbox.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OutboxEntity): Long

    /**
     * Insert multiple operations.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<OutboxEntity>)

    /**
     * Delete an operation from the outbox (after successful sync).
     */
    @Delete
    suspend fun delete(entity: OutboxEntity)

    /**
     * Delete an operation by ID.
     */
    @Query("DELETE FROM outbox WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Update sync status for an operation.
     */
    @Query("UPDATE outbox SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: String)

    /**
     * Clear all synced operations (for maintenance).
     */
    @Query("DELETE FROM outbox WHERE syncStatus != 'PENDING'")
    suspend fun clearSynced()
}
