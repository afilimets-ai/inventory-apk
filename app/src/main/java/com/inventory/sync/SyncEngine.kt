package com.inventory.sync

import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.OutboxStatus
import com.inventory.data.repository.InventoryRepository
import com.inventory.sync.catalogimport.ColumnMapping
import com.inventory.sync.catalogimport.ColumnMappingHeuristic
import com.inventory.sync.catalogimport.ImportPreview
import com.inventory.sync.catalogimport.TargetFields
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

                syncOutbox(provider, errors)

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

    private suspend fun syncOutbox(provider: SyncProvider, errors: MutableList<String>) {
        if (!provider.supportsOutbox) return

        val entries = repository.getPendingOutbox() + repository.getFailedOutboxForRetry()
        for (entry in entries.distinctBy { it.id }) {
            repository.updateOutboxStatus(entry.id, OutboxStatus.SYNCING.name)
            when (val result = provider.sendOutbox(entry)) {
                is SyncResult.Success -> repository.updateOutboxStatus(entry.id, OutboxStatus.SYNCED.name)
                is SyncResult.Failure -> {
                    repository.markOutboxFailed(entry.id, result.message)
                    errors.add("[${provider.type.displayName}] outbox ${entry.id}: ${result.message}")
                }
            }
        }
        repository.deleteSyncedOutbox()
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
                        val preview = serializer.parsePreview(importResult.data)
                        val suggestedMapping = preview.suggestMapping()
                        val savedMapping = settings.savedImportMapping?.takeIf { it.hasRequiredFields() }
                        val canImportDirectly = savedMapping == null && preview.detectedHasHeader && suggestedMapping.hasRequiredFields()
                        if (savedMapping == null && !canImportDirectly) {
                            _state.value = SyncState.PendingMapping(
                                PendingImportMapping(
                                    settings = settings,
                                    fileName = importName,
                                    preview = preview,
                                    suggestedMapping = suggestedMapping
                                )
                            )
                            return
                        }

                        val importSummary = if (savedMapping != null) {
                            val rawRows = serializer.parseRaw(importResult.data, savedMapping.treatFirstRowAsHeader)
                            repository.applyMappedImport(rawRows, savedMapping, TargetFields.all)
                            rawRows.toImportSummary(settings, importName, savedMapping)
                        } else {
                            val rows = serializer.deserialize(importResult.data)
                            applyImport(rows)
                            rows.toImportSummary(settings, importName)
                        }
                        _state.value = SyncState.Success(
                            timestamp = System.currentTimeMillis(),
                            importSummary = importSummary
                        )
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

    suspend fun applyPendingImport(mapping: ColumnMapping, saveMapping: Boolean = true) {
        val pending = (_state.value as? SyncState.PendingMapping)?.pending ?: return
        _state.value = SyncState.Running
        try {
            val provider = providerFactory.create(pending.settings.providerType)
            when (val importResult = provider.import(pending.settings.format, pending.fileName)) {
                is SyncImportResult.Success -> {
                    val serializer = getSerializer(pending.settings.format)
                    val rawRows = serializer.parseRaw(importResult.data, mapping.treatFirstRowAsHeader)
                    repository.applyMappedImport(rawRows, mapping, TargetFields.all)
                    if (saveMapping) {
                        settingsManager.saveSettings(pending.settings.copy(savedImportMapping = mapping))
                    }
                    _state.value = SyncState.Success(
                        timestamp = System.currentTimeMillis(),
                        importSummary = rawRows.toImportSummary(pending.settings, pending.fileName, mapping)
                    )
                }
                is SyncImportResult.Failure -> {
                    _state.value = SyncState.Error(
                        "[${pending.settings.providerType.displayName}] ${importResult.message}"
                    )
                }
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

private fun List<Map<String, Any?>>.toImportSummary(
    settings: SyncSettings,
    fileName: String
): SyncImportSummary =
    SyncImportSummary(
        providerName = settings.providerType.displayName,
        fileName = fileName.withFormatExtension(settings.format),
        formatName = settings.format.displayName,
        totalRows = size,
        items = mapNotNull { row ->
            val barcode = row["barcode"]?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val name = row["name"]?.toString()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            SyncImportedItem(
                barcode = barcode,
                name = name,
                quantity = row["quantity"]?.toString()?.toDoubleOrNull() ?: 0.0,
                unit = row["unit"]?.toString()?.takeIf { it.isNotBlank() } ?: "шт"
            )
        }
    )

private fun ImportPreview.suggestMapping(): ColumnMapping =
    ColumnMapping(
        treatFirstRowAsHeader = detectedHasHeader,
        mapping = if (detectedHasHeader) {
            ColumnMappingHeuristic.fuzzySuggestMapping(headerRow, TargetFields.all)
        } else {
            headerRow.indices.associateWith { index ->
                when (index) {
                    0 -> "barcode"
                    1 -> "name"
                    2 -> "quantity"
                    3 -> "unit"
                    else -> null
                }
            }
        }
    )

private fun ColumnMapping.hasRequiredFields(): Boolean =
    mapping.values.contains("barcode") && mapping.values.contains("name")

private fun List<List<String?>>.toImportSummary(
    settings: SyncSettings,
    fileName: String,
    mapping: ColumnMapping
): SyncImportSummary =
    SyncImportSummary(
        providerName = settings.providerType.displayName,
        fileName = fileName.withFormatExtension(settings.format),
        formatName = settings.format.displayName,
        totalRows = size,
        items = mapNotNull { row ->
            val barcode = row.cellFor(mapping, "barcode") ?: return@mapNotNull null
            val name = row.cellFor(mapping, "name") ?: return@mapNotNull null
            SyncImportedItem(
                barcode = barcode,
                name = name,
                quantity = row.cellFor(mapping, "quantity")?.toDoubleOrNull() ?: 0.0,
                unit = row.cellFor(mapping, "unit") ?: "шт"
            )
        }
    )

private fun List<String?>.cellFor(mapping: ColumnMapping, fieldId: String): String? =
    mapping.mapping.entries.firstOrNull { it.value == fieldId }?.key
        ?.let { getOrNull(it) }?.takeIf { it.isNotBlank() }

private fun String.withFormatExtension(format: SyncFormat): String {
    val ext = ".${format.extension}"
    return if (endsWith(ext, ignoreCase = true)) this else "$this$ext"
}
