package com.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StockAdjustmentType { RECEIPT, WRITE_OFF }
enum class StockAdjustmentStatus { CREATED, SENT, FAILED }

@Entity(
    tableName = "stock_adjustment_documents",
    indices = [
        Index(value = ["document_type"]),
        Index(value = ["status"]),
        Index(value = ["outbox_entry_id"])
    ]
)
data class StockAdjustmentDocument(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "document_type")
    val documentType: String,

    @ColumnInfo(name = "status")
    val status: String = StockAdjustmentStatus.CREATED.name,

    @ColumnInfo(name = "source_operation_type")
    val sourceOperationType: String = OperationType.AUDIT.name,

    @ColumnInfo(name = "source_note")
    val sourceNote: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "closed_at")
    val closedAt: Long? = null,

    @ColumnInfo(name = "sent_at")
    val sentAt: Long? = null,

    @ColumnInfo(name = "outbox_entry_id")
    val outboxEntryId: Long? = null
)
