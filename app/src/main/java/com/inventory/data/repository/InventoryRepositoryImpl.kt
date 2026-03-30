package com.inventory.data.repository

import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.Location
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val locationDao: LocationDao,
    private val inventoryItemDao: InventoryItemDao
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
}
