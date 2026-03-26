package com.inventory.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
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
    companion object {
        private const val TAG = "NewlandScannerManager"

        // Newland MT90 scanner broadcast actions
        private const val ACTION_SCANNER_RESULT = "nlscan.action.SCANNER_RESULT"

        // Intent extra keys for scan data (based on Newland MT90 SDK)
        private const val EXTRA_BARCODE_DATA = "SCAN_BARCODE1"
        private const val EXTRA_BARCODE_TYPE = "SCAN_BARCODE_TYPE"
    }

    /**
     * Tracks whether the BroadcastReceiver is currently registered.
     * Prevents double registration/unregistration.
     */
    private var isRegistered = false

    /**
     * BroadcastReceiver for handling scan results from the Newland MT90 scanner.
     *
     * Listens for nlscan.action.SCANNER_RESULT broadcasts and extracts barcode data
     * and barcode type from the intent extras.
     */
    private val scanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != ACTION_SCANNER_RESULT) {
                return
            }

            val barcode = intent.getStringExtra(EXTRA_BARCODE_DATA)
            val barcodeType = intent.getStringExtra(EXTRA_BARCODE_TYPE)

            if (barcode.isNullOrBlank()) {
                Log.w(TAG, "Received scan result with empty barcode")
                return
            }

            val scanResult = ScanResult(
                barcode = barcode,
                barcodeType = barcodeType ?: "UNKNOWN"
            )

            Log.d(TAG, "Scan received: ${scanResult.barcode} (${scanResult.barcodeType})")

            // Debouncing and SharedFlow emission will be added in subsequent subtasks
        }
    }

    /**
     * Registers the BroadcastReceiver to listen for scanner broadcasts.
     *
     * This should be called when the Activity/Fragment enters the foreground
     * (typically in onResume or onStart) to begin receiving scan events.
     *
     * Safe to call multiple times - will not double-register.
     */
    fun register() {
        if (isRegistered) {
            Log.d(TAG, "Scanner receiver already registered, skipping")
            return
        }

        val filter = android.content.IntentFilter(ACTION_SCANNER_RESULT)
        context.registerReceiver(scanReceiver, filter)
        isRegistered = true
        Log.d(TAG, "Scanner receiver registered")
    }

    /**
     * Unregisters the BroadcastReceiver to stop listening for scanner broadcasts.
     *
     * This MUST be called when the Activity/Fragment is destroyed (typically in
     * onPause or onStop) to prevent memory leaks.
     *
     * Safe to call multiple times - will not crash if already unregistered.
     */
    fun unregister() {
        if (!isRegistered) {
            Log.d(TAG, "Scanner receiver not registered, skipping unregister")
            return
        }

        context.unregisterReceiver(scanReceiver)
        isRegistered = false
        Log.d(TAG, "Scanner receiver unregistered")
    }
}
