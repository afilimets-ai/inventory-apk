package com.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class OutboxStatus { PENDING, SYNCING, SYNCED, FAILED }

@Entity(
    tableName = "outbox_entries",
    indices = [
        Index(value = ["status"]),
        Index(value = ["idempotency_key"], unique = true)
    ]
)
data class OutboxEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "idempotency_key")
    val idempotencyKey: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "operation_type")
    val operationType: String,

    // JSON-представлення операції для відправки на сервер
    @ColumnInfo(name = "payload")
    val payload: String,

    @ColumnInfo(name = "status")
    val status: String = OutboxStatus.PENDING.name,

    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,

    @ColumnInfo(name = "error_message")
    val errorMessage: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long? = null
)
