package com.inventory.di

import android.content.Context
import com.inventory.scanner.NewlandScannerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt dependency injection module for scanner-related dependencies.
 *
 * This module provides singleton instances of scanner components to be injected
 * throughout the application. All providers are scoped to SingletonComponent,
 * meaning they live for the entire application lifetime.
 */
@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    /**
     * Provides the singleton instance of NewlandScannerManager.
     *
     * The scanner manager handles lifecycle management of the MT90 barcode scanner,
     * including BroadcastReceiver registration, scan event processing, and reactive
     * event emission via SharedFlow.
     *
     * @param context Application context for BroadcastReceiver registration
     * @return Singleton NewlandScannerManager instance
     */
    @Provides
    @Singleton
    fun provideNewlandScannerManager(
        @ApplicationContext context: Context
    ): NewlandScannerManager {
        return NewlandScannerManager(context)
    }
}
