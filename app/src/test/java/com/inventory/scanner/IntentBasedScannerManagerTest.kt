// app/src/test/java/com/inventory/scanner/IntentBasedScannerManagerTest.kt
package com.inventory.scanner

import android.content.Context
import android.content.Intent
import app.cash.turbine.test
import com.inventory.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class IntentBasedScannerManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = mock<Context>()

    private fun makeManager() = IntentBasedScannerManager(
        context = context,
        scanAction = "android.intent.action.DECODE_DATA",
        barcodeExtra = "barcode_string",
        typeExtra = "barcode_type"
    )

    /** Creates a mock Intent with the given action and extras. */
    private fun mockIntent(
        action: String,
        barcode: String? = null,
        type: String? = null
    ): Intent = mock<Intent>().also { intent ->
        whenever(intent.action).thenReturn(action)
        whenever(intent.getStringExtra("barcode_string")).thenReturn(barcode)
        whenever(intent.getStringExtra("barcode_type")).thenReturn(type)
    }

    @Test
    fun `handleIntent emits scan event for valid barcode`() = runTest(mainDispatcherRule.scheduler) {
        val manager = makeManager()
        manager.scanEvents.test {
            manager.handleIntent(
                mockIntent(
                    action = "android.intent.action.DECODE_DATA",
                    barcode = "ABC123",
                    type = "CODE39"
                )
            )
            advanceUntilIdle()
            val event = awaitItem()
            assertEquals("ABC123", event.barcode)
            assertEquals("CODE39", event.barcodeType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleIntent ignores empty barcode`() = runTest(mainDispatcherRule.scheduler) {
        val manager = makeManager()
        manager.scanEvents.test {
            manager.handleIntent(
                mockIntent(
                    action = "android.intent.action.DECODE_DATA",
                    barcode = ""
                )
            )
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleIntent ignores wrong action`() = runTest(mainDispatcherRule.scheduler) {
        val manager = makeManager()
        manager.scanEvents.test {
            manager.handleIntent(
                mockIntent(
                    action = "some.other.action",
                    barcode = "ABC123"
                )
            )
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleIntent uses UNKNOWN when type extra is missing`() = runTest(mainDispatcherRule.scheduler) {
        val manager = makeManager()
        manager.scanEvents.test {
            manager.handleIntent(
                mockIntent(
                    action = "android.intent.action.DECODE_DATA",
                    barcode = "XYZ789",
                    type = null  // no type extra → should default to UNKNOWN
                )
            )
            advanceUntilIdle()
            val event = awaitItem()
            assertEquals("UNKNOWN", event.barcodeType)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
