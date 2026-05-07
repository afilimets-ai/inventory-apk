package com.inventory.barcode

interface BarcodeLookupProvider {
    val name: String
    suspend fun lookup(barcode: String): BarcodeLookupResult
}
