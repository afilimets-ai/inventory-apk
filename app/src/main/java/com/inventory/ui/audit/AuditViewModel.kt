package com.inventory.ui.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.Location
import com.inventory.data.entity.OperationType
import com.inventory.data.entity.OutboxEntry
import com.inventory.data.repository.InventoryRepository
import com.inventory.feedback.ScanFeedbackManager
import com.inventory.scanner.NewlandScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditViewModel @Inject constructor(
    private val scannerManager: NewlandScannerManager,
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
            val item = repository.getItemByBarcode(barcode)
            if (item != null) {
                feedbackManager.onScanSuccess()
                val existing = countLines[item.id]
                val currentCount = (existing?.countedQuantity ?: 0.0) + 1.0
                _uiState.value = AuditUiState.ItemScanned(
                    item = item,
                    currentCount = currentCount,
                    expectedQuantity = item.quantity,
                    location = selectedLocation,
                    lines = countLines.toMap()
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
                barcode = current.item.barcode,
                operationType = OperationType.AUDIT.name,
                quantity = current.currentCount
            )
            val outboxEntry = OutboxEntry(
                operationType = OperationType.AUDIT.name,
                payload = buildPayload(current.item.id, current.item.barcode, current.currentCount)
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

    fun onNewSession() {
        countLines.clear()
        selectedLocation = null
        expectedItems = emptyList()
        loadLocations()
    }

    fun triggerScan() {
        scannerManager.triggerScan()
    }

    private fun buildPayload(itemId: Long, barcode: String, quantity: Double): String =
        """{"itemId":$itemId,"barcode":"$barcode","quantity":$quantity,"operationType":"AUDIT","timestamp":${System.currentTimeMillis()}}"""
}
