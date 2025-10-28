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
    FRENCH("fr", "Monaco", R.drawable.flat_mo),
    MACAO("zh", "Macao", R.drawable.flat_macao),
    MYANMAR("my", "Myanmar", R.drawable.flat_myanmar),
    MEXICO("es", "Mexico", R.drawable.flat_mexico),
    VIETNAMESE("vi", "Vietnam", R.drawable.flat_vn),
    JAPANESE("ja", "Japan", R.drawable.flat_japan),

}