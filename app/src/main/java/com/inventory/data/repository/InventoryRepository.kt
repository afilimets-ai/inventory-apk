package com.inventory.data.repository

import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItemBarcode
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.Location
import com.inventory.data.entity.OutboxEntry
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.ImportReport
import com.inventory.sync.catalogimport.TargetField
import kotlinx.coroutines.flow.Flow

data class InventoryBarcodeMatch(
    val item: InventoryItem,
    val scannedBarcode: String,
    val unit: String,
    val coefficient: Double,
    val isPrimary: Boolean
) {
    val quantity: Double get() = coefficient
}

data class StockDiscrepancy(
    val itemId: Long?,
    val barcode: String,
    val sku: String,
    val name: String,
    val expectedQuantity: Double,
    val actualQuantity: Double,
    val unit: String,
    val reason: String = ""
) {
    val variance: Double get() = actualQuantity - expectedQuantity
}

data class CreatedStockAdjustmentDocuments(
    val receiptDocumentId: Long? = null,
    val writeOffDocumentId: Long? = null
) {
    val documentIds: List<Long> get() = listOfNotNull(receiptDocumentId, writeOffDocumentId)
}

interface InventoryRepository {
    // Categories
    fun getCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category): Long
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)

    // Locations
    fun getLocations(): Flow<List<Location>>
    suspend fun getLocationById(id: Long): Location?
    suspend fun insertLocation(location: Location): Long
    suspend fun updateLocation(location: Location)
    suspend fun deleteLocation(location: Location)

    // Inventory items
    fun getItems(): Flow<List<InventoryItem>>
    suspend fun getItemById(id: Long): InventoryItem?
    suspend fun getItemByBarcode(barcode: String): InventoryItem?
    suspend fun getItemBySku(sku: String): InventoryItem?
    suspend fun resolveBarcode(barcode: String): InventoryBarcodeMatch?
    fun getItemsByCategory(categoryId: Long): Flow<List<InventoryItem>>
    fun getItemsByLocation(locationId: Long): Flow<List<InventoryItem>>
    fun getLowStockItems(): Flow<List<InventoryItem>>
    fun searchItems(query: String): Flow<List<InventoryItem>>
    suspend fun insertItem(item: InventoryItem): Long
    suspend fun updateItem(item: InventoryItem)
    suspend fun deleteItem(item: InventoryItem)
    suspend fun updateItemQuantity(id: Long, quantity: Double)
    suspend fun getAdditionalBarcodes(itemId: Long): List<InventoryItemBarcode>
    suspend fun replaceAdditionalBarcodes(itemId: Long, barcodes: List<InventoryItemBarcode>)

    // Operations
    fun getRecentOperations(limit: Int = 20): Flow<List<InventoryOperation>>
    fun getOperationsByItem(itemId: Long): Flow<List<InventoryOperation>>
    suspend fun getPendingSyncOperations(): List<InventoryOperation>
    suspend fun insertOperation(operation: InventoryOperation): Long
    suspend fun updateOperationSyncStatus(id: Long, status: String)

    // Outbox
    fun getPendingOutboxCount(): Flow<Int>
    suspend fun getPendingOutbox(): List<OutboxEntry>
    suspend fun getFailedOutboxForRetry(maxRetries: Int = 5): List<OutboxEntry>
    suspend fun updateOutboxStatus(id: Long, status: String)
    suspend fun markOutboxFailed(id: Long, errorMessage: String)
    suspend fun deleteSyncedOutbox()
    suspend fun importItems(rows: List<Map<String, Any?>>)

    suspend fun applyMappedImport(
        rawRows: List<List<String?>>,
        mapping: ColumnMapping,
        targetFields: List<TargetField>
    ): ImportReport

    // ACID транзакція: операція + outbox entry одночасно
    suspend fun recordOperationWithOutbox(
        operation: InventoryOperation,
        outboxEntry: OutboxEntry
    ): Long

    suspend fun createStockAdjustmentDocuments(
        discrepancies: List<StockDiscrepancy>,
        sourceNote: String = ""
    ): CreatedStockAdjustmentDocuments
}
