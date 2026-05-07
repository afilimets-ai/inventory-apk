package com.inventory.ui.scan

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.inventory.barcode.BarcodeLookupProduct
import com.inventory.barcode.BarcodeLookupProvider
import com.inventory.barcode.BarcodeLookupResult
import com.inventory.barcode.BarcodeLookupService
import com.inventory.data.entity.InventoryItem
import com.inventory.data.repository.InventoryRepository
import com.inventory.feedback.ScanFeedbackManager
import com.inventory.scanner.NewlandScannerManager
import com.inventory.scanner.ScanResult
import com.inventory.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `manual barcode transitions to item found`() = runTest(mainDispatcherRule.scheduler) {
        val scannerManager = mock<NewlandScannerManager>()
        val repository = mock<InventoryRepository>()
        val feedbackManager = mock<ScanFeedbackManager>()
        val scanEvents = MutableSharedFlow<ScanResult>()
        val item = InventoryItem(id = 1L, barcode = "123", name = "Widget", quantity = 3.0)
        whenever(scannerManager.scanEvents).thenReturn(scanEvents)
        whenever(repository.getItemByBarcode("123")).thenReturn(item)

        val viewModel = ScanViewModel(
            scannerManager = scannerManager,
            repository = repository,
            barcodeLookupService = BarcodeLookupService(emptySet()),
            feedbackManager = feedbackManager,
            savedStateHandle = SavedStateHandle(),
            gson = Gson()
        )

        viewModel.processBarcode("123")

        assertEquals(ScanUiState.ItemFound(item, 1.0), viewModel.uiState.value)
        verify(feedbackManager).onScanSuccess()
    }

    @Test
    fun `confirm records operation and returns to idle after success delay`() = runTest(mainDispatcherRule.scheduler) {
        val scannerManager = mock<NewlandScannerManager>()
        val repository = mock<InventoryRepository>()
        val feedbackManager = mock<ScanFeedbackManager>()
        val scanEvents = MutableSharedFlow<ScanResult>()
        val item = InventoryItem(id = 5L, barcode = "123", name = "Widget", quantity = 3.0)
        whenever(scannerManager.scanEvents).thenReturn(scanEvents)
        whenever(repository.getItemByBarcode("123")).thenReturn(item)

        val viewModel = ScanViewModel(
            scannerManager = scannerManager,
            repository = repository,
            barcodeLookupService = BarcodeLookupService(emptySet()),
            feedbackManager = feedbackManager,
            savedStateHandle = SavedStateHandle(),
            gson = Gson()
        )

        viewModel.processBarcode("123")
        viewModel.onConfirm()
        runCurrent()

        val outboxCaptor = argumentCaptor<com.inventory.data.entity.OutboxEntry>()
        verify(repository).recordOperationWithOutbox(org.mockito.kotlin.any(), outboxCaptor.capture())
        assertEquals(ScanUiState.Success, viewModel.uiState.value)
        assertTrue(outboxCaptor.firstValue.payload.contains("\"barcode\":\"123\""))

        advanceTimeBy(200)
        runCurrent()

        assertEquals(ScanUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `unknown barcode lookup imports global product`() = runTest(mainDispatcherRule.scheduler) {
        val scannerManager = mock<NewlandScannerManager>()
        val repository = mock<InventoryRepository>()
        val feedbackManager = mock<ScanFeedbackManager>()
        val scanEvents = MutableSharedFlow<ScanResult>()
        whenever(scannerManager.scanEvents).thenReturn(scanEvents)
        whenever(repository.getItemByBarcode("4820000000000")).thenReturn(null)
        whenever(repository.insertItem(org.mockito.kotlin.any())).thenReturn(42L)

        val lookupProvider = object : BarcodeLookupProvider {
            override val name = "Test provider"
            override suspend fun lookup(barcode: String): BarcodeLookupResult =
                BarcodeLookupResult.Found(
                    BarcodeLookupProduct(
                        barcode = barcode,
                        name = "Шоколад",
                        brand = "Brand",
                        source = name
                    )
                )
        }
        val viewModel = ScanViewModel(
            scannerManager = scannerManager,
            repository = repository,
            barcodeLookupService = BarcodeLookupService(setOf(lookupProvider)),
            feedbackManager = feedbackManager,
            savedStateHandle = SavedStateHandle(),
            gson = Gson()
        )

        viewModel.processBarcode("4820000000000")
        viewModel.onLookupUnknownBarcode()
        runCurrent()

        val candidate = viewModel.uiState.value as ScanUiState.LookupCandidate
        assertEquals("Шоколад", candidate.item.name)
        assertEquals("Test provider", candidate.source)

        viewModel.onImportLookupCandidate()
        runCurrent()

        assertEquals(ScanUiState.ItemFound(candidate.item.copy(id = 42L), 1.0), viewModel.uiState.value)
        verify(repository).insertItem(org.mockito.kotlin.any())
    }
}
