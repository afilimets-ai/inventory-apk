package com.inventory.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemBarcodeDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.InventoryOperationDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.db.dao.OutboxEntryDao
import com.inventory.data.db.dao.StockAdjustmentDocumentDao
import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItemBarcode
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.InventoryOperation
import com.inventory.data.entity.Location
import com.inventory.data.entity.OutboxEntry
import com.inventory.data.entity.StockAdjustmentDocument
import com.inventory.data.entity.StockAdjustmentLine

@Database(
    entities = [
        Category::class,
        Location::class,
        InventoryItem::class,
        InventoryItemBarcode::class,
        InventoryOperation::class,
        OutboxEntry::class,
        StockAdjustmentDocument::class,
        StockAdjustmentLine::class
    ],
    version = 4,
    exportSchema = true
)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun locationDao(): LocationDao
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun inventoryItemBarcodeDao(): InventoryItemBarcodeDao
    abstract fun inventoryOperationDao(): InventoryOperationDao
    abstract fun outboxEntryDao(): OutboxEntryDao
    abstract fun stockAdjustmentDocumentDao(): StockAdjustmentDocumentDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `inventory_items` ADD COLUMN `sku` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `inventory_items` ADD COLUMN `group_name` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `inventory_items` ADD COLUMN `is_weighted` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `inventory_items` ADD COLUMN `is_package` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `inventory_items` ADD COLUMN `package_unit` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `inventory_items` ADD COLUMN `package_coefficient` REAL NOT NULL DEFAULT 1.0")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_items_sku` ON `inventory_items` (`sku`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_items_group_name` ON `inventory_items` (`group_name`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `inventory_item_barcodes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `item_id` INTEGER NOT NULL,
                        `barcode` TEXT NOT NULL,
                        `unit` TEXT NOT NULL DEFAULT '',
                        `coefficient` REAL NOT NULL DEFAULT 1.0,
                        `is_primary` INTEGER NOT NULL DEFAULT 0,
                        `created_at` INTEGER NOT NULL,
                        FOREIGN KEY(`item_id`) REFERENCES `inventory_items`(`id`) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_inventory_item_barcodes_barcode` ON `inventory_item_barcodes` (`barcode`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_inventory_item_barcodes_item_id` ON `inventory_item_barcodes` (`item_id`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stock_adjustment_documents` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `document_type` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `source_operation_type` TEXT NOT NULL,
                        `source_note` TEXT NOT NULL DEFAULT '',
                        `created_at` INTEGER NOT NULL,
                        `closed_at` INTEGER,
                        `sent_at` INTEGER,
                        `outbox_entry_id` INTEGER
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_documents_document_type` ON `stock_adjustment_documents` (`document_type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_documents_status` ON `stock_adjustment_documents` (`status`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_documents_outbox_entry_id` ON `stock_adjustment_documents` (`outbox_entry_id`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stock_adjustment_lines` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `document_id` INTEGER NOT NULL,
                        `item_id` INTEGER,
                        `barcode` TEXT NOT NULL,
                        `sku` TEXT NOT NULL DEFAULT '',
                        `name` TEXT NOT NULL,
                        `quantity` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `reason` TEXT NOT NULL DEFAULT '',
                        `created_at` INTEGER NOT NULL,
                        FOREIGN KEY(`document_id`) REFERENCES `stock_adjustment_documents`(`id`) ON DELETE CASCADE,
                        FOREIGN KEY(`item_id`) REFERENCES `inventory_items`(`id`) ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_lines_document_id` ON `stock_adjustment_lines` (`document_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_lines_item_id` ON `stock_adjustment_lines` (`item_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_adjustment_lines_barcode` ON `stock_adjustment_lines` (`barcode`)")
            }
        }
    }
}
