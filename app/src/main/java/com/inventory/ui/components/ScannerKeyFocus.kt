package com.inventory.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties

fun Modifier.noScannerKeyFocus(): Modifier =
    focusProperties { canFocus = false }
