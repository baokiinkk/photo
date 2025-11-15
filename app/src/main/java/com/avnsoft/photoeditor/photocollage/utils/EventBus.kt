package com.avnsoft.photoeditor.photocollage.utils

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
object EventBus {
    private val _events = MutableSharedFlow<IEventBusData>(
        replay = 1,                  // Giữ lại 1 event cuối cùng
        extraBufferCapacity = 64
    )
    val events = _events.asSharedFlow()

    fun post(event: IEventBusData) {
        _events.tryEmit(event)
    }

    fun subscribe(
        lifecycleOwner: LifecycleOwner,
        onEvent: (IEventBusData) -> Unit
    ) {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                events.collect { event ->
                    onEvent(event)
                }
            }
        }
    }

    fun subscribe(
        scope: CoroutineScope,
        onEvent: (IEventBusData) -> Unit
    ) {
        scope.launch {
            events.collect { event ->
                onEvent(event)
            }
        }
    }
}
