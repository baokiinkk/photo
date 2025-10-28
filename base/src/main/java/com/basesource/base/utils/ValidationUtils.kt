package com.basesource.base.utils

/**
 * Utility class for validation operations.
 */
object ValidationUtils {
    
    /**
     * Validates email format.
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return emailRegex.matches(email)
    }
    
    /**
     * Validates phone number format.
     */
    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^[+]?[0-9]{10,15}$".toRegex()
        return phoneRegex.matches(phone.replace("\\s".toRegex(), ""))
    }
    
    /**
     * Validates password strength.
     */
    fun isValidPassword(password: String): Boolean {
        // At least 8 characters, 1 uppercase, 1 lowercase, 1 number
        val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@\$!%*?&]{8,}$".toRegex()
        return passwordRegex.matches(password)
    }
    
    /**
     * Validates if string is not empty or null.
     */
    fun isNotEmpty(value: String?): Boolean {
        return !value.isNullOrBlank()
    }
    
    /**
     * Validates URL format.
     */
    fun isValidUrl(url: String): Boolean {
        val urlRegex = "^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$".toRegex()
        return urlRegex.matches(url)
    }
    
    /**
     * Validates if string has minimum length.
     */
    fun hasMinLength(value: String, minLength: Int): Boolean {
        return value.length >= minLength
    }
    
    /**
     * Validates if string has maximum length.
     */
    fun hasMaxLength(value: String, maxLength: Int): Boolean {
        return value.length <= maxLength
    }
}