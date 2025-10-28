package com.basesource.base.utils

import androidx.annotation.AnyThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext


@AnyThread
fun <T : Comparable> MutableLiveData<List<T>>.postDifferentValue(t: List<T>) = postDifferentValue(t) { old, new ->

    old?.flatMap { item -> item.getListCompare() } == new.flatMap { item -> item.getListCompare() }
}

@AnyThread
fun <T : Comparable> MutableLiveData<T>.postDifferentValue(t: T) = postDifferentValue(t) { old, new ->

    old?.getListCompare() == new.getListCompare()
}

@AnyThread
fun <T> MutableLiveData<T>.postDifferentValue(t: T, checkSame: (old: T?, new: T) -> Boolean = { old, new -> old == new }): Boolean {

    if (checkSame.invoke(this.value, t)) {
        return false
    }

    postValue(t)

    return true
}

@AnyThread
suspend fun <T> MutableLiveData<T>.postValueIfActive(t: T) {

    if (coroutineContext.isActive) postValue(t)
}

fun <T> LiveData<T>.toMutableLiveData(): MutableLiveData<T> {
    val mediatorLiveData = MediatorLiveData<T>()
    mediatorLiveData.addSource(this) {
        mediatorLiveData.value = it
    }
    return mediatorLiveData
}

interface Comparable {

    fun getListCompare(): List<*>
}

