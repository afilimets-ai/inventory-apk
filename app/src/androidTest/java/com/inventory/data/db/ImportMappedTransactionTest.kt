package com.inventory.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemBarcodeDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.InventoryOperationDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.db.dao.OutboxEntryDao
import com.inventory.data.db.dao.StockAdjustmentDocumentDao
import com.inventory.data.entity.InventoryItem
import com.inventory.data.repository.InventoryRepositoryImpl
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.TargetFields
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImportMappedTransactionTest {
    private lateinit var database: InventoryDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var locationDao: LocationDao
    private lateinit var itemDao: InventoryItemDao
    private lateinit var itemBarcodeDao: InventoryItemBarcodeDao
    private lateinit var operationDao: InventoryOperationDao
    private lateinit var outboxDao: OutboxEntryDao
    private lateinit var stockAdjustmentDao: StockAdjustmentDocumentDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            InventoryDatabase::class.java
        ).allowMainThreadQueries().build()
        categoryDao = database.categoryDao()
        locationDao = database.locationDao()
        itemDao = database.inventoryItemDao()
        itemBarcodeDao = database.inventoryItemBarcodeDao()
        operationDao = database.inventoryOperationDao()
        outboxDao = database.outboxEntryDao()
        stockAdjustmentDao = database.stockAdjustmentDocumentDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun applyMappedImport_rollsBackAllChanges_whenOutboxInsertFails() = runBlocking {
        val itemId = itemDao.insert(InventoryItem(barcode = "4820001", name = "Seed", quantity = 1.0))
        val failingRepo = object : InventoryRepositoryImpl(
            database,
            categoryDao,
            locationDao,
            itemDao,
            itemBarcodeDao,
            operationDao,
            object : OutboxEntryDao by outboxDao {
                override suspend fun insert(entry: com.inventory.data.entity.OutboxEntry): Long {
                    throw IllegalStateException("boom")
                }
            },
            stockAdjustmentDao
        ) {}
        val mapping = ColumnMapping(
            treatFirstRowAsHeader = false,
            mapping = mapOf(0 to "barcode", 1 to "name", 2 to "quantity")
        )

        try {
            failingRepo.applyMappedImport(
                rawRows = listOf(listOf("4820001", "Updated", "15.0")),
                mapping = mapping,
                targetFields = TargetFields.all
            )
        } catch (_: IllegalStateException) {
        }

        val itemAfter = itemDao.getById(itemId)
        val operations = operationDao.getPendingSync()
        val pendingOutbox = outboxDao.getPending()
        assertEquals("Seed", itemAfter?.name)
        assertEquals(1.0, itemAfter?.quantity ?: 0.0, 0.0)
        assertEquals(0, operations.size)
        assertEquals(0, pendingOutbox.size)
    }
}
