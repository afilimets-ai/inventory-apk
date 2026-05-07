package com.inventory.di

import com.inventory.barcode.BarcodeLookupProvider
import com.inventory.barcode.OpenFoodFactsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class BarcodeLookupModule {

    @Binds
    @IntoSet
    abstract fun bindOpenFoodFactsProvider(provider: OpenFoodFactsProvider): BarcodeLookupProvider
}
