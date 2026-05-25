package com.inventory.data.repository

import com.inventory.data.db.InventoryDatabase
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemBarcodeDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.InventoryOperationDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.db.dao.OutboxEntryDao
import com.inventory.data.db.dao.StockAdjustmentDocumentDao
import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.InventoryItemBarcode
import com.inventory.data.entity.StockAdjustmentDocument
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
    private lateinit var itemBarcodeDao: InventoryItemBarcodeDao
    private lateinit var operationDao: InventoryOperationDao
    private lateinit var outboxDao: OutboxEntryDao
    private lateinit var stockAdjustmentDao: StockAdjustmentDocumentDao
    private lateinit var repo: InventoryRepositoryImpl

    @Before fun setUp() {
        db = mock(); categoryDao = mock(); locationDao = mock()
        itemDao = mock(); itemBarcodeDao = mock(); operationDao = mock(); outboxDao = mock(); stockAdjustmentDao = mock()
        repo = object : InventoryRepositoryImpl(
            db,
            categoryDao,
            locationDao,
            itemDao,
            itemBarcodeDao,
            operationDao,
            outboxDao,
            stockAdjustmentDao
        ) {
            protected override suspend fun <R> runInTransaction(block: suspend () -> R): R = block()
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

        val catCaptor = argumentCaptor<Category>()
        verify(categoryDao).insert(catCaptor.capture())
        assertEquals("Продукти", catCaptor.firstValue.name)
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

    @Test
    fun `mapped import stores sku group package flags and additional barcodes`(): Unit = runBlocking {
        val mapping = ColumnMapping(
            false,
            mapOf(
                0 to "barcode",
                1 to "sku",
                2 to "name",
                3 to "group",
                4 to "additional_barcodes",
                5 to "is_weighted",
                6 to "is_package",
                7 to "package_unit",
                8 to "package_coefficient"
            )
        )
        whenever(itemDao.getByBarcode("4820001")).thenReturn(null)
        whenever(itemDao.insert(any())).thenReturn(10L)

        repo.applyMappedImport(
            listOf(listOf("4820001", "SKU-1", "Widget", "Snacks", "4820002;4820003", "так", "1", "box", "12")),
            mapping,
            TargetFields.all
        )

        val itemCaptor = argumentCaptor<InventoryItem>()
        verify(itemDao).insert(itemCaptor.capture())
        assertEquals("SKU-1", itemCaptor.firstValue.sku)
        assertEquals("Snacks", itemCaptor.firstValue.groupName)
        assertTrue(itemCaptor.firstValue.isWeighted)
        assertTrue(itemCaptor.firstValue.isPackage)
        assertEquals("box", itemCaptor.firstValue.packageUnit)
        assertEquals(12.0, itemCaptor.firstValue.packageCoefficient, 0.0)

        val barcodeCaptor = argumentCaptor<InventoryItemBarcode>()
        verify(itemBarcodeDao, times(2)).upsert(barcodeCaptor.capture())
        assertEquals(listOf("4820002", "4820003"), barcodeCaptor.allValues.map { it.barcode })
        assertEquals(12.0, barcodeCaptor.firstValue.coefficient, 0.0)
    }

    @Test
    fun `resolveBarcode returns additional barcode coefficient`() = runBlocking {
        val item = InventoryItem(id = 10L, barcode = "UNIT", name = "Widget", unit = "шт")
        whenever(itemDao.getByBarcode("BOX")).thenReturn(item)
        whenever(itemBarcodeDao.getByBarcode("BOX")).thenReturn(
            InventoryItemBarcode(itemId = 10L, barcode = "BOX", unit = "box", coefficient = 12.0)
        )

        val result = repo.resolveBarcode("BOX")

        assertEquals(item, result?.item)
        assertEquals("BOX", result?.scannedBarcode)
        assertEquals("box", result?.unit)
        assertEquals(12.0, result?.coefficient ?: 0.0, 0.0)
    }

    @Test
    fun `creates receipt and write off documents from audit discrepancies`(): Unit = runBlocking {
        whenever(stockAdjustmentDao.insertDocument(any())).thenReturn(101L, 102L)
        whenever(stockAdjustmentDao.insertLines(any())).thenReturn(listOf(1L))
        whenever(outboxDao.insert(any())).thenReturn(201L, 202L)

        val result = repo.createStockAdjustmentDocuments(
            listOf(
                StockDiscrepancy(1L, "A", "SKU-A", "A item", expectedQuantity = 2.0, actualQuantity = 5.0, unit = "шт"),
                StockDiscrepancy(2L, "B", "SKU-B", "B item", expectedQuantity = 7.0, actualQuantity = 3.0, unit = "шт")
            ),
            sourceNote = "audit:test"
        )

        assertEquals(101L, result.receiptDocumentId)
        assertEquals(102L, result.writeOffDocumentId)
        verify(stockAdjustmentDao, times(2)).insertDocument(any<StockAdjustmentDocument>())
        verify(outboxDao, times(2)).insert(any())
    }
}
