package com.inventory.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

// Retrofit інтерфейс для взаємодії з сервером.
// Буде розширюватись в spec 025 (sync engine).
interface InventoryApiService {

    // Відправка операції з outbox на сервер
    @POST("operations")
    suspend fun syncOperation(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body payload: SyncOperationRequest
    ): SyncOperationResponse

    // Delta sync каталогу товарів (тільки зміни з останнього sync_token)
    @GET("catalog/sync")
    suspend fun syncCatalog(
        @Query("sync_token") syncToken: String?
    ): CatalogSyncResponse
}

data class SyncOperationRequest(
    val idempotencyKey: String,
    val operationType: String,
    val barcode: String,
    val itemId: Long?,
    val quantity: Double,
    val timestamp: Long
)

data class SyncOperationResponse(
    val id: String,
    val status: String,
    val syncToken: String
)

data class CatalogSyncResponse(
    val items: List<CatalogItem>,
    val nextSyncToken: String
)

data class CatalogItem(
    val id: Long,
    val barcode: String,
    val name: String,
    val description: String,
    val unit: String,
    val categoryId: Long?,
    val locationId: Long?
)
