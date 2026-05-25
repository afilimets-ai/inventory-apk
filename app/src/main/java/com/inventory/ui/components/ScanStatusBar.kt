package com.inventory.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inventory.ui.theme.ScanColors
import com.inventory.ui.theme.ThemeMode

enum class ScanStatus { IDLE, SCANNING, SUCCESS, ERROR }

@Composable
fun ScanStatusBar(
    status: ScanStatus,
    label: String,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeToggle: (() -> Unit)? = null,
    onSyncSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (bgColor, indicatorColor) = when (status) {
        ScanStatus.IDLE     -> MaterialTheme.colorScheme.surfaceVariant to ScanColors.Idle
        ScanStatus.SCANNING -> MaterialTheme.colorScheme.surfaceVariant to ScanColors.Warning
        ScanStatus.SUCCESS  -> ScanColors.SuccessContainer to ScanColors.Success
        ScanStatus.ERROR    -> ScanColors.ErrorContainer to ScanColors.Error
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (onSyncSettingsClick != null) {
            IconButton(
                onClick = onSyncSettingsClick,
                modifier = Modifier
                    .noScannerKeyFocus()
                    .size(40.dp)
            ) {
                Text(text = "⇅", style = MaterialTheme.typography.titleMedium)
            }
        }
        if (onThemeToggle != null) {
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .noScannerKeyFocus()
                    .size(40.dp)
            ) {
                Text(
                    text = when (themeMode) {
                        ThemeMode.SYSTEM -> "⚙"
                        ThemeMode.LIGHT  -> "☀"
                        ThemeMode.DARK   -> "🌙"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
