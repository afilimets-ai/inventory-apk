package com.inventory.network

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID
import javax.inject.Inject

// Додає Idempotency-Key header до POST та PUT запитів
// для захисту від дублювання при повторних спробах
class IdempotencyInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val enriched = if (request.method == "POST" || request.method == "PUT") {
            request.newBuilder()
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .build()
        } else {
            request
        }

        return chain.proceed(enriched)
    }
}
