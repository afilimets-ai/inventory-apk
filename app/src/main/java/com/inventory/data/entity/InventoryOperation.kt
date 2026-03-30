package com.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class OperationType { RECEIVE, AUDIT, TRANSFER, SHIPMENT }
enum class SyncStatus { PENDING, SYNCED, FAILED }

@Entity(
    tableName = "inventory_operations",
    foreignKeys = [
        ForeignKey(
            entity = InventoryItem::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["item_id"]),
        Index(value = ["barcode"]),
        Index(value = ["sync_status"])
    ]
)
data class InventoryOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "item_id")
    val itemId: Long? = null,

    @ColumnInfo(name = "barcode")
    val barcode: String,

    @ColumnInfo(name = "operation_type")
    val operationType: String = OperationType.RECEIVE.name,

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = SyncStatus.PENDING.name,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
