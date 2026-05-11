package com.inventory.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.sync.CsvDelimiter
import com.inventory.sync.CsvFieldType
import com.inventory.sync.CsvImportConfig
import com.inventory.sync.SyncProviderType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CsvImportSettingsScreen(
    providerType: SyncProviderType,
    onBack: () -> Unit,
    viewModel: SyncSettingsViewModel = hiltViewModel(),
) {
    val initialConfig = remember(providerType) { viewModel.getCsvImportConfig(providerType) }
    var delimiter by rememberSaveable { mutableStateOf(initialConfig.delimiter) }
    var ignoreFirstRow by rememberSaveable { mutableStateOf(initialConfig.ignoreFirstRow) }
    var columns by rememberSaveable { mutableStateOf(initialConfig.columns) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CSV-імпорт: ${providerType.displayName}", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Параметри",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            DelimiterDropdown(selected = delimiter, onSelect = { delimiter = it })
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = ignoreFirstRow, onCheckedChange = { ignoreFirstRow = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ігнорувати перший рядок (заголовки)")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Поля (порядок колонок зліва направо)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Зіставте кожну колонку CSV-файла з полем товару. " +
                    "Виберіть «— ігнорувати —», щоб пропустити колонку.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            columns.forEachIndexed { index, field ->
                ColumnRow(
                    position = index + 1,
                    field = field,
                    canMoveUp = index > 0,
                    canMoveDown = index < columns.size - 1,
                    onSelect = { newField ->
                        columns = columns.toMutableList().also { it[index] = newField }
                    },
                    onMoveUp = {
                        columns = columns.toMutableList().also {
                            val tmp = it[index]; it[index] = it[index - 1]; it[index - 1] = tmp
                        }
                    },
                    onMoveDown = {
                        columns = columns.toMutableList().also {
                            val tmp = it[index]; it[index] = it[index + 1]; it[index + 1] = tmp
                        }
                    },
                    onRemove = {
                        columns = columns.toMutableList().also { it.removeAt(index) }
                    },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = { columns = columns + CsvFieldType.IGNORE },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Додати поле")
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    val defaults = CsvImportConfig.DEFAULT
                    delimiter = defaults.delimiter
                    ignoreFirstRow = defaults.ignoreFirstRow
                    columns = defaults.columns
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Скинути до стандартних")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.saveCsvImportConfig(
                        providerType,
                        CsvImportConfig(
                            delimiter = delimiter,
                            ignoreFirstRow = ignoreFirstRow,
                            columns = columns,
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = columns.any { it != CsvFieldType.IGNORE }
            ) {
                Text("Зберегти", fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DelimiterDropdown(selected: CsvDelimiter, onSelect: (CsvDelimiter) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Роздільник полів") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CsvDelimiter.entries.forEach { d ->
                DropdownMenuItem(
                    text = { Text(d.displayName) },
                    onClick = { onSelect(d); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnRow(
    position: Int,
    field: CsvFieldType,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onSelect: (CsvFieldType) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$position.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            FieldTypeDropdown(selected = field, onSelect = onSelect)
        }

        IconButton(onClick = onMoveUp, enabled = canMoveUp) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Вгору")
        }
        IconButton(onClick = onMoveDown, enabled = canMoveDown) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Вниз")
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Видалити",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FieldTypeDropdown(selected: CsvFieldType, onSelect: (CsvFieldType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CsvFieldType.entries.forEach { f ->
                DropdownMenuItem(
                    text = { Text(f.displayName) },
                    onClick = { onSelect(f); expanded = false }
                )
            }
        }
    }
}
