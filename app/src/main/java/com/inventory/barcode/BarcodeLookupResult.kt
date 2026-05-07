package com.inventory.barcode

sealed class BarcodeLookupResult {
    data class Found(val product: BarcodeLookupProduct) : BarcodeLookupResult()
    object NotFound : BarcodeLookupResult()
    data class Failure(val message: String, val cause: Throwable? = null) : BarcodeLookupResult()
}
