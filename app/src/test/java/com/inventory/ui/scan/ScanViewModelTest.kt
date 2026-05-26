package com.inventory.ui.scan

import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson
import com.inventory.barcode.BarcodeLookupProduct
import com.inventory.barcode.BarcodeLookupProvider
import com.inventory.barcode.BarcodeLookupResult
import com.inventory.barcode.BarcodeLookupService
import com.inventory.data.entity.InventoryItem
import com.inventory.data.repository.InventoryBarcodeMatch
import com.inventory.data.repository.InventoryRepository
import com.inventory.feedback.ScanFeedbackManager
import com.inventory.scanner.NewlandScannerManager
import com.inventory.scanner.ScanResult
import com.inventory.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.launch
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
    fun `manual barcode records found item without confirmation`() = runTest(mainDispatcherRule.scheduler) {
        val scannerManager = mock<NewlandScannerManager>()
        val repository = mock<InventoryRepository>()
        val feedbackManager = mock<ScanFeedbackManager>()
        val scanEvents = MutableSharedFlow<ScanResult>()
        val item = InventoryItem(id = 1L, barcode = "123", name = "Widget", quantity = 3.0)
        whenever(scannerManager.scanEvents).thenReturn(scanEvents)
        whenever(repository.resolveBarcode("123")).thenReturn(
            InventoryBarcodeMatch(item, "123", item.unit, 1.0, isPrimary = true)
        )

        val viewModel = ScanViewModel(
            scannerManager = scannerManager,
            repository = repository,
            barcodeLookupService = BarcodeLookupService(emptySet()),
            feedbackManager = feedbackManager,
            savedStateHandle = SavedStateHandle(),
            gson = Gson()
        )

        viewModel.processBarcode("123")

        val outboxCaptor = argumentCaptor<com.inventory.data.entity.OutboxEntry>()
        verify(repository).recordOperationWithOutbox(org.mockito.kotlin.any(), outboxCaptor.capture())
        assertEquals(ScanUiState.Idle, viewModel.uiState.value)
        assertTrue(outboxCaptor.firstValue.payload.contains("\"barcode\":\"123\""))
        assertEquals(1, viewModel.scannedItems.value.size)
        assertEquals("123", viewModel.scannedItems.value.first().scannedBarcode)
        verify(feedbackManager).onScanSuccess()
    }

    @Test
    fun `found scan shows success while operation is being recorded`() = runTest(mainDispatcherRule.scheduler) {
        val scannerManager = mock<NewlandScannerManager>()
        val repository = mock<InventoryRepository>()
        val feedbackManager = mock<ScanFeedbackManager>()
        val scanEvents = MutableSharedFlow<ScanResult>()
        val item = InventoryItem(id = 5L, barcode = "123", name = "Widget", quantity = 3.0)
        whenever(scannerManager.scanEvents).thenReturn(scanEvents)
        whenever(repository.resolveBarcode("123")).thenReturn(
            InventoryBarcodeMatch(item, "123", item.unit, 1.0, isPrimary = true)
        )

        val viewModel = ScanViewModel(
            scannerManager = scannerManager,
            repository = repository,
            barcodeLookupService = BarcodeLookupService(emptySet()),
            feedbackManager = feedbackManager,
            savedStateHandle = SavedStateHandle(),
            gson = Gson()
        )

        val job = launch { viewModel.processBarcode("123") }
        runCurrent()

        val outboxCaptor = argumentCaptor<com.inventory.data.entity.OutboxEntry>()
        verify(repository).recordOperationWithOutbox(org.mockito.kotlin.any(), outboxCaptor.capture())
        assertEquals(ScanUiState.Success, viewModel.uiState.value)
        assertTrue(outboxCaptor.firstValue.payload.contains("\"barcode\":\"123\""))

        advanceTimeBy(200)
        runCurrent()

        assertEquals(ScanUiState.Idle, viewModel.uiState.value)
        job.cancel()
    }

    @Test
    fun `unknown barcode lookup imports global product`() = runTest(mainDispatcherRule.scheduler) {
        val scannerManager = mock<NewlandScannerManager>()
        val repository = mock<InventoryRepository>()
        val feedbackManager = mock<ScanFeedbackManager>()
        val scanEvents = MutableSharedFlow<ScanResult>()
        whenever(scannerManager.scanEvents).thenReturn(scanEvents)
        whenever(repository.resolveBarcode("4820000000000")).thenReturn(null)
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

    @Test
    fun `unknown barcode lookup failure exits loading state`() = runTest(mainDispatcherRule.scheduler) {
        val scannerManager = mock<NewlandScannerManager>()
        val repository = mock<InventoryRepository>()
        val feedbackManager = mock<ScanFeedbackManager>()
        val scanEvents = MutableSharedFlow<ScanResult>()
        whenever(scannerManager.scanEvents).thenReturn(scanEvents)
        whenever(repository.resolveBarcode("404")).thenReturn(null)

        val lookupProvider = object : BarcodeLookupProvider {
            override val name = "Throwing provider"
            override suspend fun lookup(barcode: String): BarcodeLookupResult {
                throw IllegalStateException("network down")
            }
        }
        val viewModel = ScanViewModel(
            scannerManager = scannerManager,
            repository = repository,
            barcodeLookupService = BarcodeLookupService(setOf(lookupProvider)),
            feedbackManager = feedbackManager,
            savedStateHandle = SavedStateHandle(),
            gson = Gson()
        )

        viewModel.processBarcode("404")
        viewModel.onLookupUnknownBarcode()
        runCurrent()

        val state = viewModel.uiState.value as ScanUiState.LookupNotFound
        assertEquals("404", state.barcode)
        assertTrue(state.message.contains("Помилка пошуку"))
    }
}
