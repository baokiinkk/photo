package com.basesource.base.result

/**
 * A generic class that holds a value or an error.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or null if it is [Result.Error] or [Result.Loading].
 */
inline fun <T> Result<T>.getOrNull(): T? {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> null
        is Result.Loading -> null
    }
}

/**
 * Returns the encapsulated value if this instance represents [Result.Success] or the result of evaluating the [defaultValue] function if it is [Result.Error] or [Result.Loading].
 */
inline fun <T> Result<T>.getOrDefault(defaultValue: () -> T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> defaultValue()
        is Result.Loading -> defaultValue()
    }
}

/**
 * Returns true if this instance represents [Result.Success].
 */
fun <T> Result<T>.isSuccess(): Boolean = this is Result.Success

/**
 * Returns true if this instance represents [Result.Error].
 */
fun <T> Result<T>.isError(): Boolean = this is Result.Error

/**
 * Returns true if this instance represents [Result.Loading].
 */
fun <T> Result<T>.isLoading(): Boolean = this is Result.Loading

/**
 * Maps the encapsulated value if this instance represents [Result.Success] or returns the same [Result.Error] or [Result.Loading] if it is not.
 */
inline fun <T, R> Result<T>.map(transform: (value: T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(exception)
        is Result.Loading -> Result.Loading
    }
}

/**
 * Maps the encapsulated value if this instance represents [Result.Success] or returns the same [Result.Error] or [Result.Loading] if it is not.
 */
inline fun <T, R> Result<T>.flatMap(transform: (value: T) -> Result<R>): Result<R> {
    return when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> Result.Error(exception)
        is Result.Loading -> Result.Loading
    }
}