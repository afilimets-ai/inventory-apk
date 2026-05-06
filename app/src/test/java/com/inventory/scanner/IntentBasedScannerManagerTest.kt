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

@OptIn(ExperimentalCoroutinesApi::class)
class IntentBasedScannerManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = mock<Context>()

    @Test
    fun `handleIntent emits scan event for valid barcode`() = runTest(mainDispatcherRule.scheduler) {
        val manager = IntentBasedScannerManager(
            context = context,
            scanAction = "android.intent.action.DECODE_DATA",
            barcodeExtra = "barcode_string",
            typeExtra = "barcode_type"
        )

        manager.scanEvents.test {
            manager.handleIntent(
                Intent("android.intent.action.DECODE_DATA").apply {
                    putExtra("barcode_string", "ABC123")
                    putExtra("barcode_type", "CODE39")
                }
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
        val manager = IntentBasedScannerManager(
            context = context,
            scanAction = "android.intent.action.DECODE_DATA",
            barcodeExtra = "barcode_string",
            typeExtra = "barcode_type"
        )

        manager.scanEvents.test {
            manager.handleIntent(
                Intent("android.intent.action.DECODE_DATA").apply {
                    putExtra("barcode_string", "")
                }
            )
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleIntent ignores wrong action`() = runTest(mainDispatcherRule.scheduler) {
        val manager = IntentBasedScannerManager(
            context = context,
            scanAction = "android.intent.action.DECODE_DATA",
            barcodeExtra = "barcode_string",
            typeExtra = "barcode_type"
        )

        manager.scanEvents.test {
            manager.handleIntent(Intent("some.other.action").apply {
                putExtra("barcode_string", "ABC123")
            })
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `handleIntent uses UNKNOWN when type extra is missing`() = runTest(mainDispatcherRule.scheduler) {
        val manager = IntentBasedScannerManager(
            context = context,
            scanAction = "android.intent.action.DECODE_DATA",
            barcodeExtra = "barcode_string",
            typeExtra = "barcode_type"
        )

        manager.scanEvents.test {
            manager.handleIntent(
                Intent("android.intent.action.DECODE_DATA").apply {
                    putExtra("barcode_string", "XYZ789")
                }
            )
            advanceUntilIdle()
            val event = awaitItem()
            assertEquals("UNKNOWN", event.barcodeType)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
