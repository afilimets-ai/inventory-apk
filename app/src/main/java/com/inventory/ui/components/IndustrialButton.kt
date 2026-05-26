package com.inventory.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inventory.ui.theme.ScanColors

// Мінімум 64dp висоти для роботи в рукавицях (spec: 60dp, 80dp біля країв)
private val BUTTON_HEIGHT_DEFAULT: Dp = 64.dp
private val BUTTON_HEIGHT_EDGE: Dp = 80.dp

@Composable
fun IndustrialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    edgeTarget: Boolean = false,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .noScannerKeyFocus()
            .fillMaxWidth()
            .height(if (edgeTarget) BUTTON_HEIGHT_EDGE else BUTTON_HEIGHT_DEFAULT),
        colors = colors,
        enabled = enabled
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun IndustrialSuccessButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IndustrialButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        edgeTarget = true,
        colors = ButtonDefaults.buttonColors(
            containerColor = ScanColors.Success,
            contentColor = ScanColors.OnSuccess
        )
    )
}

@Composable
fun IndustrialOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .noScannerKeyFocus()
            .fillMaxWidth()
            .height(BUTTON_HEIGHT_DEFAULT),
        enabled = enabled
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun IndustrialQuantityButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .noScannerKeyFocus()
            .size(72.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(text = label, fontSize = 30.sp, fontWeight = FontWeight.Bold)
    }
}
