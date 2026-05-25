package com.inventory.scanner

import android.content.Context
import android.util.Log
import android.view.KeyEvent
import app.cash.turbine.test
import com.inventory.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NewlandScannerManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `handleScanPayload emits scan event`() = runTest(mainDispatcherRule.scheduler) {
        val context = mock<Context>()
        val manager = NewlandScannerManager(context)

        mockAndroidLog().use {
            manager.scanEvents.test {
                manager.handleScanPayload("12345", "CODE128", currentTime = 1_000L)
                advanceUntilIdle()

                assertEquals(ScanResult("12345", "CODE128"), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `handleScanPayload debounces duplicate scans`() = runTest(mainDispatcherRule.scheduler) {
        val context = mock<Context>()
        val manager = NewlandScannerManager(context)

        mockAndroidLog().use {
            manager.scanEvents.test {
                manager.handleScanPayload("12345", "CODE128", currentTime = 1_000L)
                manager.handleScanPayload("12345", "CODE128", currentTime = 1_200L)
                advanceUntilIdle()

                assertEquals(ScanResult("12345", "CODE128"), awaitItem())
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `unregister is safe when receiver was never registered`() {
        val context = mock<Context>()
        val manager = NewlandScannerManager(context)

        mockAndroidLog().use {
            manager.unregister()
        }

        verify(context, never()).unregisterReceiver(org.mockito.kotlin.any())
    }

    @Test
    fun `onKeyDown triggers scanner broadcast for F6`() {
        val context = mock<Context>()
        val manager = NewlandScannerManager(context)
        val event = mock<KeyEvent>()
        whenever(event.action).thenReturn(KeyEvent.ACTION_DOWN)

        mockAndroidLog().use {
            val handled = manager.onKeyDown(KeyEvent.KEYCODE_F6, event)

            assertTrue(handled)
        }
        verify(context, atLeastOnce()).sendBroadcast(org.mockito.kotlin.any())
    }

    @Test
    fun `onKeyEvent consumes enter scanner key without sending trigger broadcast`() {
        val context = mock<Context>()
        val manager = NewlandScannerManager(context)
        val event = mock<KeyEvent>()
        whenever(event.keyCode).thenReturn(KeyEvent.KEYCODE_ENTER)
        whenever(event.action).thenReturn(KeyEvent.ACTION_DOWN)

        mockAndroidLog().use {
            val handled = manager.onKeyEvent(event)

            assertTrue(handled)
        }
        verify(context, never()).sendBroadcast(org.mockito.kotlin.any())
    }

    @Test
    fun `onKeyEvent consumes scanner key up without sending trigger broadcast`() {
        val context = mock<Context>()
        val manager = NewlandScannerManager(context)
        val event = mock<KeyEvent>()
        whenever(event.keyCode).thenReturn(KeyEvent.KEYCODE_F6)
        whenever(event.action).thenReturn(KeyEvent.ACTION_UP)

        mockAndroidLog().use {
            val handled = manager.onKeyEvent(event)

            assertTrue(handled)
        }
        verify(context, never()).sendBroadcast(org.mockito.kotlin.any())
    }

    @Test
    fun `onKeyDown ignores non scanner key`() {
        val context = mock<Context>()
        val manager = NewlandScannerManager(context)
        val event = mock<KeyEvent>()
        whenever(event.action).thenReturn(KeyEvent.ACTION_DOWN)

        val handled = manager.onKeyDown(KeyEvent.KEYCODE_A, event)

        assertFalse(handled)
    }

    @Test
    fun `scannerExtraToString converts Newland non string extras`() {
        assertEquals("1", scannerExtraToString(1))
        assertEquals("CODE128", scannerExtraToString("CODE128".toByteArray()))
        assertEquals(null, scannerExtraToString(null))
    }

    private fun mockAndroidLog(): MockedStatic<Log> {
        return mockStatic(Log::class.java).apply {
            `when`<Int> { Log.d(anyString(), anyString()) }.thenReturn(0)
            `when`<Int> { Log.w(anyString(), anyString()) }.thenReturn(0)
        }
    }
}
