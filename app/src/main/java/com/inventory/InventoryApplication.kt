package com.inventory

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class InventoryApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var wmConfiguration: Configuration

    override val workManagerConfiguration: Configuration
        get() = wmConfiguration

    override fun onCreate() {
        super.onCreate()
    }
}
