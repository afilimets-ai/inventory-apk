package com.inventory.app

import android.app.Application

/**
 * Custom Application class for the Inventory app.
 * This class is instantiated before any other class when the process is created.
 * Use this for app-wide initialization and configuration.
 */
class InventoryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize app-wide components here
        // Examples: dependency injection, crash reporting, analytics, etc.
    }
}
