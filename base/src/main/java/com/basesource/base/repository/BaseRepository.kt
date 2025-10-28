package com.basesource.base.repository

import android.content.Context
import com.basesource.base.network.NetworkUtils
import com.basesource.base.result.Result
import com.basesource.base.result.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Base repository class that provides common functionality for all repositories.
 */
abstract class BaseRepository(
    protected val context: Context
) {
    
    /**
     * Executes a network call and returns the result as a Flow.
     */
    protected fun <T> executeNetworkCall(
        networkCall: suspend () -> Result<T>
    ): Flow<Result<T>> = flow {
        emit(Result.Loading)
        
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                val result = networkCall()
                emit(result)
            } catch (e: Exception) {
                emit(Result.Error(e))
            }
        } else {
            emit(Result.Error(com.basesource.base.network.NetworkException.NoInternetConnection()))
        }
    }
    
    /**
     * Executes a network call with retry mechanism.
     */
    protected suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        delayMs: Long = 1000,
        networkCall: suspend () -> Result<T>
    ): Result<T> {
        repeat(maxRetries) { attempt ->
            val result = networkCall()
            if (result.isSuccess()) {
                return result
            }
            
            if (attempt < maxRetries - 1) {
                kotlinx.coroutines.delay(delayMs * (attempt + 1))
            }
        }
        
        return networkCall()
    }
}