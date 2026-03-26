package com.inventory

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for the Inventory app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 *
 * This triggers Hilt's code generation including a base class for the application
 * that serves as the application-level dependency container.
 */
@HiltAndroidApp
class InventoryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application-level initialization will go here
        // Scanner manager lifecycle will be handled in activities/fragments
    }
}
