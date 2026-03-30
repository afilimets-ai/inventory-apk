package com.inventory.app

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.inventory.scanner.NewlandScannerManager
import com.inventory.ui.scan.ScanScreen
import com.inventory.ui.theme.IndustrialTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var scannerManager: NewlandScannerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IndustrialTheme {
                Surface {
                    ScanScreen()
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
