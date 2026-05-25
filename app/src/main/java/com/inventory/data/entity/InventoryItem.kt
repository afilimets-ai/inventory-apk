package com.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_items",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["barcode"]),
        Index(value = ["sku"]),
        Index(value = ["group_name"]),
        Index(value = ["category_id"]),
        Index(value = ["location_id"])
    ]
)
data class InventoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "barcode")
    val barcode: String = "",

    @ColumnInfo(name = "sku")
    val sku: String = "",

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "group_name")
    val groupName: String = "",

    @ColumnInfo(name = "category_id")
    val categoryId: Long? = null,

    @ColumnInfo(name = "location_id")
    val locationId: Long? = null,

    @ColumnInfo(name = "quantity")
    val quantity: Double = 0.0,

    @ColumnInfo(name = "unit")
    val unit: String = "шт",

    @ColumnInfo(name = "is_weighted")
    val isWeighted: Boolean = false,

    @ColumnInfo(name = "is_package")
    val isPackage: Boolean = false,

    @ColumnInfo(name = "package_unit")
    val packageUnit: String = "",

    @ColumnInfo(name = "package_coefficient")
    val packageCoefficient: Double = 1.0,

    @ColumnInfo(name = "min_quantity")
    val minQuantity: Double = 0.0,

    @ColumnInfo(name = "notes")
    val notes: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
