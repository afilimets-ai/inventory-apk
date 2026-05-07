package com.inventory

import android.app.Application
import androidx.work.Configuration
import com.inventory.scanner.ScannerManager
import com.inventory.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class InventoryApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var wmConfiguration: Configuration

    @Inject
    lateinit var scannerManager: ScannerManager

    override val workManagerConfiguration: Configuration
        get() = wmConfiguration

    override fun onCreate() {
        super.onCreate()
        SyncWorker.schedule(this)
    }

    override fun onTerminate() {
        scannerManager.destroy()
        super.onTerminate()
    }
}
