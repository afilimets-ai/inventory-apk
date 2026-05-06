package com.inventory.scanner

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import com.honeywell.aidc.AidcManager
import com.honeywell.aidc.BarcodeReader
import com.honeywell.aidc.BarcodeReadEvent
import com.honeywell.aidc.BarcodeFailureEvent
import com.honeywell.aidc.ScannerUnavailableException
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
 * ScannerManager for Honeywell devices (CK65, CT60, CT30 XP, CK67, etc.)
 * Uses Honeywell AIDC SDK (AidcManager / BarcodeReader).
 *
 * On Honeywell devices, com.honeywell.aidc is a system library.
 * On non-Honeywell devices, AidcManager.create() throws RuntimeException —
 * ScannerManagerFactory should not choose this implementation on non-Honeywell devices.
 */
class HoneywellScannerManager(
    private val context: Context
) : ScannerManager {

    companion object {
        private const val TAG = "HoneywellScanner"
    }

    private var aidcManager: AidcManager? = null
    private var barcodeReader: BarcodeReader? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _scanEvents = MutableSharedFlow<ScanResult>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val scanEvents: SharedFlow<ScanResult> = _scanEvents.asSharedFlow()

    private val barcodeListener = object : BarcodeReader.BarcodeListener {
        override fun onBarcodeEvent(event: BarcodeReadEvent) {
            val barcode = event.barcodeData ?: return
            if (barcode.isBlank()) return
            scope.launch {
                _scanEvents.emit(ScanResult(barcode, event.codeId ?: "UNKNOWN"))
            }
            Log.d(TAG, "Scan: $barcode (${event.codeId})")
        }

        override fun onFailureEvent(event: BarcodeFailureEvent) {
            Log.w(TAG, "Barcode read failure")
        }
    }

    override fun register() {
        try {
            AidcManager.create(context, AidcManager.CreatedCallback { manager ->
                aidcManager = manager
                try {
                    val reader = manager.createBarcodeReader()
                    barcodeReader = reader
                    reader.setProperty(
                        BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL
                    )
                    reader.addBarcodeListener(barcodeListener)
                    reader.claim()
                    Log.d(TAG, "BarcodeReader created and claimed")
                } catch (e: ScannerUnavailableException) {
                    Log.e(TAG, "Scanner unavailable", e)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create BarcodeReader", e)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "AidcManager.create failed — not a Honeywell device?", e)
        }
    }

    override fun unregister() {
        try {
            barcodeReader?.removeBarcodeListener(barcodeListener)
            barcodeReader?.release()
            barcodeReader?.close()
        } catch (e: Exception) {
            Log.w(TAG, "BarcodeReader release error", e)
        }
        barcodeReader = null
        try {
            aidcManager?.close()
        } catch (e: Exception) {
            Log.w(TAG, "AidcManager close error", e)
        }
        aidcManager = null
    }

    override fun destroy() {
        unregister()
        scope.cancel()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = false

    override fun triggerScan() {
        try {
            barcodeReader?.softwareTrigger(true)
        } catch (e: Exception) {
            Log.w(TAG, "triggerScan failed", e)
        }
    }
}
