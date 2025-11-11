package com.amb.photo.ui.activities.editor.background

import android.graphics.Bitmap
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BackgroundViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(BackgroundUIState())

    fun getConfigBackground(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                originBitmap = bitmap,
            )
        }
    }

}

data class BackgroundUIState(
    val originBitmap: Bitmap? = null,
)