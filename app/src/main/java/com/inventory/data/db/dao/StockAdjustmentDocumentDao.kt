package com.inventory.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.inventory.data.entity.StockAdjustmentDocument
import com.inventory.data.entity.StockAdjustmentLine

@Dao
interface StockAdjustmentDocumentDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDocument(document: StockAdjustmentDocument): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLines(lines: List<StockAdjustmentLine>): List<Long>

    @Query("UPDATE stock_adjustment_documents SET outbox_entry_id = :outboxEntryId WHERE id = :documentId")
    suspend fun attachOutboxEntry(documentId: Long, outboxEntryId: Long)

    @Query("SELECT * FROM stock_adjustment_documents WHERE id = :documentId LIMIT 1")
    suspend fun getDocument(documentId: Long): StockAdjustmentDocument?

    @Query("SELECT * FROM stock_adjustment_lines WHERE document_id = :documentId ORDER BY id ASC")
    suspend fun getLines(documentId: Long): List<StockAdjustmentLine>
}
