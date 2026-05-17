package com.inventory.data.repository

import com.inventory.data.db.InventoryDatabase
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.InventoryOperationDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.db.dao.OutboxEntryDao
import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItem
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.TargetFields
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.argumentCaptor

class InventoryRepositoryMappedImportTest {

    private lateinit var db: InventoryDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var locationDao: LocationDao
    private lateinit var itemDao: InventoryItemDao
    private lateinit var operationDao: InventoryOperationDao
    private lateinit var outboxDao: OutboxEntryDao
    private lateinit var repo: InventoryRepositoryImpl

    @Before fun setUp() {
        db = mock(); categoryDao = mock(); locationDao = mock()
        itemDao = mock(); operationDao = mock(); outboxDao = mock()
        repo = object : InventoryRepositoryImpl(db, categoryDao, locationDao, itemDao, operationDao, outboxDao) {
            override suspend fun <R> runInTransaction(block: suspend () -> R): R = block()
        }
    }

    @Test
    fun `inserts new item with quantity=0 when barcode not found`(): Unit = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "quantity", 3 to "unit"))
        whenever(itemDao.getByBarcode("4820001")).thenReturn(null)
        whenever(itemDao.insert(any())).thenReturn(42L)
        whenever(operationDao.insert(any())).thenReturn(1L)

        val report = repo.applyMappedImport(
            listOf(listOf("4820001", "Widget", "5.0", "шт")),
            mapping, TargetFields.all
        )

        assertEquals(1, report.insertedCount)
        assertEquals(0, report.skippedCount)
        val captor = argumentCaptor<InventoryItem>()
        verify(itemDao).insert(captor.capture())
        assertEquals("4820001", captor.firstValue.barcode)
        assertEquals("Widget", captor.firstValue.name)
        assertEquals(0.0, captor.firstValue.quantity, 0.0)
        assertEquals("шт", captor.firstValue.unit)
    }

    @Test
    fun `generates AUDIT operation when quantity is mapped and parseable`(): Unit = runBlocking {
        val existing = InventoryItem(id = 7L, barcode = "4820001", name = "Widget", quantity = 0.0)
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "quantity"))
        whenever(itemDao.getByBarcode("4820001")).thenReturn(existing)
        whenever(operationDao.insert(any())).thenReturn(1L)

        val report = repo.applyMappedImport(
            listOf(listOf("4820001", "Widget", "15.0")),
            mapping, TargetFields.all
        )

        assertEquals(1, report.updatedCount)
        verify(operationDao).insert(any())
        verify(outboxDao).insert(any())
        verify(itemDao).updateQuantity(eq(7L), eq(15.0), any())
    }

    @Test
    fun `skips row when barcode is null`(): Unit = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name"))
        val report = repo.applyMappedImport(listOf(listOf(null, "Widget")), mapping, TargetFields.all)
        assertEquals(1, report.skippedCount)
        verify(itemDao, never()).insert(any())
    }

    @Test
    fun `skips row when name is null`(): Unit = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name"))
        val report = repo.applyMappedImport(listOf(listOf("4820001", null)), mapping, TargetFields.all)
        assertEquals(1, report.skippedCount)
        verify(itemDao, never()).insert(any())
    }

    @Test
    fun `does not generate AUDIT when quantity is not parseable`(): Unit = runBlocking {
        val existing = InventoryItem(id = 7L, barcode = "4820001", name = "Widget", quantity = 3.0)
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "quantity"))
        whenever(itemDao.getByBarcode("4820001")).thenReturn(existing)

        repo.applyMappedImport(listOf(listOf("4820001", "Widget", "не_число")), mapping, TargetFields.all)

        verify(operationDao, never()).insert(any())
    }

    @Test
    fun `lookup-or-create category by name`(): Unit = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "category"))
        whenever(itemDao.getByBarcode("4820001")).thenReturn(null)
        whenever(categoryDao.getByName("Продукти")).thenReturn(null)
        whenever(categoryDao.insert(any())).thenReturn(5L)
        whenever(itemDao.insert(any())).thenReturn(1L)

        repo.applyMappedImport(listOf(listOf("4820001", "Widget", "Продукти")), mapping, TargetFields.all)

        verify(categoryDao).insert(Category(name = "Продукти"))
        val captor = argumentCaptor<InventoryItem>()
        verify(itemDao).insert(captor.capture())
        assertEquals(5L, captor.firstValue.categoryId)
    }

    @Test
    fun `reuses cached category id for duplicate names`(): Unit = runBlocking {
        val mapping = ColumnMapping(false, mapOf(0 to "barcode", 1 to "name", 2 to "category"))
        whenever(itemDao.getByBarcode(any())).thenReturn(null)
        whenever(categoryDao.getByName("Продукти")).thenReturn(null)
        whenever(categoryDao.insert(any())).thenReturn(5L)
        whenever(itemDao.insert(any())).thenReturn(1L)

        repo.applyMappedImport(
            listOf(
                listOf("AAA", "Widget A", "Продукти"),
                listOf("BBB", "Widget B", "Продукти")
            ),
            mapping, TargetFields.all
        )

        verify(categoryDao, times(1)).insert(any())
    }
}
