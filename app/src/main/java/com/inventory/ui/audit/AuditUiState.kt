package com.inventory.ui.audit

import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.Location

/** Один рядок підрахунку в сесії */
data class CountLine(
    val item: InventoryItem,
    val expectedQuantity: Double,
    val countedQuantity: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    val variance: Double get() = countedQuantity - expectedQuantity
    val hasDiscrepancy: Boolean get() = variance != 0.0
}

/** Стан екрану інвентаризації */
sealed class AuditUiState {
    /** Вибір локації перед початком підрахунку */
    data class SelectLocation(
        val locations: List<Location> = emptyList()
    ) : AuditUiState()

    /** Активне сканування в сесії */
    data class Counting(
        val location: Location?,
        val lines: Map<Long, CountLine> = emptyMap(),
        val expectedItems: List<InventoryItem> = emptyList()
    ) : AuditUiState() {
        val scannedCount: Int get() = lines.size
        val totalCounted: Double get() = lines.values.sumOf { it.countedQuantity }
    }

    /** Товар знайдено — підтвердження кількості */
    data class ItemScanned(
        val item: InventoryItem,
        val currentCount: Double,
        val expectedQuantity: Double,
        val location: Location?,
        val lines: Map<Long, CountLine>,
        val scannedBarcode: String = item.barcode
    ) : AuditUiState()

    /** Невідомий штрихкод */
    data class UnknownBarcode(
        val barcode: String,
        val location: Location?,
        val lines: Map<Long, CountLine>
    ) : AuditUiState()

    /** Звіт розбіжностей */
    data class VarianceReport(
        val location: Location?,
        val lines: List<CountLine>,
        val missingItems: List<InventoryItem>,
        val totalExpected: Double,
        val totalCounted: Double,
        val createdDocumentIds: List<Long> = emptyList(),
        val finalizeError: String? = null
    ) : AuditUiState() {
        val discrepancies: List<CountLine> get() = lines.filter { it.hasDiscrepancy }
        val isFinalized: Boolean get() = createdDocumentIds.isNotEmpty()
    }
}
