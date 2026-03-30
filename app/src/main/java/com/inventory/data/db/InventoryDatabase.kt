package com.inventory.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.InventoryOperationDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.db.dao.OutboxEntryDao
import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.Location
import com.inventory.data.entity.OutboxEntry

@Database(
    entities = [
        Category::class,
        Location::class,
        InventoryItem::class,
        InventoryOperation::class,
        OutboxEntry::class
    ],
    version = 3,
    exportSchema = true
)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun locationDao(): LocationDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun inventoryOperationDao(): InventoryOperationDao
    abstract fun outboxEntryDao(): OutboxEntryDao

    companion object {
        const val DATABASE_NAME = "inventory.db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `outbox_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `idempotency_key` TEXT NOT NULL,
                        `operation_type` TEXT NOT NULL,
                        `payload` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `retry_count` INTEGER NOT NULL DEFAULT 0,
                        `error_message` TEXT NOT NULL DEFAULT '',
                        `created_at` INTEGER NOT NULL,
                        `last_attempt_at` INTEGER
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_outbox_entries_idempotency_key` ON `outbox_entries` (`idempotency_key`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_outbox_entries_status` ON `outbox_entries` (`status`)")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `inventory_operations` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `item_id` INTEGER,
                        `barcode` TEXT NOT NULL,
                        `operation_type` TEXT NOT NULL,
                        `quantity` REAL NOT NULL,
                        `notes` TEXT NOT NULL,
                        `sync_status` TEXT NOT NULL,
                        `created_at` INTEGER NOT NULL,
                        FOREIGN KEY(`item_id`) REFERENCES `inventory_items`(`id`) ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_operations_item_id` ON `inventory_operations` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_operations_barcode` ON `inventory_operations` (`barcode`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_operations_sync_status` ON `inventory_operations` (`sync_status`)")
            }
        }
    }
}
