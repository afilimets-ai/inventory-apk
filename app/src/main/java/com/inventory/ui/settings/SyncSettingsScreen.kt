package com.inventory.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    onBack: () -> Unit,
    onProviderSettingsClick: (SyncProviderType) -> Unit,
    viewModel: SyncSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncState) {
        when (val state = syncState) {
            is SyncState.Success -> snackbarHostState.showSnackbar("Синхронізацію завершено")
            is SyncState.Error -> snackbarHostState.showSnackbar("Помилка: ${state.message}")
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Синхронізація", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Заголовок таблиці
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Провайдер",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "ІМП",
                    modifier = Modifier.width(56.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "ЕКСП",
                    modifier = Modifier.width(56.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(40.dp))
            }

            Divider()

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.rows) { row ->
                    ProviderRow(
                        row = row,
                        onImportSelect = { viewModel.setImportProvider(row.type) },
                        onExportSelect = { viewModel.setExportProvider(row.type) },
                        onSettingsClick = { onProviderSettingsClick(row.type) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            // Кнопки імпорту / експорту
            val isRunning = syncState is SyncState.Running
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.runImport() },
                    enabled = !isRunning && uiState.rows.any { it.importEnabled },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Імпорт", fontSize = 16.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.runExport() },
                    enabled = !isRunning && uiState.rows.any { it.exportEnabled },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Експорт", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun ProviderRow(
    row: ProviderRowState,
    onImportSelect: () -> Unit,
    onExportSelect: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = row.type.displayName,
            modifier = Modifier.weight(1f),
            fontSize = 15.sp
        )

        // ІМП радіо
        RadioButton(
            selected = row.importEnabled,
            onClick = onImportSelect,
            modifier = Modifier
                .width(56.dp)
                .size(48.dp)
        )

        // ЕКСП радіо
        RadioButton(
            selected = row.exportEnabled,
            onClick = onExportSelect,
            modifier = Modifier
                .width(56.dp)
                .size(48.dp)
        )

        // Кнопка налаштувань провайдера
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Налаштування ${row.type.displayName}",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
