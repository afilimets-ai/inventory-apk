package com.inventory.app

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.inventory.scanner.ScannerManager
import com.inventory.sync.SyncProviderType
import com.inventory.ui.audit.AuditScreen
import com.inventory.ui.components.SyncStatusIndicator
import com.inventory.ui.receiving.ReceivingScreen
import com.inventory.ui.scan.ScanScreen
import com.inventory.ui.settings.ProviderSettingsScreen
import com.inventory.ui.settings.SyncSettingsScreen
import com.inventory.ui.sync.SyncStatusViewModel
import com.inventory.ui.theme.IndustrialTheme
import com.inventory.ui.theme.ThemePreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var scannerManager: ScannerManager
    @Inject lateinit var themePreferenceManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by themePreferenceManager.themeMode.collectAsState()
            IndustrialTheme(themeMode = themeMode) {
                Surface {
                    val navController = rememberNavController()
                    val syncStatusViewModel: SyncStatusViewModel = hiltViewModel()
                    val syncStatus by syncStatusViewModel.syncStatus.collectAsState()
                    Column {
                        SyncStatusIndicator(state = syncStatus)
                        NavHost(
                            navController = navController,
                            startDestination = "scan",
                            modifier = Modifier.weight(1f)
                        ) {
                            composable("scan") {
                                ScanScreen(
                                    themeMode = themeMode,
                                    onThemeToggle = { themePreferenceManager.cycleTheme() },
                                    onSyncSettingsClick = { navController.navigate("sync_settings") },
                                    onReceivingClick = { navController.navigate("receiving") },
                                    onAuditClick = { navController.navigate("audit") }
                                )
                            }
                            composable("receiving") {
                                ReceivingScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("audit") {
                                AuditScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("sync_settings") {
                                SyncSettingsScreen(
                                    onBack = { navController.popBackStack() },
                                    onProviderSettingsClick = { type ->
                                        navController.navigate("provider_settings/${type.name}")
                                    }
                                )
                            }
                            composable(
                                "provider_settings/{providerType}",
                                arguments = listOf(navArgument("providerType") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val typeName = backStackEntry.arguments?.getString("providerType") ?: return@composable
                                val providerType = SyncProviderType.valueOf(typeName)
                                ProviderSettingsScreen(
                                    providerType = providerType,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        scannerManager.register()
    }

    override fun onPause() {
        super.onPause()
        scannerManager.unregister()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (scannerManager.onKeyDown(keyCode, event)) return true
        return super.onKeyDown(keyCode, event)
    }
}
