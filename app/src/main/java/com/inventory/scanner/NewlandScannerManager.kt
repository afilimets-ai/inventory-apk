package com.inventory.scanner

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton manager for the Newland MT90 barcode scanner.
 *
 * Handles lifecycle management of the scanner BroadcastReceiver, processes scan events,
 * implements debouncing to prevent duplicate scans, and provides a reactive SharedFlow
 * for consuming scan results in ViewModels and UI layers.
 *
 * Key features:
 * - Registers/unregisters BroadcastReceiver for nlscan.action.SCANNER_RESULT
 * - Handles hardware scan button (F6 key)
 * - Supports programmatic scan triggering via nlscan.action.SCANNER_TRIG
 * - 300ms debounce protection against double-scans
 * - Reactive API via SharedFlow<ScanResult>
 *
 * @property context Application context injected by Hilt
 */
@Singleton
class NewlandScannerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Implementation will be added in subsequent subtasks
}
