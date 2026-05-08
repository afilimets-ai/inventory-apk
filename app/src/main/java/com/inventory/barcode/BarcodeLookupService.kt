package com.inventory.barcode

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeLookupService @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards BarcodeLookupProvider>
) {
    suspend fun lookup(barcode: String): BarcodeLookupResult {
        val errors = mutableListOf<String>()
        for (provider in providers) {
            when (val result = provider.lookup(barcode)) {
                is BarcodeLookupResult.Found -> return result
                is BarcodeLookupResult.NotFound -> Unit
                is BarcodeLookupResult.Failure -> errors.add("${provider.name}: ${result.message}")
            }
        }
        return if (errors.isEmpty()) {
            BarcodeLookupResult.NotFound
        } else {
            BarcodeLookupResult.Failure(errors.joinToString("; "))
        }
    }
}
