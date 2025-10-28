package com.basesource.base.utils

import android.content.Intent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

val gson: Gson by lazy {
    Gson()
}

inline fun <reified T : Any> Gson.fromJson(json: String): T = this.fromJson(json, T::class.java)

fun Any.toJson(): String = gson.toJson(this)

inline fun <reified T> String.fromJson(): T {
    return gson.fromJson(this, T::class.java)
}

inline fun <reified T> String.fromJsonTypeToken(): T? {
    return try {
        gson.fromJson(this, object : TypeToken<T>() {}.type)
    } catch (ex: Exception) {
        null
    }
}

inline fun <reified T> Intent.getInput(): T {
    val data = getStringExtra("screen_input_key").orEmpty()
    return data.fromJson()
}


inline fun <reified T> Intent.getOutput(): T {
    val data = getStringExtra("EXTRA_SCREEN_OUTPUT_KEY").orEmpty()
    return data.fromJson()
}

