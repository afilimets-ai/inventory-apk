package com.inventory.data.repository

import androidx.room.withTransaction
import com.google.gson.Gson
import com.inventory.data.db.InventoryDatabase
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemBarcodeDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.InventoryOperationDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.db.dao.OutboxEntryDao
import com.inventory.data.db.dao.StockAdjustmentDocumentDao
import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItemBarcode
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.Location
import com.inventory.data.entity.OperationType
import com.inventory.data.entity.OutboxEntry
import com.inventory.data.entity.StockAdjustmentDocument
import com.inventory.data.entity.StockAdjustmentLine
import com.inventory.data.entity.StockAdjustmentType
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
    private val inventoryItemBarcodeDao: InventoryItemBarcodeDao,
    private val inventoryOperationDao: InventoryOperationDao,
    private val outboxEntryDao: OutboxEntryDao,
    private val stockAdjustmentDocumentDao: StockAdjustmentDocumentDao
) : InventoryRepository {

    private val gson = Gson()

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
    override suspend fun getItemBySku(sku: String): InventoryItem? = inventoryItemDao.getBySku(sku)
    override suspend fun resolveBarcode(barcode: String): InventoryBarcodeMatch? {
        val normalized = barcode.trim()
        if (normalized.isEmpty()) return null
        val item = inventoryItemDao.getByBarcode(normalized) ?: return null
        val alias = inventoryItemBarcodeDao.getByBarcode(normalized)
        return InventoryBarcodeMatch(
            item = item,
            scannedBarcode = normalized,
            unit = alias?.unit?.takeIf { it.isNotBlank() } ?: item.unit,
            coefficient = alias?.coefficient?.takeIf { it > 0.0 } ?: 1.0,
            isPrimary = alias?.isPrimary ?: (item.barcode == normalized)
        )
    }
    override fun getItemsByCategory(categoryId: Long): Flow<List<InventoryItem>> = inventoryItemDao.getByCategory(categoryId)
    override fun getItemsByLocation(locationId: Long): Flow<List<InventoryItem>> = inventoryItemDao.getByLocation(locationId)
    override fun getLowStockItems(): Flow<List<InventoryItem>> = inventoryItemDao.getLowStock()
    override fun searchItems(query: String): Flow<List<InventoryItem>> = inventoryItemDao.search(query.trim())
    override suspend fun insertItem(item: InventoryItem): Long = inventoryItemDao.insert(item)
    override suspend fun updateItem(item: InventoryItem) = inventoryItemDao.update(item)
    override suspend fun deleteItem(item: InventoryItem) = inventoryItemDao.delete(item)
    override suspend fun updateItemQuantity(id: Long, quantity: Double) = inventoryItemDao.updateQuantity(id, quantity)
    override suspend fun getAdditionalBarcodes(itemId: Long): List<InventoryItemBarcode> =
        inventoryItemBarcodeDao.getByItem(itemId)

    override suspend fun replaceAdditionalBarcodes(itemId: Long, barcodes: List<InventoryItemBarcode>) = runInTransaction {
        inventoryItemBarcodeDao.deleteAdditionalForItem(itemId)
        barcodes.forEach { barcode ->
            inventoryItemBarcodeDao.upsert(
                barcode.copy(
                    id = 0,
                    itemId = itemId,
                    barcode = barcode.barcode.trim(),
                    coefficient = barcode.coefficient.takeIf { it > 0.0 } ?: 1.0,
                    isPrimary = false
                )
            )
        }
    }

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
            val barcode = row.stringValue("barcode") ?: continue
            val name = row.stringValue("name") ?: continue
            val quantity = row.stringValue("quantity")?.toDoubleOrNull() ?: 0.0
            val existing = inventoryItemDao.getByBarcode(barcode)
            val itemId = if (existing != null) {
                inventoryItemDao.update(existing.copy(
                    barcode = barcode,
                    sku = row.stringValue("sku") ?: existing.sku,
                    name = name,
                    description = row.stringValue("description") ?: existing.description,
                    groupName = row.stringValue("group") ?: row.stringValue("group_name") ?: existing.groupName,
                    unit = row.stringValue("unit") ?: existing.unit,
                    isWeighted = row.stringValue("is_weighted")?.toBooleanLike() ?: existing.isWeighted,
                    isPackage = row.stringValue("is_package")?.toBooleanLike() ?: existing.isPackage,
                    packageUnit = row.stringValue("package_unit") ?: existing.packageUnit,
                    packageCoefficient = row.stringValue("package_coefficient")?.toDoubleOrNull()
                        ?.takeIf { it > 0.0 } ?: existing.packageCoefficient,
                    notes = row.stringValue("notes") ?: existing.notes,
                    updatedAt = System.currentTimeMillis()
                ))
                inventoryItemDao.updateQuantity(existing.id, quantity)
                existing.id
            } else {
                inventoryItemDao.insert(
                    InventoryItem(
                        barcode = barcode,
                        sku = row.stringValue("sku") ?: "",
                        name = name,
                        quantity = quantity,
                        unit = row.stringValue("unit") ?: "шт",
                        description = row.stringValue("description") ?: "",
                        groupName = row.stringValue("group") ?: row.stringValue("group_name") ?: "",
                        isWeighted = row.stringValue("is_weighted")?.toBooleanLike() ?: false,
                        isPackage = row.stringValue("is_package")?.toBooleanLike() ?: false,
                        packageUnit = row.stringValue("package_unit") ?: "",
                        packageCoefficient = row.stringValue("package_coefficient")?.toDoubleOrNull()
                            ?.takeIf { it > 0.0 } ?: 1.0,
                        notes = row.stringValue("notes") ?: ""
                    )
                )
            }
            (row.stringValue("additional_barcodes") ?: row.stringValue("barcodes") ?: row.stringValue("additional_barcode"))
                ?.splitBarcodes()
                ?.let { replaceAdditionalBarcodesInternal(itemId, it, row.stringValue("unit") ?: "шт", row.stringValue("package_coefficient")?.toDoubleOrNull()) }
        }
    }

    override suspend fun applyMappedImport(
        rawRows: List<List<String?>>,
        mapping: ColumnMapping,
        targetFields: List<TargetField>
    ): ImportReport = runInTransaction {
        var inserted = 0; var updated = 0; var skipped = 0
        val skipReasons = mutableListOf<String>()
        val categoryCache = mutableMapOf<String, Long?>()
        val locationCache = mutableMapOf<String, Long?>()

        fun cellFor(row: List<String?>, fieldId: String): String? =
            mapping.mapping.entries.firstOrNull { it.value == fieldId }?.key
                ?.let { row.getOrNull(it)?.trim() }?.takeIf { it.isNotBlank() }

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
                    barcode     = barcode,
                    sku         = cellFor(row, "sku") ?: existing.sku,
                    name        = name,
                    description = cellFor(row, "description") ?: existing.description,
                    groupName   = cellFor(row, "group") ?: existing.groupName,
                    unit        = cellFor(row, "unit") ?: existing.unit,
                    isWeighted  = cellFor(row, "is_weighted")?.toBooleanLike() ?: existing.isWeighted,
                    isPackage   = cellFor(row, "is_package")?.toBooleanLike() ?: existing.isPackage,
                    packageUnit = cellFor(row, "package_unit") ?: existing.packageUnit,
                    packageCoefficient = cellFor(row, "package_coefficient")?.toDoubleOrNull()
                        ?.takeIf { it > 0.0 } ?: existing.packageCoefficient,
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
                    sku         = cellFor(row, "sku") ?: "",
                    name        = name,
                    description = cellFor(row, "description") ?: "",
                    groupName   = cellFor(row, "group") ?: "",
                    unit        = cellFor(row, "unit") ?: "шт",
                    isWeighted  = cellFor(row, "is_weighted")?.toBooleanLike() ?: false,
                    isPackage   = cellFor(row, "is_package")?.toBooleanLike() ?: false,
                    packageUnit = cellFor(row, "package_unit") ?: "",
                    packageCoefficient = cellFor(row, "package_coefficient")?.toDoubleOrNull()
                        ?.takeIf { it > 0.0 } ?: 1.0,
                    notes       = cellFor(row, "notes") ?: "",
                    minQuantity = cellFor(row, "min_quantity")?.toDoubleOrNull() ?: 0.0,
                    categoryId  = categoryId,
                    locationId  = locationId,
                    quantity    = 0.0
                ))
                inserted++
            }

            cellFor(row, "additional_barcodes")?.splitBarcodes()?.let { aliases ->
                replaceAdditionalBarcodesInternal(
                    itemId = itemId,
                    barcodes = aliases.filterNot { it == barcode },
                    unit = cellFor(row, "unit") ?: "шт",
                    coefficient = cellFor(row, "package_coefficient")?.toDoubleOrNull()
                )
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

    override suspend fun createStockAdjustmentDocuments(
        discrepancies: List<StockDiscrepancy>,
        sourceNote: String
    ): CreatedStockAdjustmentDocuments = runInTransaction {
        val normalized = discrepancies.filter { it.variance != 0.0 }
        val receiptId = createStockAdjustmentDocument(
            type = StockAdjustmentType.RECEIPT,
            discrepancies = normalized.filter { it.variance > 0.0 },
            sourceNote = sourceNote
        )
        val writeOffId = createStockAdjustmentDocument(
            type = StockAdjustmentType.WRITE_OFF,
            discrepancies = normalized.filter { it.variance < 0.0 },
            sourceNote = sourceNote
        )
        CreatedStockAdjustmentDocuments(
            receiptDocumentId = receiptId,
            writeOffDocumentId = writeOffId
        )
    }

    private suspend fun createStockAdjustmentDocument(
        type: StockAdjustmentType,
        discrepancies: List<StockDiscrepancy>,
        sourceNote: String
    ): Long? {
        if (discrepancies.isEmpty()) return null
        val now = System.currentTimeMillis()
        val documentId = stockAdjustmentDocumentDao.insertDocument(
            StockAdjustmentDocument(
                documentType = type.name,
                sourceNote = sourceNote,
                createdAt = now,
                closedAt = now
            )
        )
        val lines = discrepancies.map { discrepancy ->
            StockAdjustmentLine(
                documentId = documentId,
                itemId = discrepancy.itemId,
                barcode = discrepancy.barcode,
                sku = discrepancy.sku,
                name = discrepancy.name,
                quantity = kotlin.math.abs(discrepancy.variance),
                unit = discrepancy.unit,
                reason = discrepancy.reason,
                createdAt = now
            )
        }
        stockAdjustmentDocumentDao.insertLines(lines)
        val operationType = when (type) {
            StockAdjustmentType.RECEIPT -> OperationType.STOCK_ADJUSTMENT_RECEIPT.name
            StockAdjustmentType.WRITE_OFF -> OperationType.STOCK_ADJUSTMENT_WRITE_OFF.name
        }
        val outboxId = outboxEntryDao.insert(
            OutboxEntry(
                operationType = operationType,
                payload = gson.toJson(
                    mapOf(
                        "documentId" to documentId,
                        "documentType" to type.name,
                        "operationType" to operationType,
                        "sourceOperationType" to OperationType.AUDIT.name,
                        "sourceNote" to sourceNote,
                        "createdAt" to now,
                        "lines" to lines.map { line ->
                            mapOf(
                                "itemId" to line.itemId,
                                "barcode" to line.barcode,
                                "sku" to line.sku,
                                "name" to line.name,
                                "quantity" to line.quantity,
                                "unit" to line.unit,
                                "reason" to line.reason
                            )
                        }
                    )
                )
            )
        )
        stockAdjustmentDocumentDao.attachOutboxEntry(documentId, outboxId)
        return documentId
    }

    private suspend fun replaceAdditionalBarcodesInternal(
        itemId: Long,
        barcodes: List<String>,
        unit: String,
        coefficient: Double?
    ) {
        inventoryItemBarcodeDao.deleteAdditionalForItem(itemId)
        barcodes.distinct().filter { it.isNotBlank() }.forEach { barcode ->
            inventoryItemBarcodeDao.upsert(
                InventoryItemBarcode(
                    itemId = itemId,
                    barcode = barcode,
                    unit = unit,
                    coefficient = coefficient?.takeIf { it > 0.0 } ?: 1.0,
                    isPrimary = false
                )
            )
        }
    }

    private fun InventoryItem.nextQuantity(operation: InventoryOperation): Double {
        val type = OperationType.entries.firstOrNull { it.name == operation.operationType }
        return when (type) {
            OperationType.RECEIVE -> quantity + operation.quantity
            OperationType.AUDIT -> operation.quantity
            OperationType.SHIPMENT,
            OperationType.TRANSFER -> (quantity - operation.quantity).coerceAtLeast(0.0)
            OperationType.STOCK_ADJUSTMENT_RECEIPT,
            OperationType.STOCK_ADJUSTMENT_WRITE_OFF,
            null -> quantity
        }
    }

    private fun Map<String, Any?>.stringValue(key: String): String? =
        this[key]?.toString()?.trim()?.takeIf { it.isNotBlank() }

    private fun String.splitBarcodes(): List<String> =
        split(';', ',', '|', '\n', '\t')
            .map { it.trim() }
            .filter { it.isNotBlank() }

    private fun String.toBooleanLike(): Boolean {
        return when (trim().lowercase()) {
            "1", "true", "yes", "y", "так", "т", "да", "д" -> true
            else -> false
        }
    }
}
