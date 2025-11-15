package com.avnsoft.photoeditor.photocollage.pref

import android.content.Context
import androidx.core.content.edit
import org.koin.core.annotation.Single

@Single
class SharedPrefsApi(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    fun set(key: String, value: String) =
        sharedPreferences.edit { putString(key, value) }

    fun get(key: String, defValue: String) = sharedPreferences.getString(key, defValue) ?: defValue

    fun set(key: String, value: Int) =
        sharedPreferences.edit { putInt(key, value) }

    fun get(key: String, defValue: Int) = sharedPreferences.getInt(key, defValue)

    fun set(key: String, value: Long) =
        sharedPreferences.edit { putLong(key, value) }

    fun get(key: String, defValue: Long) = sharedPreferences.getLong(key, defValue)

    fun set(key: String, value: Boolean) =
        sharedPreferences.edit { putBoolean(key, value) }

    fun get(key: String, defValue: Boolean) = sharedPreferences.getBoolean(key, defValue)

    fun get(key: String, value: Float) = sharedPreferences.getFloat(key, value)

    fun set(key: String, value: Float) = sharedPreferences.edit { putFloat(key, value) }

    fun contains(key: String) = sharedPreferences.contains(key)

    fun clear() = sharedPreferences.edit { clear() }

    fun remove(key: String) = sharedPreferences.edit { remove(key) }

    fun removeKeysStartingWith(prefix: String) {
        sharedPreferences.edit {
            for (key in sharedPreferences.all.keys) {
                if (key.startsWith(prefix)) {
                    remove(key)
                }
            }
        }
    }

}
