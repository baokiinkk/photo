package com.avnsoft.photoeditor.photocollage.data.local.sharedPref

import com.avnsoft.photoeditor.photocollage.pref.SharedPrefsApi
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

@Single
class EditorSharedPref : KoinComponent {

    private val sharedPrefsApi: SharedPrefsApi by inject()

    fun setIsSync(isSync: Boolean) {
        sharedPrefsApi.set("isSync", isSync)
    }

    fun getIsSync(): Boolean {
        return sharedPrefsApi.get("isSync", false)
    }
}