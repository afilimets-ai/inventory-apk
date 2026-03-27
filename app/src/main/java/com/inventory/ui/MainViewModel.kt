package com.inventory.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.scanner.NewlandScannerManager
import com.inventory.scanner.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main ViewModel for the Inventory application.
 *
 * Manages UI state and coordinates with the NewlandScannerManager to process
 * barcode scans. This ViewModel is scoped to the Activity/Fragment lifecycle
 * and survives configuration changes.
 *
 * Key features:
 * - Collects scan events from NewlandScannerManager's SharedFlow
 * - Exposes scan results to the UI via StateFlow
 * - Handles scanner lifecycle in coordination with Activity lifecycle
 *
 * @property scannerManager Injected singleton NewlandScannerManager instance
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val scannerManager: NewlandScannerManager
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    /**
     * Internal mutable state for the last scanned barcode.
     * Exposed as immutable StateFlow to the UI layer.
     */
    private val _lastScanResult = MutableStateFlow<ScanResult?>(null)

    /**
     * Public StateFlow for observing scan results in the UI.
     *
     * Collectors will receive updates whenever a new barcode is scanned.
     * Initial value is null until the first scan is received.
     *
     * Example usage in Compose:
     * ```
     * val scanResult by viewModel.lastScanResult.collectAsState()
     * scanResult?.let { result ->
     *     Text("Scanned: ${result.barcode}")
     * }
     * ```
     */
    val lastScanResult: StateFlow<ScanResult?> = _lastScanResult.asStateFlow()

    init {
        // Start collecting scan events from the scanner manager
        observeScanEvents()
        Log.d(TAG, "MainViewModel initialized")
    }

    /**
     * Observes scan events from NewlandScannerManager and updates UI state.
     *
     * This coroutine runs for the lifetime of the ViewModel (viewModelScope)
     * and automatically cancels when the ViewModel is cleared.
     */
    private fun observeScanEvents() {
        viewModelScope.launch {
            scannerManager.scanEvents.collect { scanResult ->
                Log.d(TAG, "Received scan: ${scanResult.barcode} (${scanResult.barcodeType})")
                _lastScanResult.value = scanResult
            }
        }
    }

    /**
     * Clears the last scan result from UI state.
     *
     * Call this when the user dismisses a scan result dialog or navigates away
     * from the scan result screen.
     */
    fun clearScanResult() {
        Log.d(TAG, "Clearing scan result")
        _lastScanResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "MainViewModel cleared")
    }
}
