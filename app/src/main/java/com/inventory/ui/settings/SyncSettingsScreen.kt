package com.inventory.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.sync.SyncImportSummary
import com.inventory.sync.SyncImportedItem
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncState
import com.inventory.sync.serializer.CsvSerializer

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

    val columnMappingState = syncState as? SyncState.ColumnMappingRequired
    if (columnMappingState != null) {
        ColumnMappingDialog(
            columnCount = columnMappingState.columnCount,
            sampleRow = columnMappingState.sampleRow,
            onConfirm = { headers ->
                viewModel.applyColumnMapping(
                    rawData = columnMappingState.rawData,
                    headers = headers,
                    settings = columnMappingState.settings,
                    importFileName = columnMappingState.importFileName
                )
            },
            onDismiss = { viewModel.cancelColumnMapping() }
        )
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

            val importSummary = (syncState as? SyncState.Success)?.importSummary
            if (importSummary != null) {
                ImportSummaryCard(importSummary)
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
private fun ImportSummaryCard(summary: SyncImportSummary) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Імпорт завершено",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${summary.providerName} · ${summary.fileName} · ${summary.formatName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Рядків у файлі: ${summary.totalRows}. Товарів показано: ${summary.items.size}.",
                style = MaterialTheme.typography.bodyMedium
            )
            Divider()
            if (summary.items.isEmpty()) {
                Text(
                    text = "У файлі не знайдено товарів з полями barcode/name.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(220.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(summary.items) { item ->
                        ImportedItemRow(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportedItemRow(item: SyncImportedItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = item.barcode,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Text(
            text = "${item.quantity} ${item.unit}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
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

@Composable
private fun ColumnMappingDialog(
    columnCount: Int,
    sampleRow: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val skipOption = "" to "— Пропустити —"
    val options = listOf(skipOption) + CsvSerializer.MAPPABLE_COLUMNS.map { it.key to it.displayName }
    var selections by remember {
        mutableStateOf(List(columnCount) { "" })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Назви колонок не знайдено",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Файл не містить рядка із назвами полів. " +
                            "Оберіть відповідне поле для кожної колонки:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                for (colIndex in 0 until columnCount) {
                    ColumnMappingRow(
                        columnIndex = colIndex,
                        sampleValue = sampleRow.getOrElse(colIndex) { "" },
                        selectedKey = selections[colIndex],
                        options = options,
                        onSelected = { key ->
                            selections = selections.toMutableList().also { it[colIndex] = key }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selections) },
                enabled = selections.any { it.isNotBlank() }
            ) {
                Text("Імпортувати")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Скасувати")
            }
        }
    )
}

@Composable
private fun ColumnMappingRow(
    columnIndex: Int,
    sampleValue: String,
    selectedKey: String,
    options: List<Pair<String, String>>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedKey }?.second ?: "— Пропустити —"

    Column {
        Text(
            text = "Колонка ${columnIndex + 1}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Зразок: $sampleValue",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedLabel,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onSelected(key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
