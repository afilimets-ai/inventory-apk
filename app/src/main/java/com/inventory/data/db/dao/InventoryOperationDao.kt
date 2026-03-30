package com.inventory.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryOperationDao {

    @Query("SELECT * FROM inventory_operations ORDER BY created_at DESC")
    fun getAll(): Flow<List<InventoryOperation>>

    @Query("SELECT * FROM inventory_operations ORDER BY created_at DESC LIMIT :limit")
    fun getRecent(limit: Int = 20): Flow<List<InventoryOperation>>

    @Query("SELECT * FROM inventory_operations WHERE item_id = :itemId ORDER BY created_at DESC")
    fun getByItem(itemId: Long): Flow<List<InventoryOperation>>

    @Query("SELECT * FROM inventory_operations WHERE sync_status = :status ORDER BY created_at ASC")
    suspend fun getPendingSync(status: String = SyncStatus.PENDING.name): List<InventoryOperation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: InventoryOperation): Long

    @Query("UPDATE inventory_operations SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: String)
}
