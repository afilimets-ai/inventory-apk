package com.inventory.scanner

import android.content.Context
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for ScannerManager — automatically selects implementation by Build.MANUFACTURER.
 *
 * Detection order:
 * 1. "newland"   → NewlandScannerManager (broadcast nlscan.action.SCANNER_RESULT)
 * 2. "honeywell" → HoneywellScannerManager (AIDC SDK)
 * 3. "urovo"     → IntentBasedScannerManager (android.intent.action.DECODE_DATA, barcode_string)
 * 4. "idata"     → IntentBasedScannerManager (android.intent.action.decode.data, SCAN_BARCODE1)
 * 5. else        → IntentBasedScannerManager with Urovo preset (most common fallback)
 *
 * @param manufacturerProvider injected in tests to mock Build.MANUFACTURER
 */
@Singleton
class ScannerManagerFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val manufacturerProvider: () -> String = { Build.MANUFACTURER }
) {
    companion object {
        private const val TAG = "ScannerManagerFactory"
    }

    fun create(): ScannerManager {
        val manufacturer = manufacturerProvider().lowercase()
        Log.i(TAG, "Detecting scanner for manufacturer: ${manufacturerProvider()}")

        return when {
            manufacturer.contains("newland") -> {
                Log.i(TAG, "→ NewlandScannerManager")
                NewlandScannerManager(context)
            }
            manufacturer.contains("honeywell") -> {
                Log.i(TAG, "→ HoneywellScannerManager")
                HoneywellScannerManager(context)
            }
            manufacturer.contains("urovo") -> {
                Log.i(TAG, "→ IntentBasedScannerManager (Urovo preset)")
                IntentBasedScannerManager(
                    context = context,
                    scanAction = "android.intent.action.DECODE_DATA",
                    barcodeExtra = "barcode_string",
                    typeExtra = "barcode_type"
                )
            }
            manufacturer.contains("idata") -> {
                Log.i(TAG, "→ IntentBasedScannerManager (iData preset)")
                IntentBasedScannerManager(
                    context = context,
                    scanAction = "android.intent.action.decode.data",
                    barcodeExtra = "SCAN_BARCODE1",
                    typeExtra = "SCAN_BARCODE_TYPE"
                )
            }
            else -> {
                Log.w(TAG, "→ IntentBasedScannerManager (fallback/Urovo preset) for unknown: ${manufacturerProvider()}")
                IntentBasedScannerManager(
                    context = context,
                    scanAction = "android.intent.action.DECODE_DATA",
                    barcodeExtra = "barcode_string",
                    typeExtra = "barcode_type"
                )
            }
        }
    }
}
