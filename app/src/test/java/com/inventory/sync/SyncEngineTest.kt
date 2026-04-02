package com.inventory.sync

import com.inventory.data.entity.InventoryItem
import com.inventory.data.repository.InventoryRepository
import com.inventory.sync.serializer.CsvSerializer
import com.inventory.sync.serializer.ExcelSerializer
import com.inventory.sync.serializer.JsonSerializer
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SyncEngineTest {

    private lateinit var repository: InventoryRepository
    private lateinit var settingsManager: SyncSettingsManager
    private lateinit var providerFactory: SyncProviderFactory
    private lateinit var csvSerializer: CsvSerializer
    private lateinit var jsonSerializer: JsonSerializer
    private lateinit var excelSerializer: ExcelSerializer
    private lateinit var engine: SyncEngine

    @Before
    fun setUp() {
        repository = mock()
        settingsManager = mock()
        providerFactory = mock()
        csvSerializer = CsvSerializer()
        jsonSerializer = mock()
        excelSerializer = mock()

        engine = SyncEngine(
            repository = repository,
            settingsManager = settingsManager,
            providerFactory = providerFactory,
            csvSerializer = csvSerializer,
            jsonSerializer = jsonSerializer,
            excelSerializer = excelSerializer,
        )
    }

    // ── runImport ────────────────────────────────────────────────────────────

    @Test
    fun `runImport does nothing and stays Idle when no import providers`() = runTest {
        whenever(settingsManager.getImportProviders()).thenReturn(emptyList())

        engine.runImport()

        assertEquals(SyncState.Idle, engine.state.value)
    }

    @Test
    fun `runImport transitions to Idle when all providers do not support import`() = runTest {
        val settings = SyncSettings(
            providerType = SyncProviderType.FTP,
            isImportEnabled = true,
            format = SyncFormat.CSV
        )
        val provider = mock<SyncProvider>()
        whenever(provider.supportsImport).thenReturn(false)
        whenever(settingsManager.getImportProviders()).thenReturn(listOf(settings))
        whenever(providerFactory.create(SyncProviderType.FTP)).thenReturn(provider)

        engine.runImport()

        // Bug: сьогодні залишається Running — після фіксу має бути Idle
        assertNotEquals(SyncState.Running, engine.state.value)
        assertEquals(SyncState.Idle, engine.state.value)
    }

    @Test
    fun `runImport transitions to Success on successful import`() = runTest {
        val csvData = "barcode,name,quantity\n123,Widget,10\n".toByteArray()
        val settings = SyncSettings(
            providerType = SyncProviderType.FTP,
            isImportEnabled = true,
            format = SyncFormat.CSV
        )
        val provider = mock<SyncProvider>()
        whenever(provider.supportsImport).thenReturn(true)
        whenever(provider.import(SyncFormat.CSV, "inventory_import"))
            .thenReturn(SyncImportResult.Success(csvData))
        whenever(settingsManager.getImportProviders()).thenReturn(listOf(settings))
        whenever(providerFactory.create(SyncProviderType.FTP)).thenReturn(provider)
        whenever(repository.getItemByBarcode(any())).thenReturn(null)
        whenever(repository.insertItem(any())).thenReturn(1L)

        engine.runImport()

        assert(engine.state.value is SyncState.Success)
    }

    @Test
    fun `runImport transitions to Error on provider failure`() = runTest {
        val settings = SyncSettings(
            providerType = SyncProviderType.FTP,
            isImportEnabled = true,
            format = SyncFormat.CSV
        )
        val provider = mock<SyncProvider>()
        whenever(provider.supportsImport).thenReturn(true)
        whenever(provider.import(SyncFormat.CSV, "inventory_import"))
            .thenReturn(SyncImportResult.Failure("Connection refused"))
        whenever(settingsManager.getImportProviders()).thenReturn(listOf(settings))
        whenever(providerFactory.create(SyncProviderType.FTP)).thenReturn(provider)

        engine.runImport()

        assert(engine.state.value is SyncState.Error)
    }

    // ── runExport ────────────────────────────────────────────────────────────

    @Test
    fun `runExport does nothing and stays Idle when no export providers`() = runTest {
        whenever(settingsManager.getExportProviders()).thenReturn(emptyList())

        engine.runExport()

        assertEquals(SyncState.Idle, engine.state.value)
    }

    @Test
    fun `runExport transitions to Success when export succeeds`() = runTest {
        val items = listOf(
            InventoryItem(id = 1, barcode = "123", name = "Widget", quantity = 5.0, unit = "шт")
        )
        val settings = SyncSettings(
            providerType = SyncProviderType.LOCAL_FOLDER,
            isExportEnabled = true,
            format = SyncFormat.CSV
        )
        val provider = mock<SyncProvider>()
        whenever(provider.supportsExport).thenReturn(true)
        whenever(provider.export(any(), any(), any())).thenReturn(SyncResult.Success)
        whenever(settingsManager.getExportProviders()).thenReturn(listOf(settings))
        whenever(providerFactory.create(SyncProviderType.LOCAL_FOLDER)).thenReturn(provider)
        whenever(repository.getItems()).thenReturn(flowOf(items))

        engine.runExport()

        assert(engine.state.value is SyncState.Success)
    }

    @Test
    fun `runExport accumulates errors from all providers`() = runTest {
        val items = listOf(
            InventoryItem(id = 1, barcode = "123", name = "Widget", quantity = 5.0, unit = "шт")
        )
        val settings1 = SyncSettings(
            providerType = SyncProviderType.FTP,
            isExportEnabled = true,
            format = SyncFormat.CSV
        )
        val settings2 = SyncSettings(
            providerType = SyncProviderType.HTTP_API,
            isExportEnabled = true,
            format = SyncFormat.JSON
        )
        val provider1 = mock<SyncProvider>()
        val provider2 = mock<SyncProvider>()
        whenever(provider1.supportsExport).thenReturn(true)
        whenever(provider2.supportsExport).thenReturn(true)
        whenever(provider1.export(any(), any(), any())).thenReturn(SyncResult.Failure("FTP error"))
        whenever(provider2.export(any(), any(), any())).thenReturn(SyncResult.Failure("HTTP error"))
        whenever(settingsManager.getExportProviders()).thenReturn(listOf(settings1, settings2))
        whenever(providerFactory.create(SyncProviderType.FTP)).thenReturn(provider1)
        whenever(providerFactory.create(SyncProviderType.HTTP_API)).thenReturn(provider2)
        whenever(jsonSerializer.serialize(any())).thenReturn(ByteArray(0))
        whenever(repository.getItems()).thenReturn(flowOf(items))

        engine.runExport()

        val state = engine.state.value
        assert(state is SyncState.Error)
        val errorMsg = (state as SyncState.Error).message
        assert(errorMsg.contains("FTP error")) { "Expected FTP error in: $errorMsg" }
        assert(errorMsg.contains("HTTP error")) { "Expected HTTP error in: $errorMsg" }
    }
}
