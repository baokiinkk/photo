package com.basesource.base.network

import okhttp3.Interceptor
import okhttp3.Response
import org.koin.core.annotation.Single

/**
 * Interceptor for adding authentication headers to requests.
 */

@Single(binds = [AuthInterceptor::class])
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenProvider()
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}

