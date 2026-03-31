package com.inventory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inventory.ui.sync.SyncDisplayMode
import com.inventory.ui.sync.SyncStatusState

// Семантичні кольори для sync індикатора
private val SyncOfflineColor = Color(0xFFE65100)       // amber/orange
private val SyncOfflineBg = Color(0xFFFFF3E0)
private val SyncOnlineColor = Color(0xFF1B5E20)        // green
private val SyncSyncingColor = Color(0xFF1565C0)       // blue
private val SyncSyncingBg = Color(0xFFE3F2FD)
private val SyncErrorColor = Color(0xFFB71C1C)         // red
private val SyncErrorBg = Color(0xFFFFEBEE)
private val SyncPendingColor = Color(0xFFE65100)       // orange for pending

// Dark theme variants
private val SyncOfflineColorDark = Color(0xFFFFB74D)
private val SyncOfflineBgDark = Color(0xFF3E2723)
private val SyncOnlineColorDark = Color(0xFF81C784)
private val SyncSyncingColorDark = Color(0xFF90CAF9)
private val SyncSyncingBgDark = Color(0xFF0D253F)
private val SyncErrorColorDark = Color(0xFFEF9A9A)
private val SyncErrorBgDark = Color(0xFF3E0000)
private val SyncPendingColorDark = Color(0xFFFFB74D)

@Composable
fun SyncStatusIndicator(
    state: SyncStatusState,
    modifier: Modifier = Modifier
) {
    val isExpanded = state.displayMode != SyncDisplayMode.SYNCED
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        val (bgColor, dotColor, text) = resolveDisplay(state, isDark)

        val animatedBg by animateColorAsState(
            targetValue = bgColor,
            animationSpec = tween(300),
            label = "syncBg"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(animatedBg)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PulsatingDot(
                color = dotColor,
                pulsate = state.displayMode == SyncDisplayMode.SYNCING
                        || state.displayMode == SyncDisplayMode.ONLINE_PENDING
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = dotColor
            )
        }
    }

    // Коли SYNCED — показуємо тонку зелену лінію (2dp)
    if (!isExpanded) {
        val greenColor = if (isDark) SyncOnlineColorDark else SyncOnlineColor
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(greenColor.copy(alpha = 0.4f))
        )
    }
}

@Composable
private fun PulsatingDot(
    color: Color,
    pulsate: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha = if (pulsate) {
        val infiniteTransition = rememberInfiniteTransition(label = "syncPulse")
        val animatedAlpha by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "syncDotAlpha"
        )
        animatedAlpha
    } else {
        1f
    }

    Box(
        modifier = modifier
            .size(8.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(color)
    )
}

private data class DisplayConfig(
    val bgColor: Color,
    val dotColor: Color,
    val text: String
)

private fun resolveDisplay(state: SyncStatusState, isDark: Boolean): DisplayConfig {
    return when (state.displayMode) {
        SyncDisplayMode.OFFLINE_PENDING -> DisplayConfig(
            bgColor = if (isDark) SyncOfflineBgDark else SyncOfflineBg,
            dotColor = if (isDark) SyncOfflineColorDark else SyncOfflineColor,
            text = "Офлайн — ${state.pendingCount} ${pluralChanges(state.pendingCount)} очікують синхронізації"
        )
        SyncDisplayMode.OFFLINE -> DisplayConfig(
            bgColor = if (isDark) SyncOfflineBgDark else SyncOfflineBg,
            dotColor = if (isDark) SyncOfflineColorDark else SyncOfflineColor,
            text = "Офлайн"
        )
        SyncDisplayMode.SYNCING -> DisplayConfig(
            bgColor = if (isDark) SyncSyncingBgDark else SyncSyncingBg,
            dotColor = if (isDark) SyncSyncingColorDark else SyncSyncingColor,
            text = "Синхронізація..."
        )
        SyncDisplayMode.ONLINE_PENDING -> DisplayConfig(
            bgColor = if (isDark) SyncOfflineBgDark else SyncOfflineBg,
            dotColor = if (isDark) SyncPendingColorDark else SyncPendingColor,
            text = "Онлайн — ${state.pendingCount} ${pluralChanges(state.pendingCount)} в черзі"
        )
        SyncDisplayMode.SYNCED -> DisplayConfig(
            bgColor = Color.Transparent,
            dotColor = if (isDark) SyncOnlineColorDark else SyncOnlineColor,
            text = ""
        )
        SyncDisplayMode.ERROR -> DisplayConfig(
            bgColor = if (isDark) SyncErrorBgDark else SyncErrorBg,
            dotColor = if (isDark) SyncErrorColorDark else SyncErrorColor,
            text = "Помилка синхронізації"
        )
    }
}

/** Відмінювання слова "зміна" для українського множинного */
private fun pluralChanges(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..19 -> "змін"
        mod10 == 1 -> "зміна"
        mod10 in 2..4 -> "зміни"
        else -> "змін"
    }
}

private fun Color.luminance(): Float {
    return 0.299f * red + 0.587f * green + 0.114f * blue
}
