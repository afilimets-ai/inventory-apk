package com.inventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventory.data.entity.InventoryItem
import com.inventory.ui.theme.ScanColors

@Composable
fun ScanResultCard(item: InventoryItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Назва товару — великий жирний текст
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Штрихкод
            LabelValueRow(label = "Штрихкод", value = item.barcode)

            if (item.sku.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                LabelValueRow(label = "SKU", value = item.sku)
            }

            if (item.groupName.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                LabelValueRow(label = "Група", value = item.groupName)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Поточна кількість — виділяємо великим шрифтом
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Залишок:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${formatQty(item.quantity)} ${item.unit}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.quantity <= item.minQuantity && item.minQuantity > 0)
                        ScanColors.Warning else MaterialTheme.colorScheme.onSurface
                )
            }

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (item.isWeighted || item.isPackage) {
                Spacer(modifier = Modifier.height(10.dp))
                val flags = buildList {
                    if (item.isWeighted) add("Ваговий")
                    if (item.isPackage) {
                        val unit = item.packageUnit.ifBlank { item.unit }
                        add("Упаковка: ${formatQty(item.packageCoefficient)} $unit")
                    }
                }
                Text(
                    text = flags.joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ScanStatusChip(
    text: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isSuccess) ScanColors.SuccessContainer else ScanColors.ErrorContainer
    val fg = if (isSuccess) ScanColors.Success else ScanColors.Error

    Text(
        text = text,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = fg,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
}

@Composable
private fun LabelValueRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatQty(qty: Double): String =
    if (qty % 1.0 == 0.0) qty.toInt().toString() else qty.toString()
