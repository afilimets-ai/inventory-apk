package com.inventory.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.Location
import com.inventory.data.entity.OperationType
import com.inventory.data.entity.OutboxEntry
import com.inventory.data.repository.InventoryRepository
import com.inventory.data.repository.StockDiscrepancy
import com.inventory.feedback.ScanFeedbackManager
import com.inventory.scanner.ScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditViewModel @Inject constructor(
    private val scannerManager: ScannerManager,
    private val repository: InventoryRepository,
    private val feedbackManager: ScanFeedbackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuditUiState>(AuditUiState.SelectLocation())
    val uiState: StateFlow<AuditUiState> = _uiState.asStateFlow()

    private val countLines = mutableMapOf<Long, CountLine>()
    private var selectedLocation: Location? = null
    private var expectedItems: List<InventoryItem> = emptyList()

    init {
        loadLocations()
        collectScanEvents()
    }

    private fun loadLocations() {
        viewModelScope.launch {
            val locations = repository.getLocations().first()
            _uiState.value = AuditUiState.SelectLocation(locations)
        }
    }

    private fun collectScanEvents() {
        viewModelScope.launch {
            scannerManager.scanEvents.collect { scanResult ->
                handleBarcode(scanResult.barcode)
            }
        }
    }

    fun onLocationSelected(location: Location?) {
        selectedLocation = location
        viewModelScope.launch {
            expectedItems = if (location != null) {
                repository.getItemsByLocation(location.id).first()
            } else {
                repository.getItems().first()
            }
            _uiState.value = AuditUiState.Counting(
                location = selectedLocation,
                lines = countLines.toMap(),
                expectedItems = expectedItems
            )
        }
    }

    fun onSkipLocation() {
        onLocationSelected(null)
    }

    fun onManualBarcodeEntered(barcode: String) {
        if (barcode.isNotBlank()) handleBarcode(barcode.trim())
    }

    private fun handleBarcode(barcode: String) {
        val current = _uiState.value
        if (current !is AuditUiState.Counting) return

        viewModelScope.launch {
            val match = repository.resolveBarcode(barcode)
            if (match != null) {
                feedbackManager.onScanSuccess()
                val item = expectedItems.firstOrNull { it.id == match.item.id } ?: match.item
                val existing = countLines[item.id]
                val currentCount = (existing?.countedQuantity ?: 0.0) + match.quantity
                _uiState.value = AuditUiState.ItemScanned(
                    item = item,
                    currentCount = currentCount,
                    expectedQuantity = item.quantity,
                    location = selectedLocation,
                    lines = countLines.toMap(),
                    scannedBarcode = match.scannedBarcode
                )
            } else {
                feedbackManager.onScanError()
                _uiState.value = AuditUiState.UnknownBarcode(
                    barcode = barcode,
                    location = selectedLocation,
                    lines = countLines.toMap()
                )
            }
        }
    }

    fun onAdjustCount(quantity: Double) {
        val current = _uiState.value as? AuditUiState.ItemScanned ?: return
        _uiState.value = current.copy(currentCount = quantity.coerceAtLeast(0.0))
    }

    fun onConfirmCount() {
        val current = _uiState.value as? AuditUiState.ItemScanned ?: return
        viewModelScope.launch {
            val line = CountLine(
                item = current.item,
                expectedQuantity = current.expectedQuantity,
                countedQuantity = current.currentCount
            )
            countLines[current.item.id] = line

            // Записуємо операцію AUDIT через outbox
            val operation = InventoryOperation(
                itemId = current.item.id,
                barcode = current.scannedBarcode,
                operationType = OperationType.AUDIT.name,
                quantity = current.currentCount
            )
            val outboxEntry = OutboxEntry(
                operationType = OperationType.AUDIT.name,
                payload = buildPayload(current.item, current.scannedBarcode, current.currentCount)
            )
            repository.recordOperationWithOutbox(operation, outboxEntry)

            // Повертаємось до сканування
            _uiState.value = AuditUiState.Counting(
                location = selectedLocation,
                lines = countLines.toMap(),
                expectedItems = expectedItems
            )
        }
    }

    fun onDismissItem() {
        _uiState.value = AuditUiState.Counting(
            location = selectedLocation,
            lines = countLines.toMap(),
            expectedItems = expectedItems
        )
    }

    fun onEndSession() {
        val allLines = countLines.values.toList()
        val scannedItemIds = countLines.keys
        val missingItems = expectedItems.filter { it.id !in scannedItemIds }

        _uiState.value = AuditUiState.VarianceReport(
            location = selectedLocation,
            lines = allLines,
            missingItems = missingItems,
            totalExpected = expectedItems.sumOf { it.quantity },
            totalCounted = allLines.sumOf { it.countedQuantity }
        )
    }

    fun onFinalizeAudit() {
        val current = _uiState.value as? AuditUiState.VarianceReport ?: return
        if (current.isFinalized) return
        viewModelScope.launch {
            try {
                val discrepancies = current.toStockDiscrepancies()
                val result = repository.createStockAdjustmentDocuments(
                    discrepancies = discrepancies,
                    sourceNote = "audit:${current.location?.name ?: "all"}:${System.currentTimeMillis()}"
                )
                _uiState.value = current.copy(
                    createdDocumentIds = result.documentIds,
                    finalizeError = null
                )
            } catch (e: Exception) {
                _uiState.value = current.copy(
                    finalizeError = e.message ?: "Не вдалося створити документи"
                )
            }
        }
    }

    fun onNewSession() {
        countLines.clear()
        selectedLocation = null
        expectedItems = emptyList()
        loadLocations()
    }

    fun triggerScan() {
        scannerManager.triggerScan()
    }

    private fun AuditUiState.VarianceReport.toStockDiscrepancies(): List<StockDiscrepancy> {
        val counted = lines.filter { it.hasDiscrepancy }.map { line ->
            StockDiscrepancy(
                itemId = line.item.id,
                barcode = line.item.barcode,
                sku = line.item.sku,
                name = line.item.name,
                expectedQuantity = line.expectedQuantity,
                actualQuantity = line.countedQuantity,
                unit = line.item.unit,
                reason = if (line.variance > 0.0) "audit_surplus" else "audit_shortage"
            )
        }
        val missing = missingItems.filter { it.quantity != 0.0 }.map { item ->
            StockDiscrepancy(
                itemId = item.id,
                barcode = item.barcode,
                sku = item.sku,
                name = item.name,
                expectedQuantity = item.quantity,
                actualQuantity = 0.0,
                unit = item.unit,
                reason = "audit_missing"
            )
        }
        return counted + missing
    }

    private fun buildPayload(item: InventoryItem, scannedBarcode: String, quantity: Double): String =
        """{"itemId":${item.id},"barcode":"$scannedBarcode","itemBarcode":"${item.barcode}","sku":"${item.sku}","quantity":$quantity,"unit":"${item.unit}","operationType":"AUDIT","timestamp":${System.currentTimeMillis()}}"""
}
