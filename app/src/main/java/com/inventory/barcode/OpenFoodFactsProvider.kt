package com.inventory.barcode

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OpenFoodFactsProvider @Inject constructor(
    @Named("externalApi") client: OkHttpClient
) : BarcodeLookupProvider {
    override val name: String = "Open Food Facts"

    private val api: OpenFoodFactsApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(
            client.newBuilder()
                .addNetworkInterceptor { chain ->
                    val request: Request = chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT)
                        .build()
                    chain.proceed(request)
                }
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenFoodFactsApi::class.java)

    override suspend fun lookup(barcode: String): BarcodeLookupResult =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getProduct(barcode)
                if (response.status != 1 || response.product == null) {
                    BarcodeLookupResult.NotFound
                } else {
                    val product = response.product.toLookupProduct(barcode)
                    if (product.name.isBlank()) BarcodeLookupResult.NotFound
                    else BarcodeLookupResult.Found(product)
                }
            } catch (e: Exception) {
                BarcodeLookupResult.Failure("Не вдалося отримати дані товару", e)
            }
        }

    private fun OpenFoodFactsProduct.toLookupProduct(barcode: String): BarcodeLookupProduct {
        val productName = productNameUk
            ?: productNameEn
            ?: productName
            ?: genericNameUk
            ?: genericNameEn
            ?: genericName
            ?: ""
        val brand = brands.orEmpty()
        val quantityText = quantity.orEmpty()
        val description = listOf(brand, quantityText)
            .filter { it.isNotBlank() }
            .joinToString(", ")
        return BarcodeLookupProduct(
            barcode = barcode,
            name = productName,
            description = description,
            brand = brand,
            unit = "шт",
            source = name
        )
    }

    private interface OpenFoodFactsApi {
        @GET("api/v2/product/{barcode}")
        suspend fun getProduct(
            @Path("barcode") barcode: String,
            @Query("fields") fields: String = "product_name,product_name_en,product_name_uk,generic_name,generic_name_en,generic_name_uk,brands,quantity"
        ): OpenFoodFactsResponse
    }

    private data class OpenFoodFactsResponse(
        val status: Int,
        val product: OpenFoodFactsProduct?
    )

    private data class OpenFoodFactsProduct(
        @SerializedName("product_name") val productName: String?,
        @SerializedName("product_name_en") val productNameEn: String?,
        @SerializedName("product_name_uk") val productNameUk: String?,
        @SerializedName("generic_name") val genericName: String?,
        @SerializedName("generic_name_en") val genericNameEn: String?,
        @SerializedName("generic_name_uk") val genericNameUk: String?,
        val brands: String?,
        val quantity: String?
    )

    private companion object {
        const val BASE_URL = "https://world.openfoodfacts.org/"
        const val USER_AGENT = "InventoryAPK/1.0 (https://github.com/afilimets-ai/inventory-apk)"
    }
}
