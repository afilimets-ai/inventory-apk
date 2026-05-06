// app/src/main/java/com/inventory/scanner/IntentBasedScannerManager.kt
package com.inventory.scanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Universal intent-based scanner for devices without a specialized SDK.
 * Supports Urovo (DECODE_DATA), iData, and any device with broadcast-based scanner.
 *
 * @param scanAction  intent action from scanner (e.g. "android.intent.action.DECODE_DATA")
 * @param barcodeExtra name of Extra with barcode (e.g. "barcode_string")
 * @param typeExtra    name of Extra with symbology type (e.g. "barcode_type"), may be absent
 */
class IntentBasedScannerManager(
    private val context: Context,
    val scanAction: String,
    private val barcodeExtra: String,
    private val typeExtra: String
) : ScannerManager {

    companion object {
        private const val TAG = "IntentScannerManager"
        private const val DEBOUNCE_MS = 300L

        object Presets {
            val UROVO = Triple(
                "android.intent.action.DECODE_DATA",
                "barcode_string",
                "barcode_type"
            )
            val IDATA = Triple(
                "android.intent.action.decode.data",
                "SCAN_BARCODE1",
                "SCAN_BARCODE_TYPE"
            )
        }
    }

    @Volatile private var isRegistered = false
    @Volatile private var lastScanTime = 0L
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _scanEvents = MutableSharedFlow<ScanResult>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val scanEvents: SharedFlow<ScanResult> = _scanEvents.asSharedFlow()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            handleIntent(intent ?: return)
        }
    }

    /** Internal — for testing without BroadcastReceiver */
    internal fun handleIntent(intent: Intent) {
        if (intent.action != scanAction) return
        val barcode = intent.getStringExtra(barcodeExtra) ?: return
        if (barcode.isBlank()) return

        val now = System.currentTimeMillis()
        if (now - lastScanTime < DEBOUNCE_MS) return
        lastScanTime = now

        val type = intent.getStringExtra(typeExtra) ?: "UNKNOWN"
        scope.launch {
            _scanEvents.emit(ScanResult(barcode, type))
        }
        Log.d(TAG, "Scan: $barcode ($type)")
    }

    override fun register() {
        if (isRegistered) return
        ContextCompat.registerReceiver(context, receiver, IntentFilter(scanAction),
            ContextCompat.RECEIVER_EXPORTED)
        isRegistered = true
        Log.d(TAG, "Registered for: $scanAction")
    }

    override fun unregister() {
        if (!isRegistered) return
        context.unregisterReceiver(receiver)
        isRegistered = false
    }

    override fun destroy() { unregister(); scope.cancel() }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = false

    override fun triggerScan() {
        Log.d(TAG, "triggerScan: not supported for intent-based scanner")
    }
}
