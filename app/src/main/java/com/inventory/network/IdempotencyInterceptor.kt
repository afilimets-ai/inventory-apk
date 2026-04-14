package com.inventory.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.UUID
import javax.inject.Inject
import okio.Buffer

// Додає Idempotency-Key header до POST та PUT запитів
// для захисту від дублювання при повторних спробах
class IdempotencyInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val enriched = if (request.method == "POST" || request.method == "PUT") {
            val existingKey = request.header("Idempotency-Key")
            if (existingKey != null) {
                request
            } else {
                request.newBuilder()
                    .header("Idempotency-Key", generateStableKey(request))
                    .build()
            }
        } else {
            request
        }

        return chain.proceed(enriched)
    }

    private fun generateStableKey(request: Request): String {
        val buffer = Buffer()
        request.body?.writeTo(buffer)
        val bodyHash = buffer.readByteString().md5().hex()
        return UUID.nameUUIDFromBytes("${request.url}:$bodyHash".toByteArray()).toString()
    }
}
