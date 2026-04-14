package com.inventory.sync.provider

import com.inventory.sync.SyncFormat
import com.inventory.sync.SyncImportResult
import com.inventory.sync.SyncProviderType
import com.inventory.sync.SyncResult
import com.inventory.sync.SyncSettings
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpApiProviderTest {

    @Test
    fun `export uses auth header and returns failure on error response`() {
        var authHeader: String? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain ->
                    authHeader = chain.request().header("Authorization")
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(500)
                        .message("Server error")
                        .body("nope".toResponseBody())
                        .build()
                }
            )
            .build()
        val provider = HttpApiProvider(
            settings = SyncSettings(
                providerType = SyncProviderType.HTTP_API,
                apiUrl = "https://example.com/api",
                apiToken = "secret"
            ),
            client = client
        )

        val result = kotlinx.coroutines.runBlocking {
            provider.export("data".toByteArray(), SyncFormat.JSON, "inventory")
        }

        assertEquals("Bearer secret", authHeader)
        assertTrue(result is SyncResult.Failure)
    }

    @Test
    fun `import returns response bytes on success`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                Interceptor { chain ->
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("payload".toResponseBody())
                        .build()
                }
            )
            .build()
        val provider = HttpApiProvider(
            settings = SyncSettings(
                providerType = SyncProviderType.HTTP_API,
                apiUrl = "https://example.com/api"
            ),
            client = client
        )

        val result = kotlinx.coroutines.runBlocking {
            provider.import(SyncFormat.JSON, "inventory")
        }

        assertEquals(SyncImportResult.Success("payload".toByteArray()), result)
    }
}
