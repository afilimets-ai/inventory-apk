package com.inventory.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun QuantityInput(
    quantity: Double,
    unit: String,
    onQuantityChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(formatQuantityInput(quantity)) }

    LaunchedEffect(quantity) {
        val parsed = text.parseQuantityOrNull()
        if (parsed == null || kotlin.math.abs(parsed - quantity) > 0.0001) {
            text = formatQuantityInput(quantity)
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { value ->
            text = value
            value.parseQuantityOrNull()?.takeIf { it >= 0.0 }?.let(onQuantityChange)
        },
        label = { Text("Кількість, $unit") },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth()
    )
}

private fun String.parseQuantityOrNull(): Double? =
    replace(',', '.').toDoubleOrNull()

private fun formatQuantityInput(quantity: Double): String =
    if (quantity % 1.0 == 0.0) quantity.toInt().toString() else quantity.toString()
