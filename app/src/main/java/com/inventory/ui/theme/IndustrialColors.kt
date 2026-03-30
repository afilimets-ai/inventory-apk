package com.inventory.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Семантичні кольори стану сканування (однакові для обох тем)
object ScanColors {
    val Success = Color(0xFF1B5E20)       // темно-зелений — відомий товар
    val SuccessContainer = Color(0xFFE8F5E9)
    val OnSuccess = Color(0xFFFFFFFF)
    val Error = Color(0xFFB71C1C)         // темно-червоний — помилка / невідомий
    val ErrorContainer = Color(0xFFFFEBEE)
    val OnError = Color(0xFFFFFFFF)
    val Warning = Color(0xFFE65100)       // темно-помаранчевий — увага
    val WarningContainer = Color(0xFFFFF3E0)
    val OnWarning = Color(0xFFFFFFFF)
    val Idle = Color(0xFF1565C0)          // синій — очікування сканування
}

// Світла тема — для яскравих умов складу (завантажувальні рампи, вікна)
val IndustrialLightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),          // темно-синій (7.5:1 на білому)
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001A4D),

    secondary = Color(0xFF37474F),        // темно-сірий
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFECEFF1),
    onSecondaryContainer = Color(0xFF102027),

    error = Color(0xFFB71C1C),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFF5F0000),

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF121212),     // контраст 16:1

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFECEFF1),
    onSurfaceVariant = Color(0xFF37474F),

    outline = Color(0xFF90A4AE)
)

// Темна тема — для темних/напівтемних умов складу (холодильні камери, нічні зміни)
val IndustrialDarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),          // світло-синій на темному (7:1)
    onPrimary = Color(0xFF003066),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFD6E4FF),

    secondary = Color(0xFFB0BEC5),
    onSecondary = Color(0xFF1C2A30),
    secondaryContainer = Color(0xFF37474F),
    onSecondaryContainer = Color(0xFFECEFF1),

    error = Color(0xFFEF9A9A),
    onError = Color(0xFF5F0000),
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color(0xFFFFEBEE),

    background = Color(0xFF121212),
    onBackground = Color(0xFFEEEEEE),     // контраст 14:1

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFEEEEEE),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0BEC5),

    outline = Color(0xFF546E7A)
)
