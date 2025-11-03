package com.amb.photo.ui.activities.editor

import androidx.compose.runtime.mutableStateOf
import com.basesource.base.viewmodel.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CropViewModel() : BaseViewModel() {

    val uiState = mutableStateOf(CropUIState())
}

data class CropUIState(
    var currentAspect: CropAspect = CropAspect.RATIO_1_1
)