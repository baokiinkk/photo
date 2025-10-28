package com.basesource.base.network

import java.io.IOException

/**
 * Base class for network-related exceptions.
 */
sealed class NetworkException(message: String, cause: Throwable? = null) : IOException(message, cause) {
    
    /**
     * Exception thrown when there's no internet connection.
     */
    class NoInternetConnection : NetworkException("No internet connection available")
    
    /**
     * Exception thrown when the server returns an error response.
     */
    class ServerError(
        val code: Int,
        override val message: String? = null
    ) : NetworkException("Server error: $code - $message")
    
    /**
     * Exception thrown when the request times out.
     */
    class TimeoutException : NetworkException("Request timeout")
    
    /**
     * Exception thrown when the response cannot be parsed.
     */
    class ParseException(cause: Throwable) : NetworkException("Failed to parse response", cause)
    
    /**
     * Exception thrown for unknown network errors.
     */
    class UnknownException(cause: Throwable) : NetworkException("Unknown network error", cause)
}