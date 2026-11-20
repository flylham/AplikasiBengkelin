package com.bengkelin.app.data.model

/**
 * Wrapper serbaguna untuk state respon asynchronous autentikasi & network.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
    data object Idle : Resource<Nothing>()
}
