package com.amb.photo.ui.activities.editor.text_sticker

import android.graphics.Bitmap
import com.basesource.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TextStickerViewModel : BaseViewModel() {

    val uiState = MutableStateFlow(TextStickerUIState())

    fun getConfigTextSticker(bitmap: Bitmap?) {
        uiState.update {
            it.copy(
                originBitmap = bitmap,
            )
        }
    }

}

data class TextStickerUIState(
    val originBitmap: Bitmap? = null,
)