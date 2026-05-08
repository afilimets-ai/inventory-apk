package com.inventory.ui.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.inventory.barcode.BarcodeLookupResult
import com.inventory.barcode.BarcodeLookupService
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.OperationType
import com.inventory.data.entity.OutboxEntry
import com.inventory.data.repository.InventoryRepository
import com.inventory.feedback.ScanFeedbackManager
import com.inventory.scanner.ScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scannerManager: ScannerManager,
    private val repository: InventoryRepository,
    private val barcodeLookupService: BarcodeLookupService,
    private val feedbackManager: ScanFeedbackManager,
    private val savedStateHandle: SavedStateHandle,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()
    private val pendingBarcodes = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var currentOperationType: OperationType = OperationType.RECEIVE

    init {
        collectScanEvents()
        processPendingBarcodes()
    }

    private fun collectScanEvents() {
        viewModelScope.launch {
            scannerManager.scanEvents.collect { scanResult ->
                enqueueBarcode(scanResult.barcode)
            }
        }
    }

    private fun processPendingBarcodes() {
        viewModelScope.launch {
            pendingBarcodes.collect { barcode ->
                waitUntilIdle()
                processBarcode(barcode)
            }
        }
    }

    fun onManualBarcodeEntered(barcode: String) {
        if (barcode.isNotBlank()) {
            enqueueBarcode(barcode.trim())
        }
    }

    private fun enqueueBarcode(barcode: String) {
        if (!pendingBarcodes.tryEmit(barcode)) {
            viewModelScope.launch {
                pendingBarcodes.emit(barcode)
            }
        }
    }

    internal suspend fun processBarcode(barcode: String) {
        val item = repository.getItemByBarcode(barcode)
        if (item != null) {
            feedbackManager.onScanSuccess()
            _uiState.value = ScanUiState.ItemFound(item = item, quantity = 1.0)
        } else {
            feedbackManager.onScanError()
            _uiState.value = ScanUiState.UnknownBarcode(barcode = barcode)
        }
    }

    fun onQuantityChanged(quantity: Double) {
        val current = _uiState.value
        if (current is ScanUiState.ItemFound) {
            _uiState.value = current.copy(quantity = quantity.coerceAtLeast(0.0))
        }
    }

    fun onConfirm() {
        val current = _uiState.value as? ScanUiState.ItemFound ?: return
        viewModelScope.launch {
            val operation = InventoryOperation(
                itemId = current.item.id,
                barcode = current.item.barcode,
                operationType = currentOperationType.name,
                quantity = current.quantity
            )
            val outboxEntry = OutboxEntry(
                operationType = currentOperationType.name,
                payload = buildPayload(current.item.id, current.item.barcode, current.quantity, currentOperationType)
            )
            // ACID транзакція: залишок + операція + outbox entry
            repository.recordOperationWithOutbox(operation, outboxEntry)
            _uiState.value = ScanUiState.Success
            delay(200)
            _uiState.value = ScanUiState.Idle
        }
    }

    fun onLookupUnknownBarcode() {
        val current = _uiState.value as? ScanUiState.UnknownBarcode ?: return
        _uiState.value = ScanUiState.LookingUpBarcode(current.barcode)
        viewModelScope.launch {
            try {
                when (val result = barcodeLookupService.lookup(current.barcode)) {
                    is BarcodeLookupResult.Found -> {
                        _uiState.value = ScanUiState.LookupCandidate(
                            barcode = current.barcode,
                            item = result.product.toInventoryItem(),
                            source = result.product.source
                        )
                    }
                    is BarcodeLookupResult.NotFound -> {
                        _uiState.value = ScanUiState.LookupNotFound(
                            barcode = current.barcode,
                            message = "Глобальні бази не містять товар з цим штрихкодом."
                        )
                    }
                    is BarcodeLookupResult.Failure -> {
                        _uiState.value = ScanUiState.LookupNotFound(
                            barcode = current.barcode,
                            message = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = ScanUiState.LookupNotFound(
                    barcode = current.barcode,
                    message = "Помилка пошуку: ${e.message ?: "невідома помилка"}"
                )
            }
        }
    }

    fun onImportLookupCandidate() {
        val current = _uiState.value as? ScanUiState.LookupCandidate ?: return
        viewModelScope.launch {
            val itemId = repository.insertItem(current.item)
            _uiState.value = ScanUiState.ItemFound(current.item.copy(id = itemId), quantity = 1.0)
        }
    }

    private fun buildPayload(
        itemId: Long,
        barcode: String,
        quantity: Double,
        operationType: OperationType
    ): String = gson.toJson(
        mapOf(
            "itemId" to itemId,
            "barcode" to barcode,
            "quantity" to quantity,
            "operationType" to operationType.name,
            "timestamp" to System.currentTimeMillis()
        )
    )

    fun onDismiss() {
        _uiState.value = ScanUiState.Idle
    }

    private fun com.inventory.barcode.BarcodeLookupProduct.toInventoryItem(): InventoryItem =
        InventoryItem(
            barcode = barcode,
            name = name,
            description = description,
            unit = unit,
            notes = "Джерело: $source"
        )

    fun triggerScan() {
        scannerManager.triggerScan()
    }

    private suspend fun waitUntilIdle() {
        if (_uiState.value is ScanUiState.Idle) {
            return
        }
        _uiState.filter { it is ScanUiState.Idle }.first()
    }
}
