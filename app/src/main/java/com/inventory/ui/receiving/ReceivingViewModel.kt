package com.inventory.ui.receiving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.barcode.BarcodeLookupResult
import com.inventory.barcode.BarcodeLookupService
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.OperationType
import com.inventory.data.entity.OutboxEntry
import com.inventory.data.repository.InventoryRepository
import com.inventory.feedback.ScanFeedbackManager
import com.inventory.scanner.ScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceivingViewModel @Inject constructor(
    private val scannerManager: ScannerManager,
    private val repository: InventoryRepository,
    private val barcodeLookupService: BarcodeLookupService,
    private val feedbackManager: ScanFeedbackManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReceivingUiState>(
        ReceivingUiState.Scanning()
    )
    val uiState: StateFlow<ReceivingUiState> = _uiState.asStateFlow()

    private val sessionLines = mutableListOf<ReceivedLine>()

    init {
        collectScanEvents()
    }

    private fun collectScanEvents() {
        viewModelScope.launch {
            scannerManager.scanEvents.collect { scanResult ->
                handleBarcode(scanResult.barcode)
            }
        }
    }

    fun onManualBarcodeEntered(barcode: String) {
        if (barcode.isNotBlank()) handleBarcode(barcode.trim())
    }

    private fun handleBarcode(barcode: String) {
        val current = _uiState.value
        // Приймаємо сканування тільки у стані Scanning
        if (current !is ReceivingUiState.Scanning) return

        viewModelScope.launch {
            val match = repository.resolveBarcode(barcode)
            if (match != null) {
                feedbackManager.onScanSuccess()
                _uiState.value = ReceivingUiState.ItemFound(
                    item = match.item,
                    quantity = match.quantity,
                    sessionLines = sessionLines.toList(),
                    defaultLocationId = current.defaultLocationId,
                    scannedBarcode = match.scannedBarcode
                )
            } else {
                feedbackManager.onScanError()
                _uiState.value = ReceivingUiState.UnknownBarcode(
                    barcode = barcode,
                    sessionLines = sessionLines.toList()
                )
            }
        }
    }

    fun onQuantityChanged(quantity: Double) {
        val current = _uiState.value
        if (current is ReceivingUiState.ItemFound) {
            _uiState.value = current.copy(quantity = quantity.coerceAtLeast(0.0))
        }
    }

    fun onConfirmReceive() {
        val current = _uiState.value as? ReceivingUiState.ItemFound ?: return
        viewModelScope.launch {
            val operation = InventoryOperation(
                itemId = current.item.id,
                barcode = current.scannedBarcode,
                operationType = OperationType.RECEIVE.name,
                quantity = current.quantity
            )
            val outboxEntry = OutboxEntry(
                operationType = OperationType.RECEIVE.name,
                payload = buildPayload(current.item, current.scannedBarcode, current.quantity)
            )
            repository.recordOperationWithOutbox(operation, outboxEntry)

            sessionLines.add(
                ReceivedLine(item = current.item, quantity = current.quantity)
            )

            feedbackManager.onScanSuccess()

            // Повертаємось до сканування
            _uiState.value = ReceivingUiState.Scanning(
                sessionLines = sessionLines.toList(),
                defaultLocationId = current.defaultLocationId
            )
        }
    }

    fun onDismissItem() {
        val locationId = when (val s = _uiState.value) {
            is ReceivingUiState.ItemFound -> s.defaultLocationId
            is ReceivingUiState.UnknownBarcode -> null
            is ReceivingUiState.LookupCandidate -> s.defaultLocationId
            else -> null
        }
        _uiState.value = ReceivingUiState.Scanning(
            sessionLines = sessionLines.toList(),
            defaultLocationId = locationId
        )
    }

    fun onEndSession() {
        if (sessionLines.isEmpty()) return
        _uiState.value = ReceivingUiState.SessionSummary(
            lines = sessionLines.toList(),
            totalItems = sessionLines.size,
            totalQuantity = sessionLines.sumOf { it.quantity }
        )
    }

    fun onNewSession() {
        sessionLines.clear()
        _uiState.value = ReceivingUiState.Scanning()
    }

    fun triggerScan() {
        scannerManager.triggerScan()
    }

    fun onLookupUnknownBarcode() {
        val current = _uiState.value as? ReceivingUiState.UnknownBarcode ?: return
        _uiState.value = ReceivingUiState.LookingUpBarcode(
            barcode = current.barcode,
            sessionLines = current.sessionLines
        )
        viewModelScope.launch {
            try {
                when (val result = barcodeLookupService.lookup(current.barcode)) {
                    is BarcodeLookupResult.Found -> {
                        _uiState.value = ReceivingUiState.LookupCandidate(
                            barcode = current.barcode,
                            item = result.product.toInventoryItem(),
                            source = result.product.source,
                            sessionLines = current.sessionLines
                        )
                    }
                    is BarcodeLookupResult.NotFound -> {
                        _uiState.value = ReceivingUiState.LookupNotFound(
                            barcode = current.barcode,
                            message = "Глобальні бази не містять товар з цим штрихкодом.",
                            sessionLines = current.sessionLines
                        )
                    }
                    is BarcodeLookupResult.Failure -> {
                        _uiState.value = ReceivingUiState.LookupNotFound(
                            barcode = current.barcode,
                            message = result.message,
                            sessionLines = current.sessionLines
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = ReceivingUiState.LookupNotFound(
                    barcode = current.barcode,
                    message = "Помилка пошуку: ${e.message ?: "невідома помилка"}",
                    sessionLines = current.sessionLines
                )
            }
        }
    }

    fun onImportLookupCandidate() {
        val current = _uiState.value as? ReceivingUiState.LookupCandidate ?: return
        viewModelScope.launch {
            val itemId = repository.insertItem(current.item)
            _uiState.value = ReceivingUiState.ItemFound(
                item = current.item.copy(id = itemId),
                quantity = 1.0,
                sessionLines = current.sessionLines,
                defaultLocationId = current.defaultLocationId
            )
        }
    }

    val sessionItemCount: Int get() = sessionLines.size
    val sessionTotalQty: Double get() = sessionLines.sumOf { it.quantity }

    private fun com.inventory.barcode.BarcodeLookupProduct.toInventoryItem(): InventoryItem =
        InventoryItem(
            barcode = barcode,
            name = name,
            description = description,
            unit = unit,
            notes = "Джерело: $source"
        )

    private fun buildPayload(item: InventoryItem, scannedBarcode: String, quantity: Double): String =
        """{"itemId":${item.id},"barcode":"$scannedBarcode","itemBarcode":"${item.barcode}","sku":"${item.sku}","quantity":$quantity,"unit":"${item.unit}","operationType":"RECEIVE","timestamp":${System.currentTimeMillis()}}"""
}
