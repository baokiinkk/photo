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
            uri.endsWith(MOCK_TRENDING_API) -> {
                println("DEBUG: Matched MOCK_TRENDING_API")
                getJsonStringFromFile(context.assets, MOCK_FILE_TRENDING_JSON)
            }

            uri.contains(MOCK_SEARCH_API) -> {
                println("DEBUG: Matched MOCK_SEARCH_API")
                getJsonStringFromFile(context.assets, MOCK_FILE_SEARCH_JSON)
            }

            uri.contains(MOCK_MOST_SEARCHED_API) -> {
                println("DEBUG: Matched MOCK_MOST_SEARCHED_API")
                getJsonStringFromFile(context.assets, MOCK_FILE_TRENDING_JSON)
            }

            uri.contains(MOCK_RECOMMEND_API) -> {
                println("DEBUG: Matched MOCK_RECOMMEND_API")
                getJsonStringFromFile(context.assets, MOCK_FILE_TRENDING_JSON)
            }

            uri.contains(MOCK_STATUS_BAR_CUSTOMIZE_API) -> {
                println("DEBUG: Matched MOCK_STATUS_BAR_CUSTOMIZE_API")
                getJsonStringFromFile(context.assets, MOCK_STATUS_BAR_CUSTOMIZE_API)
            }

            uri.contains(MOCK_EMOJIS_API) -> {
                println("DEBUG: Matched MOCK_EMOJIS_API")
                getJsonStringFromFile(context.assets, MOCK_EMOJIS_API)
            }

            uri.contains(MOCK_ANIMATIONS_API) -> {
                println("DEBUG: Matched MOCK_ANIMATIONS_API")
                getJsonStringFromFile(context.assets, MOCK_ANIMATIONS_API)
            }

            uri.contains(MOCK_EMOTIONS_API) -> {
                println("DEBUG: Matched MOCK_EMOTIONS_API")
                getJsonStringFromFile(context.assets, MOCK_EMOTIONS_API)
            }

            uri.contains(MOCK_CHARGE_API) -> {
                println("DEBUG: Matched MOCK_CHARGE_API")
                getJsonStringFromFile(context.assets, MOCK_CHARGE_API)
            }
            uri.contains(STICKERS_MOCK_API) -> {
                println("DEBUG: Matched STICKERS_MOCK_API")
                getJsonStringFromFile(context.assets, STICKERS_MOCK_API)
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
        const val MOCK_TRENDING_API = "mock/trending"
        const val MOCK_FILE_TRENDING_JSON = "trending_mock.json"
        const val MOCK_SEARCH_API = "mock/search"
        const val MOCK_FILE_SEARCH_JSON = "search_mock.json"
        const val MOCK_MOST_SEARCHED_API = "mock/most_searched"
        const val MOCK_RECOMMEND_API = "mock/recommend"
        const val MOCK_STATUS_BAR_CUSTOMIZE_API = "status_bar_customize_mock.json"

        const val MOCK_EMOJIS_API = "emojis_mock.json"

        const val MOCK_ANIMATIONS_API = "animations_mock.json"

        const val MOCK_EMOTIONS_API = "emotions_mock.json"

        const val MOCK_CHARGE_API = "charges_mock.json"

        const val STICKERS_MOCK_API = "stickers_mock.json"
    }

}
