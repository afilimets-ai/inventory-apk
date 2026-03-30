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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    onReceivingClick: () -> Unit = {}
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
                        is ScanUiState.Success -> ScanStatus.SUCCESS
                        is ScanUiState.Error -> ScanStatus.ERROR
                    },
                    label = when (uiState) {
                        is ScanUiState.Idle -> "Очікування сканування"
                        is ScanUiState.ItemFound -> "Товар знайдено"
                        is ScanUiState.UnknownBarcode -> "Невідомий штрихкод"
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
                        onReceivingClick = onReceivingClick
                    )
                    is ScanUiState.ItemFound -> ItemFoundScreen(
                        state = state,
                        onQuantityChange = viewModel::onQuantityChanged,
                        onConfirm = viewModel::onConfirm,
                        onDismiss = viewModel::onDismiss
                    )
                    is ScanUiState.UnknownBarcode -> UnknownBarcodeScreen(
                        barcode = state.barcode,
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
    onReceivingClick: () -> Unit = {}
) {
    var manualBarcode by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp),
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
            onClick = onTriggerScan,
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

        IndustrialOutlinedButton(
            text = "Ввести штрихкод вручну",
            onClick = { showManualInput = !showManualInput }
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
                keyboardActions = KeyboardActions(onSearch = {
                    onManualEntry(manualBarcode)
                    manualBarcode = ""
                    showManualInput = false
                })
            )
        }
    }
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

        Spacer(modifier = Modifier.weight(1f))

        IndustrialSuccessButton(text = "✓  ПІДТВЕРДИТИ", onClick = onConfirm)
        Spacer(modifier = Modifier.height(12.dp))
        IndustrialOutlinedButton(text = "Скасувати", onClick = onDismiss)
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun UnknownBarcodeScreen(barcode: String, onDismiss: () -> Unit) {
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
        Spacer(modifier = Modifier.height(48.dp))
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
