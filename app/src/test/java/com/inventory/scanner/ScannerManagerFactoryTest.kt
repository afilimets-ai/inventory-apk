package com.inventory.scanner

import android.content.Context
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock

class ScannerManagerFactoryTest {

    private val context = mock<Context>()

    @Test
    fun `creates NewlandScannerManager for Newland manufacturer`() {
        val factory = ScannerManagerFactory(context, manufacturerProvider = { "Newland" })
        val manager = factory.create()
        assertTrue("Expected NewlandScannerManager, got ${manager::class.simpleName}",
            manager is NewlandScannerManager)
    }

    @Test
    fun `creates NewlandScannerManager for NEWLAND uppercase`() {
        val factory = ScannerManagerFactory(context, manufacturerProvider = { "NEWLAND" })
        assertTrue(factory.create() is NewlandScannerManager)
    }

    @Test
    fun `creates IntentBasedScannerManager for unknown manufacturer`() {
        val factory = ScannerManagerFactory(context, manufacturerProvider = { "SomeUnknownBrand" })
        assertTrue(factory.create() is IntentBasedScannerManager)
    }

    @Test
    fun `creates IntentBasedScannerManager for Urovo manufacturer`() {
        val factory = ScannerManagerFactory(context, manufacturerProvider = { "Urovo" })
        val manager = factory.create()
        assertTrue(manager is IntentBasedScannerManager)
        val intManager = manager as IntentBasedScannerManager
        assertTrue(intManager.scanAction.contains("DECODE_DATA", ignoreCase = true))
    }

    @Test
    fun `creates HoneywellScannerManager for Honeywell manufacturer`() {
        val factory = ScannerManagerFactory(context, manufacturerProvider = { "Honeywell" })
        val manager = factory.create()
        assertTrue("Expected HoneywellScannerManager, got ${manager::class.simpleName}",
            manager is HoneywellScannerManager)
    }

    @Test
    fun `creates HoneywellScannerManager for HONEYWELL uppercase`() {
        val factory = ScannerManagerFactory(context, manufacturerProvider = { "HONEYWELL" })
        assertTrue(factory.create() is HoneywellScannerManager)
    }

    @Test
    fun `creates IntentBasedScannerManager for iData manufacturer with correct action`() {
        val factory = ScannerManagerFactory(context, manufacturerProvider = { "iData" })
        val manager = factory.create()
        assertTrue(manager is IntentBasedScannerManager)
        val intManager = manager as IntentBasedScannerManager
        assertTrue("Expected iData action, got: ${intManager.scanAction}",
            intManager.scanAction == "android.intent.action.decode.data")
    }
}
