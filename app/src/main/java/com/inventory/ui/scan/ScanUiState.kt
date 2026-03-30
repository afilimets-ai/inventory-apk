package com.inventory.ui.scan

import com.inventory.data.entity.InventoryItem

sealed class ScanUiState {
    object Idle : ScanUiState()
    data class ItemFound(val item: InventoryItem, val quantity: Double = 1.0) : ScanUiState()
    data class UnknownBarcode(val barcode: String) : ScanUiState()
    object Success : ScanUiState()
    data class Error(val message: String) : ScanUiState()
}
