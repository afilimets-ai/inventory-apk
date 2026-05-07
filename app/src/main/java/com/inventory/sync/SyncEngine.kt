package com.inventory.sync

import com.inventory.data.entity.InventoryItem
import com.inventory.data.repository.InventoryRepository
import com.inventory.sync.serializer.CsvSerializer
import com.inventory.sync.serializer.ExcelSerializer
import com.inventory.sync.serializer.JsonSerializer
import com.inventory.sync.serializer.SyncSerializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_EXPORT_FILE_NAME = "inventory_export"
private const val DEFAULT_IMPORT_FILE_NAME = "inventory_import"

@Singleton
class SyncEngine @Inject constructor(
    private val repository: InventoryRepository,
    private val settingsManager: SyncSettingsManager,
    private val providerFactory: SyncProviderFactory,
    private val csvSerializer: CsvSerializer,
    private val jsonSerializer: JsonSerializer,
    private val excelSerializer: ExcelSerializer,
) {
    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state

    /** Запустити повний цикл: експорт через всі активні export-провайдери */
    suspend fun runExport() {
        val exportProviders = settingsManager.getExportProviders()
        if (exportProviders.isEmpty()) return

        _state.value = SyncState.Running

        try {
            val items = repository.getItems().first()
            val errors = mutableListOf<String>()

            for (settings in exportProviders) {
                val provider = providerFactory.create(settings.providerType)
                if (!provider.supportsExport) continue

                val serializer = getSerializer(settings.format)
                val rows = items.map { it.toExportRow() }
                val data = serializer.serialize(rows)

                val exportName = settings.exportFileName.ifEmpty { DEFAULT_EXPORT_FILE_NAME }
                when (val result = provider.export(data, settings.format, exportName)) {
                    is SyncResult.Success -> { /* ok */ }
                    is SyncResult.Failure -> errors.add("[${settings.providerType.displayName}] ${result.message}")
                }
            }

            _state.value = if (errors.isEmpty()) {
                SyncState.Success(System.currentTimeMillis())
            } else {
                SyncState.Error(errors.joinToString("\n"))
            }
        } catch (e: Exception) {
            _state.value = SyncState.Error("Export failed: ${e.message}")
        }
    }

    /** Запустити імпорт через перший активний import-провайдер */
    suspend fun runImport() {
        val importProviders = settingsManager.getImportProviders()
        if (importProviders.isEmpty()) return

        _state.value = SyncState.Running
        try {
            for (settings in importProviders) {
                val provider = providerFactory.create(settings.providerType)
                if (!provider.supportsImport) continue

                // Якщо ім'я файлу не задане — шукаємо найновіший файл за форматом
                val importName = if (settings.importFileName.isNotEmpty()) {
                    settings.importFileName
                } else {
                    val discovered = provider.discoverImportFiles(settings.format)
                    discovered.firstOrNull() ?: DEFAULT_IMPORT_FILE_NAME
                }
                when (val importResult = provider.import(settings.format, importName)) {
                    is SyncImportResult.Success -> {
                        val serializer = getSerializer(settings.format)
                        val rows = serializer.deserialize(importResult.data)
                        applyImport(rows)
                        _state.value = SyncState.Success(System.currentTimeMillis())
                        return
                    }
                    is SyncImportResult.Failure -> {
                        _state.value = SyncState.Error(
                            "[${settings.providerType.displayName}] ${importResult.message}"
                        )
                        return
                    }
                }
            }

            if (_state.value == SyncState.Running) {
                _state.value = SyncState.Idle
            }
        } catch (e: Exception) {
            _state.value = SyncState.Error("Import failed: ${e.message}")
        }
    }

    private suspend fun applyImport(rows: List<Map<String, Any?>>) {
        repository.importItems(rows)
    }

    private fun getSerializer(format: SyncFormat): SyncSerializer = when (format) {
        SyncFormat.CSV -> csvSerializer
        SyncFormat.JSON -> jsonSerializer
        SyncFormat.EXCEL -> excelSerializer
    }
}

private fun InventoryItem.toExportRow(): Map<String, Any?> = mapOf(
    "id" to id,
    "barcode" to barcode,
    "name" to name,
    "description" to description,
    "quantity" to quantity,
    "unit" to unit,
    "min_quantity" to minQuantity,
    "category_id" to categoryId,
    "location_id" to locationId,
    "notes" to notes,
    "updated_at" to updatedAt
)
