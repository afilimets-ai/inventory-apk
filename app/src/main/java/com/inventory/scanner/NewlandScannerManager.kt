package com.inventory.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong

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
// Constructed by ScannerManagerFactory — not a direct Hilt binding
class NewlandScannerManager(
    private val context: Context
) : ScannerManager {
    companion object {
        private const val TAG = "NewlandScannerManager"

        // Newland MT90 scanner broadcast actions
        private const val ACTION_SCANNER_RESULT = "nlscan.action.SCANNER_RESULT"
        private const val ACTION_SCANNER_TRIG = "nlscan.action.SCANNER_TRIG"

        // Intent extra keys for scan data (based on Newland MT90 SDK)
        private const val EXTRA_BARCODE_DATA = "SCAN_BARCODE1"
        private const val EXTRA_BARCODE_TYPE = "SCAN_BARCODE_TYPE"

        // Debounce protection: ignore scans within 300ms of the previous scan
        private const val DEBOUNCE_DELAY_MS = 300L
    }

    /**
     * Tracks whether the BroadcastReceiver is currently registered.
     * Prevents double registration/unregistration.
     */
    @Volatile
    private var isRegistered = false

    /**
     * Timestamp of the last processed scan in milliseconds (System.currentTimeMillis()).
     * Used for debounce protection to prevent duplicate scans within 300ms.
     */
    private val lastScanTimestamp = AtomicLong(0L)

    /**
     * CoroutineScope for emitting scan events to SharedFlow.
     * Uses SupervisorJob to isolate failures and Dispatchers.Main for UI thread safety.
     */
    private val scannerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Internal MutableSharedFlow for emitting scan results.
     * Configured with replay = 0 (no replay) and extraBufferCapacity = 1 to buffer
     * one event if no collectors are actively listening.
     */
    private val _scanEvents = MutableSharedFlow<ScanResult>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Public SharedFlow for consuming scan results in ViewModels and UI layers.
     *
     * Collectors will receive ScanResult events emitted when the scanner captures
     * a barcode. Use this in ViewModels with viewModelScope.launch { scanEvents.collect {...} }
     *
     * Example usage:
     * ```
     * viewModelScope.launch {
     *     scannerManager.scanEvents.collect { scanResult ->
     *         Log.d("MyViewModel", "Scanned: ${scanResult.barcode}")
     *     }
     * }
     * ```
     */
    override val scanEvents: SharedFlow<ScanResult> = _scanEvents.asSharedFlow()

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

            handleScanPayload(
                barcode = intent.scannerExtraAsString(EXTRA_BARCODE_DATA),
                barcodeType = intent.scannerExtraAsString(EXTRA_BARCODE_TYPE)
            )
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
    override fun register() {
        if (isRegistered) {
            Log.d(TAG, "Scanner receiver already registered, skipping")
            return
        }

        val filter = IntentFilter(ACTION_SCANNER_RESULT)
        ContextCompat.registerReceiver(context, scanReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
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
    override fun unregister() {
        if (!isRegistered) {
            Log.d(TAG, "Scanner receiver not registered, skipping unregister")
            return
        }

        context.unregisterReceiver(scanReceiver)
        isRegistered = false
        Log.d(TAG, "Scanner receiver unregistered")
    }

    override fun destroy() {
        unregister()
        scannerScope.cancel()
    }

    /**
     * Handles hardware key events from the Newland MT90 scanner.
     *
     * The MT90's hardware scan button generates a KeyEvent.KEYCODE_F6 event.
     * When detected, this method triggers a scan by sending the nlscan.action.SCANNER_TRIG
     * broadcast intent.
     *
     * This method should be called from your Activity's onKeyDown override:
     * ```
     * override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
     *     if (scannerManager.onKeyDown(keyCode, event)) {
     *         return true
     *     }
     *     return super.onKeyDown(keyCode, event)
     * }
     * ```
     *
     * @param keyCode The key code from the KeyEvent
     * @param event The KeyEvent object
     * @return true if the key event was handled (F6 key pressed), false otherwise
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return handleScannerKey(keyCode, event)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        return handleScannerKey(event.keyCode, event)
    }

    private fun handleScannerKey(keyCode: Int, event: KeyEvent): Boolean {
        if (!isScannerActivationKey(keyCode)) {
            return false
        }

        if (
            event.action == KeyEvent.ACTION_DOWN &&
            event.repeatCount == 0 &&
            shouldTriggerProgrammaticScan(keyCode)
        ) {
            Log.d(TAG, "Hardware scanner key pressed: keyCode=$keyCode")
            triggerScan()
        } else {
            Log.d(TAG, "Hardware scanner key consumed: keyCode=$keyCode action=${event.action}")
        }

        return true
    }

    private fun isScannerActivationKey(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_F6,
            KeyEvent.KEYCODE_F9,
            KeyEvent.KEYCODE_F10,
            KeyEvent.KEYCODE_F11,
            KeyEvent.KEYCODE_F12,
            KeyEvent.KEYCODE_BUTTON_L1,
            KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_FOCUS,
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER -> true
            else -> false
        }
    }

    private fun shouldTriggerProgrammaticScan(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_NUMPAD_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_FOCUS -> false
            else -> true
        }
    }

    /**
     * Programmatically triggers a barcode scan by sending the nlscan.action.SCANNER_TRIG broadcast.
     *
     * This sends an intent to the Newland scanner service to initiate a barcode scan.
     * The scan result will be received via the registered BroadcastReceiver listening
     * for nlscan.action.SCANNER_RESULT and emitted through the scanEvents SharedFlow.
     *
     * This method can be called from ViewModels, UI components, or any other part of
     * the application that needs to trigger a scan programmatically (e.g., via a button click).
     *
     * Example usage:
     * ```
     * // In a ViewModel or Activity
     * scannerManager.triggerScan()
     * ```
     *
     * Note: The BroadcastReceiver must be registered (via register()) before calling
     * this method, otherwise scan results will not be received.
     */
    override fun triggerScan() {
        val intent = Intent(ACTION_SCANNER_TRIG)
        context.sendBroadcast(intent)
        Log.d(TAG, "Scan trigger broadcast sent")
    }

    internal fun handleScanPayload(
        barcode: String?,
        barcodeType: String?,
        currentTime: Long = System.currentTimeMillis()
    ) {
        if (barcode.isNullOrBlank()) {
            Log.w(TAG, "Received scan result with empty barcode")
            return
        }

        val scanResult = ScanResult(
            barcode = barcode,
            barcodeType = barcodeType ?: "UNKNOWN"
        )

        Log.d(TAG, "Scan received: ${scanResult.barcode} (${scanResult.barcodeType})")

        if (!shouldProcessScan(currentTime)) {
            val previousTimestamp = lastScanTimestamp.get()
            val timeSinceLastScan = currentTime - previousTimestamp
            Log.d(TAG, "Scan ignored (debounce): ${timeSinceLastScan}ms since last scan")
            return
        }

        scannerScope.launch {
            _scanEvents.emit(scanResult)
            Log.d(TAG, "Scan event emitted to SharedFlow")
        }
    }

    private fun shouldProcessScan(currentTime: Long): Boolean {
        while (true) {
            val previousTimestamp = lastScanTimestamp.get()
            if (currentTime - previousTimestamp < DEBOUNCE_DELAY_MS) {
                return false
            }
            if (lastScanTimestamp.compareAndSet(previousTimestamp, currentTime)) {
                return true
            }
        }
    }

    private fun Intent.scannerExtraAsString(key: String): String? {
        return scannerExtraToString(extras?.get(key))
    }
}

internal fun scannerExtraToString(value: Any?): String? {
    return when (value) {
        null -> null
        is String -> value
        is CharSequence -> value.toString()
        is ByteArray -> value.toString(Charsets.UTF_8)
        is CharArray -> value.concatToString()
        else -> value.toString()
    }
}
