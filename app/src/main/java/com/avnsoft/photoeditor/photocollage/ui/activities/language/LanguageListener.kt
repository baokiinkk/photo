package com.avnsoft.photoeditor.photocollage.ui.activities.language

import com.basesource.base.utils.LanguageType

interface LanguageListener {
    fun onBackClicked()
    fun onLanguageSelected(language: LanguageType)
    fun onLanguageChanged(language: LanguageType)
}