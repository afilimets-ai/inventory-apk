package com.inventory.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.inventory.app.data.local.outbox.OutboxDao
import com.inventory.app.data.local.outbox.OutboxEntity

/**
 * Main Room database for the application.
 * Implements offline-first architecture with outbox pattern for sync operations.
 */
@Database(
    entities = [OutboxEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    /**
     * Access to outbox operations for pending sync management.
     */
    abstract fun outboxDao(): OutboxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get singleton database instance.
         * Uses double-check locking pattern for thread-safe initialization.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
