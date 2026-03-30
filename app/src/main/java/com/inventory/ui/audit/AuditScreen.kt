package com.inventory.ui.audit

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inventory.ui.components.IndustrialButton
import com.inventory.ui.components.IndustrialOutlinedButton
import com.inventory.ui.components.IndustrialQuantityButton
import com.inventory.ui.components.IndustrialSuccessButton
import com.inventory.ui.components.ScanResultCard
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(
    onBack: () -> Unit,
    viewModel: AuditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var flashColor by remember { mutableStateOf(Color.Transparent) }
    val animatedFlash by animateColorAsState(
        targetValue = flashColor,
        animationSpec = tween(durationMillis = 150),
        label = "flash"
    )

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuditUiState.ItemScanned -> {
                flashColor = Color(0x5500C853); delay(300); flashColor = Color.Transparent
            }
            is AuditUiState.UnknownBarcode -> {
                flashColor = Color(0x55D32F2F); delay(300); flashColor = Color.Transparent
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Інвентаризація", fontSize = 20.sp)
                            val count = when (val s = uiState) {
                                is AuditUiState.Counting -> s.scannedCount
                                is AuditUiState.ItemScanned -> s.lines.size
                                is AuditUiState.UnknownBarcode -> s.lines.size
                                is AuditUiState.VarianceReport -> s.lines.size
                                else -> 0
                            }
                            if (count > 0) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("($count)", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (val state = uiState) {
                    is AuditUiState.SelectLocation -> SelectLocationContent(
                        state = state,
                        onSelect = viewModel::onLocationSelected,
                        onSkip = viewModel::onSkipLocation
                    )
                    is AuditUiState.Counting -> CountingContent(
                        state = state,
                        onTriggerScan = viewModel::triggerScan,
                        onManualEntry = viewModel::onManualBarcodeEntered,
                        onEndSession = viewModel::onEndSession
                    )
                    is AuditUiState.ItemScanned -> ItemScannedContent(
                        state = state,
                        onAdjust = viewModel::onAdjustCount,
                        onConfirm = viewModel::onConfirmCount,
                        onDismiss = viewModel::onDismissItem
                    )
                    is AuditUiState.UnknownBarcode -> UnknownBarcodeContent(
                        barcode = state.barcode,
                        onDismiss = viewModel::onDismissItem
                    )
                    is AuditUiState.VarianceReport -> VarianceReportContent(
                        state = state,
                        onNewSession = viewModel::onNewSession,
                        onBack = onBack
                    )
                }
            }
        }

        if (animatedFlash != Color.Transparent) {
            Box(modifier = Modifier.fillMaxSize().background(animatedFlash))
        }
    }
}

