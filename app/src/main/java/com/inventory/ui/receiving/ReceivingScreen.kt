package com.inventory.ui.receiving

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivingScreen(
    onBack: () -> Unit,
    viewModel: ReceivingViewModel = hiltViewModel()
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
            is ReceivingUiState.ItemFound -> {
                flashColor = Color(0x5500C853)
                delay(300)
                flashColor = Color.Transparent
            }
            is ReceivingUiState.UnknownBarcode -> {
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
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Прийом товару", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            val lineCount = when (val s = uiState) {
                                is ReceivingUiState.Scanning -> s.sessionLines.size
                                is ReceivingUiState.ItemFound -> s.sessionLines.size
                                is ReceivingUiState.UnknownBarcode -> s.sessionLines.size
                                is ReceivingUiState.LookingUpBarcode -> s.sessionLines.size
                                is ReceivingUiState.LookupCandidate -> s.sessionLines.size
                                is ReceivingUiState.LookupNotFound -> s.sessionLines.size
                                is ReceivingUiState.SessionSummary -> s.totalItems
                            }
                            if (lineCount > 0) {
                                Text(
                                    "($lineCount)",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.background
            ) {
                when (val state = uiState) {
                    is ReceivingUiState.Scanning -> ScanningContent(
                        state = state,
                        onTriggerScan = viewModel::triggerScan,
                        onManualEntry = viewModel::onManualBarcodeEntered,
                        onEndSession = viewModel::onEndSession
                    )
                    is ReceivingUiState.ItemFound -> ItemFoundContent(
                        state = state,
                        onQuantityChange = viewModel::onQuantityChanged,
                        onConfirm = viewModel::onConfirmReceive,
                        onDismiss = viewModel::onDismissItem
                    )
                    is ReceivingUiState.UnknownBarcode -> UnknownBarcodeContent(
                        barcode = state.barcode,
                        onLookup = viewModel::onLookupUnknownBarcode,
                        onDismiss = viewModel::onDismissItem
                    )
                    is ReceivingUiState.LookingUpBarcode -> LookingUpBarcodeContent(barcode = state.barcode)
                    is ReceivingUiState.LookupCandidate -> LookupCandidateContent(
                        state = state,
                        onImport = viewModel::onImportLookupCandidate,
                        onDismiss = viewModel::onDismissItem
                    )
                    is ReceivingUiState.LookupNotFound -> LookupNotFoundContent(
                        state = state,
                        onDismiss = viewModel::onDismissItem
                    )
                    is ReceivingUiState.SessionSummary -> SessionSummaryContent(
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
private fun ScanningContent(
    state: ReceivingUiState.Scanning,
    onTriggerScan: () -> Unit,
    onManualEntry: (String) -> Unit,
    onEndSession: () -> Unit
) {
    var manualBarcode by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }
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
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Running total
        if (state.sessionLines.isNotEmpty()) {
            RunningTotal(state.sessionLines)
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "СКАНУЙТЕ ТОВАР",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Натисніть кнопку сканера або F6",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

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

            if (showManualInput) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it },
                    label = { Text("Штрихкод") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(onSearch = { submitManualBarcode() })
                )
            }
        }

        // Кнопка завершення сесії (якщо є записи)
        if (state.sessionLines.isNotEmpty()) {
            IndustrialOutlinedButton(
                text = "Завершити прийом (${state.sessionLines.size} позицій)",
                onClick = onEndSession
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun RunningTotal(lines: List<ReceivedLine>) {
    val totalQty = lines.sumOf { it.quantity }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Прийнято: ${lines.size} позицій",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            "Σ ${formatQty(totalQty)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun ItemFoundContent(
    state: ReceivingUiState.ItemFound,
    onQuantityChange: (Double) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
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

        // Показуємо лічильник сесії
        if (state.sessionLines.isNotEmpty()) {
            Text(
                "Вже прийнято: ${state.sessionLines.size} позицій",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        IndustrialSuccessButton(text = "✓  ПРИЙНЯТИ", onClick = onConfirm)
        Spacer(modifier = Modifier.height(12.dp))
        IndustrialOutlinedButton(text = "Скасувати", onClick = onDismiss)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun UnknownBarcodeContent(
    barcode: String,
    onLookup: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
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
            "Цей штрихкод відсутній у каталозі.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        IndustrialButton(text = "Шукати у глобальній базі", onClick = onLookup)
        Spacer(modifier = Modifier.height(12.dp))
        IndustrialButton(text = "← Сканувати далі", onClick = onDismiss)
    }
}

@Composable
private fun LookingUpBarcodeContent(barcode: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
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
private fun LookupCandidateContent(
    state: ReceivingUiState.LookupCandidate,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
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
private fun LookupNotFoundContent(
    state: ReceivingUiState.LookupNotFound,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
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
        IndustrialButton(text = "← Сканувати далі", onClick = onDismiss)
    }
}

@Composable
private fun SessionSummaryContent(
    state: ReceivingUiState.SessionSummary,
    onNewSession: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Заголовок
        Text(
            "ПРИЙОМ ЗАВЕРШЕНО",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Позицій: ${state.totalItems}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Загалом: ${formatQty(state.totalQuantity)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()

        // Список прийнятих позицій
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.lines) { line ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            line.item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            line.item.barcode,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "${formatQty(line.quantity)} ${line.item.unit}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        IndustrialSuccessButton(text = "Новий прийом", onClick = onNewSession)
        Spacer(modifier = Modifier.height(12.dp))
        IndustrialOutlinedButton(text = "На головну", onClick = onBack)
        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun formatQty(qty: Double): String =
    if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
