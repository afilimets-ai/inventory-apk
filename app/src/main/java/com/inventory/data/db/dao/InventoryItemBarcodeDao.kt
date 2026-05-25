package com.inventory.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.inventory.data.entity.InventoryItemBarcode

@Dao
interface InventoryItemBarcodeDao {

    @Query("SELECT * FROM inventory_item_barcodes WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): InventoryItemBarcode?

    @Query("SELECT * FROM inventory_item_barcodes WHERE item_id = :itemId ORDER BY is_primary DESC, barcode ASC")
    suspend fun getByItem(itemId: Long): List<InventoryItemBarcode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(barcode: InventoryItemBarcode): Long

    @Query("DELETE FROM inventory_item_barcodes WHERE item_id = :itemId AND is_primary = 0")
    suspend fun deleteAdditionalForItem(itemId: Long)
}
