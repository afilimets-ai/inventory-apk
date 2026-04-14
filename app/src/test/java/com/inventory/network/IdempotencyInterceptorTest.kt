package com.inventory.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IdempotencyInterceptorTest {

    private val interceptor = IdempotencyInterceptor()

    @Test
    fun `retries for same request keep stable idempotency key`() {
        val headers = mutableListOf<String?>()
        val client = clientCapturing(headers)
        val request = Request.Builder()
            .url("https://example.com/items")
            .post("""{"id":1}""".toRequestBody())
            .build()

        client.newCall(request).execute().close()
        client.newCall(request).execute().close()

        assertEquals(2, headers.size)
        assertEquals(headers[0], headers[1])
    }

    @Test
    fun `existing idempotency key is preserved`() {
        val headers = mutableListOf<String?>()
        val client = clientCapturing(headers)
        val request = Request.Builder()
            .url("https://example.com/items")
            .put("""{"id":1}""".toRequestBody())
            .header("Idempotency-Key", "fixed-key")
            .build()

        client.newCall(request).execute().close()

        assertEquals("fixed-key", headers.single())
    }

    @Test
    fun `get requests do not receive idempotency key`() {
        val headers = mutableListOf<String?>()
        val client = clientCapturing(headers)
        val request = Request.Builder()
            .url("https://example.com/items")
            .get()
            .build()

        client.newCall(request).execute().close()

        assertNull(headers.single())
    }

    private fun clientCapturing(headers: MutableList<String?>): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(
                Interceptor { chain ->
                    headers += chain.request().header("Idempotency-Key")
                    Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body("ok".toResponseBody())
                        .build()
                }
            )
            .build()
    }
}
