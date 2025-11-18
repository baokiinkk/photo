package com.avnsoft.photoeditor.photocollage.data.local.sharedPref

import androidx.core.content.edit
import com.avnsoft.photoeditor.photocollage.pref.SharedPrefsApi
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@Single
class EditorSharedPref : KoinComponent {

    val IS_SYNC_TEMPLATE = "IS_SYNC_TEMPLATE"
    val IS_SYNC_STICKER = "isSyncSticker"
    val IS_SYNC_PATTERN = "IS_SYNC_PATTERN"
    val KEY_STORE_ACCESS_TOKEN = "KEY_STORE_ACCESS_TOKEN"
    val KEY_IS_REQUESTED_TOKEN = "KEY_IS_REQUESTED_TOKEN"


    private val sharedPrefsApi: SharedPrefsApi by inject()

    fun setIsSyncSticker(isSync: Boolean) {
        sharedPrefsApi.set(IS_SYNC_STICKER, isSync)
    }

    fun getIsSyncSticker(): Boolean {
        return sharedPrefsApi.get(IS_SYNC_STICKER, false)
    }

    fun setIsSyncPattern(isSync: Boolean) {
        sharedPrefsApi.set(IS_SYNC_PATTERN, isSync)
    }

    fun getIsSyncPattern(): Boolean {
        return sharedPrefsApi.get(IS_SYNC_PATTERN, false)
    }

    fun getAccessToken(): String {
        return sharedPrefsApi.get(KEY_STORE_ACCESS_TOKEN, "")
    }

    fun saveAccessToken(token: String) {
        sharedPrefsApi.set(KEY_STORE_ACCESS_TOKEN, token)
    }

    fun isRequestedToken(): Boolean {
        return sharedPrefsApi.get(KEY_IS_REQUESTED_TOKEN, false)
    }

    fun setIsRequestedToken(value: Boolean) {
        sharedPrefsApi.set(KEY_IS_REQUESTED_TOKEN, value)
    }

    fun getIsSyncTemplate(): Boolean {
        return sharedPrefsApi.get(IS_SYNC_TEMPLATE, false)
    }

    fun setIsSyncTemplate(isSync: Boolean) {
        sharedPrefsApi.set(IS_SYNC_TEMPLATE, isSync)
    }
}