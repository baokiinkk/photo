package com.basesource.base.network.mock

import android.content.Context
import com.basesource.base.network.mock.IOUtils.getJsonStringFromFile
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.koin.core.annotation.Single
import java.net.HttpURLConnection.HTTP_OK

@Single(binds = [MockInterceptor::class])
class MockInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val uri = chain.request().url.toUri().toString()
        println("DEBUG: MockInterceptor - Request URI: $uri")
        val responseString = when {
            uri.contains(MOCK_COLLAGE_TEMPLATES_API) -> {
                println("DEBUG: Matched MOCK_COLLAGE_TEMPLATES_API")
                getJsonStringFromFile(context.assets, COLLAGE_TEMPLATES_FILE)
            }

            else -> {
                println("DEBUG: No match found for URI: $uri")
                ""
            }
        }

        return if (responseString.isNotEmpty()) {
            Response.Builder()
                .code(HTTP_OK)
                .protocol(Protocol.HTTP_2)
                .message("OK")
                .request(chain.request())
                .body(
                    responseString.toByteArray()
                        .toResponseBody("application/json".toMediaTypeOrNull())
                )
                .addHeader("content-type", "application/json")
                .build()
        } else {
            chain.proceed(chain.request())
        }
    }

    companion object {
        const val MOCK_COLLAGE_TEMPLATES_API = "mock/collage_templates"
        const val COLLAGE_TEMPLATES_FILE = "collage_templates_mock.json"
    }

}
