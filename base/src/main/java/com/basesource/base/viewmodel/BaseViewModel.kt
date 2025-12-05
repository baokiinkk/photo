package com.basesource.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.basesource.base.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException

interface IEventHandler {
    fun sendEvent(event: IEvent)

    fun onReceivedEvent(event: IEvent)
}

interface IEvent

interface IState

/**
 * Base ViewModel class that provides common functionality for all ViewModels.
 */
abstract class BaseViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val networkUIState = MutableStateFlow(NetworkUIState())

    /**
     * Executes a suspend function and handles the result.
     */
    protected fun <T> execute(
        block: suspend () -> Result<T>,
        onLoading: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                onLoading?.invoke()
                when (val result = block()) {
                    is Result.Success -> {
                        _isLoading.value = false
                        onSuccess?.invoke(result.data)
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        _error.value = result.exception.message
                        onError?.invoke(result.exception)
                    }

                    is Result.Loading -> {
                        _isLoading.value = true
                        onLoading?.invoke()
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
                onError?.invoke(e)
            }
        }
    }

    /**
     * Executes a Flow<Result<T>> and handles the results.
     */
    protected fun <T> executeResult(
        flow: Flow<Result<T>>,
        onLoading: (() -> Unit)? = null,
        onError: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null,
    ) {
        viewModelScope.launch {
            try {
                flow.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _isLoading.value = false
                            onSuccess?.invoke(result.data)
                        }

                        is Result.Error -> {
                            _isLoading.value = false
                            _error.value = result.exception.message
                            onError?.invoke(result.exception)
                        }

                        is Result.Loading -> {
                            _isLoading.value = true
                            onLoading?.invoke()
                        }
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                _error.value = e.message
                onError?.invoke(e)
            }
        }
    }

    fun <T> executeTask(
        onTask: suspend () -> Result<T>,
        onSuccess: (T) -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                setLoading(true)
                val response = onTask.invoke()
                when (response) {
                    is Result.Loading -> {
                        setLoading(true)
                    }

                    is Result.Success -> {
                        onSuccess.invoke(response.data)
                        setLoading(false)
                    }

                    is Result.Error -> {
                        setLoading(false)
                        setError(response.exception.message ?: "Unknown error")
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    /**
     * Clears the current error state.
     */
    fun clearError() {
        viewModelScope.launch {
            delay(50)
            _error.value = null
        }
    }

    /**
     * Sets the loading state.
     */
    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    /**
     * Sets an error message.
     */
    protected fun setError(message: String) {
        _error.value = message
    }

    fun showNetworkDialog() {
        networkUIState.update {
            it.copy(
                showNetworkDialog = true
            )
        }
    }

    fun hideNetworkDialog() {
        networkUIState.update {
            it.copy(
                showNetworkDialog = false
            )
        }
    }
}
data class NetworkUIState(
    val showNetworkDialog: Boolean = false
)