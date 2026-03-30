package com.inventory.ui.scan

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ScanScreen(viewModel: ScanViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (val state = uiState) {
            is ScanUiState.Idle -> IdleScreen(onTriggerScan = viewModel::triggerScan, onManualEntry = viewModel::onManualBarcodeEntered)
            is ScanUiState.ItemFound -> ItemFoundScreen(state = state, onQuantityChange = viewModel::onQuantityChanged, onConfirm = viewModel::onConfirm, onDismiss = viewModel::onDismiss)
            is ScanUiState.UnknownBarcode -> UnknownBarcodeScreen(barcode = state.barcode, onDismiss = viewModel::onDismiss)
            is ScanUiState.Success -> SuccessScreen()
            is ScanUiState.Error -> ErrorScreen(message = state.message, onDismiss = viewModel::onDismiss)
        }
    }
}

@Composable
private fun IdleScreen(onTriggerScan: () -> Unit, onManualEntry: (String) -> Unit) {
    var manualBarcode by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Готовий до сканування", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Натисніть кнопку сканера або кнопку нижче", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onTriggerScan,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("СКАНУВАТИ", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { showManualInput = !showManualInput },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Ввести штрихкод вручну", fontSize = 16.sp)
        }

        if (showManualInput) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = manualBarcode,
                onValueChange = { manualBarcode = it },
                label = { Text("Штрихкод") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
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
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("Товар знайдено", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(state.item.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Штрихкод: ${state.item.barcode}", style = MaterialTheme.typography.bodyMedium)
                Text("Поточна кількість: ${state.item.quantity} ${state.item.unit}", style = MaterialTheme.typography.bodyMedium)
                if (state.item.description.isNotBlank()) {
                    Text(state.item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Кількість:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { onQuantityChange((state.quantity - 1).coerceAtLeast(0.0)) },
                modifier = Modifier.size(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("−", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = state.quantity.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(24.dp))
            Button(
                onClick = { onQuantityChange(state.quantity + 1) },
                modifier = Modifier.size(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("+", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(72.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("ПІДТВЕРДИТИ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Скасувати", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UnknownBarcodeScreen(barcode: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Товар не знайдено", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Штрихкод: $barcode", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Цей товар відсутній у базі даних.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(48.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text("Назад до сканування", fontSize = 18.sp)
        }
    }
}

@Composable
private fun SuccessScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF2E7D32)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✓", fontSize = 80.sp, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ЗАПИСАНО", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun ErrorScreen(message: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Помилка", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text("Закрити", fontSize = 18.sp)
        }
    }
}
