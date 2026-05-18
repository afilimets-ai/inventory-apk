package com.inventory.data.repository

import androidx.room.withTransaction
import com.google.gson.Gson
import com.inventory.data.db.InventoryDatabase
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.InventoryOperationDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.db.dao.OutboxEntryDao
import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.Location
import com.inventory.data.entity.OperationType
import com.inventory.data.entity.OutboxEntry
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.ImportReport
import com.inventory.sync.catalogimport.TargetField
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import androidx.annotation.VisibleForTesting

open class InventoryRepositoryImpl @Inject constructor(
    private val db: InventoryDatabase,
    private val categoryDao: CategoryDao,
    private val locationDao: LocationDao,
    private val inventoryItemDao: InventoryItemDao,
    private val inventoryOperationDao: InventoryOperationDao,
    private val outboxEntryDao: OutboxEntryDao
) : InventoryRepository {

    // Categories
    override fun getCategories(): Flow<List<Category>> = categoryDao.getAll()
    override suspend fun getCategoryById(id: Long): Category? = categoryDao.getById(id)
    override suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)
    override suspend fun updateCategory(category: Category) = categoryDao.update(category)
    override suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    // Locations
    override fun getLocations(): Flow<List<Location>> = locationDao.getAll()
    override suspend fun getLocationById(id: Long): Location? = locationDao.getById(id)
    override suspend fun insertLocation(location: Location): Long = locationDao.insert(location)
    override suspend fun updateLocation(location: Location) = locationDao.update(location)
    override suspend fun deleteLocation(location: Location) = locationDao.delete(location)

    // Inventory items
    override fun getItems(): Flow<List<InventoryItem>> = inventoryItemDao.getAll()
    override suspend fun getItemById(id: Long): InventoryItem? = inventoryItemDao.getById(id)
    override suspend fun getItemByBarcode(barcode: String): InventoryItem? = inventoryItemDao.getByBarcode(barcode)
    override fun getItemsByCategory(categoryId: Long): Flow<List<InventoryItem>> = inventoryItemDao.getByCategory(categoryId)
    override fun getItemsByLocation(locationId: Long): Flow<List<InventoryItem>> = inventoryItemDao.getByLocation(locationId)
    override fun getLowStockItems(): Flow<List<InventoryItem>> = inventoryItemDao.getLowStock()
    override fun searchItems(query: String): Flow<List<InventoryItem>> = inventoryItemDao.search(query)
    override suspend fun insertItem(item: InventoryItem): Long = inventoryItemDao.insert(item)
    override suspend fun updateItem(item: InventoryItem) = inventoryItemDao.update(item)
    override suspend fun deleteItem(item: InventoryItem) = inventoryItemDao.delete(item)
    override suspend fun updateItemQuantity(id: Long, quantity: Double) = inventoryItemDao.updateQuantity(id, quantity)

    // Operations
    override fun getRecentOperations(limit: Int): Flow<List<InventoryOperation>> = inventoryOperationDao.getRecent(limit)
    override fun getOperationsByItem(itemId: Long): Flow<List<InventoryOperation>> = inventoryOperationDao.getByItem(itemId)
    override suspend fun getPendingSyncOperations(): List<InventoryOperation> = inventoryOperationDao.getPendingSync()
    override suspend fun insertOperation(operation: InventoryOperation): Long = inventoryOperationDao.insert(operation)
    override suspend fun updateOperationSyncStatus(id: Long, status: String) = inventoryOperationDao.updateSyncStatus(id, status)

    // Outbox
    override fun getPendingOutboxCount(): Flow<Int> = outboxEntryDao.getPendingCount()
    override suspend fun getPendingOutbox(): List<OutboxEntry> = outboxEntryDao.getPending()
    override suspend fun getFailedOutboxForRetry(maxRetries: Int): List<OutboxEntry> = outboxEntryDao.getFailedForRetry(maxRetries)
    override suspend fun updateOutboxStatus(id: Long, status: String) = outboxEntryDao.updateStatus(id, status)
    override suspend fun markOutboxFailed(id: Long, errorMessage: String) = outboxEntryDao.markFailed(id, errorMessage)
    override suspend fun deleteSyncedOutbox() = outboxEntryDao.deleteSynced()
    @VisibleForTesting
    protected open suspend fun <R> runInTransaction(block: suspend () -> R): R =
        db.withTransaction(block)

    override suspend fun importItems(rows: List<Map<String, Any?>>) = runInTransaction {
        for (row in rows) {
            val barcode = row["barcode"]?.toString() ?: continue
            val name = row["name"]?.toString() ?: continue
            val quantity = row["quantity"]?.toString()?.toDoubleOrNull() ?: 0.0
            val existing = inventoryItemDao.getByBarcode(barcode)
            if (existing != null) {
                inventoryItemDao.updateQuantity(existing.id, quantity)
            } else {
                inventoryItemDao.insert(
                    InventoryItem(
                        barcode = barcode,
                        name = name,
                        quantity = quantity,
                        unit = row["unit"]?.toString() ?: "шт",
                        description = row["description"]?.toString() ?: ""
                    )
                )
            }
        }
    }

    override suspend fun applyMappedImport(
        rawRows: List<List<String?>>,
        mapping: ColumnMapping,
        targetFields: List<TargetField>
    ): ImportReport = runInTransaction {
        var inserted = 0; var updated = 0; var skipped = 0
        val skipReasons = mutableListOf<String>()
        val gson = Gson()
        val categoryCache = mutableMapOf<String, Long?>()
        val locationCache = mutableMapOf<String, Long?>()

        fun cellFor(row: List<String?>, fieldId: String): String? =
            mapping.mapping.entries.firstOrNull { it.value == fieldId }?.key
                ?.let { row.getOrNull(it) }?.takeIf { it.isNotBlank() }

        for ((idx, row) in rawRows.withIndex()) {
            val barcode = cellFor(row, "barcode")
            if (barcode == null) { skipped++; skipReasons += "row $idx: empty barcode"; continue }
            val name = cellFor(row, "name")
            if (name == null) { skipped++; skipReasons += "row $idx: empty name"; continue }

            val categoryId: Long? = cellFor(row, "category")?.let { catName ->
                categoryCache.getOrPut(catName) {
                    categoryDao.getByName(catName)?.id ?: categoryDao.insert(Category(name = catName))
                }
            }
            val locationId: Long? = cellFor(row, "location")?.let { locName ->
                locationCache.getOrPut(locName) {
                    locationDao.getByName(locName)?.id ?: locationDao.insert(Location(name = locName))
                }
            }

            val existing = inventoryItemDao.getByBarcode(barcode)
            val itemId: Long
            if (existing != null) {
                inventoryItemDao.update(existing.copy(
                    name        = name,
                    description = cellFor(row, "description") ?: existing.description,
                    unit        = cellFor(row, "unit") ?: existing.unit,
                    notes       = cellFor(row, "notes") ?: existing.notes,
                    minQuantity = cellFor(row, "min_quantity")?.toDoubleOrNull() ?: existing.minQuantity,
                    categoryId  = categoryId ?: existing.categoryId,
                    locationId  = locationId ?: existing.locationId,
                    updatedAt   = System.currentTimeMillis()
                ))
                itemId = existing.id
                updated++
            } else {
                itemId = inventoryItemDao.insert(InventoryItem(
                    barcode     = barcode,
                    name        = name,
                    description = cellFor(row, "description") ?: "",
                    unit        = cellFor(row, "unit") ?: "шт",
                    notes       = cellFor(row, "notes") ?: "",
                    minQuantity = cellFor(row, "min_quantity")?.toDoubleOrNull() ?: 0.0,
                    categoryId  = categoryId,
                    locationId  = locationId,
                    quantity    = 0.0
                ))
                inserted++
            }

            val qty = cellFor(row, "quantity")?.toDoubleOrNull()
            if (qty != null) {
                inventoryItemDao.updateQuantity(itemId, qty)
                val op = InventoryOperation(
                    itemId = itemId, barcode = barcode,
                    operationType = OperationType.AUDIT.name, quantity = qty
                )
                inventoryOperationDao.insert(op)
                outboxEntryDao.insert(OutboxEntry(
                    operationType = OperationType.AUDIT.name,
                    payload = gson.toJson(mapOf(
                        "barcode" to barcode,
                        "quantity" to qty,
                        "operationType" to "AUDIT"
                    ))
                ))
            }
        }
        ImportReport(inserted, updated, skipped, skipReasons)
    }

    override suspend fun recordOperationWithOutbox(
        operation: InventoryOperation,
        outboxEntry: OutboxEntry
    ): Long = runInTransaction {
        operation.itemId?.let { itemId ->
            val current = inventoryItemDao.getById(itemId)
            if (current != null) {
                inventoryItemDao.updateQuantity(
                    id = itemId,
                    quantity = current.nextQuantity(operation)
                )
            }
        }
        val operationId = inventoryOperationDao.insert(operation)
        outboxEntryDao.insert(outboxEntry)
        operationId
    }

    private fun InventoryItem.nextQuantity(operation: InventoryOperation): Double {
        val type = OperationType.entries.firstOrNull { it.name == operation.operationType }
        return when (type) {
            OperationType.RECEIVE -> quantity + operation.quantity
            OperationType.AUDIT -> operation.quantity
            OperationType.SHIPMENT,
            OperationType.TRANSFER -> (quantity - operation.quantity).coerceAtLeast(0.0)
            null -> quantity
        }
    }
}
