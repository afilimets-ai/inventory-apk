package com.inventory.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.sync.SyncEngine
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncSettings
import com.inventory.sync.SyncSettingsManager
import com.inventory.sync.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderRowState(
    val type: SyncProviderType,
    val importEnabled: Boolean,
    val exportEnabled: Boolean,
)

data class SyncSettingsUiState(
    val rows: List<ProviderRowState> = SyncProviderType.entries.map {
        ProviderRowState(it, false, false)
    }
)

@HiltViewModel
class SyncSettingsViewModel @Inject constructor(
    private val settingsManager: SyncSettingsManager,
    private val syncEngine: SyncEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyncSettingsUiState())
    val uiState: StateFlow<SyncSettingsUiState> = _uiState.asStateFlow()

    val syncState: StateFlow<SyncState> = syncEngine.state

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val rows = SyncProviderType.entries.map { type ->
            val s = settingsManager.getSettings(type)
            ProviderRowState(type, s.isImportEnabled, s.isExportEnabled)
        }
        _uiState.update { it.copy(rows = rows) }
    }

    fun setImportProvider(type: SyncProviderType) {
        // Радіо-кнопка: тільки один активний import
        _uiState.update { state ->
            state.copy(rows = state.rows.map { row ->
                row.copy(importEnabled = row.type == type)
            })
        }
        SyncProviderType.entries.forEach { t ->
            val enabled = t == type
            val current = settingsManager.getSettings(t)
            settingsManager.saveSettings(current.copy(isImportEnabled = enabled))
        }
    }

    fun setExportProvider(type: SyncProviderType) {
        // Радіо-кнопка: тільки один активний export
        _uiState.update { state ->
            state.copy(rows = state.rows.map { row ->
                row.copy(exportEnabled = row.type == type)
            })
        }
        SyncProviderType.entries.forEach { t ->
            val enabled = t == type
            val current = settingsManager.getSettings(t)
            settingsManager.saveSettings(current.copy(isExportEnabled = enabled))
        }
    }

    fun clearImport() {
        _uiState.update { state ->
            state.copy(rows = state.rows.map { it.copy(importEnabled = false) })
        }
        SyncProviderType.entries.forEach { t ->
            val current = settingsManager.getSettings(t)
            settingsManager.saveSettings(current.copy(isImportEnabled = false))
        }
    }

    fun clearExport() {
        _uiState.update { state ->
            state.copy(rows = state.rows.map { it.copy(exportEnabled = false) })
        }
        SyncProviderType.entries.forEach { t ->
            val current = settingsManager.getSettings(t)
            settingsManager.saveSettings(current.copy(isExportEnabled = false))
        }
    }

    fun runImport() {
        viewModelScope.launch { syncEngine.runImport() }
    }

    fun runExport() {
        viewModelScope.launch { syncEngine.runExport() }
    }

    fun applyColumnMapping(
        rawData: ByteArray,
        headers: List<String>,
        settings: SyncSettings,
        importFileName: String
    ) {
        viewModelScope.launch {
            syncEngine.applyImportWithMapping(rawData, headers, settings, importFileName)
        }
    }

    fun cancelColumnMapping() {
        syncEngine.cancelColumnMapping()
    }

    fun getProviderSettings(type: SyncProviderType): SyncSettings =
        settingsManager.getSettings(type)

    fun saveProviderSettings(settings: SyncSettings) {
        settingsManager.saveSettings(settings)
    }
}
