package com.inventory.di

import android.content.Context
import androidx.room.Room
import com.inventory.data.db.InventoryDatabase
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemBarcodeDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.InventoryOperationDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.db.dao.OutboxEntryDao
import com.inventory.data.db.dao.StockAdjustmentDocumentDao
import com.inventory.data.repository.InventoryRepository
import com.inventory.data.repository.InventoryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InventoryDatabase {
        return Room.databaseBuilder(
            context,
            InventoryDatabase::class.java,
            InventoryDatabase.DATABASE_NAME
        )
            .addMigrations(
                InventoryDatabase.MIGRATION_1_2,
                InventoryDatabase.MIGRATION_2_3,
                InventoryDatabase.MIGRATION_3_4
            )
            .build()
    }

    @Provides
    fun provideCategoryDao(db: InventoryDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideLocationDao(db: InventoryDatabase): LocationDao = db.locationDao()

    @Provides
    fun provideInventoryItemDao(db: InventoryDatabase): InventoryItemDao = db.inventoryItemDao()

    @Provides
    fun provideInventoryItemBarcodeDao(db: InventoryDatabase): InventoryItemBarcodeDao = db.inventoryItemBarcodeDao()

    @Provides
    fun provideInventoryOperationDao(db: InventoryDatabase): InventoryOperationDao = db.inventoryOperationDao()

    @Provides
    fun provideOutboxEntryDao(db: InventoryDatabase): OutboxEntryDao = db.outboxEntryDao()

    @Provides
    fun provideStockAdjustmentDocumentDao(db: InventoryDatabase): StockAdjustmentDocumentDao =
        db.stockAdjustmentDocumentDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository
}