@Composable
private fun SelectLocationContent(
    state: AuditUiState.SelectLocation,
    onSelect: (com.inventory.data.entity.Location) -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp)
    ) {
        Text(
            "Оберіть локацію",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Або пропустіть для підрахунку всіх товарів",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (state.locations.isEmpty()) {
            Text(
                "Локації не знайдено. Натисніть 'Пропустити' для підрахунку всіх товарів.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(state.locations) { location ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelect(location) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                location.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (location.description.isNotBlank()) {
                                Text(
                                    location.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        IndustrialOutlinedButton(text = "Пропустити (всі товари)", onClick = onSkip)
    }
}

@Composable
private fun CountingContent(
    state: AuditUiState.Counting,
    onTriggerScan: () -> Unit,
    onManualEntry: (String) -> Unit,
    onEndSession: () -> Unit
) {
    var manualBarcode by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Інфо про локацію та прогрес
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    state.location?.name ?: "Всі локації",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "Підраховано: ${state.scannedCount} / ${state.expectedItems.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Text(
                "Σ ${formatQty(state.totalCounted)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список вже підрахованих
        if (state.lines.isNotEmpty()) {
            LazyColumn(modifier = Modifier.weight(0.4f)) {
                items(state.lines.values.toList()) { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(line.item.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(
                            "${formatQty(line.countedQuantity)} / ${formatQty(line.expectedQuantity)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (line.hasDiscrepancy) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Зона сканування
        Column(
            modifier = Modifier.weight(if (state.lines.isEmpty()) 1f else 0.6f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "СКАНУЙТЕ ТОВАР",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            IndustrialButton(
                text = "СКАНУВАТИ",
                onClick = onTriggerScan,
                edgeTarget = true,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            IndustrialOutlinedButton(
                text = "Ввести штрихкод вручну",
                onClick = { showManualInput = !showManualInput }
            )
            if (showManualInput) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it },
                    label = { Text("Штрихкод") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        onManualEntry(manualBarcode); manualBarcode = ""; showManualInput = false
                    })
                )
            }
        }

        if (state.lines.isNotEmpty()) {
            IndustrialOutlinedButton(
                text = "Завершити підрахунок (${state.scannedCount} позицій)",
                onClick = onEndSession
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ItemScannedContent(
    state: AuditUiState.ItemScanned,
    onAdjust: (Double) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        ScanResultCard(item = state.item)
        Spacer(modifier = Modifier.height(20.dp))

        // Очікувана кількість
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Очікувана:", style = MaterialTheme.typography.bodyLarge)
            Text(
                formatQty(state.expectedQuantity),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Підрахована кількість:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IndustrialQuantityButton(label = "−", onClick = { onAdjust((state.currentCount - 1).coerceAtLeast(0.0)) })
            Spacer(modifier = Modifier.width(28.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatQty(state.currentCount),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (state.currentCount != state.expectedQuantity) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(state.item.unit, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(28.dp))
            IndustrialQuantityButton(label = "+", onClick = { onAdjust(state.currentCount + 1) })
        }

        Spacer(modifier = Modifier.weight(1f))
        IndustrialSuccessButton(text = "✓  ПІДТВЕРДИТИ", onClick = onConfirm)
        Spacer(modifier = Modifier.height(12.dp))
        IndustrialOutlinedButton(text = "Скасувати", onClick = onDismiss)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun UnknownBarcodeContent(barcode: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ТОВАР НЕ ЗНАЙДЕНО", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(12.dp))
        Text(barcode, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(48.dp))
        IndustrialButton(text = "← Сканувати далі", onClick = onDismiss)
    }
}

@Composable
private fun VarianceReportContent(
    state: AuditUiState.VarianceReport,
    onNewSession: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            "ЗВІТ ІНВЕНТАРИЗАЦІЇ",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (state.location != null) {
            Text("Локація: ${state.location.name}", style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // Підсумки
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Очікувано: ${formatQty(state.totalExpected)}", style = MaterialTheme.typography.titleMedium)
            Text("Підраховано: ${formatQty(state.totalCounted)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        val variance = state.totalCounted - state.totalExpected
        if (variance != 0.0) {
            Text(
                "Розбіжність: ${if (variance > 0) "+" else ""}${formatQty(variance)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()

        // Заголовки
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Товар", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text("Очік.", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(50.dp))
            Text("Факт.", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(50.dp))
            Text("Δ", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(50.dp))
        }
        Divider()

        LazyColumn(modifier = Modifier.weight(1f)) {
            // Спочатку розбіжності
            val discrepancies = state.discrepancies
            if (discrepancies.isNotEmpty()) {
                items(discrepancies) { line ->
                    VarianceRow(line, highlight = true)
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }

            // Товари без розбіжностей
            val matches = state.lines.filter { !it.hasDiscrepancy }
            items(matches) { line ->
                VarianceRow(line, highlight = false)
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            // Не скановані товари
            if (state.missingItems.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Не підраховано (${state.missingItems.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                items(state.missingItems) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                        Text(formatQty(item.quantity), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(50.dp))
                        Text("—", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(50.dp))
                        Text("-${formatQty(item.quantity)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, modifier = Modifier.width(50.dp))
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        IndustrialSuccessButton(text = "Нова інвентаризація", onClick = onNewSession)
        Spacer(modifier = Modifier.height(12.dp))
        IndustrialOutlinedButton(text = "На головну", onClick = onBack)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun VarianceRow(line: CountLine, highlight: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            line.item.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(formatQty(line.expectedQuantity), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(50.dp))
        Text(formatQty(line.countedQuantity), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(50.dp))
        val v = line.variance
        Text(
            "${if (v > 0) "+" else ""}${formatQty(v)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (v != 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(50.dp)
        )
    }
}

private fun formatQty(qty: Double): String =
    if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
