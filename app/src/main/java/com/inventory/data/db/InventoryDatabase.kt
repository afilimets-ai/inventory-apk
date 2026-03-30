package com.inventory.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inventory.data.db.dao.CategoryDao
import com.inventory.data.db.dao.InventoryItemDao
import com.inventory.data.db.dao.LocationDao
import com.inventory.data.entity.Category
import com.inventory.data.entity.InventoryItem
import com.inventory.data.entity.Location

@Database(
    entities = [
        Category::class,
        Location::class,
        InventoryItem::class
    ],
    version = 1,
    exportSchema = true
)
abstract class InventoryDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun locationDao(): LocationDao
    abstract fun inventoryItemDao(): InventoryItemDao

    companion object {
        const val DATABASE_NAME = "inventory.db"
    }
}
