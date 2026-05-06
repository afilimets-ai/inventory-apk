package com.inventory.di

import com.inventory.scanner.ScannerManager
import com.inventory.scanner.ScannerManagerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    /**
     * Provides a single ScannerManager for the entire application.
     * Concrete implementation determined by ScannerManagerFactory based on Build.MANUFACTURER.
     */
    @Provides
    @Singleton
    fun provideScannerManager(factory: ScannerManagerFactory): ScannerManager =
        factory.create()
}
