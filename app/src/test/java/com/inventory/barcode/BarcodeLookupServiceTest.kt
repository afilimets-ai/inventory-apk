package com.inventory.barcode

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BarcodeLookupServiceTest {

    @Test
    fun `lookup returns first found product`() = runTest {
        val product = BarcodeLookupProduct(
            barcode = "123",
            name = "Product",
            source = "Second"
        )
        val service = BarcodeLookupService(
            setOf(
                provider("First", BarcodeLookupResult.NotFound),
                provider("Second", BarcodeLookupResult.Found(product))
            )
        )

        assertEquals(BarcodeLookupResult.Found(product), service.lookup("123"))
    }

    @Test
    fun `lookup returns failure when providers fail`() = runTest {
        val service = BarcodeLookupService(
            setOf(provider("First", BarcodeLookupResult.Failure("Timeout")))
        )

        val result = service.lookup("123")

        assertTrue(result is BarcodeLookupResult.Failure)
        assertTrue((result as BarcodeLookupResult.Failure).message.contains("First: Timeout"))
    }

    private fun provider(name: String, result: BarcodeLookupResult): BarcodeLookupProvider =
        object : BarcodeLookupProvider {
            override val name: String = name
            override suspend fun lookup(barcode: String): BarcodeLookupResult = result
        }
}
