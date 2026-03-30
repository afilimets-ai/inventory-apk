package com.inventory.data.repository

import androidx.room.withTransaction
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
import com.inventory.data.entity.OutboxEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
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

    // ACID транзакція: оновлення залишку + запис операції + запис у outbox
    override suspend fun recordOperationWithOutbox(
        operation: InventoryOperation,
        outboxEntry: OutboxEntry
    ): Long = db.withTransaction {
        // 1. Оновлюємо залишок товару
        operation.itemId?.let { itemId ->
            val current = inventoryItemDao.getById(itemId)
            if (current != null) {
                inventoryItemDao.updateQuantity(
                    id = itemId,
                    quantity = current.quantity + operation.quantity
                )
            }
        }
        // 2. Записуємо бізнес-операцію
        val operationId = inventoryOperationDao.insert(operation)
        // 3. Записуємо в outbox для синхронізації
        outboxEntryDao.insert(outboxEntry)
        operationId
    }
}
