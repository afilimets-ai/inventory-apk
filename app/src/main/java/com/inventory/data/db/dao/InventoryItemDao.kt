package com.inventory.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.inventory.data.entity.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryItemDao {

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAll(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getById(id: Long): InventoryItem?

    @Query("""
        SELECT * FROM inventory_items
        WHERE barcode = :barcode
           OR EXISTS (
               SELECT 1 FROM inventory_item_barcodes
               WHERE inventory_item_barcodes.item_id = inventory_items.id
                 AND inventory_item_barcodes.barcode = :barcode
           )
        LIMIT 1
    """)
    suspend fun getByBarcode(barcode: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE sku = :sku LIMIT 1")
    suspend fun getBySku(sku: String): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE category_id = :categoryId ORDER BY name ASC")
    fun getByCategory(categoryId: Long): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE location_id = :locationId ORDER BY name ASC")
    fun getByLocation(locationId: Long): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE quantity <= min_quantity ORDER BY name ASC")
    fun getLowStock(): Flow<List<InventoryItem>>

    @Query("""
        SELECT * FROM inventory_items
        WHERE :query = ''
           OR barcode LIKE '%' || :query || '%'
           OR sku LIKE '%' || :query || '%'
           OR name LIKE '%' || :query || '%'
           OR group_name LIKE '%' || :query || '%'
           OR EXISTS (
               SELECT 1 FROM categories
               WHERE categories.id = inventory_items.category_id
                 AND categories.name LIKE '%' || :query || '%'
           )
           OR EXISTS (
               SELECT 1 FROM inventory_item_barcodes
               WHERE inventory_item_barcodes.item_id = inventory_items.id
                 AND inventory_item_barcodes.barcode LIKE '%' || :query || '%'
           )
        ORDER BY name ASC
    """)
    fun search(query: String): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItem): Long

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE inventory_items SET quantity = :quantity, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Double, updatedAt: Long = System.currentTimeMillis())
}
