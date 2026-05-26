package com.inventory.ui.scan

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalFocusManager
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
import com.inventory.ui.components.QuantityInput
import com.inventory.ui.components.ScanResultCard
import com.inventory.ui.components.ScanStatus
import com.inventory.ui.components.ScanStatusBar
import com.inventory.ui.theme.ThemeMode
import kotlinx.coroutines.delay

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel(),
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeToggle: () -> Unit = {},
    onSyncSettingsClick: () -> Unit = {},
    onReceivingClick: () -> Unit = {},
    onAuditClick: () -> Unit = {},
    onCatalogClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lastScannedItem by viewModel.lastScannedItem.collectAsState()
    val scannedItems by viewModel.scannedItems.collectAsState()

    var flashColor by remember { mutableStateOf(Color.Transparent) }
    val animatedFlash by animateColorAsState(
        targetValue = flashColor,
        animationSpec = tween(durationMillis = 150),
        label = "flash"
    )

    LaunchedEffect(uiState) {
        when (uiState) {
            is ScanUiState.ItemFound -> {
                flashColor = Color(0x5500C853)
                delay(300)
                flashColor = Color.Transparent
            }
            is ScanUiState.UnknownBarcode -> {
                flashColor = Color(0x55D32F2F)
                delay(300)
                flashColor = Color.Transparent
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                ScanStatusBar(
                    status = when (uiState) {
                        is ScanUiState.Idle -> ScanStatus.IDLE
                        is ScanUiState.ItemFound -> ScanStatus.SUCCESS
                        is ScanUiState.UnknownBarcode -> ScanStatus.ERROR
                        is ScanUiState.LookingUpBarcode -> ScanStatus.IDLE
                        is ScanUiState.LookupCandidate -> ScanStatus.SUCCESS
                        is ScanUiState.LookupNotFound -> ScanStatus.ERROR
                        is ScanUiState.Success -> ScanStatus.SUCCESS
                        is ScanUiState.Error -> ScanStatus.ERROR
                    },
                    label = when (uiState) {
                        is ScanUiState.Idle -> "Очікування сканування"
                        is ScanUiState.ItemFound -> "Товар знайдено"
                        is ScanUiState.UnknownBarcode -> "Невідомий штрихкод"
                        is ScanUiState.LookingUpBarcode -> "Пошук у глобальній базі"
                        is ScanUiState.LookupCandidate -> "Знайдено у глобальній базі"
                        is ScanUiState.LookupNotFound -> "Не знайдено"
                        is ScanUiState.Success -> "Записано"
                        is ScanUiState.Error -> "Помилка"
                    },
                    themeMode = themeMode,
                    onThemeToggle = onThemeToggle,
                    onSyncSettingsClick = onSyncSettingsClick
                )
            }
        ) { innerPadding ->
            Surface(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (val state = uiState) {
                    is ScanUiState.Idle -> IdleScreen(
                        onTriggerScan = viewModel::triggerScan,
                        onManualEntry = viewModel::onManualBarcodeEntered,
                        onReceivingClick = onReceivingClick,
                        onAuditClick = onAuditClick,
                        onCatalogClick = onCatalogClick,
                        lastScannedItem = lastScannedItem,
                        scannedItems = scannedItems
                    )
                    is ScanUiState.ItemFound -> ItemFoundScreen(
                        state = state,
                        onQuantityChange = viewModel::onQuantityChanged,
                        onConfirm = viewModel::onConfirm,
                        onDismiss = viewModel::onDismiss
                    )
                    is ScanUiState.UnknownBarcode -> UnknownBarcodeScreen(
                        barcode = state.barcode,
                        onLookup = viewModel::onLookupUnknownBarcode,
                        onDismiss = viewModel::onDismiss
                    )
                    is ScanUiState.LookingUpBarcode -> LookingUpBarcodeScreen(barcode = state.barcode)
                    is ScanUiState.LookupCandidate -> LookupCandidateScreen(
                        state = state,
                        onImport = viewModel::onImportLookupCandidate,
                        onDismiss = viewModel::onDismiss
                    )
                    is ScanUiState.LookupNotFound -> LookupNotFoundScreen(
                        state = state,
                        onDismiss = viewModel::onDismiss
                    )
                    is ScanUiState.Success -> SuccessScreen()
                    is ScanUiState.Error -> ErrorScreen(
                        message = state.message,
                        onDismiss = viewModel::onDismiss
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
private fun IdleScreen(
    onTriggerScan: () -> Unit,
    onManualEntry: (String) -> Unit,
    onReceivingClick: () -> Unit = {},
    onAuditClick: () -> Unit = {},
    onCatalogClick: () -> Unit = {},
    lastScannedItem: LastScannedItem? = null,
    scannedItems: List<LastScannedItem> = emptyList()
) {
    var manualBarcode by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }
    var showScannedItems by remember { mutableStateOf(false) }
    val scannedSummaries = remember(scannedItems) { aggregateScannedItems(scannedItems) }
    val focusManager = LocalFocusManager.current

    fun submitManualBarcode() {
        onManualEntry(manualBarcode)
        manualBarcode = ""
        showManualInput = false
        focusManager.clearFocus(force = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "СКАНЕР ГОТОВИЙ",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Натисніть кнопку сканера або кнопку нижче",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Основна кнопка — 80dp (biля нижнього краю)
        IndustrialButton(
            text = "СКАНУВАТИ",
            onClick = {
                focusManager.clearFocus(force = true)
                onTriggerScan()
            },
            edgeTarget = true,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        IndustrialButton(
            text = "ПРИЙОМ ТОВАРУ",
            onClick = onReceivingClick,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        IndustrialButton(
            text = "ІНВЕНТАРИЗАЦІЯ",
            onClick = onAuditClick,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        IndustrialOutlinedButton(
            text = "Довідник товарів",
            onClick = onCatalogClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        IndustrialOutlinedButton(
            text = "Ввести штрихкод вручну",
            onClick = {
                showManualInput = !showManualInput
                if (showManualInput) {
                    manualBarcode = ""
                } else {
                    focusManager.clearFocus(force = true)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        IndustrialOutlinedButton(
            text = "Відскановані товари (${scannedSummaries.size})",
            onClick = { showScannedItems = true },
            enabled = scannedSummaries.isNotEmpty()
        )

        if (showManualInput) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = manualBarcode,
                onValueChange = { manualBarcode = it },
                label = { Text("Штрихкод") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = { submitManualBarcode() })
            )
        }

        if (lastScannedItem != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Останній скан: ${formatQty(lastScannedItem.quantity)} ${lastScannedItem.item.unit}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            ScanResultCard(item = lastScannedItem.item)
        }

        if (showScannedItems) {
            ScannedItemsDialog(
                items = scannedSummaries,
                onDismiss = { showScannedItems = false }
            )
        }
    }
}

@Composable
private fun ScannedItemsDialog(
    items: List<ScannedItemSummary>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Відскановані товари") },
        text = {
            LazyColumn(modifier = Modifier.height(360.dp)) {
                itemsIndexed(items, key = { _, scanned -> scanned.key }) { index, scanned ->
                    ScannedItemRow(index + 1, scanned)
                    if (index < items.lastIndex) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрити")
            }
        }
    )
}

@Composable
private fun ScannedItemRow(index: Int, scanned: ScannedItemSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$index.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${scanned.name} - ${scanned.barcode}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "${formatQty(scanned.quantity)} ${scanned.unit}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class ScannedItemSummary(
    val key: String,
    val name: String,
    val barcode: String,
    val unit: String,
    val quantity: Double
)

private fun aggregateScannedItems(items: List<LastScannedItem>): List<ScannedItemSummary> {
    val summaries = LinkedHashMap<String, ScannedItemSummary>()
    items.forEach { scanned ->
        val key = "${scanned.item.id}|${scanned.scannedBarcode}|${scanned.item.unit}"
        val existing = summaries[key]
        summaries[key] = if (existing == null) {
            ScannedItemSummary(
                key = key,
                name = scanned.item.name,
                barcode = scanned.scannedBarcode,
                unit = scanned.item.unit,
                quantity = scanned.quantity
            )
        } else {
            existing.copy(quantity = existing.quantity + scanned.quantity)
        }
    }
    return summaries.values.toList()
}

@Composable
private fun ItemFoundScreen(
    state: ScanUiState.ItemFound,
    onQuantityChange: (Double) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        ScanResultCard(item = state.item)

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            "Кількість для прийому:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IndustrialQuantityButton(
                label = "−",
                onClick = { onQuantityChange((state.quantity - 1).coerceAtLeast(0.0)) }
            )
            Spacer(modifier = Modifier.width(28.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatQty(state.quantity),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = state.item.unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(28.dp))
            IndustrialQuantityButton(
                label = "+",
                onClick = { onQuantityChange(state.quantity + 1) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        QuantityInput(
            quantity = state.quantity,
            unit = state.item.unit,
            onQuantityChange = onQuantityChange
        )

        Spacer(modifier = Modifier.weight(1f))

        IndustrialSuccessButton(text = "✓  ПІДТВЕРДИТИ", onClick = onConfirm)
        Spacer(modifier = Modifier.height(12.dp))
        IndustrialOutlinedButton(text = "Скасувати", onClick = onDismiss)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun UnknownBarcodeScreen(
    barcode: String,
    onLookup: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "ТОВАР НЕ ЗНАЙДЕНО",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            barcode,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Цей штрихкод відсутній у базі даних.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        IndustrialButton(text = "Шукати у глобальній базі", onClick = onLookup)
        Spacer(modifier = Modifier.height(12.dp))
        IndustrialButton(text = "← Назад до сканування", onClick = onDismiss)
    }
}

@Composable
private fun LookingUpBarcodeScreen(barcode: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(20.dp))
        Text("Шукаю товар у глобальній базі", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(barcode, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LookupCandidateScreen(
    state: ScanUiState.LookupCandidate,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text("Товар знайдено", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Джерело: ${state.source}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(20.dp))
        ScanResultCard(item = state.item)
        Spacer(modifier = Modifier.weight(1f))
        IndustrialSuccessButton(text = "Додати товар", onClick = onImport)
        Spacer(modifier = Modifier.height(12.dp))
        IndustrialOutlinedButton(text = "Скасувати", onClick = onDismiss)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun LookupNotFoundScreen(
    state: ScanUiState.LookupNotFound,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "НЕ ЗНАЙДЕНО",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(state.barcode, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(state.message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))
        IndustrialButton(text = "← Назад до сканування", onClick = onDismiss)
    }
}

@Composable
private fun SuccessScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(com.inventory.ui.theme.ScanColors.Success),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✓", fontSize = 96.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "ЗАПИСАНО",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun ErrorScreen(message: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "ПОМИЛКА",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))
        IndustrialButton(text = "Закрити", onClick = onDismiss)
    }
}

private fun formatQty(qty: Double): String =
    if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
