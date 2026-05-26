package com.inventory.ui.receiving

import com.inventory.data.entity.InventoryItem

/** Один запис прийому у поточній сесії */
data class ReceivedLine(
    val item: InventoryItem,
    val quantity: Double,
    val timestamp: Long = System.currentTimeMillis()
)

/** Стан екрану прийому */
sealed class ReceivingUiState {
    /** Готовий до сканування наступного товару */
    data class Scanning(
        val sessionLines: List<ReceivedLine> = emptyList(),
        val defaultLocationId: Long? = null
    ) : ReceivingUiState()

    /** Товар знайдено — підтвердження кількості */
    data class ItemFound(
        val item: InventoryItem,
        val quantity: Double = 1.0,
        val sessionLines: List<ReceivedLine>,
        val defaultLocationId: Long? = null,
        val scannedBarcode: String = item.barcode
    ) : ReceivingUiState()

    /** Невідомий штрихкод */
    data class UnknownBarcode(
        val barcode: String,
        val sessionLines: List<ReceivedLine>
    ) : ReceivingUiState()

    data class LookingUpBarcode(
        val barcode: String,
        val sessionLines: List<ReceivedLine>
    ) : ReceivingUiState()

    data class LookupCandidate(
        val barcode: String,
        val item: InventoryItem,
        val source: String,
        val sessionLines: List<ReceivedLine>,
        val defaultLocationId: Long? = null
    ) : ReceivingUiState()

    data class LookupNotFound(
        val barcode: String,
        val message: String,
        val sessionLines: List<ReceivedLine>
    ) : ReceivingUiState()

    /** Сесію завершено — підсумок */
    data class SessionSummary(
        val lines: List<ReceivedLine>,
        val totalItems: Int,
        val totalQuantity: Double
    ) : ReceivingUiState()
}
