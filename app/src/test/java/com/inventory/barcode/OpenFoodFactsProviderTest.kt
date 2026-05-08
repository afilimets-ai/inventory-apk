package com.inventory.barcode

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenFoodFactsProviderTest {

    @Test
    fun `lookup maps successful response to product`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain ->
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(
                            """
                            {
                              "status": 1,
                              "product": {
                                "product_name": "Chocolate",
                                "brands": "Brand",
                                "quantity": "90 g"
                              }
                            }
                            """.trimIndent().toResponseBody()
                        )
                        .build()
                }
            )
            .build()
        val provider = OpenFoodFactsProvider(client)

        val result = runBlocking { provider.lookup("4820000000000") }

        assertTrue(result is BarcodeLookupResult.Found)
        val product = (result as BarcodeLookupResult.Found).product
        assertEquals("Chocolate", product.name)
        assertEquals("Brand, 90 g", product.description)
    }

    @Test
    fun `lookup does not forward authorization header to external API`() {
        var authorization: String? = null
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                authorization = chain.request().header("Authorization")
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("""{"status":0}""".toResponseBody())
                    .build()
            }
            .build()
        val provider = OpenFoodFactsProvider(client)

        runBlocking { provider.lookup("4820000000000") }

        assertEquals(null, authorization)
    }

    @Test
    fun `lookup returns not found on missing product`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain ->
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("""{"status":0}""".toResponseBody())
                        .build()
                }
            )
            .build()
        val provider = OpenFoodFactsProvider(client)

        assertEquals(BarcodeLookupResult.NotFound, runBlocking { provider.lookup("missing") })
    }
}
