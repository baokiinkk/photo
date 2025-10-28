package com.basesource.base.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun <T> ViewModel.listenerSources(vararg source: LiveData<*>, context: CoroutineContext = Dispatchers.IO, change: suspend MediatorLiveData<T>.() -> Unit): MediatorLiveData<T> {

    var job: Job? = null

    val liveData = MediatorLiveData<T>()

    val onChange: () -> Unit = {

        job?.cancel()

        job = viewModelScope.launch(context) {

            change(liveData)
        }
    }

    source.forEach {

        liveData.addSource(it) { onChange() }
    }

    return liveData
}

fun <T> ViewModel.combineSources(vararg source: LiveData<*>, context: CoroutineContext = Dispatchers.IO, change: suspend MediatorLiveData<T>.() -> Unit): MediatorLiveData<T> {

    var job: Job? = null

    val liveData = MediatorLiveData<T>()

    val onChange: () -> Unit = {

        job?.cancel()

        job = viewModelScope.launch(context) {

            change(liveData)
        }
    }

    source.forEach {

        liveData.addSource(it) { if (!source.any { it.value == null }) onChange() }
    }

    return liveData
}

fun <T> ViewModel.combineSourcesWithoutCancel(vararg source: LiveData<*>, context: CoroutineContext = Dispatchers.IO, change: suspend MediatorLiveData<T>.() -> Unit): MediatorLiveData<T> {


    val liveData = MediatorLiveData<T>()

    val onChange: () -> Unit = {

        viewModelScope.launch(context) {

            change(liveData)
        }
    }

    source.forEach {

        liveData.addSource(it) { if (!source.any { it.value == null }) onChange() }
    }

    return liveData
}

fun <T> ViewModel.mediatorLiveData(context: CoroutineContext = Dispatchers.IO, change: suspend MediatorLiveData<T>.() -> Unit): MediatorLiveData<T> {

    val liveData = MediatorLiveData<T>()

    viewModelScope.launch(context) {

        change(liveData)
    }

    return liveData
}
