package com.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_adjustment_lines",
    foreignKeys = [
        ForeignKey(
            entity = StockAdjustmentDocument::class,
            parentColumns = ["id"],
            childColumns = ["document_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = InventoryItem::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["document_id"]),
        Index(value = ["item_id"]),
        Index(value = ["barcode"])
    ]
)
data class StockAdjustmentLine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "document_id")
    val documentId: Long,

    @ColumnInfo(name = "item_id")
    val itemId: Long? = null,

    @ColumnInfo(name = "barcode")
    val barcode: String,

    @ColumnInfo(name = "sku")
    val sku: String = "",

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "unit")
    val unit: String,

    @ColumnInfo(name = "reason")
    val reason: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
