package com.inventory.app.data.local.outbox

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Outbox entity for storing pending sync operations.
 * Part of the offline-first architecture with outbox pattern.
 */
@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Type of operation (e.g., "CREATE_ITEM", "UPDATE_ITEM", "DELETE_ITEM")
     */
    val operationType: String,

    /**
     * JSON payload containing the operation data
     */
    val payload: String,

    /**
     * Timestamp when the operation was created (milliseconds since epoch)
     */
    val createdAt: Long = System.currentTimeMillis(),

    /**
     * Sync status: "PENDING", "SYNCING", "FAILED"
     */
    val syncStatus: String = "PENDING"
)
