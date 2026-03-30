package com.inventory.ui.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.OperationType
import com.inventory.data.repository.InventoryRepository
import com.inventory.scanner.NewlandScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val scannerManager: NewlandScannerManager,
    private val repository: InventoryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    private var currentOperationType: OperationType = OperationType.RECEIVE

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
        if (barcode.isNotBlank()) {
            handleBarcode(barcode.trim())
        }
    }

    private fun handleBarcode(barcode: String) {
        if (_uiState.value !is ScanUiState.Idle) return
        viewModelScope.launch {
            val item = repository.getItemByBarcode(barcode)
            _uiState.value = if (item != null) {
                ScanUiState.ItemFound(item = item, quantity = 1.0)
            } else {
                ScanUiState.UnknownBarcode(barcode = barcode)
            }
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
            repository.insertOperation(operation)
            repository.updateItemQuantity(
                id = current.item.id,
                quantity = current.item.quantity + current.quantity
            )
            _uiState.value = ScanUiState.Success
            delay(800)
            _uiState.value = ScanUiState.Idle
        }
    }

    fun onDismiss() {
        _uiState.value = ScanUiState.Idle
    }

    fun triggerScan() {
        scannerManager.triggerScan()
    }
}
