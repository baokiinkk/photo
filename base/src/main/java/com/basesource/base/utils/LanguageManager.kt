package com.basesource.base.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.content.edit
import com.basesource.base.R

object LanguageManager {

    private const val PREF_LANGUAGE = "pref_language"
    private const val PREF_LANGUAGE_CODE = "pref_language_code"

    fun setLanguage(context: Context, language: LanguageType) {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        prefs.edit {
            putString(PREF_LANGUAGE_CODE, language.code)
        }
    }

    fun getCurrentLanguage(context: Context): LanguageType {
        val prefs = context.getSharedPreferences(PREF_LANGUAGE, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(PREF_LANGUAGE_CODE, "en") ?: "en"

        return LanguageType.entries.find { it.code == languageCode } ?: LanguageType.ENGLISH
    }

}

enum class LanguageType(
    val code: String,
    val displayName: String,
    @DrawableRes val flag: Int
) {
    ENGLISH("en", "English", R.drawable.flag_en),
    VIETNAMESE("vi", "Vietnamese", R.drawable.flat_vn),
    CHINESE("zh", "Chinese", R.drawable.flat_macao),
    KOREAN("ko", "Korean", R.drawable.flat_korean),
    JAPANESE("ja", "Japanese", R.drawable.flat_japan),
    FRENCH("fr", "French", R.drawable.flat_french),

}