package com.inventory.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.data.entity.InventoryItem
import com.inventory.data.repository.InventoryRepository
import com.inventory.scanner.ScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogSearchUiState(
    val query: String = "",
    val items: List<InventoryItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CatalogSearchViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val scannerManager: ScannerManager
) : ViewModel() {

    private val query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            scannerManager.scanEvents.collect { scanResult ->
                query.value = scanResult.barcode
            }
        }
    }

    val uiState: StateFlow<CatalogSearchUiState> = query
        .flatMapLatest { value ->
            val trimmed = value.trim()
            val results = if (trimmed.isBlank()) {
                repository.getItems()
            } else {
                repository.searchItems(trimmed)
            }
            results.map { items -> CatalogSearchUiState(query = value, items = items) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CatalogSearchUiState()
        )

    fun onQueryChanged(value: String) {
        query.value = value
    }
}
