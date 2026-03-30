package com.inventory.app

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.inventory.scanner.NewlandScannerManager
import com.inventory.ui.scan.ScanScreen
import com.inventory.ui.theme.IndustrialTheme
import com.inventory.ui.theme.ThemePreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var scannerManager: NewlandScannerManager
    @Inject lateinit var themePreferenceManager: ThemePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by themePreferenceManager.themeMode.collectAsState()
            IndustrialTheme(themeMode = themeMode) {
                Surface {
                    ScanScreen(
                        themeMode = themeMode,
                        onThemeToggle = { themePreferenceManager.cycleTheme() }
                    )
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
