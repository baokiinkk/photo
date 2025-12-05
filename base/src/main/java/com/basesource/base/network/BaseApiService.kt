package com.basesource.base.network

import android.content.Context
import android.util.Log
import com.android.amg.AMGUtil
import com.basesource.base.result.Result
import com.basesource.base.utils.fromJson
import okhttp3.ResponseBody
import retrofit2.Response


/**
 * Extension function to handle API responses safely.
 */
suspend inline fun <reified T> safeApiCall(
    context: Context,
    apiCallMock: suspend () -> Response<ResponseBody>,
    apiCall: suspend () -> Response<ResponseBody>
): Result<T> {
    return try {
        val response = apiCall()
        callData(context, response)
    } catch (e: Exception) {
        val response = apiCallMock()
        callData(context, response)
    }
}

inline fun <reified T> callData(context: Context, response: Response<ResponseBody>): Result<T> {
    return if (response.isSuccessful) {
        val responseBody = response.body()
        if (responseBody != null) {
            val stringData = responseBody.string()
            Log.d("network api root:", stringData)
            val jsonString = if (isValidJson(stringData)) {
                stringData
            } else {
                decryptResponse(context, stringData)
            }
            Log.d("network api:", jsonString)

            Result.Success(jsonString.fromJson<T>())
        } else {
            Log.d("network api:", "Error")
            Result.Error(Exception("Response body is null"))
        }
    } else {
        com.basesource.base.result.Result.Error(
            NetworkException.ServerError(
                response.code(),
                response.message()
            )
        )
    }
}

/**
 * Kiểm tra xem string có phải là JSON hợp lệ không
 */
fun isValidJson(jsonString: String): Boolean {
    return try {
        val trimmed = jsonString.trim()
        // Kiểm tra xem có bắt đầu và kết thúc bằng {} hoặc [] không
        (trimmed.startsWith("{") && trimmed.endsWith("}")) || 
        (trimmed.startsWith("[") && trimmed.endsWith("]"))
    } catch (e: Exception) {
        false
    }
}

fun decryptResponse(context: Context, encryptedBody: String): String {
    return try {
        // Strategy 1: Clean and decode
        val cleanedBody = encryptedBody
            .replace("\"", "")
            .replace("\\", "")
            .replace("\n", "")
            .replace("\r", "")
            .trim()

        val decryptedResult = AMGUtil.decrypt(context, cleanedBody)

        decryptedResult.ifEmpty {
            // Strategy 2: Base64 validation and re-encoding
            val decoded = android.util.Base64.decode(cleanedBody, android.util.Base64.DEFAULT)
            val reencoded =
                android.util.Base64.encodeToString(decoded, android.util.Base64.DEFAULT)
            AMGUtil.decrypt(context, reencoded.replace("\n", ""))
        }
    } catch (e: Throwable) {
        ""
    }
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> this
}
