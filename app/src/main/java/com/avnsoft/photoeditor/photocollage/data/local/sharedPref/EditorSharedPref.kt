package com.avnsoft.photoeditor.photocollage.data.local.sharedPref

import com.avnsoft.photoeditor.photocollage.pref.SharedPrefsApi
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@Single
class EditorSharedPref : KoinComponent {

    val IS_SYNC_STICKER = "isSyncSticker"
    val IS_SYNC_PATTERN = "IS_SYNC_PATTERN"


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
}