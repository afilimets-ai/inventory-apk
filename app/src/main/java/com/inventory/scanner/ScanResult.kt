package com.inventory.scanner

/**
 * Data class representing a barcode scan result from the MT90 scanner.
 *
 * @property barcode The scanned barcode value (e.g., "1234567890123")
 * @property barcodeType The type of barcode scanned (e.g., "EAN13", "CODE128", "QR_CODE")
 */
data class ScanResult(
    val barcode: String,
    val barcodeType: String
)
