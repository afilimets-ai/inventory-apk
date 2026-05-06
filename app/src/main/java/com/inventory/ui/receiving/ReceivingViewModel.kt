package com.inventory.ui.receiving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.OperationType
import com.inventory.data.entity.OutboxEntry
import com.inventory.data.repository.InventoryRepository
import com.inventory.feedback.ScanFeedbackManager
import com.inventory.scanner.ScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceivingViewModel @Inject constructor(
    private val scannerManager: ScannerManager,
    private val repository: InventoryRepository,
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
            val item = repository.getItemByBarcode(barcode)
            if (item != null) {
                feedbackManager.onScanSuccess()
                _uiState.value = ReceivingUiState.ItemFound(
                    item = item,
                    quantity = 1.0,
                    sessionLines = sessionLines.toList(),
                    defaultLocationId = current.defaultLocationId
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
                barcode = current.item.barcode,
                operationType = OperationType.RECEIVE.name,
                quantity = current.quantity
            )
            val outboxEntry = OutboxEntry(
                operationType = OperationType.RECEIVE.name,
                payload = buildPayload(current.item.id, current.item.barcode, current.quantity)
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

    val sessionItemCount: Int get() = sessionLines.size
    val sessionTotalQty: Double get() = sessionLines.sumOf { it.quantity }

    private fun buildPayload(itemId: Long, barcode: String, quantity: Double): String =
        """{"itemId":$itemId,"barcode":"$barcode","quantity":$quantity,"operationType":"RECEIVE","timestamp":${System.currentTimeMillis()}}"""
}
