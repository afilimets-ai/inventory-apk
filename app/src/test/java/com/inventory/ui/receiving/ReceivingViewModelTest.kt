package com.inventory.ui.receiving

import com.inventory.barcode.BarcodeLookupProvider
import com.inventory.barcode.BarcodeLookupResult
import com.inventory.barcode.BarcodeLookupService
import com.inventory.data.repository.InventoryRepository
import com.inventory.feedback.ScanFeedbackManager
import com.inventory.scanner.ScannerManager
import com.inventory.scanner.ScanResult
import com.inventory.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ReceivingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `unknown barcode lookup failure exits loading state`() = runTest(mainDispatcherRule.scheduler) {
        val scannerManager = mock<ScannerManager>()
        val repository = mock<InventoryRepository>()
        val feedbackManager = mock<ScanFeedbackManager>()
        val scanEvents = MutableSharedFlow<ScanResult>()
        whenever(scannerManager.scanEvents).thenReturn(scanEvents)
        whenever(repository.getItemByBarcode("404")).thenReturn(null)
        val lookupProvider = object : BarcodeLookupProvider {
            override val name = "Throwing provider"
            override suspend fun lookup(barcode: String): BarcodeLookupResult {
                throw IllegalStateException("network down")
            }
        }
        val viewModel = ReceivingViewModel(
            scannerManager = scannerManager,
            repository = repository,
            barcodeLookupService = BarcodeLookupService(setOf(lookupProvider)),
            feedbackManager = feedbackManager
        )

        viewModel.onManualBarcodeEntered("404")
        runCurrent()
        viewModel.onLookupUnknownBarcode()
        runCurrent()

        val state = viewModel.uiState.value as ReceivingUiState.LookupNotFound
        assertEquals("404", state.barcode)
        assertTrue(state.message.contains("Помилка пошуку"))
    }
}
