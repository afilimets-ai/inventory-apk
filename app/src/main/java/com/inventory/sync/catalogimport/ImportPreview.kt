package com.inventory.sync.catalogimport

data class ImportPreview(
    val headerRow: List<String?>,
    val sampleRows: List<List<String?>>,
    val detectedHasHeader: Boolean,
    val totalRowsEstimate: Int
)
