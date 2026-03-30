package com.inventory.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.inventory.data.entity.OutboxEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxEntryDao {

    // Кількість записів у черзі — для індикатора синхронізації
    @Query("SELECT COUNT(*) FROM outbox_entries WHERE status IN ('PENDING', 'FAILED')")
    fun getPendingCount(): Flow<Int>

    // Список для відправки (FIFO порядок)
    @Query("SELECT * FROM outbox_entries WHERE status = 'PENDING' ORDER BY created_at ASC")
    suspend fun getPending(): List<OutboxEntry>

    // Всі записи з помилками — для повторної спроби
    @Query("SELECT * FROM outbox_entries WHERE status = 'FAILED' AND retry_count < :maxRetries ORDER BY created_at ASC")
    suspend fun getFailedForRetry(maxRetries: Int = 5): List<OutboxEntry>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: OutboxEntry): Long

    @Query("UPDATE outbox_entries SET status = :status, last_attempt_at = :now WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, now: Long = System.currentTimeMillis())

    @Query("""
        UPDATE outbox_entries
        SET status = 'FAILED', retry_count = retry_count + 1,
            error_message = :errorMessage, last_attempt_at = :now
        WHERE id = :id
    """)
    suspend fun markFailed(id: Long, errorMessage: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM outbox_entries WHERE status = 'SYNCED'")
    suspend fun deleteSynced()

    @Query("SELECT COUNT(*) FROM outbox_entries WHERE status IN ('PENDING', 'FAILED')")
    suspend fun getPendingCountOnce(): Int
}
