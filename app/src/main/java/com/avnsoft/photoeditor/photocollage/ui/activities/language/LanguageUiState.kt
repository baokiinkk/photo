package com.avnsoft.photoeditor.photocollage.ui.activities.language

import com.basesource.base.utils.LanguageType

data class LanguageUiState(
    val currentLanguage: LanguageType = LanguageType.ENGLISH,
    val isLoading: Boolean = false
)