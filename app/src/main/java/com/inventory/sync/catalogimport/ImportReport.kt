package com.inventory.sync.catalogimport

data class ImportReport(
    val insertedCount: Int,
    val updatedCount: Int,
    val skippedCount: Int,
    val skipReasons: List<String> = emptyList()
)
